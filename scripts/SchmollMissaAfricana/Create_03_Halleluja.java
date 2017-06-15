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
package SchmollMissaAfricana;

import Salome_MissaBrevis.*;
import de.free_creations.importexport.ChannelCleaner;
import de.free_creations.importexport.ControllerRemover;
import de.free_creations.importexport.Randomizer;
import de.free_creations.importexport.SlurBinderB;
import de.free_creations.importexport.TimeShifter;
import de.free_creations.importexport.TrackMerger;
import de.free_creations.importexport.VelocityCorrectionA;
import de.free_creations.importexport.VelocityCorrectionB;
import de.free_creations.midiutil.MidiUtil;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.logging.Handler;
import javax.sound.midi.*;
import javax.xml.bind.JAXBException;

/**
 *
 * @author Harald Postner
 */
public class Create_03_Halleluja {

  private final File inputFile;
  private File outputMidiFile;
  private final Handler loggingHandler;
  final static private String piece = "03_Halleluja";
  // final static private String variant = "";
  final static private int resolution = 480;
  static final private File resourceDir = new File("/audioVideo/Music/Midi/Chor/Patrozinium2017/MissaAfricana");

  private Create_03_Halleluja() throws IOException {
    loggingHandler = null;
    File projectDir = new File(resourceDir, piece);
    if (!projectDir.exists()) {
      throw new RuntimeException(projectDir.getPath() + " not found.");
    }
    if (!projectDir.isDirectory()) {
      throw new RuntimeException(projectDir + " is not a directory.");
    }
    inputFile = new File(projectDir, "FullScore.mid");
    if (!inputFile.exists()) {
      throw new RuntimeException(inputFile.getPath() + " not found.");
    }

    outputMidiFile = new File(projectDir, "Nice.mid");

  }

  private void process() throws InvalidMidiDataException, IOException, JAXBException {

    Sequence inputSequence = MidiSystem.getSequence(inputFile);
    // clean the volume and expression
    inputSequence = ControllerRemover.process(inputSequence, MidiUtil.contMainVolume_MSB, loggingHandler);
    inputSequence = ControllerRemover.process(inputSequence, ControllerRemover.contProgramChange, loggingHandler);

    Sequence masterSequence = new Sequence(Sequence.PPQ, resolution);

    //  copy and clean  the input sequence to make sure we have the correct resolution
    for (int i = 0; i < inputSequence.getTracks().length; i++) {
      masterSequence = TrackMerger.process(masterSequence, inputSequence, new int[]{i}, -1, null, loggingHandler); //
    }
    ChannelCleaner sequenceImporter = new ChannelCleaner(masterSequence, loggingHandler);
    masterSequence = sequenceImporter.getResult();

    //-- correct velocity values
    /*
    int[] backgroundTracks = new int[]{5, 6, 7, 8};
    masterSequence = VelocityCorrectionA.process(masterSequence, backgroundTracks, loggingHandler);
    int[] pianoTracks = new int[]{1, 2, 3, 4};
    masterSequence = VelocityCorrectionB.process(masterSequence, pianoTracks, loggingHandler);
     */
    // --- Time shift
    // values in ms.
    // (the piece plays at 168 qpm there are 1344 Ticks per second)
    long[] deltaTicks = {
      0, // 0, director
      // Forground
      0, //  1 , S piano
      0, //  2 , A piano
      0, //  3 , M piano

      // Back ground -> 10 ms
      15, // 04 , S solo
      15, // 05 , A choir
      15, // 06,  M choir

      // Accompaniment
      10, // 07 ,  acc Piano
      0, // 08 , bass guitar

      0, // Percussion
      0, // metronome
    };
    masterSequence = TimeShifter.process(masterSequence, deltaTicks, loggingHandler);

    // --- pan positions
    // (64 is middle)
    int[] panPos = {
      0, // 0, director

      // Forground
      64, //  1 , S piano
      64, //  2 , A piano
      64, //  3 , M piano

      // Back ground -> 10 ms
      48, // 04 , S solo
      80, // 05 , A choir
      64, // 06,  M choir

      // Accompaniment
      50, // 07 ,  acc Piano
      78, // 08 , bass guitar

      64, // Percussion
      64, // metronome
    };

    // --- volume
    // (127 is full)
    int[] volume = {
      0, // 0, director

      // Forground
      80, //  1 , S piano
      80, //  2 , A piano
      80, //  3 , M piano

      // Back ground -> 10 ms
      64, // 04 , S solo
      64, // 05 , A choir
      64, // 06,  M choir

      // Accompaniment
      50, // 07 ,  acc Piano
      78, // 08 , bass guitar

      30, // Percussion
      100, // metronome
    };

    // --- bank select
    // 
    int[] bank = {
      0, //  0 , director track

      // Forground
      0, //  1 , S piano
      0, //  2 , A piano
      0, //  3 , M piano

      // Back ground
      0, //  4 , S c (0/0 Aaah Choir) 
      0, //  5 , A c (0/0 Aaah Choir) 
      0, //  6 , M c (0/0 Aaah Choir) 

      // Accompaniment
      0, // 07 ,  acc Piano
      0, // 08 , bass guitar

      0, // Percussion
      0, // metronome
    };

    // --- programm select
    // 
    int[] programm = {
      0, //  0 , director track
      // Forground Kawai CS7 Digitalpiano (3 Studio Grand)
      0, //  1 , S piano
      0, //  2 , A piano
      0, //  3 , M piano

      // Back ground
      0, //  6 , S c (0/0 Aaah Choir) 
      0, //  7 , A c (0/0 Aaah Choir) 
      0, //  8 , B c (0/0 Aaah Choir) 

      // Accompaniment
      4, // 07 ,  acc Piano (Electric Piano)
      33, // 08 , bass guitar (Finger Bass)

      0, // Percussion (Standard Kit)
      0, // metronome
    };
    // --- create track initialization
    Track[] masterTracks = masterSequence.getTracks();
    for (int i = 1; i < masterTracks.length; i++) {
      int channel = sequenceImporter.getChannel(i);
      if (channel != ChannelCleaner._undefinedChannel) {
        masterTracks[i].add(newVolumeMessage(0, channel, volume[i]));
        masterTracks[i].add(newExpressionMessage(0, channel, 127));
        masterTracks[i].add(newPanMessage(0, channel, panPos[i]));
        masterTracks[i].add(newBankSelectMessage(0, channel, bank[i]));
        masterTracks[i].add(newProgramMessage(1, channel, programm[i]));
      }
    }

    // --- Randomize
    // values in ms.
    // (the piece plays at 168 qpm, a delay of 22 ms corresponds to 1/64 note)
    int[] maxDelay = {
      0, //  0 , director track
      // 
      0, //  1 , S piano
      0, //  2 , A piano
      0, //  3 , M piano

      // Back ground
      22, //  6 , S c (0/0 Aaah Choir) 
      22, //  7 , A c (0/0 Aaah Choir) 
      22, //  8 , B c (0/0 Aaah Choir) 

      // Accompaniment
      22, // 07 ,  acc Piano (Electric Piano)
      0, // 08 , bass guitar (Finger Bass)

      0, // Percussion (Standard Kit)
      0, // metronome
    };
    masterSequence = Randomizer.process(masterSequence, maxDelay, true, loggingHandler);

    // ---- bind (slur notes)
    long startTick = 0;
    long lastTick = masterSequence.getTickLength();
    int noteOverlap = 480 / 8; // in MidiTicks -> 1/32
    int minimumRest = 480 / 2; // eighth rest

    masterSequence = SlurBinderB.process(masterSequence, 4, startTick, lastTick, noteOverlap, minimumRest, loggingHandler);
    masterSequence = SlurBinderB.process(masterSequence, 5, startTick, lastTick, noteOverlap, minimumRest, loggingHandler);
    masterSequence = SlurBinderB.process(masterSequence, 6, startTick, lastTick, noteOverlap, minimumRest, loggingHandler);

// Write the file to disk
    MidiSystem.write(masterSequence, 1, outputMidiFile);
    System.out.println("############ Midi file is: " + outputMidiFile.getCanonicalPath());

    //----------------------------------------------------------------------------------------
  }

