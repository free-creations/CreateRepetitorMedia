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
package BrahmsRequiem;

import de.free_creations.importexport.ChannelCleaner;
import de.free_creations.importexport.MetaMessageFilter;
import de.free_creations.importexport.InstrumentExchanger;
import de.free_creations.importexport.MetronomeCreator;
import de.free_creations.importexport.Randomizer;
import de.free_creations.importexport.SlurBinderA;
import de.free_creations.importexport.TrackMerger;
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
public class Create_1_SeligSind {

  private File voicesFile;
  private File orchestraFile;
  private File outputMidiFile;
  private File outputSongFile;
  private Handler loggingHandler;
  final static private String piece = "BrahmsRequiem";
  final static private String description = "Selig sind, die da Leid tragen";
  final static private String number = "1";
  final static private String camelTitle = "SeligSind";
  final static private int resolution = 480;
  private final File outputMidiFileTemp;

  private Create_1_SeligSind() throws IOException {
    loggingHandler = null;

    orchestraFile = new File("scripts/BrahmsRequiem/resources/orchestra1.mid");
    if (!orchestraFile.exists()) {
      throw new RuntimeException("orchestra1.mid file not found.");
    }
    voicesFile = new File("scripts/BrahmsRequiem/resources/choir1.mid");
    if (voicesFile == null) {
      throw new RuntimeException("Voices file not found.");
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
    outputMidiFileTemp = new File(outDir, number + "_" + camelTitle + "Temp" + ".mid");
    outputSongFile = new File(outDir, number + "_" + camelTitle + ".xml");

  }

  private void process() throws InvalidMidiDataException, IOException, JAXBException {

    Sequence masterSequence = new Sequence(Sequence.PPQ, resolution);
    Sequence orchestraSequence = MidiSystem.getSequence(orchestraFile);

    ChannelCleaner sequenceImporter1 = new ChannelCleaner(orchestraSequence, loggingHandler);
    orchestraSequence = sequenceImporter1.getResult();

    // import the orchestra tracks
    masterSequence = TrackMerger.process(masterSequence, orchestraSequence, new int[]{0}, -1, description, loggingHandler); //Master

    // Track 1 - Flutes
    masterSequence = TrackMerger.process(masterSequence, orchestraSequence, new int[]{5}, 0, "Flute", loggingHandler); // 1
    masterSequence = InstrumentExchanger.process(masterSequence, 1, -1, 73, loggingHandler);
    // Track 2 - Oboe
    masterSequence = TrackMerger.process(masterSequence, orchestraSequence, new int[]{6}, 1, "Oboe", loggingHandler); // 3
    masterSequence = InstrumentExchanger.process(masterSequence, 2, -1, 68, loggingHandler);
    // Track 3 - Clarinet (empty)
    masterSequence.createTrack(); //3
    // Track 4 - Bassoon
    masterSequence = TrackMerger.process(masterSequence, orchestraSequence, new int[]{7}, 3, "Bassoon", loggingHandler); // 5
    masterSequence = InstrumentExchanger.process(masterSequence, 4, -1, 70, loggingHandler);
    // Track 5 - Horns
    masterSequence = TrackMerger.process(masterSequence, orchestraSequence, new int[]{8}, 4, "Horns", loggingHandler); // 6
    masterSequence = InstrumentExchanger.process(masterSequence, 5, -1, 60, loggingHandler);
    // Track 6 Trumpet (empty)
    masterSequence.createTrack(); //6
    // Track 7 - Trombone
    masterSequence = TrackMerger.process(masterSequence, orchestraSequence, new int[]{9}, 6, "Trombone", loggingHandler); // 6
    masterSequence = InstrumentExchanger.process(masterSequence, 6, -1, 57, loggingHandler);
    // Track 8 Timpani (empty)
    masterSequence.createTrack(); //8
    // Track 9 - Harp
    masterSequence = TrackMerger.process(masterSequence, orchestraSequence, new int[]{10}, 8, "Harp", loggingHandler); // 6
    masterSequence = InstrumentExchanger.process(masterSequence, 8, -1, 46, loggingHandler);
    // Track 10 - Choir
    masterSequence = TrackMerger.process(masterSequence, orchestraSequence, new int[]{11, 12, 13, 14}, 10, "Choir", loggingHandler); // 11
    masterSequence = InstrumentExchanger.process(masterSequence, 10, -1, 19, loggingHandler);
    // Track 11 - Violins I (empty)
    masterSequence.createTrack(); //11
    // Track 12 - Cello 1,2
    masterSequence = TrackMerger.process(masterSequence, orchestraSequence, new int[]{16}, 12, "Cello 1 2", loggingHandler); // 13
    masterSequence = InstrumentExchanger.process(masterSequence, 12, -1, 42, loggingHandler);
    // Track 13 - Violas
    masterSequence = TrackMerger.process(masterSequence, orchestraSequence, new int[]{15}, 13, "Violas", loggingHandler); // 14
    masterSequence = InstrumentExchanger.process(masterSequence, 13, -1, 41, loggingHandler);
    // Track 14 - Celli
    masterSequence = TrackMerger.process(masterSequence, orchestraSequence, new int[]{17}, 14, "Celli", loggingHandler); // 15
    masterSequence = InstrumentExchanger.process(masterSequence, 14, 48, 42, loggingHandler);
    // Track 15 - Contrabass
    masterSequence = TrackMerger.process(masterSequence, orchestraSequence, new int[]{18}, 15, "Contrabass", loggingHandler); // 16
    masterSequence = InstrumentExchanger.process(masterSequence, 15, 48, 43, loggingHandler);
//

    // process the slurs
    long startTick = 0;
    long lastTick = masterSequence.getTickLength();
    int noteOverlap = 480 / 16; // in MidiTicks
    int minimumRest = 480 / 2; // eighth rest

    masterSequence = SlurBinderA.process(masterSequence, 1, startTick, lastTick, noteOverlap, minimumRest, loggingHandler);
    masterSequence = SlurBinderA.process(masterSequence, 2, startTick, lastTick, noteOverlap, minimumRest, loggingHandler);
    masterSequence = SlurBinderA.process(masterSequence, 3, startTick, lastTick, noteOverlap, minimumRest, loggingHandler);
    masterSequence = SlurBinderA.process(masterSequence, 4, startTick, lastTick, noteOverlap, minimumRest, loggingHandler);
    masterSequence = SlurBinderA.process(masterSequence, 5, startTick, lastTick, noteOverlap, minimumRest, loggingHandler);
    masterSequence = SlurBinderA.process(masterSequence, 7, startTick, lastTick, noteOverlap, minimumRest, loggingHandler);

    masterSequence = SlurBinderA.process(masterSequence, 11, startTick, lastTick, noteOverlap, minimumRest, loggingHandler);
    masterSequence = SlurBinderA.process(masterSequence, 12, startTick, lastTick, noteOverlap, minimumRest, loggingHandler);
    masterSequence = SlurBinderA.process(masterSequence, 13, startTick, lastTick, noteOverlap, minimumRest, loggingHandler);
    masterSequence = SlurBinderA.process(masterSequence, 14, startTick, lastTick, noteOverlap, minimumRest, loggingHandler);
    masterSequence = SlurBinderA.process(masterSequence, 15, startTick, lastTick, noteOverlap, minimumRest, loggingHandler);

    masterSequence = Randomizer.process(masterSequence,
            new int[]{
              0, //Director
              0, //Track 1 -  Flutes
              10, //Track 2 -  Oboe
              10, //Track 3 -  Clarinet
              10, //Track 4 -  Bassoon
              10, //Track 5 -  Horns
              0, //Track 6 -  Trumpet
              0, //Track 7 -  Trombones
              20, //Track 8 -  Timpani
              35, //Track 9 -  Harp
              0, //Track 10 - Choir
              15, //Track 11 - Violins 1
              30, //Track 12 - Violins 2
              30, //Track 13 - Violas
              15, //Track 14 - Celli
              10, //Track 15 - Contrabass
            },
            true,
            loggingHandler);

    // import the choir voices
    Sequence textSequence = MidiSystem.getSequence(voicesFile);
    textSequence = MetaMessageFilter.process(textSequence, MidiUtil.lyricMeta, loggingHandler);
    //textSequence = MidiUtil.cut(textSequence, 0, 2 * textSequence.getResolution());

    Sequence voiceSequence = new Sequence(Sequence.PPQ, resolution);
    voiceSequence = TrackMerger.process(voiceSequence, masterSequence, new int[]{0}, -1, "VoiceMaster", loggingHandler);

    voiceSequence = TrackMerger.process(voiceSequence, orchestraSequence, new int[]{11}, 0, "SopranVoice", loggingHandler);
    voiceSequence = TrackMerger.process(voiceSequence, textSequence, new int[]{1}, 0, "SopranText", loggingHandler);

    voiceSequence = TrackMerger.process(voiceSequence, orchestraSequence, new int[]{12}, 0, "AltVoice", loggingHandler);
    voiceSequence = TrackMerger.process(voiceSequence, textSequence, new int[]{2}, 0, "AltText", loggingHandler);

    voiceSequence = TrackMerger.process(voiceSequence, orchestraSequence, new int[]{13}, 0, "TenorVoice", loggingHandler);
    voiceSequence = TrackMerger.process(voiceSequence, textSequence, new int[]{3}, 0, "TenorText", loggingHandler);

    voiceSequence = TrackMerger.process(voiceSequence, orchestraSequence, new int[]{14}, 0, "BassVoice", loggingHandler);
    voiceSequence = TrackMerger.process(voiceSequence, textSequence, new int[]{4}, 0, "BassText", loggingHandler);

    // merge voices into master
    masterSequence = TrackMerger.process(masterSequence, voiceSequence, new int[]{1, 2}, 0, "Sopran", loggingHandler); // 16
    masterSequence = InstrumentExchanger.process(masterSequence, 16, -1, 0, loggingHandler);

    masterSequence = TrackMerger.process(masterSequence, voiceSequence, new int[]{3, 4}, 1, "Alt", loggingHandler); // 17
    masterSequence = InstrumentExchanger.process(masterSequence, 17, -1, 0, loggingHandler);

    masterSequence = TrackMerger.process(masterSequence, voiceSequence, new int[]{5, 6}, 2, "Tenor", loggingHandler); // 18
    masterSequence = InstrumentExchanger.process(masterSequence, 18, -1, 0, loggingHandler);

    masterSequence = TrackMerger.process(masterSequence, voiceSequence, new int[]{7, 8}, 3, "Bass", loggingHandler); // 19
    masterSequence = InstrumentExchanger.process(masterSequence, 19, -1, 0, loggingHandler);

    ChannelCleaner sequenceImporter = new ChannelCleaner(masterSequence, loggingHandler);
    masterSequence = sequenceImporter.getResult();

    // make the track 0 as long as the whole sequence and round up to a whole bar
    double rawSeqLen = masterSequence.getTickLength();
    double quarterLen = masterSequence.getResolution();
    double barLen = 4 * quarterLen;
    long fullSeqLen = (long) (barLen * (Math.ceil((rawSeqLen + quarterLen) / barLen)));
    masterSequence.getTracks()[0].add(newEndOfTrackMessage(fullSeqLen));

    // add track 20; the metronome track
    masterSequence = MetronomeCreator.process(masterSequence, MetronomeCreator.perf2beats, loggingHandler);

    // write the sequence to file
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
    OrchestraSynt.setSoundbankfile("../Requiem.sf2");
    orchestraSuperTrack.setSynthesizer(OrchestraSynt);

    //create a super track that will collect the voices tracs
    MidiSynthesizerTrack voicesSuperTrack = new MidiSynthesizerTrack();
    voicesSuperTrack.setName("Chor");
    mastertrack.addSubtrack(voicesSuperTrack);
    BuiltinSynthesizer voicesSynt = new BuiltinSynthesizer();
    voicesSynt.setSoundbankfile("../StringPiano.sf2");
    voicesSuperTrack.setSynthesizer(voicesSynt);

    //link all the orchestra tracks 
    int orchestraBase = 1; //<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
    int orchestraEnd = 15; //<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
    for (int i = orchestraBase; i <= orchestraEnd; i++) {
      MidiTrack songTrack = new MidiTrack();
      songTrack.setName(sequenceImporter.getTrackName(i));
      songTrack.setMidiTrackIndex(i);
      songTrack.setMidiChannel(sequenceImporter.getChannel(i));
      songTrack.setInstrumentDescription(sequenceImporter.getInstrumentDescription(i));
      orchestraSuperTrack.addSubtrack(songTrack);
    }

    // link the voices
    int voiceBase = orchestraEnd + 1; //16
    MidiTrack newSongTrack;
    // -- Sopran
    newSongTrack = new MidiTrack();
    newSongTrack.setName("Sopran");
    newSongTrack.setMidiTrackIndex(voiceBase);
    newSongTrack.setMidiTrackIndex(voiceBase);
    newSongTrack.setMidiChannel(0);
    newSongTrack.setInstrumentDescription("Piano");
    newSongTrack.setMute(false);
    voicesSuperTrack.addSubtrack(newSongTrack);
    // -- Alt
    voiceBase++; //17
    newSongTrack = new MidiTrack();
    newSongTrack.setName("Alt");
    newSongTrack.setMidiTrackIndex(voiceBase);
    newSongTrack.setMidiChannel(1);
    newSongTrack.setInstrumentDescription("Piano");
    newSongTrack.setMute(false);
    voicesSuperTrack.addSubtrack(newSongTrack);
    // -- Tenor
    voiceBase++; //18
    newSongTrack = new MidiTrack();
    newSongTrack.setName("Tenor");
    newSongTrack.setMidiTrackIndex(voiceBase);
    newSongTrack.setMidiChannel(1);
    newSongTrack.setInstrumentDescription("Piano");
    newSongTrack.setMute(false);
    voicesSuperTrack.addSubtrack(newSongTrack);
    // -- Bass
    voiceBase++; //19
    newSongTrack = new MidiTrack();
    newSongTrack.setName("Bass");
    newSongTrack.setMidiTrackIndex(voiceBase);
    newSongTrack.setMidiChannel(1);
    newSongTrack.setInstrumentDescription("Piano");
    newSongTrack.setMute(false);
    voicesSuperTrack.addSubtrack(newSongTrack);

    // -- Metronome
    voiceBase++; //20
    newSongTrack = new MidiTrack();
    newSongTrack.setName("Metronome");
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

    Create_1_SeligSind processor = new Create_1_SeligSind();
    System.out.println("############ Creating \"Brahms " + number + " " + camelTitle + "\"");
    processor.process();

  }
}
