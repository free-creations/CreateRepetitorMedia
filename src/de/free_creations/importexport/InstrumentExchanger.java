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
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
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
public class InstrumentExchanger {

  public final static int contProgramChange = 1001;
  public final static int contPitchBend = 1002;

  /**
   *
   * @param inputSequence the value of inputSequence
   * @param track the value of track
   * @param oldInstrument the value of oldInstrument
   * @param newInstrument the value of newInstrument
   * @param loggingHandler the value of loggingHandler
   * @throws InvalidMidiDataException
   */
  public static Sequence process(final Sequence inputSequence, int track, int oldInstrument, int newInstrument, Handler loggingHandler) throws InvalidMidiDataException {
    Sequence outputSequence = new Sequence(inputSequence.getDivisionType(), inputSequence.getResolution());
    Track[] inputTracks = inputSequence.getTracks();

    for (int trackI = 0; trackI < inputTracks.length; trackI++) {
      Track inputTrack = inputTracks[trackI];
      Track outputTrack = outputSequence.createTrack();
      for (int eventI = 0; eventI < inputTrack.size(); eventI++) {
        MidiEvent midEvent = inputTrack.get(eventI);
        if(trackI == track){
          MidiEvent newEvent = handleEvent(oldInstrument, newInstrument, midEvent);
          if(newEvent != null){
            outputTrack.add(newEvent);
          }
        }else{
          outputTrack.add(midEvent);
        }
      }
    }
    return outputSequence;

  }

  /**
   * 
   * @param oldInstrument
   * @param newInstrument
   * @param event
   * @return 
   */
  private static MidiEvent handleEvent(int oldInstrument, int newInstrument, MidiEvent event) {
    MidiMessage rawMessage = event.getMessage();
    if (!(rawMessage instanceof ShortMessage)) {
      return event;
    }
    ShortMessage message = (ShortMessage) rawMessage;
    if (message.getCommand() != ShortMessage.PROGRAM_CHANGE) {
      return event;
    }
    if (oldInstrument != -1) {
      if(oldInstrument != message.getData1()){
        return event;
      }
    }
    if (newInstrument == -1) {
      return null;
    }
    ShortMessage newMessage;
    try {
      newMessage = new ShortMessage(message.getStatus(),
                   newInstrument, 0);
      return new MidiEvent(newMessage, event.getTick());
    } catch (InvalidMidiDataException ex) {
      throw new RuntimeException(ex);
    }
  }
}
