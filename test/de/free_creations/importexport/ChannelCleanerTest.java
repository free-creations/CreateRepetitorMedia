/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.free_creations.importexport;

import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.Level;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.Track;
import javax.sound.midi.InvalidMidiDataException;
import org.junit.Before;
import javax.sound.midi.Sequence;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Harald Postner <Harald at H-Postner.de>
 */
public class ChannelCleanerTest {

  /**
   * A sequence to test the normal case.
   */
  Sequence normalSequence;

  @Before
  public void setUp() throws InvalidMidiDataException {
    normalSequence = new Sequence(Sequence.PPQ, 480);
    Track directorTrack = normalSequence.createTrack();
    addDirectorTrackEvents(directorTrack);
    Track musicalTrack_1 = normalSequence.createTrack();
    addMusicTrackEvents(musicalTrack_1, 1);
    Track musicalTrack_2 = normalSequence.createTrack();
    addMusicTrackEvents(musicalTrack_2, 2);
    Track musicalTrack_3 = normalSequence.createTrack();
    addMusicTrackEvents(musicalTrack_3, 3);
  }

  /**
   * Test whether the importer correctly takes over a sequence
   * that is already in the normalized form.
   */
  @Test
  public void testNormalSequence() throws InvalidMidiDataException {
    System.out.println("testNormalSequence");
    Track track_3 = normalSequence.getTracks()[3];
    track_3.add(makeTrackNameEvent("Third Track", 0));

    MocLoggingHandler mocHandler = new MocLoggingHandler(null);
    ChannelCleaner instance = new ChannelCleaner(normalSequence, mocHandler);
    Sequence result = instance.getResult();

    assertNotNull(result);
    Track[] resultTracks = result.getTracks();
    Track[] inputTracks = normalSequence.getTracks();

    // A non-pathological sequence should be taken-over without any changes.
    // Therefore the input sequence (->normalTracks) and the ouput
    // sequence (->resultTracks) should have the same characteristics.
    assertEquals(inputTracks.length, resultTracks.length);
    assertEquals(inputTracks[0].size(), resultTracks[0].size());
    for (int i = 0; i < inputTracks.length; i++) {
      assertEquals(inputTracks[i].size(), resultTracks[i].size());
    }

    // There should be only one message issued during procesing(the one about free channels)
    assertEquals(1, mocHandler.getCount());

    // We have put channel 1 into track 1 and channel 2 into track 2 and so on.
    for (int i = 1; i < resultTracks.length; i++) {
      assertEquals(i, instance.getChannel(i));
    }


    
    // Have the track names been assigned as expected?
    // Tracks 1 and 2  do not have individual names;
    // so the instrument should be used.
    assertTrue(instance.getTrackName(1).indexOf("Bright Acoustic Piano") != -1);
    assertTrue(instance.getTrackName(2).indexOf("Electric grand Piano") != -1);
    // Track 3 has got its individual name (see setUp)
    assertTrue(instance.getTrackName(3).indexOf("Third Track") != -1);
  }

  /**
   * In a file of type zero, all events are one one track.
   * The importer should spread these events over several tracks.
   * @throws InvalidMidiDataException
   */
  @Test
  public void testMidiTypeZeroSequence() throws InvalidMidiDataException {
    System.out.println("testMidiTypeZeroSequence");
    Sequence midiOneSequence = new Sequence(Sequence.PPQ, 480);
    Track theOneAndOnlyTrack = midiOneSequence.createTrack();
    addDirectorTrackEvents(theOneAndOnlyTrack);
    addMusicTrackEvents(theOneAndOnlyTrack, 1);
    addMusicTrackEvents(theOneAndOnlyTrack, 2);
    addMusicTrackEvents(theOneAndOnlyTrack, 3);

    MocLoggingHandler mocHandler = new MocLoggingHandler(null);
    ChannelCleaner instance = new ChannelCleaner(midiOneSequence, mocHandler);
    Sequence result = instance.getResult();
    assertNotNull(result);
    Track[] resultTracks = result.getTracks();

    // the resulting sequence should look like the "normal" sequence
    // Therefore we compare the ouput
    // sequence (->resultTracks) against the normal sequence (->normalTracks)
    Track[] normalTracks = normalSequence.getTracks();
    assertTrue(resultTracks.length >= normalTracks.length);

    assertEquals(normalTracks[0].size(), resultTracks[0].size());


    assertEquals(normalTracks.length, resultTracks.length);
    assertEquals(normalTracks[0].size(), resultTracks[0].size());
    for (int i = 1; i < normalTracks.length; i++) {
      assertEquals(normalTracks[i].size(), resultTracks[i].size());
    }
    // we expect some info messages that tracks have been added
    assertTrue(0 < mocHandler.getCount());

    //we have put channel 1 into track 1 and channel 2 into track 2 and so on.
    for (int i = 1; i < resultTracks.length; i++) {
      assertEquals(i, instance.getChannel(i));
    }

  }

