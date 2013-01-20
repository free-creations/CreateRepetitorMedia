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
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

/**
 * Removes unwanted Controller events from the input sequence.

 * @author Harald Postner 
 */
public class EmptyTrackRemover {

  public static Sequence process(final Sequence inputSequence, Handler loggingHandler) throws InvalidMidiDataException {
    Sequence outputSequence = new Sequence(inputSequence.getDivisionType(), inputSequence.getResolution());
    Track[] inputTracks = inputSequence.getTracks();

    if (inputTracks.length > 0) {
      Track outputTrack = outputSequence.createTrack();
      copyTrack(inputTracks[0], outputTrack);
    }

    for (int trackI = 1; trackI < inputTracks.length; trackI++) {
      Track inputTrack = inputTracks[trackI];
      if (!isEmptyTrack(inputTrack)) {
        Track outputTrack = outputSequence.createTrack();
        copyTrack(inputTracks[trackI], outputTrack);
      }else{
        printTrack(inputTrack, trackI);
      }
    }
    return outputSequence;

  }

  private static Track copyTrack(Track in, Track out) {
    for (int eventI = 0; eventI < in.size(); eventI++) {
      out.add(in.get(eventI));
    }
    return out;
  }

  private static boolean isEmptyTrack(Track inputTrack) {
    for (int eventI = 0; eventI < inputTrack.size(); eventI++) {
      MidiEvent midEvent = inputTrack.get(eventI);
      if (midEvent.getMessage() instanceof ShortMessage) {
        return false;
      }
    }
    return true;
  }

  private static void printTrack(Track inputTrack,int tracknum) {
    for (int eventI = 0; eventI < inputTrack.size(); eventI++) {
      MidiEvent midEvent = inputTrack.get(eventI);
      if (midEvent.getMessage() instanceof MetaMessage) {
        MetaMessage message = (MetaMessage)midEvent.getMessage();
        byte[] data = message.getData();
        System.out.print("Track ("+tracknum+") Ignored meta message type("+message.getType()+") data:");
        if(message.getType()==MidiUtil.tracknameMeta){
          System.out.print(new String(message.getData()));
        }
        if(message.getType()==2){
          System.out.print(new String(message.getData()));
        }
        System.out.println("");
                
      }
    }
    
  }
}
