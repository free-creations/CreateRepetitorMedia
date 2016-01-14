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

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Handler;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

/**
 * Adjusts velocity values. Version D is adapted for the "Kawai Piano"
 * synthesizer. Like Version B, but adds some statistics.
 *
 * @author Harald Postner
 */
public class VelocityCorrectionD {

  private static final double maxRandomRange = 0.05 * 128;
  private static final double scale = 0.7;

  public static Sequence process(final Sequence inputSequence, int[] tracks, Handler loggingHandler) throws InvalidMidiDataException {
    assert (tracks != null);

    Sequence outputSequence = new Sequence(inputSequence.getDivisionType(), inputSequence.getResolution());
    Track[] inputTracks = inputSequence.getTracks();
    Set<Integer> trackSet = new HashSet<>();
    for (int t : tracks) {
      trackSet.add(t);
    }

    for (int i = 0; i < inputTracks.length; i++) {
      Track inputTrack = inputTracks[i];
      Track outputTrack = outputSequence.createTrack();
      double randomVel = 0D;
      for (int eventI = 0; eventI < inputTrack.size(); eventI++) {
        MidiEvent midEvent = inputTrack.get(eventI);
        if (trackSet.contains(i)) {
          outputTrack.add(processEvent(midEvent, randomVel));
          randomVel = randomIncrement(randomVel);
        } else {
          outputTrack.add(midEvent);
        }
      }
    }
    return outputSequence;

  }

  private static MidiEvent processEvent(MidiEvent event, double randVelocity) throws InvalidMidiDataException {
    MidiMessage message = event.getMessage();
    if (!(message instanceof ShortMessage)) {
      return event;
    }
    ShortMessage smessage = (ShortMessage) message;
    if (smessage.getCommand() == ShortMessage.NOTE_ON) {
      return new MidiEvent(processNoteOnMessage(smessage, randVelocity), event.getTick());
    }
    return event;
  }

  private static ShortMessage processNoteOnMessage(ShortMessage message, double randVelovity) throws InvalidMidiDataException {
    return new ShortMessage(message.getStatus(),
            message.getData1(),
            processVelocity(message.getData2(), randVelovity));

  }

  private static final int[] inputVel
          = {
            0,
            53, // ppp
            62, // pp
            69, // p
            77, // mp
            86, // mf
            95, // f
            101,// ff
            107,// fff
            128
          };

  private static final int[] outputVel
          = {
            0,
            30, // ppp
            35, // pp
            40, // p
            45, // mp
            50, // mf
            55, // f
            60,// ff
            70,// fff
            128
          };

  /**
   * Linear interpolation between inputVel and outputVel
   *
   * @param velocity
   * @return
   */
  private static int processVelocity(int velocity, double randVelocity) {
    if (velocity == 0) {
      return velocity;
    }
    int interval = inputVel.length - 1;
    while (velocity < inputVel[interval]) {
      interval--;
    }
    double x = (double) (velocity - inputVel[interval]) / (double) (inputVel[interval + 1] - inputVel[interval]);
    double v = (x * (double) (outputVel[interval + 1] - outputVel[interval])) + outputVel[interval];

    int result = (int) Math.round(v + randVelocity);
    if (result > 127) {
      result = 127;
    }
    if (result < 0) {
      result = 0;
    }

    return result;

  }

  /**
   * a kind of f noise.
   *
   * @param previous
   * @return
   */
  private static double randomIncrement(double previous) {

    double random = (Math.random() - 0.5D) * maxRandomRange;

    return random;//+ scale * previous;

  }

}