  /**
   * Lets see what happens when the input sequence has several tracks
   * that use the same channel. The importer should generate a number
   * of warnings, we'll check for that.
   * @throws InvalidMidiDataException
   */
  @Test
  public void testMidiChannelProblem() throws InvalidMidiDataException {
    System.out.println("testMidiChannelProblem");
    Track notSoNormalTrack = normalSequence.createTrack();
    addMusicTrackEvents(notSoNormalTrack, 1);
    MocLoggingHandler mocHandler = new MocLoggingHandler("CTL_MulipleTracksForOneChannelWarning");
    ChannelCleaner instance = new ChannelCleaner(normalSequence, mocHandler);

    instance.getResult();
    assertEquals(1, mocHandler.getCount());

  }


  /**
   * Create a time-signature test-event.
   * @param num numerator
   * @param denomFactor denominator
   * @param tick where to insert the event
   * @return the new event.
   */
  private MidiEvent makeTimeSigEvent(byte num, byte denomFactor, long tick) {
    try {
      MetaMessage message = new MetaMessage();
      message.setMessage(88, new byte[]{num, denomFactor}, 2);
      MidiEvent event = new MidiEvent(message, tick);
      return event;
    } catch (InvalidMidiDataException ex) {
      throw new RuntimeException(ex.getCause());
    }
  }

  /**
   * Create a test event that indicates a tempo change.
   * @param tt1 the first byte of the tempo in microseconds per quarter note.
   * @param tt1 the second byte of the tempo in microseconds per quarter note.
   * @param tt1 the third byte of the tempo in microseconds per quarter note.
   * @param tick where to insert the event
   * @return the new event.
   */
  private MidiEvent makeTempoEvent(byte tt1, byte tt2, byte tt3, long tick) {
    try {
      MetaMessage message = new MetaMessage();
      message.setMessage(0x51, new byte[]{tt1, tt2, tt3}, 3);
      MidiEvent event = new MidiEvent(message, tick);
      return event;
    } catch (InvalidMidiDataException ex) {
      throw new RuntimeException(ex.getCause());
    }
  }

  /**
   * Create a test event that indicates the key in which the piece is composed.
   * @param key A positive value for the key specifies the number of
   * sharps and a negative value specifies the number of flats
   * @param scale A value of 0 for the scale specifies a major key and a
   * value of 1 specifies a minor key.
   * @param tick
   * @return
   */
  private MidiEvent makeKeySignatureEvent(int key, int scale, long tick) {
    try {
      MetaMessage message = new MetaMessage();
      message.setMessage(0x59, new byte[]{(byte) key, (byte) scale}, 2);
      MidiEvent event = new MidiEvent(message, tick);
      return event;
    } catch (InvalidMidiDataException ex) {
      throw new RuntimeException(ex.getCause());
    }
  }

  /**
   * Create an event for a note on message.
   * @return the new event.
   */
  private MidiEvent makeNoteOnEvent(int pitch, int channel, long tick) {
    ShortMessage message = new ShortMessage();
    try {
      message.setMessage(ShortMessage.NOTE_ON, channel, pitch, 64);
    } catch (InvalidMidiDataException ex) {
      Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
    }
    return new MidiEvent(message, tick);
  }

  private MidiEvent makeNoteOffEvent(int pitch, int channel, long tick) {
    ShortMessage message = new ShortMessage();
    try {
      message.setMessage(ShortMessage.NOTE_OFF, channel, pitch, 64);
    } catch (InvalidMidiDataException ex) {
      Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
    }
    return new MidiEvent(message, tick);
  }

