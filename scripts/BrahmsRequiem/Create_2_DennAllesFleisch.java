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
import de.free_creations.importexport.InstrumentExchanger;
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
public class Create_2_DennAllesFleisch {

    private File voicesFile;
    private File orchestraFile;
    private File outputMidiFile;
    private File outputSongFile;
    private Handler loggingHandler;
    final static private String piece = "BrahmsRequiem";
    final static private String description = "Denn alles Fleisch";
    final static private String number = "2";
    final static private String camelTitle = "DennAllesFleisch";
    final static private int resolution = 480;

    private Create_2_DennAllesFleisch() throws IOException {
        loggingHandler = null;

        orchestraFile = new File("scripts/BrahmsRequiem/resources/orchestra2.mid");
        if (!orchestraFile.exists()) {
            throw new RuntimeException("orchestra2.mid file not found.");
        }
        voicesFile = new File("scripts/BrahmsRequiem/resources/choir2.mid");
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
        outputSongFile = new File(outDir, number + "_" + camelTitle + ".xml");


    }

    private void process() throws InvalidMidiDataException, IOException, JAXBException {

        Sequence masterSequence = new Sequence(Sequence.PPQ, resolution);
        Sequence orchestraSequence = MidiSystem.getSequence(orchestraFile);

        // import the orchestra tracks
        masterSequence = TrackMerger.process(masterSequence, orchestraSequence, new int[]{0}, -1, description, loggingHandler); //Master

        //masterSequence = TrackMerger.process(masterSequence, orchestraSequence, new int[]{5}, 0, "Picollo", loggingHandler); // 1
        masterSequence = TrackMerger.process(masterSequence, orchestraSequence, new int[]{5,6}, 0, "Flutes", loggingHandler); // 1
        masterSequence = InstrumentExchanger.process(masterSequence, 1, -1, 73, loggingHandler);

        //masterSequence = TrackMerger.process(masterSequence, orchestraSequence, new int[]{6}, 1, "Flute", loggingHandler); // 2
        //masterSequence = InstrumentExchanger.process(masterSequence, 2, -1, 73, loggingHandler);

        masterSequence = TrackMerger.process(masterSequence, orchestraSequence, new int[]{7}, 2, "Oboe", loggingHandler); // 3
        masterSequence = InstrumentExchanger.process(masterSequence, 3, -1, 68, loggingHandler);

        masterSequence = TrackMerger.process(masterSequence, orchestraSequence, new int[]{8}, 3, "Clarinet", loggingHandler); // 4
        masterSequence = InstrumentExchanger.process(masterSequence, 4, -1, 71, loggingHandler);

        masterSequence = TrackMerger.process(masterSequence, orchestraSequence, new int[]{9}, 4, "Bassoon", loggingHandler); // 5
        masterSequence = InstrumentExchanger.process(masterSequence, 5, -1, 70, loggingHandler);

        masterSequence = TrackMerger.process(masterSequence, orchestraSequence, new int[]{10, 11}, 5, "Horns", loggingHandler); // 6
        masterSequence = InstrumentExchanger.process(masterSequence, 6, -1, 60, loggingHandler);

        masterSequence = TrackMerger.process(masterSequence, orchestraSequence, new int[]{12}, 6, "Trumpet", loggingHandler); // 7
        masterSequence = InstrumentExchanger.process(masterSequence, 7, -1, 56, loggingHandler);

        masterSequence = TrackMerger.process(masterSequence, orchestraSequence, new int[]{13, 14, 15}, 7, "Trombones, Tuba", loggingHandler); // 8
        masterSequence = InstrumentExchanger.process(masterSequence, 8, -1, 57, loggingHandler);// 

        masterSequence = TrackMerger.process(masterSequence, orchestraSequence, new int[]{16}, 8, "Timpani", loggingHandler); // 9
        masterSequence = InstrumentExchanger.process(masterSequence, 9, -1, 47, loggingHandler);

       // masterSequence = TrackMerger.process(masterSequence, orchestraSequence, new int[]{17}, 9, "Harp", loggingHandler); // 10
        masterSequence = TrackMerger.process(masterSequence, orchestraSequence, new int[]{17}, 1, "Harp", loggingHandler); // 10
        masterSequence = InstrumentExchanger.process(masterSequence, 10, -1, 46, loggingHandler);

        masterSequence = TrackMerger.process(masterSequence, orchestraSequence, new int[]{18, 19, 20, 21}, 10, "Choir", loggingHandler); // 11
        masterSequence = InstrumentExchanger.process(masterSequence, 11, -1, 19, loggingHandler);

        masterSequence = TrackMerger.process(masterSequence, orchestraSequence, new int[]{22}, 11, "Violins I", loggingHandler);// 12
        //masterSequence = InstrumentExchanger.process(masterSequence, 12, 48, 48, loggingHandler);

        masterSequence = TrackMerger.process(masterSequence, orchestraSequence, new int[]{23}, 12, "Violins II", loggingHandler); // 13
        masterSequence = InstrumentExchanger.process(masterSequence, 13, 48, 50, loggingHandler);

        masterSequence = TrackMerger.process(masterSequence, orchestraSequence, new int[]{24, 25, 26}, 13, "Violas", loggingHandler); // 14
        masterSequence = InstrumentExchanger.process(masterSequence, 14, 48, 41, loggingHandler);

        masterSequence = TrackMerger.process(masterSequence, orchestraSequence, new int[]{28}, 14, "Celli", loggingHandler); // 15
        masterSequence = InstrumentExchanger.process(masterSequence, 15, 48, 42, loggingHandler);

        masterSequence = TrackMerger.process(masterSequence, orchestraSequence, new int[]{29, 30, 31}, 15, "Contrabass", loggingHandler); // 16
        masterSequence = InstrumentExchanger.process(masterSequence, 16, 48, 43, loggingHandler);


        // import the choir voices
        Sequence voicesSequence = MidiSystem.getSequence(voicesFile);

        masterSequence = TrackMerger.process(masterSequence, voicesSequence, new int[]{1}, 0, "Alt", loggingHandler); // 17
        masterSequence = TrackMerger.process(masterSequence, voicesSequence, new int[]{2}, 1, "Sopran", loggingHandler); // 18
        masterSequence = TrackMerger.process(masterSequence, voicesSequence, new int[]{3}, 2, "Tenor", loggingHandler); // 19
        masterSequence = TrackMerger.process(masterSequence, voicesSequence, new int[]{4}, 3, "Bass", loggingHandler); // 20

        ChannelCleaner sequenceImporter = new ChannelCleaner(masterSequence, loggingHandler);
        masterSequence = sequenceImporter.getResult();

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
        voicesSynt.setSoundbankfile("../SingingPiano.sf2");
        voicesSuperTrack.setSynthesizer(voicesSynt);

        //link all the orchestra tracks 
        int orchestraBase = 1; //<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
        //int orchestraEnd = 16; //<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
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
        int voiceBase = orchestraEnd+1; //<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
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
        voiceBase++; //18
        newSongTrack = new MidiTrack();
        newSongTrack.setName("Alt");
        newSongTrack.setMidiTrackIndex(voiceBase);
        newSongTrack.setMidiChannel(1);
        newSongTrack.setInstrumentDescription("Piano");
        newSongTrack.setMute(false);
        voicesSuperTrack.addSubtrack(newSongTrack);
        // -- Tenor
        voiceBase++; //19
        newSongTrack = new MidiTrack();
        newSongTrack.setName("Tenor");
        newSongTrack.setMidiTrackIndex(voiceBase);
        newSongTrack.setMidiChannel(1);
        newSongTrack.setInstrumentDescription("Piano");
        newSongTrack.setMute(false);
        voicesSuperTrack.addSubtrack(newSongTrack);
        // -- Bass
        voiceBase++; //20
        newSongTrack = new MidiTrack();
        newSongTrack.setName("Bass");
        newSongTrack.setMidiTrackIndex(voiceBase);
        newSongTrack.setMidiChannel(1);
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

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws InvalidMidiDataException, IOException, URISyntaxException, JAXBException {

        Create_2_DennAllesFleisch processor = new Create_2_DennAllesFleisch();
        System.out.println("############ Creating \"Brahms " + number + " " + camelTitle + "\"");
        processor.process();

    }
}