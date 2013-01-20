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
 * Removes unwanted Controller events from the input sequence.
 *
 * @author Harald Postner
 */
public class ControllerRemover {

  public final static int contProgramChange = 1001;
  public final static int contPitchBend = 1002;

  public static Sequence process(final Sequence inputSequence, int badControllerType, Handler loggingHandler) throws InvalidMidiDataException {
    Sequence outputSequence = new Sequence(inputSequence.getDivisionType(), inputSequence.getResolution());
    Track[] inputTracks = inputSequence.getTracks();

    for (int trackI = 0; trackI < inputTracks.length; trackI++) {
      Track inputTrack = inputTracks[trackI];
      Track outputTrack = outputSequence.createTrack();
      for (int eventI = 0; eventI < inputTrack.size(); eventI++) {
        MidiEvent midEvent = inputTrack.get(eventI);
        if (keepEvent(badControllerType, midEvent)) {
          outputTrack.add(midEvent);
        }
      }
    }
    return outputSequence;

  }

  private static boolean keepEvent(int badControllerType, MidiEvent event) {
    MidiMessage rawMessage = event.getMessage();
    if (!(rawMessage instanceof ShortMessage)) {
      return true;
    }
    ShortMessage message = (ShortMessage) rawMessage;
    if (message.getCommand() == ShortMessage.CONTROL_CHANGE) {
      return message.getData1() != badControllerType;
    }
    if (badControllerType == contProgramChange) {
      return message.getCommand() != ShortMessage.PROGRAM_CHANGE;
    }
    if (badControllerType == contPitchBend) {
      return message.getCommand() != ShortMessage.PROGRAM_CHANGE;
    }
    return true;

  }
}
