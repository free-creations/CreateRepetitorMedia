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
import javax.sound.midi.*;

/**
 * Merges tracks from an other sequence into a master sequence. The
 * tick-positions from input tracks are re-calculated so as to match the
 * resolution of the master track.
 * 
 * @Todo When Tracks are merged there might be trouble with colliding notes.
 * Add collision detection for NoteOn NoteOff events. Add detection
 * for hanging notes.
 *
 * @author Harald Postner
 */
public class TrackMerger {

  final Sequence masterSequence;
  final Sequence inputSequence;
  final Sequence outputSequence;
  final int[] trackIndices;
  final double resolutionFactor;
  final int channel;
  final String trackname;
  private boolean tracknameIsSet = false;

  private TrackMerger(Sequence masterSequence, Sequence inputSequence, int[] trackIndices, int channel, String trackname) throws InvalidMidiDataException {
    if (masterSequence.getDivisionType() != Sequence.PPQ) {
      throw new InvalidMidiDataException("This division type is not (yet) implemented.");
    }

    this.masterSequence = masterSequence;
    this.inputSequence = inputSequence;
    this.resolutionFactor = (double) masterSequence.getResolution() / (double) inputSequence.getResolution();
    this.trackIndices = trackIndices;
    this.outputSequence = new Sequence(masterSequence.getDivisionType(), masterSequence.getResolution());
    this.channel = channel;
    this.trackname = trackname;
  }

  private Sequence process() throws InvalidMidiDataException {
    // copy all the tracks from the master sequence
    Track[] inputTracks = masterSequence.getTracks();
    for (int trackI = 0; trackI < inputTracks.length; trackI++) {
      Track inputTrack = inputTracks[trackI];
      Track outputTrack = outputSequence.createTrack();
      for (int eventI = 0; eventI < inputTrack.size(); eventI++) {
        MidiEvent midEvent = inputTrack.get(eventI);
        outputTrack.add(midEvent);
      }
    }
    // copy the requested tracks into a new track in the output
    inputTracks = inputSequence.getTracks();
    Track outputTrack = outputSequence.createTrack();
    if (trackname != null) {
      outputTrack.add(makeTracknameEvent(trackname, 0));
    }
    for (int trackI : trackIndices) {
      Track inputTrack = inputTracks[trackI];
      for (int eventI = 0; eventI < inputTrack.size(); eventI++) {
        if (isCopyEvent(inputTrack.get(eventI))) {
          MidiEvent midEvent = channelizeEvent(inputTrack.get(eventI));
          outputTrack.add(midEvent);
        }
      }
    }
    return outputSequence;
  }

  private MidiEvent channelizeEvent(MidiEvent event) throws InvalidMidiDataException {
    MidiMessage rawMessage = event.getMessage();
    long newTick = Math.round(resolutionFactor * event.getTick());
    if (!(rawMessage instanceof ShortMessage)) {
      return new MidiEvent((MidiMessage) rawMessage.clone(), newTick);
    }
    ShortMessage in_message = (ShortMessage) rawMessage;
    ShortMessage out_message = new ShortMessage();
    int channelToUse = channel;
    if (channelToUse < 0) {
      channelToUse = in_message.getChannel();
    }
    out_message.setMessage(in_message.getCommand(),
            channelToUse,
            in_message.getData1(),
            in_message.getData2());

    return new MidiEvent(out_message, newTick);
  }

  private MidiEvent makeTracknameEvent(String text, long tickPos) throws InvalidMidiDataException {
    MetaMessage message = new MetaMessage();
    message.setMessage(MidiUtil.tracknameMeta, text.getBytes(), text.length());

    return new MidiEvent(message, tickPos);
  }

  /**
   * Merge the content of some tracks from an input sequence into 
   * one new track of a master sequence.
   *
   * @param masterSequence the sequence where the tracks should be merged into.
   * @param inputSequence the sequence from which the tracks should be copied
   * @param trackIndices the indices of the tracks that should be taken from the
   * input sequence, all these track will be merged into one new track which
   * will be appened to the master-tracks.
   * @param channel all events from the input-track will be re-channelized to
   * this channel. If a value of -1 is given, no re-channelizing will take
   * effect.
   * @param trackname the name of the new track. If null the name of the input
   * track will be used.
   * @param loggingHandler
   * @return a new sequence that contains all the tracks of the master sequence
   * plus one new track with all events from from the input tracks.
   * @throws InvalidMidiDataException
   */
  public static Sequence process(final Sequence masterSequence, final Sequence inputSequence, int[] trackIndices, int channel, String trackname, Handler loggingHandler) throws InvalidMidiDataException {
    TrackMerger trackMerger = new TrackMerger(masterSequence, inputSequence, trackIndices, channel, trackname);
    return trackMerger.process();

  }

  /**
   * Copy all events except the track name, but if we do not have a valid
   * track-name we will also take the track-name.
   *
   * @param midEvent
   * @return
   */
  private boolean isCopyEvent(MidiEvent midEvent) {

    if (midEvent.getMessage() instanceof MetaMessage) {
      MetaMessage message = (MetaMessage) midEvent.getMessage();
      if (message.getType() == MidiUtil.tracknameMeta) {
        if (tracknameIsSet) {
          return false;
        }
        if (trackname == null) {
          tracknameIsSet = true;
          return true;
        }        
        return false;
      }
    }
    return true;
  }
}
