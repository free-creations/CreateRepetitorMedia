/*
 *  Copyright 2011 Harald Postner <Harald at H-Postner.de>.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package de.free_creations.importexport;

import de.free_creations.midiutil.GmUtil;
import de.free_creations.midiutil.MidiUtil;
import java.util.ArrayList;
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
 * This utility re-orders the events in the tracks so that every track can be
 * unambiguously related to one musical instrument. <p> The Midi File
 * specification allows events for different channels to be put in one and the
 * same track (format 0 files have only one track); and vice-versa it is also
 * allowed to have events for one and the same channel to be dispersed over many
 * tracks. Authors of Midi sequences sometimes make use of these possibilities
 * in order to be able use more than sixteen instruments in a sequence. This
 * makes it difficult to control what happens where. </p> <p> Therefore we will
 * assign to each track only events for one and the same channel. </p> <p>
 * Strategy of the importer: </p> <p> for each track in the input sequence: <ol>
 * <li>create a track in the output sequence.</li> <li>scan the input track for
 * events that should be used to initialise the resulting tracks and store them
 * separately.</li> <li>if the input track contains events that should not
 * figure in one and the same track, create the corresponding number of
 * additional tracks and shift these events in the these additional tracks.</li>
 * </ol> </p>
 *
 * @author Harald Postner <Harald at H-Postner.de>
 */
public class ChannelCleaner {

  private static final Logger logger =
          Logger.getLogger(ChannelCleaner.class.getName());
  private Sequence result;
  private final Sequence inputSequence;
  /**
   * This array indicates the name given to each track in the
   * instrumentDescription sequence.
   */
  private ArrayList<String> TrackNames = new ArrayList<String>();
  /**
   * This array indicates for which channel each track is reserved. a Value
   * between 0 and 15 indicates a midi channel. Outside these values there are
   * values witch internally have a special meaning. (Notably
   * "_undefinedChannel" : this channel has yet not been reserved and
   * "_directorChannel" : this channel is reserved for the director channel)
   */
  private ArrayList<Integer> trackChannels = new ArrayList<Integer>();
  public final int _undefinedChannel = -1;
  private final int _directorChannel = -2;
  /**
   * This array indicates for which Program (Musical instrument sound) each
   * track is reserved.
   */
  // private ArrayList<Integer> trackPrograms = new ArrayList<Integer>();
  private ArrayList<String> trackInstrumentDescription = new ArrayList<String>();
  public final int _undefinedProgram = -1;
  /**
   * This array is used for bookkeeping. It indicates for each of the 16
   * channels, which program is to be used.
   */
  private int[] channelUsage = new int[]{
    -1, -1, -1, -1, -1, -1, -1, -1,
    -1, -1, -1, -1, -1, -1, -1, -1,};

  ChannelCleaner(final Sequence inputSequence) throws InvalidMidiDataException {
    this(inputSequence, null);
  }

  public ChannelCleaner(final Sequence inputSequence, Handler loggingHandler) throws InvalidMidiDataException {
    if (loggingHandler != null) {
      logger.addHandler(loggingHandler);
    }
    this.inputSequence = inputSequence;
    createResult();
  }

  public static Sequence process(final Sequence inputSequence, Handler loggingHandler) throws InvalidMidiDataException {
    ChannelCleaner cleaner = new ChannelCleaner(inputSequence, loggingHandler);
    return cleaner.getResult();
  }

  public Sequence getResult() {
    return result;
  }

  private void createResult() throws InvalidMidiDataException {

    // Create a instrumentDescription sequence
    result = new Sequence(inputSequence.getDivisionType(), inputSequence.getResolution());
    // add a director track to the instrumentDescription sequence
    addResultTrack();
    reserveResultTrack(0, _directorChannel);

    Track[] inputTracks = inputSequence.getTracks();
    // for each track in the inputSequence create a track in the instrumentDescription
    for (int i = 1; i < inputTracks.length; i++) {
      addResultTrack();
    }
    // import each input track
    for (int i = 0; i < inputTracks.length; i++) {
      importTrack(i, inputTracks[i]);
    }
    //housekeeping
    Track[] resultTracks = result.getTracks();
    for (int i = 0; i < resultTracks.length; i++) {
      channelVerification(i);
      programVerification(i);
      makeTrackName(i);
    }
    listFreeChannels();
  }