  private MidiEvent makeVolumeEvent(int volume, int channel, long tick) {
    ShortMessage message = new ShortMessage();
    try {
      message.setMessage(ShortMessage.CONTROL_CHANGE, channel, 07, volume);
    } catch (InvalidMidiDataException ex) {
      Logger.getLogger(ChannelCleanerTest.class.getName()).log(Level.SEVERE, null, ex);
    }
    return new MidiEvent(message, tick);
  }

  /**
   * Creates a Expression Controller message.
   * @param expressionVolume the attenuation (127 no attenuation. 0 full attenuation)
   * @param channel
   * @param tick
   * @return
   */
  private MidiEvent makeExpressionEvent(int expressionVolume, int channel, long tick) {
    ShortMessage message = new ShortMessage();
    try {
      message.setMessage(ShortMessage.CONTROL_CHANGE, channel, 0xB, expressionVolume);
    } catch (InvalidMidiDataException ex) {
      Logger.getLogger(ChannelCleanerTest.class.getName()).log(Level.SEVERE, null, ex);
    }
    return new MidiEvent(message, tick);
  }

  /**
   * Creates a program change event.
   * @param instrument
   * @param channel
   * @param tick
   * @return
   */
  private MidiEvent makeProgramEvent(int instrument, int channel, long tick) {
    ShortMessage message = new ShortMessage();
    try {
      message.setMessage(ShortMessage.PROGRAM_CHANGE, channel, instrument, 0);
    } catch (InvalidMidiDataException ex) {
      Logger.getLogger(ChannelCleanerTest.class.getName()).log(Level.SEVERE, null, ex);
    }
    return new MidiEvent(message, tick);
  }

  /**
   * Creates a program change event.
   * @param instrument
   * @param channel
   * @param tick
   * @return
   */
  private MidiEvent makeTrackNameEvent(String trackname, long tick) {
    try {
      MetaMessage message = new MetaMessage();
      byte[] nameBytes = trackname.getBytes("UTF-8");
      message.setMessage(0x03, nameBytes, nameBytes.length);
      MidiEvent event = new MidiEvent(message, tick);
      return event;
    } catch (Exception ex) {
      throw new RuntimeException(ex.getCause());
    }
  }

  /**
   * Helper function that adds events which are normally to be found in a musical track.
   * @param track
   * @param channel
   * @return the number of events inserted.
   */
  private int addMusicTrackEvents(Track track, int channel) {
    track.add(makeProgramEvent(channel, channel, 0));
    track.add(makeVolumeEvent(64, channel, 0));
    //track.add(makeKeySignatureEvent(1, 0, 0));
    track.add(makeNoteOnEvent(60, channel, 480));
    track.add(makeExpressionEvent(71, channel, 500));
    track.add(makeNoteOffEvent(60, channel, 560));
    return 6;
  }

  /**
   * Helper function that adds events which typically should figure in
   * Track 0.
   * @param directorTrack
   * @return the number of events inserted.
   */
  private int addDirectorTrackEvents(Track directorTrack) {
    directorTrack.add(makeTimeSigEvent((byte) 4, (byte) 4, 0L));
    directorTrack.add(makeTimeSigEvent((byte) 3, (byte) 4, 480L * 16L));
    directorTrack.add(makeTempoEvent((byte) 0x07, (byte) 0xA1, (byte) 0x20, 0));
    directorTrack.add(makeTempoEvent((byte) 0x01, (byte) 0xB2, (byte) 0x21, 50000));
    return 4;
  }

  private static class MocLoggingHandler extends Handler {

    private final String watchedMsgId;
    private int count = 0;

    /**
     * Create a MocLoggingHandler and
     * set the message-Id to the one we are looking for.
     * Setting this value to null means that all messages should be watched.
     * @param watchedMsgId these message will be counted. If
     * <code>null</code>, all messages will be counted.
     */
    MocLoggingHandler(String watchedMsgId) {
      this.watchedMsgId = watchedMsgId;
    }

    @Override
    public void publish(LogRecord record) {
      if (watchedMsgId == null) {
        count++;
      } else {
        if (watchedMsgId.equals(record.getMessage())) {
          count++;
        }
      }
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() throws SecurityException {
    }

    public int getCount() {
      return count;
    }
  }
}
