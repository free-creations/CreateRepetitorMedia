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
package Rachmaninow;

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
public class Create_09_GelobtSeistDu {

  private final File inputFile;
  private File outputMidiFile;
  private final Handler loggingHandler;
  final static private String piece = "09_BlessedArtTou";
  final static private int resolution = 480;
  static final private File resourceDir = new File("../rachmaninow-vigil");

  private Create_09_GelobtSeistDu() throws IOException {
    loggingHandler = null;
    File projectDir = new File(resourceDir, piece);
    if (!projectDir.exists()) {
      throw new RuntimeException(projectDir.getPath() + " not found.");
    }
    if (!projectDir.isDirectory()) {
      throw new RuntimeException(projectDir + " is not a directory.");
    }
    inputFile = new File(projectDir, "SheetMidi.mid");
    if (!inputFile.exists()) {
      throw new RuntimeException(inputFile.getPath() + " not found.");
    }

    outputMidiFile = new File(projectDir, "NiceMidi.mid");

  }

  private void process() throws InvalidMidiDataException, IOException, JAXBException {

    Sequence inputSequence = MidiSystem.getSequence(inputFile);
    // clean the volume and expression
    inputSequence = ControllerRemover.process(inputSequence, MidiUtil.contMainVolume_MSB, loggingHandler);

    Sequence masterSequence = new Sequence(Sequence.PPQ, resolution);

    //  copy and clean  the input sequence to make sure we have the correct resolution
    for (int i = 0; i < inputSequence.getTracks().length; i++) {
      masterSequence = TrackMerger.process(masterSequence, inputSequence, new int[]{i}, -1, null, loggingHandler); //
    }
    ChannelCleaner sequenceImporter = new ChannelCleaner(masterSequence, loggingHandler);
    masterSequence = sequenceImporter.getResult();

    //-- correct velocity values
    int[] backgroundTracks = new int[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25};
    masterSequence = VelocityCorrectionA.process(masterSequence, backgroundTracks, loggingHandler);
    int[] pianoTracks = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
    masterSequence = VelocityCorrectionB.process(masterSequence, pianoTracks, loggingHandler);

    // --- Time shift
    // values in ms.
    // (the piece plays at 66 qpm there are 530 Ticks per second)
    long[] deltaTicks = {
      0, // 0, director
      // Forground
      0, //  1 , S 1
      0, //  2 , S 2
      0, //  3 , S 3
      0, //  4 , A 1 
      0, //  5 , A 2 
      0, //  6 , T 1 
      0, //  7 , T 2 
      0, //  8 , B 1
      0, //  9 , B 2 
      0, // 10 , B 3

      // Back ground
      20, // 11 , S 1 B
      20, // 12 , S 2 B
      20, // 13 , S 3 B
      20, // 14 , A 1 B
      20, // 15 , A 2 B 
      20, // 16 , T 1 B 
      20, // 17 , T 2 B
      20, // 18 , B 1 B
      20, // 19 , B 2 B     
      20, // 20 , B 3 B      
      20, // 21 , Solo B

      // Organ
      13, // 22 , Female 
      13, // 23 , Male
      13, // 23 , subbasss
      0, // 24 , Solo Organ
    };
    masterSequence = TimeShifter.process(masterSequence, deltaTicks, loggingHandler);

    // --- pan positions
    // (64 is middle)
    int[] panPos = {
      0, //  0 , director track
      // Forground
      64, //  1 , S 1
      64, //  2 , S 2
      64, //  3 , S 3
      64, //  4 , A 1 
      64, //  5 , A 2 
      64, //  6 , T 1 
      64, //  7 , T 2 
      64, //  8 , B 1
      64, //  9 , B 2 
      64, // 10 , B 3

      // Back ground
      36, // 11 , S 1 B
      44, // 12 , S 2 B
      48, // 13 , S 3 B
      84, // 14 , A 1 B
      88, // 15 , A 2 B 
      54, // 16 , T 1 B 
      56, // 17 , T 2 B
      80, // 18 , B 1 B
      84, // 19 , B 2 B     
      88, // 20 , B 3 B      
      64, // 21 , Solo B

      // Organ
      60, // 22 , Female 
      68, // 23 , Male
      64, // 24 , subbass     
      64, // 24 , Solo Organ
    };

    // --- volume
    // (64 is middle)
    int[] volume = {
      0, //  0 , director track
      // Forground
      100, //  1 , S 1
      100, //  2 , S 2
      100, //  3 , S 3
      100, //  4 , A 1 
      100, //  5 , A 2 
      100, //  6 , T 1 
      100, //  7 , T 2 
      100, //  8 , B 1
      100, //  9 , B 2 
      100, // 10 , B 3

      // Back ground
      100, // 11 , S 1 B
      100, // 12 , S 2 B
      100, // 13 , S 3 B
      100, // 14 , A 1 B
      100, // 15 , A 2 B 
      100, // 16 , T 1 B 
      100, // 17 , T 2 B
      100, // 18 , B 1 B
      100, // 19 , B 2 B     
      100, // 20 , B 3 B      
      100, // 21 , Solo B

      // Organ
      60, // 22 , Female 
      100, // 23 , Male
      120, // 24 , subbass     
      40, // 24 , Solo Organ
    };
    // --- create track initialization
    Track[] masterTracks = masterSequence.getTracks();
    for (int i = 1; i < masterTracks.length; i++) {
      int channel = sequenceImporter.getChannel(i);
      if (channel != ChannelCleaner._undefinedChannel) {
        masterTracks[i].add(newVolumeMessage(0, channel, volume[i]));
        masterTracks[i].add(newExpressionMessage(0, channel, 127));
        masterTracks[i].add(newPanMessage(0, channel, panPos[i]));
      }

    }

    // --- Randomize
    // values in ms.
    // (the piece plays at 66 qpm a delay of 230 ms corresponds to 1/16 note)
    int[] maxDelay = {
      // Forground
      0, //  1 , S 1
      0, //  2 , S 2
      0, //  3 , S 3
      0, //  4 , A 1 
      0, //  5 , A 2 
      0, //  6 , T 1 
      0, //  7 , T 2 
      0, //  8 , B 1
      0, //  9 , B 2 
      0, // 10 , B 3

      // Back ground
      20, // 11 , S 1 B
      100, // 12 , S 2 B
      100, // 13 , S 3 B
      20, // 14 , A 1 B
      100, // 15 , A 2 B 
      20, // 16 , T 1 B 
      200, // 17 , T 2 B
      20, // 18 , B 1 B
      30, // 19 , B 2 B     
      20, // 20 , B 3 B      
      20, // 21 , Solo B

      // Organ
      20, // 22 , Female 
      20, // 23 , Male
      20, // 23 , subbasss
      0, // 24 , Solo Organ
    };
    masterSequence = Randomizer.process(masterSequence, maxDelay, true, loggingHandler);

    // ---- bind (slur notes)
    long startTick = 0;
    long lastTick = masterSequence.getTickLength();
    int noteOverlap = 480 / 16; // in MidiTicks
    int minimumRest = 480 / 2; // eighth rest

    masterSequence = SlurBinderB.process(masterSequence, 11, startTick, lastTick, noteOverlap, minimumRest, loggingHandler);
    masterSequence = SlurBinderB.process(masterSequence, 12, startTick, lastTick, noteOverlap, minimumRest, loggingHandler);
    masterSequence = SlurBinderB.process(masterSequence, 13, startTick, lastTick, noteOverlap, minimumRest, loggingHandler);
    masterSequence = SlurBinderB.process(masterSequence, 14, startTick, lastTick, noteOverlap, minimumRest, loggingHandler);
    masterSequence = SlurBinderB.process(masterSequence, 15, startTick, lastTick, noteOverlap, minimumRest, loggingHandler);
    masterSequence = SlurBinderB.process(masterSequence, 16, startTick, lastTick, noteOverlap, minimumRest, loggingHandler);
    masterSequence = SlurBinderB.process(masterSequence, 17, startTick, lastTick, noteOverlap, minimumRest, loggingHandler);
    masterSequence = SlurBinderB.process(masterSequence, 18, startTick, lastTick, noteOverlap, minimumRest, loggingHandler);
    masterSequence = SlurBinderB.process(masterSequence, 19, startTick, lastTick, noteOverlap, minimumRest, loggingHandler);
    masterSequence = SlurBinderB.process(masterSequence, 20, startTick, lastTick, noteOverlap, minimumRest, loggingHandler);
    masterSequence = SlurBinderB.process(masterSequence, 21, startTick, lastTick, noteOverlap, minimumRest, loggingHandler);

    masterSequence = SlurBinderB.process(masterSequence, 22, startTick, lastTick, noteOverlap, minimumRest, loggingHandler);
    masterSequence = SlurBinderB.process(masterSequence, 23, startTick, lastTick, noteOverlap, minimumRest, loggingHandler);
    masterSequence = SlurBinderB.process(masterSequence, 24, startTick, lastTick, noteOverlap, minimumRest, loggingHandler);

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

    Create_09_GelobtSeistDu processor = new Create_09_GelobtSeistDu();

    processor.process();

  }
}