  private void importTrack(int originalIdx, Track track) {

    for (int i = 0; i < track.size(); i++) {
      MidiEvent event = track.get(i);
      Track resultTrack = establishTrackForEvent(event, originalIdx);
      if (resultTrack != null) {
        resultTrack.add(event);
      }
    }
  }

  /**
   * Find among the the instrumentDescription tracks one that suits the given
   * event. The primary rule is, that a track shall contain only events for one
   * and the same channel.
   *
   * @param event
   * @param originalIdx
   * @return
   */
  private Track establishTrackForEvent(MidiEvent event, int originalIdx) {
    // If it is a director event it must go on track zero.
    if (isDirectorEvent(event)) {
      return result.getTracks()[0];
    }
    // Determine the channel for the event.
    int channel = channelFromEvent(event);

    // If the event has no channel (a meta event) we will
    // put it on the same track where it originally was.
    if (channel == _undefinedChannel) {
      return result.getTracks()[originalIdx];
    }

    // The original track has allready been reserved
    // for this channel so we can just use it
    if (trackChannels.get(originalIdx) == channel) {
      return result.getTracks()[originalIdx];
    }

    // The original track is still unreserved; so we'll reserve and use it.
    if (trackChannels.get(originalIdx) == _undefinedChannel) {
      reserveResultTrack(originalIdx, channel);
      return result.getTracks()[originalIdx];
    }

    // We still did not find a suitable track,
    // so let's search among all track allocated so far, if there
    // is one that has suitable channel.
    for (int i = 0; i < trackChannels.size(); i++) {
      if (trackChannels.get(i) == channel) {
        return result.getTracks()[i];
      }
    }

    // All attemps to find a track among the existing tracks
    // have failed, we must create a new track.
    int newTrackIdx = result.getTracks().length;
    Track newTrack = addResultTrack();
    reserveResultTrack(newTrackIdx, channel);

    // Report the creation of a new track with the following message:
    // "Additional track {0} created, because track {1} originally used channel {2} and {3}."
    logger.logrb(Level.INFO,
            getClass().toString(),
            "establishTrackForEvent",
            "de.free_creations.importexport.Bundle",
            "CTL_TrackAddedInfo",
            new Object[]{
              new Integer(newTrackIdx),
              new Integer(originalIdx),
              channelToStr(trackChannels.get(originalIdx)),
              channelToStr(channel)});

    return newTrack;

  }

  /**
   * add a new track to the instrumentDescription sequence.
   *
   * @return
   */
  private Track addResultTrack() {
    int newTrackIdx = result.getTracks().length;
    Track newTrack = result.createTrack();
    TrackNames.add("Track " + newTrackIdx);
    trackChannels.add(_undefinedChannel);
    trackInstrumentDescription.add(null);
    return newTrack;
  }

  /**
   * reserves a track for a given channel.
   *
   * @param trackIdx
   * @param channel
   */
  private void reserveResultTrack(int trackIdx, int channel) {
    Track[] resultTracks = result.getTracks();
    if (trackIdx >= resultTracks.length) {
      throw new RuntimeException("Out of bounds, trackIdx=" + trackIdx);
    }
    if (channel == trackChannels.get(trackIdx)) {
      return;
    }
    if ((_undefinedChannel != trackChannels.get(trackIdx))) {
      throw new RuntimeException("Attempt to reserve already taken track " + trackIdx);
    }
    trackChannels.set(trackIdx, channel);
  }

  /**
   * Test whether the given event is an event that should exclusively appear in
   * the director track (track (0)).
   *
   * @param event the event to be tested.
   * @return true if the event is one of those events that should not appear in
   * other tracks than track zero.
   */
  private boolean isDirectorEvent(MidiEvent event) {
    MidiMessage message = event.getMessage();
    if (!(message instanceof MetaMessage)) {
      return false;
    }
    int type = ((MetaMessage) message).getType();
    if (type == MidiUtil.tempoMeta) {
      return true;
    }
    if (type == MidiUtil.timeSignatureMeta) {
      return true;
    }
    return false;
  }

  private int channelFromEvent(MidiEvent event) {
    MidiMessage message = event.getMessage();
    if (!(message instanceof ShortMessage)) {
      return _undefinedChannel;
    }
    ShortMessage shortMessage = (ShortMessage) message;

    return shortMessage.getChannel();
  }

