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
package RachmaninowRepetitor;


import de.free_creations.importexport.ChannelCleaner;
import de.free_creations.importexport.ControllerRemover;
import de.free_creations.importexport.Randomizer;
import de.free_creations.importexport.SlurBinderB;
import de.free_creations.importexport.TimeShifter;
import de.free_creations.importexport.TrackMerger;
import de.free_creations.importexport.VelocityCorrectionA;
import de.free_creations.importexport.VelocityCorrectionB;
import de.free_creations.midisong.BuiltinSynthesizer;
import de.free_creations.midisong.MasterTrack;
import de.free_creations.midisong.MidiSynthesizerTrack;
import de.free_creations.midisong.MidiTrack;
import de.free_creations.midisong.Song;
import de.free_creations.midiutil.MidiUtil;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.logging.Handler;
import javax.sound.midi.*;
import javax.xml.bind.JAXBException;

/**
 *
 * @author Harald Postner
 */
public class Create_11_MeineSeeleLobpreiseDenHerrn {

  private final File inputFile;
  private File outputMidiFile;
  private File outputSongFile;
  private final Handler loggingHandler;
  final static private String piece = "11_Magnificat";
  final static private String description = "11_Magnificat";
  final static private int resolution = 480;
  static final private File resourceDir = new File("../rachmaninow-vigil");

  private Create_11_MeineSeeleLobpreiseDenHerrn() throws IOException {
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

    File tempDir = new File("../temp");

    if (!tempDir.exists()) {
      throw new RuntimeException(tempDir + " not found.");
    }
    if (!tempDir.isDirectory()) {
      throw new RuntimeException(tempDir + " is not a directory.");
    }
    File outDir = new File(tempDir, "Rachmaninow");
    if (!outDir.exists()) {
      if (!outDir.mkdir()) {
        throw new RuntimeException("Could not make : " + outDir.getPath());
      }
    }
    outputMidiFile = new File(outDir, piece + ".mid");
    outputSongFile = new File(outDir, piece + ".xml");

  }

