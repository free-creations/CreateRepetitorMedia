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
public class Create_4_Autunno {

  private URL orchestraFileURL;
  private URL voicesFileURL;
  private URL introFileURL;
  private File outputMidiFile;
  private File outputSongFile;
  private Handler loggingHandler;
  final static private String description = "Le Quattro Stagioni, L'Autunno";
  final static private String number = "4";
  final static private String camelTitle = "Autunno";

  private Create_4_Autunno() throws IOException {
    loggingHandler = null;
    String commonResourcesUrl = "resources/";
    String introUrl = commonResourcesUrl + "Intro4_4.mid";
    String orchUrl = "resources/_" + number + camelTitle + "Raw.mid";
    String voicesUrl = "resources/_" + number + camelTitle + "Voices.mid";
    introFileURL = this.getClass().getResource(introUrl);
    if (introFileURL == null) {
      throw new RuntimeException("Intro file not found. >" + introUrl + "<");
    }
    orchestraFileURL = this.getClass().getResource(orchUrl);
    if (orchestraFileURL == null) {
      throw new RuntimeException("Raw file not found. >" + orchUrl + "<");
    }
    voicesFileURL = this.getClass().getResource(voicesUrl);
    if (voicesFileURL == null) {
      throw new RuntimeException("Voices file not found.>" + voicesUrl + "<");
    }

    File tempDir = new File("../temp");
    if (!tempDir.exists()) {
      throw new RuntimeException(tempDir + " not found.");
    }
    if (!tempDir.isDirectory()) {
      throw new RuntimeException(tempDir + " is not a directory.");
    }
    File outDir = new File(tempDir, number + "_" + camelTitle);
    if (!outDir.exists()) {
      if (!outDir.mkdir()) {
        throw new RuntimeException("Could not make : " + outDir.getPath());
      }
    }
    outputMidiFile = new File(outDir, number + "_" + camelTitle + ".mid");
    outputSongFile = new File(outDir, number + "_" + camelTitle + ".xml");


  }

