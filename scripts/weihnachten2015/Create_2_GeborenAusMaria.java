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
/**
 * ... channel 1 -4 Voices: Kawai CS7 Digitalpiano (5 Mellow Grand)
 *
 * ------------------------------------------ channel x - y Choir Synth 4
 * Papelmedia_Ahh-Choir through calf Multi Chorus Delays: (max 60 Ticks -> 1/32)
 * ch 1 S I delay 15 ch 2 A I delay 20 ch 3 T I delay 17 ch 4 B I delay 13
 * ------------------------------------------- Synt 2 channels 9 - 14 Orchestra:
 * Jeux14 *
 *
 * 09) Female: 1/30 Fonds rec 8 12) Male: 1/35 Soft pos. 16-8-4 13) Male
 * subbass: 0/ 15 Bourdon 16 14) Solo: 0/90 Petit Jeu
 *
 * Reverb lv2rack IR Basilica di Foligno .config/rncbc.org/Qsynth.conf
 */
/**
 *
 */
package weihnachten2015;

import de.free_creations.importexport.ChannelCleaner;
import de.free_creations.importexport.ControllerRemover;
import de.free_creations.importexport.Randomizer;
import de.free_creations.importexport.SlurBinderB;
import de.free_creations.importexport.TimeShifter;
import de.free_creations.importexport.TrackMerger;
import de.free_creations.importexport.VelocityCorrectionB;
import de.free_creations.importexport.VelocityCorrectionC;
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
public class Create_2_GeborenAusMaria {

  private final File inputFile;
  private File outputMidiFile;
  private final Handler loggingHandler;
  final static private String piece = "2_GeborenAusMaria";
  final static private int resolution = 480;
  static final private File resourceDir = new File("../weihnachten2015");

  private Create_2_GeborenAusMaria() throws IOException {
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
    int[] backgroundTracks = new int[]{5, 6, 7, 8, 9, 10, 11,};
    masterSequence = VelocityCorrectionC.process(masterSequence, backgroundTracks, loggingHandler);
    int[] pianoTracks = new int[]{1, 2, 3, 4};
    masterSequence = VelocityCorrectionB.process(masterSequence, pianoTracks, loggingHandler);

    // --- Time shift
    // values in ms.
    // (the piece plays at 66 qpm there are 530 Ticks per second)
    long[] deltaTicks = {
      0, // 0, director
      // Forground
      0, //  1 , S
      0, //  2 , A
      0, //  3 , T
      0, //  4 , B

      // Back ground
      20, //  5 , S  B
      20, //  6 , A  B
      20, //  7 , T B
      20, //  8 , B B

      // Organ
      13, //  9 , Female 
      13, // 10 , Male
      13, // 11 , subbasss
    };
    masterSequence = TimeShifter.process(masterSequence, deltaTicks, loggingHandler);

    // --- pan positions
    // (64 is middle)   
    int[] panPos = {
      0, // 0, director
      // Forground
      64, //  1 , S
      64, //  2 , A
      64, //  3 , T
      64, //  4 , B

      // Back ground
      36, //  5 , S  B
      84, //  6 , A  B
      54, //  7 , T B
      88, //  8 , B B

      // Organ
      60, //  9 , Female 
      68, // 10 , Male
      64, // 11 , subbasss
    };

    // --- volume
    // (127 is full)
    int[] volume = {
      0, //  0 , director track
      // Forground
      100, //  1 , S 
      100, //  2 , A
      100, //  3 , T
      100, //  4 , B

      // Back ground
      100, //  5 , S B
      100, //  6 , A B
      100, //  7 , T B
      100, //  8 , B B

      // Organ
      60, //  9 , Female 
      100, // 10 , Male
      120, // 11 , subbass     
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

    // --- Randomize Time
    // values in ms.
    // (the piece plays at 66 qpm a delay of 230 ms corresponds to 1/16 note)
    int[] maxDelay = {
      // Forground
      0, //  1 , S 
      0, //  2 , A
      0, //  3 , T
      0, //  4 , B

      // Back ground
      60, //  5 , S 1 B
      60, //  6 , A 1 B
      60, //  7 , T 1 B 
      60, //  8 , B 1 B

      // Organ
      40, //  9 , Female 
      40, // 10 , Male
      40, // 11 , subbasss
    };
    masterSequence = Randomizer.process(masterSequence, maxDelay, true, loggingHandler);

    // ---- bind (slur notes)
    long startTick = 0;
    long lastTick = masterSequence.getTickLength();
    int noteOverlap = 480 / 8; // in MidiTicks
    int minimumRest = 480 / 2; // eighth rest

    masterSequence = SlurBinderB.process(masterSequence, 5, startTick, lastTick, noteOverlap, minimumRest, loggingHandler);
    masterSequence = SlurBinderB.process(masterSequence, 6, startTick, lastTick, noteOverlap, minimumRest, loggingHandler);
    masterSequence = SlurBinderB.process(masterSequence, 7, startTick, lastTick, noteOverlap, minimumRest, loggingHandler);
    masterSequence = SlurBinderB.process(masterSequence, 8, startTick, lastTick, noteOverlap, minimumRest, loggingHandler);

    masterSequence = SlurBinderB.process(masterSequence, 9, startTick, lastTick, noteOverlap, minimumRest, loggingHandler);
    masterSequence = SlurBinderB.process(masterSequence, 10, startTick, lastTick, noteOverlap, minimumRest, loggingHandler);
    masterSequence = SlurBinderB.process(masterSequence, 11, startTick, lastTick, noteOverlap, minimumRest, loggingHandler);

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

    Create_2_GeborenAusMaria processor = new Create_2_GeborenAusMaria();

    processor.process();

  }
}
