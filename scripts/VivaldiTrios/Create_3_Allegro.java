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
package VivaldiTrios;

import de.free_creations.importexport.ChannelCleaner;
import de.free_creations.importexport.ControllerRemover;
import de.free_creations.importexport.MetronomeCreator;
import de.free_creations.importexport.TrackMerger;
import de.free_creations.midisong.*;
import de.free_creations.midiutil.MidiUtil;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Handler;
import javax.sound.midi.*;
import javax.xml.bind.JAXBException;

/**
 *
 * @author Harald Postner
 */
public class Create_3_Allegro {

  private File orchestraFile;
  private File outputMidiFile;
  private File outputSongFile;
  private Handler loggingHandler;
  final static private String piece = "VivaldiTrios";
  final static private String description = "Vivaldi Trios RV103";
  final static private String number = "3";
  final static private String camelTitle = "Allegro";
  final static private int resolution = 480;
  static final private File resourceDir = new File("scripts/VivaldiTrios/resources");

  private Create_3_Allegro() throws IOException {
    loggingHandler = null;

    orchestraFile = new File(resourceDir, "3_Allegro.mid");
    if (!orchestraFile.exists()) {
      throw new RuntimeException(orchestraFile.getPath() + " not found.");
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
    // orchestraSequence = ControllerRemover.process(orchestraSequence, MidiUtil.contMainVolume_MSB,  loggingHandler);



    Sequence masterSequence = new Sequence(Sequence.PPQ, resolution);
    Sequence voiceSequence = new Sequence(Sequence.PPQ, resolution);

    voiceSequence = TrackMerger.process(voiceSequence, orchestraSequence, new int[]{1}, -1, "Flauto 1", loggingHandler); //
    voiceSequence = TrackMerger.process(voiceSequence, orchestraSequence, new int[]{2}, -1, "Flauto 2", loggingHandler); //
    voiceSequence = TrackMerger.process(voiceSequence, orchestraSequence, new int[]{5}, -1, "Basso", loggingHandler); //
    voiceSequence = ControllerRemover.process(voiceSequence, ControllerRemover.contProgramChange, loggingHandler);
    voiceSequence = ControllerRemover.process(voiceSequence, MidiUtil.contEffectsLevel, loggingHandler);



     // next copy the orchestra tracks
    //... master track 0
    masterSequence = TrackMerger.process(masterSequence, orchestraSequence, new int[]{0}, -1, null, loggingHandler); //
    //... Sopran track 1
    masterSequence = TrackMerger.process(masterSequence, orchestraSequence, new int[]{1}, -1, "Flauto 1", loggingHandler); //
    //... Floeten track 2
    masterSequence = TrackMerger.process(masterSequence, orchestraSequence, new int[]{2}, -1, "Flauto 2", loggingHandler); //
    //... Bass track 3
    masterSequence = TrackMerger.process(masterSequence, orchestraSequence, new int[]{3}, -1, "Basso Cont. Dextra", loggingHandler); //
    //... Bass track 4
    masterSequence = TrackMerger.process(masterSequence, orchestraSequence, new int[]{4}, -1, "Basso Cont. Sinistra", loggingHandler); //
    //... Bass track 5
    masterSequence = TrackMerger.process(masterSequence, orchestraSequence, new int[]{5}, -1, "Basso", loggingHandler); //

    // insert an empty bar into the voices in order to allign with pre-count at the beginning
    //masterSequence = MidiUtil.insertSilence(masterSequence, 4 * resolution);

    // next copy the voice track
    //... Bass track 5
    masterSequence = TrackMerger.process(masterSequence, voiceSequence, new int[]{0}, 10, "Flauto 1", loggingHandler); //
    masterSequence = TrackMerger.process(masterSequence, voiceSequence, new int[]{1}, 11, "Flauto 2", loggingHandler); //
    masterSequence = TrackMerger.process(masterSequence, voiceSequence, new int[]{2}, 12, "Bass", loggingHandler); //

    // add track 20; the metronome track
    masterSequence = MetronomeCreator.process(masterSequence, MetronomeCreator.perf4beats, loggingHandler);



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
    OrchestraSynt.setSoundbankfile("../VivaldiTrios.sf2");
    orchestraSuperTrack.setSynthesizer(OrchestraSynt);

    //create a super track that will collect the voices tracs
    MidiSynthesizerTrack voicesSuperTrack = new MidiSynthesizerTrack();
    voicesSuperTrack.setName("Guitar");
    mastertrack.addSubtrack(voicesSuperTrack);
    BuiltinSynthesizer voicesSynt = new BuiltinSynthesizer();
    voicesSynt.setSoundbankfile("../StringPiano.sf2");
    voicesSuperTrack.setSynthesizer(voicesSynt);

    //link all the orchestra tracks 
    //link all the orchestra tracks 
    int orchestraBase = 1; //<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
    int orchestraEnd = 5; //<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
    for (int i = orchestraBase; i <= orchestraEnd; i++) {
      MidiTrack songTrack = new MidiTrack();
      songTrack.setName(sequenceImporter.getTrackName(i));
      songTrack.setMidiTrackIndex(i);
      songTrack.setMidiChannel(sequenceImporter.getChannel(i));
      songTrack.setInstrumentDescription(sequenceImporter.getInstrumentDescription(i));
      orchestraSuperTrack.addSubtrack(songTrack);
    }




    // link the voices
    int voiceBase = 6; //<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
    MidiTrack newSongTrack;
    newSongTrack = new MidiTrack();
    newSongTrack.setName("Flauto 1");
    newSongTrack.setMidiTrackIndex(voiceBase);
    newSongTrack.setMidiTrackIndex(voiceBase);
    newSongTrack.setMidiChannel(0);
    newSongTrack.setInstrumentDescription("Piano");
    newSongTrack.setMute(true);
    voicesSuperTrack.addSubtrack(newSongTrack);

    voiceBase = 7; //<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
    newSongTrack = new MidiTrack();
    newSongTrack.setName("Flauto 2");
    newSongTrack.setMidiTrackIndex(voiceBase);
    newSongTrack.setMidiTrackIndex(voiceBase);
    newSongTrack.setMidiChannel(0);
    newSongTrack.setInstrumentDescription("Piano");
    newSongTrack.setMute(true);
    voicesSuperTrack.addSubtrack(newSongTrack);
    
    voiceBase = 8; //<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
    newSongTrack = new MidiTrack();
    newSongTrack.setName("Basso");
    newSongTrack.setMidiTrackIndex(voiceBase);
    newSongTrack.setMidiTrackIndex(voiceBase);
    newSongTrack.setMidiChannel(0);
    newSongTrack.setInstrumentDescription("Piano");
    newSongTrack.setMute(true);
    voicesSuperTrack.addSubtrack(newSongTrack);
    
    voiceBase = 9; //<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
    newSongTrack = new MidiTrack();
    newSongTrack.setName("Metronome");
    newSongTrack.setMidiTrackIndex(voiceBase);
    newSongTrack.setMidiTrackIndex(voiceBase);
    newSongTrack.setMidiChannel(9);
    newSongTrack.setInstrumentDescription("Metronome");
    newSongTrack.setMute(true);
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

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) throws InvalidMidiDataException, IOException, URISyntaxException, JAXBException {

    Create_3_Allegro processor = new Create_3_Allegro();
    System.out.println("############ Creating \"Vivaldi " + number + " " + camelTitle + "\"");
    processor.process();

  }
}
