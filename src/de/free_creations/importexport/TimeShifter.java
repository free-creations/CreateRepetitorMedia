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

import java.util.logging.Handler;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

/**
 * Moves events by a given time.
 *
 * @author Harald Postner
 */
public class TimeShifter {

  /**
   * Moves events by a given time. The offset can be set individually for each
   * track.
   *
   * @param inputSequence
   * @param deltaTicks an array that gives for each track the shift time. The
   * values are given in Midi ticks.
   * @param loggingHandler
   * @return
   * @throws InvalidMidiDataException
   */
  public static Sequence process(final Sequence inputSequence, long[] deltaTicks, Handler loggingHandler) throws InvalidMidiDataException {
    Sequence outputSequence = new Sequence(inputSequence.getDivisionType(), inputSequence.getResolution());
    Track[] inputTracks = inputSequence.getTracks();

    for (int trackI = 0; trackI < inputTracks.length; trackI++) {
      Track inputTrack = inputTracks[trackI];
      Track outputTrack = outputSequence.createTrack();
      for (int eventI = 0; eventI < inputTrack.size(); eventI++) {
        MidiEvent midEvent = inputTrack.get(eventI);

        long delta = getDelta(deltaTicks, trackI);
        outputTrack.add(shiftEvent(delta, midEvent));

      }
    }
    return outputSequence;

  }

  private static long getDelta(long[] deltaTicks, int index) {
    if (deltaTicks == null) {
      return 0;
    }
    if (deltaTicks.length <= index) {
      return 0;
    }
    return deltaTicks[index];
  }

  private static MidiEvent shiftEvent(long deltaTicks, MidiEvent event) {
    if (!doShift(event)) {
      return event;
    }
    MidiMessage message = event.getMessage();
    long newTick = event.getTick() + deltaTicks;
    if (newTick < 0) {
      newTick = 0L;
    }
    return new MidiEvent(message, newTick);
  }

  /**
   * Determine whether this is an event that can be shifted.
   *
   * @param event
   * @return
   */
  private static boolean doShift(MidiEvent event) {
    // all messages beyond 0 can be shifted
    if (event.getTick() != 0) {
      return true;
    }
    // among the starting events, only note-on events must be shifted
    MidiMessage message = event.getMessage();
    if (!(message instanceof ShortMessage)) {
      return false;
    }
    ShortMessage smessage = (ShortMessage) message;
    return smessage.getCommand() != ShortMessage.NOTE_ON;

  }
}