  /**
   * Verify that very channel is used only in one track.
   *
   * @param trackIdx the track to be verified.
   */
  private void channelVerification(int trackIdx) {

    int channel = trackChannels.get(trackIdx);
    if ((channel >= 0) && (channel < 16)) {
      int usedTrck = channelUsage[channel];
      if (usedTrck != -1) {
        if (usedTrck != trackIdx) {
          logger.logrb(Level.WARNING,
                  getClass().toString(),
                  "channelVerification",
                  "de.free_creations.importexport.Bundle",
                  "CTL_MulipleTracksForOneChannelWarning",
                  new Object[]{
                    channelToStr(channel),
                    new Integer(usedTrck),
                    new Integer(trackIdx)
                  });
        }
      } else {
        channelUsage[channel] = trackIdx;
      }
    }
  }

  /**
   * Returns the channel used in a given track. May return
   * <code>_undefinedChannel</code>.
   *
   * @param trackIdx the index of the track in the instrumentDescription
   * sequence
   */
  public int getChannel(int trackIdx) {
    if ((trackIdx < trackChannels.size())
            && (trackIdx >= 0)) {
      return trackChannels.get(trackIdx);
    } else {
      return _undefinedChannel;
    }
  }

  private void programVerification(int trackIdx) {
    Track track = result.getTracks()[trackIdx];
    for (int i = 0; i < track.size(); i++) {
      MidiEvent event = track.get(i);
      if (MidiUtil.isProgramEvent(event)) {
        int program = ((ShortMessage) event.getMessage()).getData1();
        String newInstrDescription = (program + 1) + " " + GmUtil.numberToString(program);
        String prevInstrDescription = trackInstrumentDescription.get(trackIdx);
        if (prevInstrDescription == null) {
          trackInstrumentDescription.set(trackIdx, newInstrDescription);
        } else {
          trackInstrumentDescription.set(trackIdx, prevInstrDescription
                  + "/" + newInstrDescription);
        }
      }
    }
  }

  private void makeTrackName(int trackIdx) {
    Track track = result.getTracks()[trackIdx];
    String nameFromEvent = MidiUtil.readTrackname(track);
    if (nameFromEvent.length() > 0) {
      TrackNames.set(trackIdx, nameFromEvent);
      return;
    }
    String instruments = trackInstrumentDescription.get(trackIdx);
    if (instruments != null) {
      TrackNames.set(trackIdx, instruments);
      return;
    }
    TrackNames.set(trackIdx, trackIdx + "");
  }

  public String getTrackName(int trackIdx) {
    if ((trackIdx < TrackNames.size())
            && (trackIdx >= 0)) {
      return TrackNames.get(trackIdx);
    } else {
      return "";
    }
  }

  public int getTrackCount() {
    return result.getTracks().length;
  }

  /**
   * Assemble a list of free channels and report it through the logger.
   */
  private void listFreeChannels() {
    String freeChannels = "";
    for (int channel = 0; channel < channelUsage.length; channel++) {
      if (channelUsage[channel] == _undefinedProgram) {
        freeChannels = freeChannels + ", " + channelToStr(channel);
      }
    }

    if (freeChannels.length() < 1) {
      freeChannels = "none";
    } else {
      freeChannels = freeChannels.substring(2);
    }
    logger.logrb(Level.INFO,
            getClass().toString(),
            "listFreeChannels",
            "de.free_creations.importexport.Bundle",
            "CTL_FreeChannels",
            new Object[]{
              freeChannels
            });
  }

  /**
   * Traditionally channels are count from 1 in printouts.
   *
   * @param channel
   * @return
   */
  private String channelToStr(int channel) {
    if (channel == _directorChannel) {
      return "\"director channel\"";
    }
    if (channel == _undefinedChannel) {
      return "\"undefined channel\"";
    }
    return "" + (channel + 1);
  }

  public String getInstrumentDescription(int trackIdx) {
    String instrumentDescription = null;
    if ((trackIdx < trackInstrumentDescription.size())
            && (trackIdx >= 0)) {
      instrumentDescription = trackInstrumentDescription.get(trackIdx);
    }
    if (instrumentDescription != null) {
      return instrumentDescription;
    } else {
      return "undefined";
    }
  }
}
