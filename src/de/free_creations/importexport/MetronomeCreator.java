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

import de.free_creations.midiutil.MidiUtil;
import de.free_creations.midiutil.TimeSignature;
import de.free_creations.midiutil.TimeSignatureTrack;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

/**
 * The MetronomeCreator adds a track of metronome clicks to the given sequence.
 *
 * @author Harald <Harald at free-creations.de>
 */
public class MetronomeCreator {

  //General Midi Percussion Map
  private static final int SideStick = 37;
  private static final int Claves = 75;
  private static final int HighWoodBlock = 76;
  private static final int LowWoodBlock = 77;
  public static final int perf4beats = 1;
  public static final int perf2beats = 2;
  private static int choosenPrefs = 0;

  public static Sequence process(final Sequence inputSequence, int preferences, Handler loggingHandler) throws InvalidMidiDataException {
    choosenPrefs = preferences;

    Sequence outputSequence = new Sequence(inputSequence.getDivisionType(), inputSequence.getResolution());
    Track[] inputTracks = inputSequence.getTracks();

    for (int trackI = 0; trackI < inputTracks.length; trackI++) {
      Track inputTrack = inputTracks[trackI];
      Track outputTrack = outputSequence.createTrack();


      for (int eventI = 0; eventI < inputTrack.size(); eventI++) {
        MidiEvent midEvent = inputTrack.get(eventI);
        outputTrack.add(midEvent);

      }
    }
    TimeSignatureTrack timeSignatureTrack = new TimeSignatureTrack(inputSequence);
    Track metronomeTrack = outputSequence.createTrack();
    metronomeTrack.add(makeTrackNameEvent(0, "Metronome"));
    createMetronomeClicks(timeSignatureTrack, metronomeTrack);

    return outputSequence;

  }

  public static Sequence process(final Sequence inputSequence, Handler loggingHandler) throws InvalidMidiDataException {
    return process(inputSequence, perf2beats, loggingHandler);
  }

  private static void createMetronomeClicks(TimeSignatureTrack timeSignatureTrack, Track metronomeTrack) {

    TimeSignature startTimeSig = timeSignatureTrack.get(0);

    for (int i = 1; i < timeSignatureTrack.size(); i++) {
      TimeSignature endTimeSig = timeSignatureTrack.get(i);
      createMetronomeClicksRegion(metronomeTrack, startTimeSig, endTimeSig.getTickPos());
      startTimeSig = endTimeSig;
    }

    createMetronomeClicksRegion(metronomeTrack, startTimeSig, timeSignatureTrack.ticks());
  }

  /**
   */
  private static void createMetronomeClicksRegion(Track track, TimeSignature startTimeSig, long lastTick) {
    long beatLength = startTimeSig.getBeatLength();
    long barLength = startTimeSig.getBarLength();
    long startTick = startTimeSig.getTickPos();
    long range = lastTick - startTick;
    long count = range / barLength;
    long remainingTicks = range - (count + 1) * barLength;
    int remainingBeats = (int) (remainingTicks / beatLength);


    for (long i = 0; i < count; i++) {
      createClicksMeasure(track, startTick, startTimeSig.getNumerator(), beatLength);
      startTick = startTick + barLength;
    }
    if (remainingBeats > 0) {
      createClicksMeasure(track, startTick + count * barLength, remainingBeats, beatLength);
    }


  }

  private static void createClicksMeasure(Track track, long startTick, int beatNum, long beatLength) {
    switch (beatNum) {
      case 2:
        create2ClicksMeasure(track, startTick, beatLength);
        return;
      case 3:
        create3ClicksMeasure(track, startTick, beatLength);
        return;
      case 4:
        create4ClicksMeasure(track, startTick, beatLength);
        return;
      case 6:
        create6ClicksMeasure(track, startTick, beatLength);
        return;

      default:
        createAnyClicksMeasure(track, startTick, beatNum, beatLength);
    }

  }

  private static void create2ClicksMeasure(Track track, long startTick, long beatLength) {
    addClick(track, startTick, beatLength, 0, LowWoodBlock, 84);
    addClick(track, startTick, beatLength, 1, HighWoodBlock, 64);
  }

  private static void create3ClicksMeasure(Track track, long startTick, long beatLength) {
    addClick(track, startTick, beatLength, 0, LowWoodBlock, 84);
    addClick(track, startTick, beatLength, 1, HighWoodBlock, 54);
    addClick(track, startTick, beatLength, 2, HighWoodBlock, 74);
  }

  private static void create4ClicksMeasure(Track track, long startTick, long beatLength) {
    if ((choosenPrefs & perf2beats)!=0) {
      addClick(track, startTick, beatLength, 0, LowWoodBlock, 84);
      addClick(track, startTick, beatLength, 2, HighWoodBlock, 84);
    } else {
      addClick(track, startTick, beatLength, 0, LowWoodBlock, 84);
      addClick(track, startTick, beatLength, 1, HighWoodBlock, 54);
      addClick(track, startTick, beatLength, 2, HighWoodBlock, 74);
      addClick(track, startTick, beatLength, 3, HighWoodBlock, 34);
    }
  }

  private static void create6ClicksMeasure(Track track, long startTick, long beatLength) {
    addClick(track, startTick, beatLength, 0, LowWoodBlock, 84);
    addClick(track, startTick, beatLength, 1, HighWoodBlock, 64);
    addClick(track, startTick, beatLength, 2, HighWoodBlock, 64);
    addClick(track, startTick, beatLength, 3, HighWoodBlock, 84);
    addClick(track, startTick, beatLength, 4, HighWoodBlock, 64);
    addClick(track, startTick, beatLength, 5, HighWoodBlock, 64);
  }

  private static void createAnyClicksMeasure(Track track, long startTick, int beatCount, long beatLength) {
    addClick(track, startTick, beatLength, 0, LowWoodBlock, 84);
    for (int i = 1; i < beatCount; i++) {
      addClick(track, startTick, beatLength, i, HighWoodBlock, 64);
    }
  }

  private static void addClick(Track track, long startTick, long beatLength, int beatIdx, int percussionInstrument, int velocity) {
    long beatTick = startTick + (beatIdx * beatLength);
    track.add(makeNoteOnEvent(percussionInstrument, velocity, beatTick));
    track.add(makeNoteOnEvent(percussionInstrument, 0, beatTick + beatLength - 1));
  }

  private static MidiMessage makeNoteOn(int percussionInstrument, int velocity) {
    final int percussionChannel = 9;
    ShortMessage message = new ShortMessage();
    try {
      message.setMessage(ShortMessage.NOTE_ON, percussionChannel, percussionInstrument, velocity);
    } catch (InvalidMidiDataException ex) {
      Logger.getLogger(MetronomeCreator.class.getName()).log(Level.SEVERE, null, ex);
    }
    return message;
  }

  private static MidiEvent makeNoteOnEvent(int percussionInstrument, int velocity, long tick) {
    return new MidiEvent(makeNoteOn(percussionInstrument, velocity), tick);
  }

  private static MidiEvent makeTrackNameEvent(long tickPos, String text) throws InvalidMidiDataException {
    return new MidiEvent(makeTrackNameMessage(text), tickPos);
  }

  private static MidiMessage makeTrackNameMessage(String text) throws InvalidMidiDataException {
    MetaMessage message = new MetaMessage();
    message.setMessage(MidiUtil.tracknameMeta, text.getBytes(), text.length());
    return message;
  }
}