  private void process() throws InvalidMidiDataException, IOException, JAXBException {


    Sequence orchestraSequence = MidiSystem.getSequence(orchestraFileURL);
    Sequence voicesSequence = MidiSystem.getSequence(voicesFileURL);
    Sequence introSequence = MidiSystem.getSequence(introFileURL);

    // the master sequence is our new output sequence
    Sequence masterSequence = new Sequence(orchestraSequence.getDivisionType(),
            orchestraSequence.getResolution());

    // first merge the director track from the voices

    masterSequence = TrackMerger.process(masterSequence, voicesSequence, new int[]{0}, -1, description, loggingHandler); //0 Master

    // define some note values
    long quarter = orchestraSequence.getResolution();

    long eight = quarter / 2;
    long bar = 4 * quarter;

    //remove pauses
    long oldPos = findNote(orchestraSequence, 5, 0, 81);
    long newPos = 0;
    orchestraSequence = MidiUtil.leftCut(orchestraSequence, oldPos - newPos);

    // remove pauses
    long toPos = 3 * bar;
    long fromPos = findNote(orchestraSequence, 5, toPos, 81);
    orchestraSequence = MidiUtil.stretch(orchestraSequence, toPos - eight, fromPos, toPos);

    toPos = 6 * bar;
    fromPos = findNote(orchestraSequence, 5, toPos, 81);
    orchestraSequence = MidiUtil.stretch(orchestraSequence, toPos - eight, fromPos, toPos);

    toPos = 10 * bar + 2 * quarter;
    fromPos = findNote(orchestraSequence, 5, toPos, 69);
    orchestraSequence = MidiUtil.stretch(orchestraSequence, toPos - eight, fromPos, toPos);

    toPos = 13 * bar;
    fromPos = findNote(orchestraSequence, 4, toPos, 81);
    orchestraSequence = MidiUtil.stretch(orchestraSequence, toPos - eight, fromPos, toPos);

    toPos = 16 * bar;
    fromPos = findNote(orchestraSequence, 4, toPos, 81);
    orchestraSequence = MidiUtil.stretch(orchestraSequence, toPos - eight, fromPos, toPos);

    toPos = 20 * bar;
    fromPos = findNote(orchestraSequence, 4, toPos, 89);
    orchestraSequence = MidiUtil.stretch(orchestraSequence, toPos - eight, fromPos, toPos);

    toPos = 23 * bar;
    fromPos = findNote(orchestraSequence, 4, toPos, 77);
    orchestraSequence = MidiUtil.stretch(orchestraSequence, toPos - eight, fromPos, toPos);

    toPos = 29 * bar;
    fromPos = findNote(orchestraSequence, 5, toPos, 69);
    orchestraSequence = MidiUtil.stretch(orchestraSequence, toPos - eight, fromPos, toPos);
    // remove everything afer the 32,5 bar
    long cutPos = 31 * bar + 2 * quarter;
    orchestraSequence = MidiUtil.rightCut(orchestraSequence, cutPos);

    // stretch the whole sequence by a factor of two
    orchestraSequence = MidiUtil.stretch(orchestraSequence, 2);

    // next, copy all the orchestra tracks
    for (int i = 1; i < orchestraSequence.getTracks().length; i++) {
      masterSequence = TrackMerger.process(masterSequence, orchestraSequence, new int[]{i}, -1, "", loggingHandler); //
    }

    ChannelCleaner sequenceImporter = new ChannelCleaner(masterSequence, loggingHandler);
    masterSequence = sequenceImporter.getResult();

    masterSequence = ControllerRemover.process(masterSequence, MidiUtil.contAllControllersOff, loggingHandler);
    masterSequence = ControllerRemover.process(masterSequence, MidiUtil.contChorusLevel, loggingHandler);
    masterSequence = ControllerRemover.process(masterSequence, MidiUtil.contModulationWheel_MSB, loggingHandler);
    masterSequence = EmptyTrackRemover.process(masterSequence, loggingHandler);

    masterSequence = Randomizer.process(masterSequence, new int[]{0, 80, 120, 30, 30, 30, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60,}, loggingHandler);


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

//    System.out.println("** Track 0 length " + masterSequence.getTracks()[0].ticks());
//    System.out.println("** Track 18 length " + masterSequence.getTracks()[18].ticks());
//    System.out.println("** Sequence length " + masterSequence.getTickLength());

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
    OrchestraSynt.setSoundbankfile("../Chorium.SF2");
    orchestraSuperTrack.setSynthesizer(OrchestraSynt);

    //create a super track that will collect the voices tracs
    MidiSynthesizerTrack voicesSuperTrack = new MidiSynthesizerTrack();
    voicesSuperTrack.setName("Flöten");
    mastertrack.addSubtrack(voicesSuperTrack);
    BuiltinSynthesizer voicesSynt = new BuiltinSynthesizer();
    voicesSynt.setSoundbankfile("../mk_1_rhodes.sf2");
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
    voicesSuperTrack.addSubtrack(newSongTrack);
    // -- alt
    voiceBase++;
    newSongTrack = new MidiTrack();
    newSongTrack.setName("Alt");
    newSongTrack.setMidiTrackIndex(voiceBase);
    newSongTrack.setMidiChannel(1);
    newSongTrack.setInstrumentDescription("Piano");
    voicesSuperTrack.addSubtrack(newSongTrack);
    // -- tenor
    voiceBase++;
    newSongTrack = new MidiTrack();
    newSongTrack.setName("Tenor");
    newSongTrack.setMidiTrackIndex(voiceBase);
    newSongTrack.setMidiChannel(2);
    newSongTrack.setInstrumentDescription("Piano");
    voicesSuperTrack.addSubtrack(newSongTrack);
    // -- bass 
    voiceBase++;
    newSongTrack = new MidiTrack();
    newSongTrack.setName("Bass");
    newSongTrack.setMidiTrackIndex(voiceBase);
    newSongTrack.setMidiChannel(3);
    newSongTrack.setInstrumentDescription("Piano");
    voicesSuperTrack.addSubtrack(newSongTrack);

    // -- link the precount track (to the orchestra)
    voiceBase++;
    newSongTrack = new MidiTrack();
    newSongTrack.setName("Pre-Count");
    newSongTrack.setMidiTrackIndex(voiceBase);
    newSongTrack.setMidiChannel(9);
    newSongTrack.setInstrumentDescription("Percussion");
    orchestraSuperTrack.addSubtrack(newSongTrack);

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
    Create_4_Autunno processor = new Create_4_Autunno();
    System.out.println("############ Creating - Four Seasons - " + number + " " + camelTitle);
    processor.process();

  }
}