  private MidiEvent newEndOfTrackMessage(long tick) {
    MetaMessage message = new MetaMessage();
    try {
      message.setMessage(
              MidiUtil.endOfTrackMeta,
              new byte[]{},
              0);//length)
    } catch (InvalidMidiDataException ex) {
      throw new RuntimeException(ex);
    }

    return new MidiEvent(message, tick);
  }

  private MidiEvent newProgramMessage(long tick, int channel, int programNumber) {
    ShortMessage message = new ShortMessage();
    try {
      message.setMessage(
              ShortMessage.PROGRAM_CHANGE,// command
              channel,
              programNumber,
              0);//not used
    } catch (InvalidMidiDataException ex) {
      throw new RuntimeException(ex);
    }

    return new MidiEvent(message, tick);
  }

  private MidiEvent newBankSelectMessage(long tick, int channel, int bank) {
    ShortMessage message = new ShortMessage();
    try {
      message.setMessage(ShortMessage.CONTROL_CHANGE,// command
              channel,
              MidiUtil.contBankSelect_MSB,
              bank);
    } catch (InvalidMidiDataException ex) {
      throw new RuntimeException(ex);
    }

    return new MidiEvent(message, tick);
  }

  private MidiEvent newVolumeMessage(long tick, int channel, int volume) {
    ShortMessage message = new ShortMessage();
    try {
      message.setMessage(
              ShortMessage.CONTROL_CHANGE,// command
              channel,
              MidiUtil.contMainVolume_MSB,
              volume);
    } catch (InvalidMidiDataException ex) {
      throw new RuntimeException(ex);
    }

    return new MidiEvent(message, tick);
  }

  private MidiEvent newExpressionMessage(long tick, int channel, int volume) {
    ShortMessage message = new ShortMessage();
    try {
      message.setMessage(
              ShortMessage.CONTROL_CHANGE,// command
              channel,
              MidiUtil.contExpression_MSB,
              volume);
    } catch (InvalidMidiDataException ex) {
      throw new RuntimeException(ex);
    }

    return new MidiEvent(message, tick);
  }

  private MidiEvent newPanMessage(long tick, int channel, int pos) {
    ShortMessage message = new ShortMessage();
    try {
      message.setMessage(ShortMessage.CONTROL_CHANGE,// command
              channel,
              MidiUtil.contPan_MSB,
              pos);
    } catch (InvalidMidiDataException ex) {
      throw new RuntimeException(ex);
    }

    return new MidiEvent(message, tick);
  }

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) throws InvalidMidiDataException, IOException, URISyntaxException, JAXBException {

    Create_03_Halleluja processor = new Create_03_Halleluja();

    processor.process();

  }
}