  private void process() throws InvalidMidiDataException, IOException, JAXBException {

    Sequence inputSequence = MidiSystem.getSequence(inputFile);
    // clean the volume and expression
    inputSequence = ControllerRemover.process(inputSequence, MidiUtil.contMainVolume_MSB, loggingHandler);

    Sequence masterSequence = new Sequence(Sequence.PPQ, resolution);

    //  copy and clean  the input sequence to make sure we have the correct resolution
    // the director track
    masterSequence = TrackMerger.process(masterSequence, inputSequence, new int[]{0}, -1, null, loggingHandler); //

    // the background tracks
    for (int i = 22; i < inputSequence.getTracks().length; i++) {
      masterSequence = TrackMerger.process(masterSequence, inputSequence, new int[]{i}, -1, null, loggingHandler); //
    }

    // the solo tracks
    masterSequence = TrackMerger.process(masterSequence, inputSequence, new int[]{19}, -1, null, loggingHandler); //

    ChannelCleaner sequenceImporter = new ChannelCleaner(masterSequence, loggingHandler);
    masterSequence = sequenceImporter.getResult();

    // add ProgramMessages
    Track[] masterTracks = masterSequence.getTracks();
    // track 1 => Female: 1/33 Soft pos. 8-4
    int track = 1;
    int channel = sequenceImporter.getChannel(track);
    if (channel != ChannelCleaner._undefinedChannel) {
      masterTracks[track].add(newBankSelectMessage(0, channel, 1));
      masterTracks[track].add(newProgramMessage(1, channel, 33));
      masterTracks[track].add(newReverbLevelMessage(2, channel, 80));
    }
    // track 2 => Male: 1/35 Soft pos. 16-8-4
    track = 2;
    channel = sequenceImporter.getChannel(track);
    if (channel != ChannelCleaner._undefinedChannel) {
      masterTracks[track].add(newBankSelectMessage(0, channel, 1));
      masterTracks[track].add(newProgramMessage(1, channel, 35));
      masterTracks[track].add(newReverbLevelMessage(2, channel, 80));

    }
    // track 3 => Male subbass: 0/ 15 Bourdon 16
    track = 3;
    channel = sequenceImporter.getChannel(track);
    if (channel != ChannelCleaner._undefinedChannel) {
      masterTracks[track].add(newBankSelectMessage(0, channel, 0));
      masterTracks[track].add(newProgramMessage(1, channel, 15));
      masterTracks[track].add(newReverbLevelMessage(2, channel, 80));

    }
    // track 4 => Solo: 0/90 Petit Jeu
    track = 4;
    channel = sequenceImporter.getChannel(track);
    if (channel != ChannelCleaner._undefinedChannel) {
      masterTracks[track].add(newBankSelectMessage(0, channel, 0));
      masterTracks[track].add(newProgramMessage(1, channel, 90));
      masterTracks[track].add(newReverbLevelMessage(2, channel, 80));

    }

    // ---- bind (slur notes)
    long startTick = 0;
    long lastTick = masterSequence.getTickLength();
    int noteOverlap = 480 / 8; // in MidiTicks -> 1/32
    int minimumRest = 480 / 2; // eighth rest

    masterSequence = SlurBinderB.process(masterSequence, 1, startTick, lastTick, noteOverlap, minimumRest, loggingHandler);
    masterSequence = SlurBinderB.process(masterSequence, 2, startTick, lastTick, noteOverlap, minimumRest, loggingHandler);
    masterSequence = SlurBinderB.process(masterSequence, 3, startTick, lastTick, noteOverlap, minimumRest, loggingHandler);
    masterSequence = SlurBinderB.process(masterSequence, 4, startTick, lastTick, noteOverlap, minimumRest, loggingHandler);

    // --- Randomize
    // values in ms.
    // (the piece plays at 66 qpm a delay of 230 ms corresponds to 1/16 note)
    int[] maxDelay = {
      0,
      // Organ
      20, // 22 , Female 
      20, // 23 , Male
      20, // 23 , subbasss
      0, // 24 , Solo Organ
    };
    masterSequence = Randomizer.process(masterSequence, maxDelay, true, loggingHandler);

    
        // This is a hack....to make the track 0 as long as the whole sequence
    double rawSeqLen = masterSequence.getTickLength();
    double quarterLen = masterSequence.getResolution();
    double barLen = 4 * quarterLen;
    long fullSeqLen = (long) (barLen * (Math.ceil((rawSeqLen + quarterLen) / barLen)));
    masterSequence.getTracks()[0].add(newEndOfTrackMessage(fullSeqLen));
    
    // Write the file to disk
    MidiSystem.write(masterSequence, 1, outputMidiFile);
    System.out.println("############ Midi file is: " + outputMidiFile.getCanonicalPath());

    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    // create the appropriate song object
    Song songObject = new Song();
    songObject.setName(description);

    MasterTrack mastertrack = songObject.createMastertrack();
    mastertrack.setSequencefile(outputMidiFile.getName());
    mastertrack.setName(sequenceImporter.getTrackName(0));
    mastertrack.setMidiTrackIndex(0);
    mastertrack.setMidiChannel(sequenceImporter.getChannel(0));

    //create a super track that will collect all orchestra tracs
    MidiSynthesizerTrack orchestraSuperTrack = new MidiSynthesizerTrack();
    orchestraSuperTrack.setName("Orchester");
    mastertrack.addSubtrack(orchestraSuperTrack);
    BuiltinSynthesizer OrchestraSynt = new BuiltinSynthesizer();
    OrchestraSynt.setSoundbankfile("../Jeux14.SF2");
    orchestraSuperTrack.setSynthesizer(OrchestraSynt);

    //create a super track that will collect the voices tracs
    MidiSynthesizerTrack voicesSuperTrack = new MidiSynthesizerTrack();
    voicesSuperTrack.setName("Voices");
    mastertrack.addSubtrack(voicesSuperTrack);
    BuiltinSynthesizer voicesSynt = new BuiltinSynthesizer();
    voicesSynt.setSoundbankfile("../StringPiano.sf2");
    voicesSuperTrack.setSynthesizer(voicesSynt);

    //link all the orchestra tracks 
    int orchestraBase = 1; //<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
    int orchestraEnd = 4; //<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
    for (int i = orchestraBase; i <= orchestraEnd; i++) {
      MidiTrack songTrack = new MidiTrack();
      songTrack.setName(sequenceImporter.getTrackName(i));
      songTrack.setMidiTrackIndex(i);
      songTrack.setMidiChannel(sequenceImporter.getChannel(i));
      songTrack.setInstrumentDescription(sequenceImporter.getInstrumentDescription(i));
      orchestraSuperTrack.addSubtrack(songTrack);
    }

    // link the voices
    //link all the orchestra tracks 
    int voiceBase = 5; //<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

    MidiTrack newSongTrack;
    // -- Bass
    newSongTrack = new MidiTrack();
    newSongTrack.setName("Basso 2");
    newSongTrack.setMidiTrackIndex(voiceBase);
    newSongTrack.setMidiTrackIndex(voiceBase);
    newSongTrack.setMidiChannel(0);
    newSongTrack.setInstrumentDescription("Piano");
    newSongTrack.setMute(false);
    voicesSuperTrack.addSubtrack(newSongTrack);

    songObject.marshal(new FileOutputStream(outputSongFile));
    System.out.println("############ Song file is: " + outputSongFile.getCanonicalPath());

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

  private MidiEvent newReverbLevelMessage(long tick, int channel, int level) {
    ShortMessage message = new ShortMessage();
    try {
      message.setMessage(ShortMessage.CONTROL_CHANGE,// command
              channel,
              MidiUtil.contEffectsLevel,
              level);
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

    Create_11_MeineSeeleLobpreiseDenHerrn processor = new Create_11_MeineSeeleLobpreiseDenHerrn();

    processor.process();

  }
}
