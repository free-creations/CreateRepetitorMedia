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

import de.free_creations.midiutil.LoudnessTrack;
import java.util.HashMap;
import java.util.logging.Handler;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Track;

/**
 * The "GM Level 1 Developer Guidelines" (1998 MIDI Manufacturers Association)
 * says:
 * 
 * {@code 
 * 
 * Volume (CC#7) and Expression (CC #11) should be implemented as follows:
 *
 * For situations in which only CC# 7 is used (CC#11 is assumed “127”):
 *    L(dB) = 40 log (V/127) where V= CC#7 value
 *    (note the strange use of a factor of 40 where I would have expected 20
 *     that means the amplitude is square the value of V/127)
 * 
 * For situations in which both controllers are used:
 *    L(dB) = 40 log (V/127^2) where V = (volume x expression)
 * 
 * Composer/Application Recommendations: Volume should be used to set the overall
 * volume of the Channel prior to music data playback as well as for mixdown fader-style
 * movements, while Expression should be used during music data playback to attenuate the
 * programmed MIDI volume, thus creating diminuendos and crescendos. This enables a
 * listener, after the fact, to adjust the relative mix of instruments (using MIDI volume)
 * without destroying the dynamic expression of that instrument. 
 * }
 * @author Harald Postner 
 */
public class VolumeExpressionCleaner {
  
  private static HashMap<Integer, Integer> trackVolumes = new HashMap<Integer, Integer>();
  
  public static void setTrackVolume(int track, int volume) {
    trackVolumes.put(track, volume);
    
  }
  
  public static Sequence process(final Sequence inputSequence, Handler loggingHandler) throws InvalidMidiDataException {
    Sequence outputSequence = new Sequence(inputSequence.getDivisionType(), inputSequence.getResolution());
    Track[] inputTracks = inputSequence.getTracks();
    
    for (int i = 0; i < inputTracks.length; i++) {
      Track outputTrack = outputSequence.createTrack();
      LoudnessTrack loudnessTrack = new LoudnessTrack(inputTracks[i]);
      Integer trackVolume = trackVolumes.get(i);
      if (trackVolume != null) {
        loudnessTrack.setTrackVolume(trackVolume);
      }
      outputTrack = loudnessTrack.createNormalizedTrack(inputTracks[i], outputTrack);
    }
    return outputSequence;
    
  }
}
