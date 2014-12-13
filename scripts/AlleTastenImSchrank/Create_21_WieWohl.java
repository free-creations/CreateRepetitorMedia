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
package AlleTastenImSchrank;

import de.free_creations.importexport.ChannelCleaner;
import de.free_creations.importexport.MetronomeCreator;
import static de.free_creations.importexport.MetronomeCreator.perf3beats;
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
public class Create_21_WieWohl {

  private final File inputFile;
  private File outputMidiFile;
  private File outputSongFile;
  private final Handler loggingHandler;
  final static private String piece = "AlleTastenImSchrank";
  final static private String description = "Oh Wie wohl ist mir am Abend";
  final static private String number = "21";
  final static private String camelTitle = "WieWohl";
  final static private int resolution = 480;
  static final private File resourceDir = new File("scripts/AlleTastenImSchrank/resources");

  private Create_21_WieWohl() throws IOException {
    loggingHandler = null;

    inputFile = new File(resourceDir, "21_OhWieWohl.mid");
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

    Sequence orchestraSequence = MidiSystem.getSequence(inputFile);

    Sequence masterSequence = new Sequence(Sequence.PPQ, resolution);

    int orchestraTrackCount = orchestraSequence.getTracks().length;
    //  copy all the tracks
    for (int i = 0; i < orchestraTrackCount; i++) {
      masterSequence = TrackMerger.process(masterSequence, orchestraSequence, new int[]{i}, -1, null, loggingHandler); //
    }
    // add track 7; the metronome track
    masterSequence = MetronomeCreator.process(masterSequence, perf3beats, loggingHandler);

    // This is a hack....to make the track 0 as long as the whole sequence
    double rawSeqLen = masterSequence.getTickLength();
    double quarterLen = masterSequence.getResolution();
    double barLen = 3 * quarterLen;
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
    OrchestraSynt.setSoundbankfile("../Chorium.sf2");
    orchestraSuperTrack.setSynthesizer(OrchestraSynt);

    //create a super track that will collect the voices tracs
    MidiSynthesizerTrack voicesSuperTrack = new MidiSynthesizerTrack();
    voicesSuperTrack.setName("Solo");
    mastertrack.addSubtrack(voicesSuperTrack);
    BuiltinSynthesizer voicesSynt = new BuiltinSynthesizer();
    voicesSynt.setSoundbankfile("../mk_1_rhodes.sf2");
    voicesSuperTrack.setSynthesizer(voicesSynt);

    // link the voices
    int voiceBase = 1; //<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
    MidiTrack newSongTrack;
    // -- Flute 1
    newSongTrack = new MidiTrack();
    newSongTrack.setName("Piano");
    newSongTrack.setMidiTrackIndex(voiceBase);
    newSongTrack.setMidiChannel(3);
    newSongTrack.setInstrumentDescription("Piano");
    newSongTrack.setMute(true);
    voicesSuperTrack.addSubtrack(newSongTrack);

    //link all the orchestra tracks 
    int orchestraBase = voiceBase + 1;
    for (int i = orchestraBase; i < orchestraTrackCount; i++) {
      MidiTrack songTrack = new MidiTrack();
      songTrack.setName(sequenceImporter.getTrackName(i));
      songTrack.setMidiTrackIndex(i);
      songTrack.setMidiChannel(sequenceImporter.getChannel(i));
      songTrack.setInstrumentDescription(sequenceImporter.getInstrumentDescription(i));
      orchestraSuperTrack.addSubtrack(songTrack);
    }

    //link  the metronome
    voiceBase = orchestraTrackCount;
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

  /**
   * @param args the command line arguments
   * @throws javax.sound.midi.InvalidMidiDataException
   * @throws java.io.IOException
   * @throws java.net.URISyntaxException
   * @throws javax.xml.bind.JAXBException
   */
  public static void main(String[] args) throws InvalidMidiDataException, IOException, URISyntaxException, JAXBException {

    Create_21_WieWohl processor = new Create_21_WieWohl();
    System.out.println("############ Creating \"" + number + " " + camelTitle + "\"");
    processor.process();

  }

}