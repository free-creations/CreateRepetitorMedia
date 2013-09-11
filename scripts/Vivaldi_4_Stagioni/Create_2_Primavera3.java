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
package Vivaldi_4_Stagioni;

import de.free_creations.importexport.*;
import de.free_creations.midisong.*;
import de.free_creations.midiutil.MidiUtil;
import de.free_creations.midiutil.Note;
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
public class Create_2_Primavera3 {

  private File orchestraFile;
  private File voicesFile;
  private File introFile;
  private File outputMidiFile;
  private File outputSongFile;
  private Handler loggingHandler;
  final static private String piece = "Vivaldi_4_Stagioni";
  final static private String description = "Le Quattro Stagioni, La Primavera 3";
  final static private String number = "2";
  final static private String camelTitle = "Primavera3";

  private Create_2_Primavera3() throws IOException {
    loggingHandler = null;
    String commonResourcesUrl = "scripts/Vivaldi_4_Stagioni/resources/";
    String introUrl = commonResourcesUrl + "Intro6_8.mid";
    String orchUrl = commonResourcesUrl + "_" + number + camelTitle + "Raw.mid";
    String voicesUrl = commonResourcesUrl + "_" + number + camelTitle + "Voices.mid";
    introFile = new File(introUrl);
    if (!introFile.exists()) {
      throw new RuntimeException("Intro file not found. >" + introUrl + "<");
    }
    orchestraFile = new File(orchUrl);
    if (!orchestraFile.exists()) {
      throw new RuntimeException("Raw file not found. >" + orchUrl + "<");
    }
    voicesFile = new File(voicesUrl);
    if (!voicesFile.exists()) {
      throw new RuntimeException("Voices file not found.>" + voicesUrl + "<");
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
    Sequence voicesSequence = MidiSystem.getSequence(voicesFile);
    Sequence introSequence = MidiSystem.getSequence(introFile);

    // the master sequence is or new output sequence
    Sequence masterSequence = new Sequence(orchestraSequence.getDivisionType(),
            orchestraSequence.getResolution());

    // first merge the director track from the voices

    masterSequence = TrackMerger.process(masterSequence, voicesSequence, new int[]{0}, -1, description, loggingHandler); //0 Master

    // define some note values
    long quarter = orchestraSequence.getResolution();

    long eight = quarter / 2;
    long bar = 6 * eight;

    //remove pauses
    long cutPos = findNote(orchestraSequence, 5, 0, 80);
    orchestraSequence = MidiUtil.leftCut(orchestraSequence, cutPos);

    // remove pauses
    long toPos = 6 * bar;
    long fromPos = findNote(orchestraSequence, 5, toPos, 80);
    orchestraSequence = MidiUtil.stretch(orchestraSequence, toPos, fromPos, toPos);

    toPos = 15 * bar + 3 * eight;
    fromPos = findNote(orchestraSequence, 5, toPos, 75);
    orchestraSequence = MidiUtil.stretch(orchestraSequence, toPos, fromPos, toPos);

    toPos = 19 * bar + 3 * eight;
    fromPos = findNote(orchestraSequence, 5, toPos, 85);
    orchestraSequence = MidiUtil.stretch(orchestraSequence, toPos, fromPos, toPos);


    toPos = 28 * bar;
    fromPos = findNote(orchestraSequence, 4, toPos, 83);
    orchestraSequence = MidiUtil.stretch(orchestraSequence, toPos, fromPos, toPos);

    // remove everything afer the 25th bar
    cutPos = 24 * bar;
    orchestraSequence = MidiUtil.rightCut(orchestraSequence, cutPos);

    // transpose from E to F
    orchestraSequence = MidiUtil.transpose(orchestraSequence, 1);


    // next, copy all the orchestra tracks
    for (int i = 1; i < orchestraSequence.getTracks().length; i++) {
      masterSequence = TrackMerger.process(masterSequence, orchestraSequence, new int[]{i}, -1, null, loggingHandler); //
    }

    ChannelCleaner sequenceImporter = new ChannelCleaner(masterSequence, loggingHandler);
    masterSequence = sequenceImporter.getResult();

    masterSequence = ControllerRemover.process(masterSequence, MidiUtil.contAllControllersOff, loggingHandler);
    masterSequence = ControllerRemover.process(masterSequence, MidiUtil.contChorusLevel, loggingHandler);
    masterSequence = ControllerRemover.process(masterSequence, MidiUtil.contModulationWheel_MSB, loggingHandler);
    masterSequence = EmptyTrackRemover.process(masterSequence, loggingHandler);

    masterSequence = Randomizer.process(masterSequence, new int[]{100, 100, 100, 100, 100, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30}, loggingHandler);


    masterSequence = VolumeExpressionCleaner.process(masterSequence, loggingHandler);


    // merge the voices into the master sequence
    masterSequence = TrackMerger.process(masterSequence, voicesSequence, new int[]{1}, 0, "Sopran", loggingHandler); //16 sopr
    masterSequence = TrackMerger.process(masterSequence, voicesSequence, new int[]{2}, 1, "Alt", loggingHandler); //17 Alt
    masterSequence = TrackMerger.process(masterSequence, voicesSequence, new int[]{3}, 2, "Tenor", loggingHandler); //18 Tenor
    masterSequence = TrackMerger.process(masterSequence, voicesSequence, new int[]{4}, 3, "Bass", loggingHandler); //19 Bass

    // insert pre-count at the beginning
    masterSequence = MidiUtil.insertSilence(masterSequence, 2 * bar);
    masterSequence = TrackMerger.process(masterSequence, introSequence, new int[]{1}, 9, "PreCount", loggingHandler); //


    // This is a hack....to make the track 0 as long as the whole sequence
    masterSequence.getTracks()[0].add(newEndOfTrackMessage(orchestraSequence.getTickLength()));

    // add track 21; the metronome track
    masterSequence = MetronomeCreator.process(masterSequence, MetronomeCreator.perf2beats, loggingHandler);

    System.out.println("** Track 0 length " + masterSequence.getTracks()[0].ticks());
    System.out.println("** Track 18 length " + masterSequence.getTracks()[18].ticks());
    System.out.println("** Sequence length " + masterSequence.getTickLength());

    // Write the file to disk
    MidiSystem.write(masterSequence, 1, outputMidiFile);
    System.out.println("############ Midi file is: " + outputMidiFile.getCanonicalPath());


    //----------------------------------------------------------------------------------------
    // create the approriate song object

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
    OrchestraSynt.setSoundbankfile("../Vivaldi4Stagioni.sf2");
    orchestraSuperTrack.setSynthesizer(OrchestraSynt);

    //create a super track that will collect the voices tracs
    MidiSynthesizerTrack voicesSuperTrack = new MidiSynthesizerTrack();
    voicesSuperTrack.setName("Flöten");
    mastertrack.addSubtrack(voicesSuperTrack);
    BuiltinSynthesizer voicesSynt = new BuiltinSynthesizer();
    voicesSynt.setSoundbankfile("../StringPiano.sf2");
    voicesSuperTrack.setSynthesizer(voicesSynt);

    //link all the orchestra tracks 
    int voiceBase = 16; //<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
    for (int i = 1; i < voiceBase; i++) {
      MidiTrack songTrack = new MidiTrack();
      songTrack.setName(sequenceImporter.getTrackName(i));
      songTrack.setMidiTrackIndex(i);
      songTrack.setMidiChannel(sequenceImporter.getChannel(i));
      songTrack.setInstrumentDescription(sequenceImporter.getInstrumentDescription(i));
      orchestraSuperTrack.addSubtrack(songTrack);
    }
    // link the voices

    MidiTrack newSongTrack;
    // -- sopran
    newSongTrack = new MidiTrack();
    newSongTrack.setName("Sopran");
    newSongTrack.setMidiTrackIndex(voiceBase);
    newSongTrack.setMidiTrackIndex(voiceBase);
    newSongTrack.setMidiChannel(0);
    newSongTrack.setInstrumentDescription("Piano");
    newSongTrack.setMute(true);
    voicesSuperTrack.addSubtrack(newSongTrack);
    // -- alt
    voiceBase++;
    newSongTrack = new MidiTrack();
    newSongTrack.setName("Alt");
    newSongTrack.setMidiTrackIndex(voiceBase);
    newSongTrack.setMidiChannel(1);
    newSongTrack.setInstrumentDescription("Piano");
    newSongTrack.setMute(true);
    voicesSuperTrack.addSubtrack(newSongTrack);
    // -- tenor
    voiceBase++;
    newSongTrack = new MidiTrack();
    newSongTrack.setName("Tenor");
    newSongTrack.setMidiTrackIndex(voiceBase);
    newSongTrack.setMidiChannel(2);
    newSongTrack.setInstrumentDescription("Piano");
    newSongTrack.setMute(true);
    voicesSuperTrack.addSubtrack(newSongTrack);
    // -- bass 
    voiceBase++;
    newSongTrack = new MidiTrack();
    newSongTrack.setName("Bass");
    newSongTrack.setMidiTrackIndex(voiceBase);
    newSongTrack.setMidiChannel(3);
    newSongTrack.setInstrumentDescription("Piano");
    newSongTrack.setMute(true);
    voicesSuperTrack.addSubtrack(newSongTrack);

    // -- link the precount track (to the orchestra)
    voiceBase++;
    newSongTrack = new MidiTrack();
    newSongTrack.setName("Pre-Count");
    newSongTrack.setMidiTrackIndex(voiceBase);
    newSongTrack.setMidiChannel(9);
    newSongTrack.setInstrumentDescription("Percussion");
    newSongTrack.setMute(true);
    orchestraSuperTrack.addSubtrack(newSongTrack);
    // -- link the metronome track (to the voices)
    voiceBase++;
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
   * this finds the first pick-up note of the violin part
   *
   * @param orchestraSeq
   * @return position in midi ticks
   */
  private long findFirstViolinoNote(Sequence orchestraSeq) {
    Track violinoTrack = orchestraSeq.getTracks()[5];
    MidiEvent firstNote = null;
    for (int i = 0; i < violinoTrack.size(); i++) {
      MidiEvent ev = violinoTrack.get(i);
      if (Note.isNoteOnEvent(ev)) {
        firstNote = ev;
        break;
      }
    }
    if (firstNote != null) {
      return firstNote.getTick();
    } else {
      return 0;
    }
  }

  /**
   * finds a note with a given pitch and that follows the given position
   *
   * @param orchestraSeq
   * @return position in midi ticks
   */
  private long findNote(Sequence orchestraSeq, int trackNumber, long pos, int pitch) {
    int quarter = orchestraSeq.getResolution();
    long searchOffset = pos + quarter;
    Track track = orchestraSeq.getTracks()[trackNumber];
    MidiEvent searchedNote = null;
    for (int i = 0; i < track.size(); i++) {
      MidiEvent ev = track.get(i);
      if (ev.getTick() >= pos) {
        if (Note.isNoteOnEvent(ev)) {
          ShortMessage message = (ShortMessage) ev.getMessage();
          int foundPitch = message.getData1();
          if (pitch == foundPitch) {
            searchedNote = ev;
            break;
          }
        }
      }
    }
    if (searchedNote == null) {
      throw new RuntimeException("Could not find any matching event in the input sequence.");
    }

    if (searchedNote.getTick() > searchOffset) {
      throw new RuntimeException("Matching event in the input sequence is too far away."
              + " (about " + ((float) searchOffset / (float) quarter) + " quarters away)");
    }
    return searchedNote.getTick();
  }

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) throws InvalidMidiDataException, IOException, URISyntaxException, JAXBException {
    Create_2_Primavera3 processor = new Create_2_Primavera3();
    System.out.println("############ Creating - Four Seasons - " + number + " " + camelTitle);
    processor.process();

  }
}
