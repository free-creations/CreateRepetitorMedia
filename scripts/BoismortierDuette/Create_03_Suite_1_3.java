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
package BoismortierDuette;

import de.free_creations.importexport.ChannelCleaner;
import de.free_creations.importexport.MetronomeCreator;
import static de.free_creations.importexport.MetronomeCreator.*;

import de.free_creations.importexport.TrackMerger;
import de.free_creations.midisong.*;
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
public class Create_03_Suite_1_3 {

  private File orchestraFile;
  private static final File resourceDir = new File("scripts/BoismortierDuette/resources");
  private File outputMidiFile;
  private File outputSongFile;
  private Handler loggingHandler;
  final static private String piece = "Boismortier Duette";
  final static private String description = "Suite I, 3 Paysane";
  final static private String number = "03";
  final static private String camelTitle = "Paysane";
  final static private int resolution = 480;

  private Create_03_Suite_1_3() throws IOException {
    loggingHandler = null;

    if (!resourceDir.exists()) {
      if (!resourceDir.isDirectory()) {
        throw new RuntimeException("resourceDir not found.");
      }
    }

    orchestraFile = new File(resourceDir, "03_suite_1_3-o.mid");
    if (!orchestraFile.exists()) {
      throw new RuntimeException("03_suite_1_3-o.mid file not found.");
    }

    File tempDir = new File("../temp");

    if (!tempDir.exists()) {
      throw new RuntimeException(tempDir + " not found.");
    }
    if (!tempDir.isDirectory()) {
      throw new RuntimeException(tempDir + " is not a directory.");
    }
    File outDir = new File(tempDir, piece);
    if (!outDir.exists()) {
      if (!outDir.mkdir()) {
        throw new RuntimeException("Could not make : " + outDir.getPath());
      }
    }
    outputMidiFile = new File(outDir, number + "_" + camelTitle + ".mid");
    outputSongFile = new File(outDir, number + "_" + camelTitle + ".xml");

  }

  private void process() throws InvalidMidiDataException, IOException, JAXBException {

    Sequence orchestraSequence = MidiSystem.getSequence(orchestraFile);

    Sequence masterSequence = new Sequence(Sequence.PPQ, resolution);

    //  copy all the orchestra tracks
    for (int i = 0; i < orchestraSequence.getTracks().length; i++) {
      masterSequence = TrackMerger.process(masterSequence, orchestraSequence, new int[]{i}, -1, null, loggingHandler); //
    }

    // add track 7; the metronome track
    masterSequence = MetronomeCreator.process(masterSequence, perf2beats, loggingHandler);

    // This is a hack....to make the track 0 as long as the whole sequence
    double rawSeqLen = masterSequence.getTickLength();
    double quarterLen = masterSequence.getResolution();
    double barLen = 4 * quarterLen;
    long fullSeqLen = (long) (barLen * (Math.ceil((rawSeqLen + quarterLen) / barLen)));
    masterSequence.getTracks()[0].add(newEndOfTrackMessage(fullSeqLen));

    System.out.println("** Track 0 length " + masterSequence.getTracks()[0].ticks());
    System.out.println("** Sequence length " + masterSequence.getTickLength());

    ChannelCleaner sequenceImporter = new ChannelCleaner(masterSequence, loggingHandler);
    masterSequence = sequenceImporter.getResult();
    
        // add pitch bend 
    for (int i = 1; i < masterSequence.getTracks().length; i++) {
      int channel = sequenceImporter.getChannel(i);
      int shift = -656;
      masterSequence.getTracks()[i].add(newPitchbendMessage(0, channel, shift));

    }

    // Write the file to disk
    MidiSystem.write(masterSequence, 1, outputMidiFile);
    System.out.println("############ Midi file is: " + outputMidiFile.getCanonicalPath());

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
    OrchestraSynt.setSoundbankfile("../Boimortier.sf2");
    orchestraSuperTrack.setSynthesizer(OrchestraSynt);

    //create a super track that will collect the voices tracs
    MidiSynthesizerTrack voicesSuperTrack = new MidiSynthesizerTrack();
    voicesSuperTrack.setName("Flöte");
    mastertrack.addSubtrack(voicesSuperTrack);
    BuiltinSynthesizer voicesSynt = new BuiltinSynthesizer();
    voicesSynt.setSoundbankfile("../StringPiano.sf2");
    voicesSuperTrack.setSynthesizer(voicesSynt);

    //link all the orchestra tracks 
    int orchestraBase = 1; //<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
    int orchestraEnd = 2; //<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
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
    int voiceBase = 3; //<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

    MidiTrack newSongTrack;
    // -- Sopran
    newSongTrack = new MidiTrack();
    newSongTrack.setName("Flauto 1");
    newSongTrack.setMidiTrackIndex(voiceBase);
    newSongTrack.setMidiTrackIndex(voiceBase);
    newSongTrack.setMidiChannel(0);
    newSongTrack.setInstrumentDescription("Piano");
    newSongTrack.setMute(true);
    voicesSuperTrack.addSubtrack(newSongTrack);
    // -- Flauto 
    voiceBase++; //4
    newSongTrack = new MidiTrack();
    newSongTrack.setName("Flauto 2");
    newSongTrack.setMidiTrackIndex(voiceBase);
    newSongTrack.setMidiChannel(1);
    newSongTrack.setInstrumentDescription("Piano");
    newSongTrack.setMute(true);
    voicesSuperTrack.addSubtrack(newSongTrack);


    // -- Metronome
    voiceBase = 5; //
    newSongTrack = new MidiTrack();
    newSongTrack.setName("Metronome");
    newSongTrack.setMidiTrackIndex(voiceBase);
    newSongTrack.setMidiChannel(9);
    newSongTrack.setInstrumentDescription("Metronome");
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
    private MidiEvent newPitchbendMessage(long tick, int channel, int value) {
    ShortMessage message = new ShortMessage();
    int offseted = value + 0x2000;

    int lowByte = offseted & 0x7F;
    int hiByte = (offseted & 0x3F80) >> 7;
    try {
      message.setMessage(ShortMessage.PITCH_BEND,// command
              channel,
              lowByte,
              hiByte);//not used
    } catch (InvalidMidiDataException ex) {
      throw new RuntimeException(ex);
    }

    return new MidiEvent(message, tick);
  }

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) throws InvalidMidiDataException, IOException, URISyntaxException, JAXBException {

    Create_03_Suite_1_3 processor = new Create_03_Suite_1_3();
    System.out.println("############ Creating \"Boismortier " + number + " " + camelTitle + "\"");
    processor.process();

  }
}
