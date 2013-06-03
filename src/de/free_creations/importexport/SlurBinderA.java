/*
 * Copyright 2011 Harald Postner .
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.free_creations.importexport;

import de.free_creations.midiutil.Note;
import de.free_creations.midiutil.NoteTrack;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Handler;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.Sequence;
import javax.sound.midi.Track;

/**
 * Binds the notes in way a pianist would execute slurs.
 *
 * Algorithm A binds all groups of notes found in the given region. A group is
 * defined as all the notes in between two separators. A separator is a rest or
 * two successive notes with the same pitch.
 *
 * @author Harald Postner
 */
public class SlurBinderA {

  private static int noteOverlap;
  private static int minimumRest;
  private static Set<Note> slurEndNotes;

  public static Sequence process(final Sequence inputSequence, int track, long startTick, long endTick, int noteOverlap, int minimumRest, Handler loggingHandler) throws InvalidMidiDataException {
    Sequence outputSequence = new Sequence(inputSequence.getDivisionType(), inputSequence.getResolution());
    Track[] inputTracks = inputSequence.getTracks();
    SlurBinderA.noteOverlap = noteOverlap;
    SlurBinderA.minimumRest = minimumRest;
    slurEndNotes = new HashSet<>();

    for (int trackI = 0; trackI < inputTracks.length; trackI++) {
      Track inputTrack = inputTracks[trackI];
      Track outputTrack = outputSequence.createTrack();

      if (trackI == track) {
        handleTrack(outputTrack, inputTrack, startTick, endTick);
      } else {
        for (int eventI = 0; eventI < inputTrack.size(); eventI++) {
          MidiEvent midEvent = inputTrack.get(eventI);
          outputTrack.add(midEvent);
        }
      }
    }

    return outputSequence;

  }

  private static void handleTrack(Track outputTrack, Track inputTrack, long startTick, long endTick) {
    // handle all other events
    for (int eventI = 0; eventI < inputTrack.size(); eventI++) {
      MidiEvent midEvent = inputTrack.get(eventI);
      if (!Note.isNoteOnEvent(midEvent)) {
        if (!Note.isNoteOffEvent(midEvent)) {
          outputTrack.add(midEvent);
        }
      }
    }
    // handle all note events
    NoteTrack noteTrack = new NoteTrack(inputTrack);
    for (int i = 0; i < noteTrack.size(); i++) {
      Note thisNote = noteTrack.get(i);
      List<Note> followingNotes = findFollowingNotes(noteTrack, i);
      Note newNote = handleNote(thisNote, followingNotes, startTick, endTick);
      outputTrack.add(newNote.getNoteOnEvent());
      outputTrack.add(newNote.getNoteOffEvent());
    }


  }

  private static Note handleNote(Note note, List<Note> follwoingNotes, long startTick, long endTick) {
    if (note.getTickPos() < startTick) {
      return note;
    }
    if (note.getTickPos() + note.getDuration() > endTick) {
      return note;
    }

    Note noteToLinkWith = noteToLinkWith(note, follwoingNotes);
    if (noteToLinkWith != null) {
      long newDuration = prolongatedDuration(note, noteToLinkWith);

      Note newNote = new Note(note.getChannel(),
              note.getPitch(),
              note.getVelocity(),
              note.getTickPos(),
              newDuration);
      slurEndNotes.add(noteToLinkWith);
      slurEndNotes.remove(note);
      return newNote;
    }

    if (slurEndNotes.contains(note)) {
      Note newNote = new Note(note.getChannel(),
              note.getPitch(),
              (note.getVelocity() * 80) / 100,
              note.getTickPos(),
              (note.getDuration() * 80) / 100);
      slurEndNotes.remove(note);
      return newNote;
    }


    return note;
  }

  private static long prolongatedDuration(Note note, Note noteToLinkWith) {
    return (noteToLinkWith.getTickPos() - note.getTickPos()) + noteOverlap;
  }

  /**
   * Within the set set of following notes find one that we can link with.
   *
   * @param note
   * @param follwoingNotes
   * @return
   */
  private static Note noteToLinkWith(Note note, List<Note> follwoingNotes) {
    if (follwoingNotes.isEmpty()) {
      return null;
    }

    Note candidate = nearestNote(note.getPitch(), follwoingNotes);
    if (candidate == null) {
      return null;
    }
    // avoid collision with a following note
    long newNoteEnd = note.getTickPos() + prolongatedDuration(note, candidate);
    for (Note fNote : follwoingNotes) {
      if (fNote.getPitch() == note.getPitch()) {
        if (fNote.getTickPos() <= newNoteEnd) {
          return null;
        }
      }
    }
    return candidate;
  }

  /**
   * Within the given set of notes find the one that is the leftmost one and
   * that is nearest to the given pitch, but exclude notes that have the same
   * pitch.
   *
   * @param pitch
   * @param notes
   * @return
   */
  private static Note nearestNote(int pitch, List<Note> notes) {
    Note result = null;
    int smallestPitchdiff = Integer.MAX_VALUE;
    long nearestPos = Long.MAX_VALUE;
    for (Note n : notes) {
      int thisPitchDiff = Math.abs(pitch - n.getPitch());
      long thisPos = n.getTickPos();
      if (thisPos < nearestPos) {
        result = n;
        nearestPos = thisPos;
        smallestPitchdiff = thisPitchDiff;
      } else {
        if (nearestPos == thisPos) {
          if (thisPitchDiff < smallestPitchdiff) {
            result = n;
            smallestPitchdiff = thisPitchDiff;
          }
        }
      }
    }
    if (smallestPitchdiff != 0) {
      return result;
    } else {
      return null;
    }
  }

  /**
   * Collect all the notes that follow the given note, within a range
   * correspondin to the minimum rest.
   *
   * @param noteTrack
   * @param index
   * @return
   */
  private static List<Note> findFollowingNotes(NoteTrack noteTrack, int index) {
    ArrayList<Note> result = new ArrayList<>();
    long posStart = noteTrack.get(index).getTickPos();
    long noteDur = noteTrack.get(index).getDuration();
    long posEnd = posStart + noteDur;
    long maxSearch = posEnd + minimumRest;
    for (int i = index + 1; i < noteTrack.size(); i++) {
      Note note = noteTrack.get(i);
      if (note.getTickPos() > maxSearch) {
        //the note is outside our search area, as the note are sorted in ascending order, we have finished.
        break;
      }
      // all notes that follow the end of the given note are added to the list
      // remarque: we allow for an overlap of five ticks because of the impression of some Midi editors.
      if (note.getTickPos() >= (posEnd - 5)) {
        result.add(note);
      }
    }
    return result;
  }
}
