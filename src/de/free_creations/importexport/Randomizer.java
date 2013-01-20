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
import de.free_creations.midiutil.TempoTrack;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.midi.*;

/**
 * Removes unwanted Controller events from the input sequence.
 *
 * @author Harald Postner
 */
public class Randomizer {

  private static final Logger logger =
          Logger.getLogger(Randomizer.class.getName());
  final Sequence inputSequence;
  final Sequence outputSequence;
  final int[] maxDelays;
  final boolean velocityDependant;
  int[] tickDelays;

  private Randomizer(Sequence inputSequence, int[] maxDelay, boolean velocityDependant) throws InvalidMidiDataException {
    if (inputSequence.getDivisionType() != Sequence.PPQ) {
      throw new InvalidMidiDataException("This division type is not (yet) implemented.");
    }
    this.velocityDependant = velocityDependant;
    this.inputSequence = inputSequence;
    this.maxDelays = maxDelay;
    this.outputSequence = new Sequence(inputSequence.getDivisionType(), inputSequence.getResolution());

  }

  private Sequence process() throws InvalidMidiDataException {
    calculateTickdelays();

    Track[] inputTracks = inputSequence.getTracks();


    for (int trackI = 0; trackI < inputTracks.length; trackI++) {
      Track inputTrack = inputTracks[trackI];
      NoteTrack noteTrack = new NoteTrack(inputTrack);
      Track outputTrack = outputSequence.createTrack();
      moveNonNoteEvents(inputTrack, outputTrack);
      moveNoteEvents(noteTrack, outputTrack, tickDelays[trackI], false);
      logger.log(Level.INFO, "Track({0}) has {1} events.", new Object[]{trackI, outputTrack.size()});

    }
    return outputSequence;
  }

  private void calculateTickdelays() {
    TempoTrack tempoTrack = new TempoTrack(inputSequence);
    TempoTrack.TimeMap timeMap = tempoTrack.CreateTimeMap(0, 0.1, 1.0);
    tickDelays = new int[inputSequence.getTracks().length];
    for (int i = 0; i < tickDelays.length; i++) {

      double delay = 0; // default in seconds
      if (maxDelays.length > i + 1) {
        // if the maxDelays-array provides a value, use it.
        delay = maxDelays[i] / 1000D;
      }

      long tickdelay = Math.round(timeMap.getTickForOffset(delay));
      if (tickdelay > 0) {
        long fraction = (4 * inputSequence.getResolution()) / tickdelay;
        logger.log(Level.INFO, "* Track {0}) Given delay = {1}sec ; tick = {2} fraction  = 1/{3}", new Object[]{i, delay, tickdelay, fraction});
      } else {
        logger.log(Level.INFO, "* Track {0}) Given delay = {1}", new Object[]{i, delay});
      }
      tickDelays[i] = (int) tickdelay;
    }

  }

  public static Sequence process(final Sequence inputSequence, int[] maxDelays, Handler loggingHandler) throws InvalidMidiDataException {
    return process(inputSequence, maxDelays, false, loggingHandler);
  }

  public static Sequence process(final Sequence inputSequence, int[] maxDelays, boolean velocityDependant, Handler loggingHandler) throws InvalidMidiDataException {
    if (loggingHandler != null) {
      logger.addHandler(loggingHandler);
    }
    Randomizer trackMerger = new Randomizer(inputSequence, maxDelays, velocityDependant);
    return trackMerger.process();

  }

  /**
   * Shift the start of the note (delay the note) by a random value.
   *
   * @param note the note to be delayed
   * @param maxDelay the maximum delay in ticks
   * @param useVelocity use the velocity value to delay less the high volume
   * notes.
   * @return
   */
  private Note randomizeNote(Note note, int maxDelay, boolean useVelocity) {
    int channel = note.getChannel();
    int velocity = note.getVelocity();
    int pitch = note.getPitch();
    long tickPos = note.getTickPos();
    long duration = note.getDuration();
    if (useVelocity) {
      double factor = (100D - note.getVelocity())/100D;
      factor = Math.max(factor, 0.1);
      maxDelay = (int)(factor * maxDelay);
    }
    long randDelay = Math.round(Math.random() * maxDelay);
    if (randDelay < duration / 2) {
      tickPos = tickPos + randDelay;
      duration = duration - randDelay;
    }
    return new Note(channel, pitch, velocity, tickPos, duration);

  }

  private void moveNonNoteEvents(Track inputTrack, Track outputTrack) {
    for (int eventI = 0; eventI < inputTrack.size(); eventI++) {
      MidiEvent midEvent = inputTrack.get(eventI);
      if (!Note.isNoteOnEvent(midEvent)) {
        if (!Note.isNoteOffEvent(midEvent)) {
          outputTrack.add(midEvent);
        }
      }
    }
  }

  /**
   * Delay the note all notes in the given in the input track by a random value.
   *
   * @param noteTrack
   * @param outputTrack
   * @param maxDelay the maximum delay in ticks
   * @param useVelocity use the velocity value to delay less the high
   */
  private void moveNoteEvents(NoteTrack noteTrack, Track outputTrack, int maxDelay, boolean useVelocity) {
    try {
      for (Note note : noteTrack) {
        Note randNote = randomizeNote(note, maxDelay, useVelocity);
        MidiMessage noteOn = new ShortMessage(
                ShortMessage.NOTE_ON,
                randNote.getChannel(),
                randNote.getPitch(),
                randNote.getVelocity());
        MidiEvent noteOnEvent = new MidiEvent(noteOn, randNote.getTickPos());
        MidiMessage noteOff = new ShortMessage(
                ShortMessage.NOTE_OFF,
                randNote.getChannel(),
                randNote.getPitch(),
                0);
        MidiEvent noteOffEvent = new MidiEvent(noteOff, randNote.getTickPos() + randNote.getDuration());
        outputTrack.add(noteOnEvent);
        outputTrack.add(noteOffEvent);
      }
    } catch (InvalidMidiDataException ex) {
      Logger.getLogger(Randomizer.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
}
