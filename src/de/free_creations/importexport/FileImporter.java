/*
 *  Copyright 2011 Harald Postner <Harald at H-Postner.de>.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package de.free_creations.importexport;

import de.free_creations.midisong.BuiltinSynthesizer;
import de.free_creations.midisong.MasterTrack;
import de.free_creations.midisong.MidiSynthesizerTrack;
import de.free_creations.midisong.MidiTrack;
import de.free_creations.midisong.Song;
import java.io.IOException;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.xml.bind.JAXBException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;

/**
 * This class can be used to create a normalised song file
 * given a MIDI file and a sound font file.
 * @author Harald Postner <Harald at H-Postner.de>
 */
public class FileImporter {

  private static final Logger logger =
          Logger.getLogger(FileImporter.class.getName());
  static Handler loggingHandler = null;
  static FileObject inputMidiFile = null;
  static FileObject outputMidiFile = null;
  static FileObject soundBankFile = null;
  static FileObject songFile = null;

  /**
   * Creates a Song file based on the given Midi file.
   * The auxiliary files like the midi file and the soundbank
   * will be copied into the same directory as the newly created
   * song file and their name chosen to be the same as the songfile.
   * @param inputMidiFile a file object representing an existing  MIDI file
   * @param soundBank a file object representing an existing  SF2 file or null
   * @param songFile a file object representing the song that shall be created
   */
  public static void prepareFilesObjects(FileObject midiInput, FileObject soundBank, FileObject song)
          throws IllegalArgumentException, IOException {
    if (midiInput == null) {
      throw new IllegalArgumentException("Argument \"midiInput\" is null.");
    }
    if (song == null) {
      throw new IllegalArgumentException("Argument \"songFile\" is null.");
    }
    inputMidiFile = midiInput;

    songFile = song;
    try {
      if (songFile.getParent().getURL().sameFile(inputMidiFile.getParent().getURL())) {
        if (songFile.getName().equalsIgnoreCase(inputMidiFile.getName())) {
          throw new IllegalArgumentException("Cannot process " + midiInput.getPath() + ", filename clashes.");
        }
      }
    } catch (FileStateInvalidException ex) {
      logger.log(Level.SEVERE, null, ex);
    }

    outputMidiFile = FileUtil.createData(songFile.getParent(),
            songFile.getName() + ".mid");


    soundBankFile = soundBank;
    if (soundBankFile != null) {
      if (soundBankFile.getParent() != songFile.getParent()) {
        FileObject copiedBank =
                soundBankFile.copy(song.getParent(), soundBankFile.getName(), soundBankFile.getExt());
        soundBankFile = copiedBank;
      }
    }

  }

  /**
   *
   * @param midiInput
   * @param song
   * @return
   * @throws IllegalArgumentException
   * @throws IOException
   */
  static void processFiles() throws InvalidMidiDataException, IOException {
    Sequence inputSequence = MidiSystem.getSequence(inputMidiFile.getInputStream());
    ChannelCleaner sequenceImporter = new ChannelCleaner(inputSequence, loggingHandler);
    Sequence outputSequence = sequenceImporter.getResult();
    if (sequenceImporter.getTrackCount() < 1) {
      throw new InvalidMidiDataException("Sequnce has no tracks.");
    }

    // copy the input midi file to a file named after the song file
    MidiSystem.write(outputSequence, 1, outputMidiFile.getOutputStream());

    Song songObject = new Song();

    songObject.setName(songFile.getName());
    MasterTrack mastertrack = songObject.createMastertrack();

    mastertrack.setSequencefile(outputMidiFile.getNameExt());

    //link track 0 from the input file with the mastertrack
    mastertrack.setName(sequenceImporter.getTrackName(0));
    mastertrack.setMidiTrackIndex(0);
    mastertrack.setMidiChannel(sequenceImporter.getChannel(0));

    //create a synthesizer-track that will collect all midi tracks from the sequence.
    MidiSynthesizerTrack synthTrack = new MidiSynthesizerTrack();
    mastertrack.addSubtrack(synthTrack);
    BuiltinSynthesizer synt = new BuiltinSynthesizer();
    if (soundBankFile != null) {
      synt.setSoundbankfile(soundBankFile.getNameExt());
    }
    synthTrack.setSynthesizer(synt);

    //link all the other tracks from the input file with subtracks of the synthesizer-track.
    for (int i = 1; i < sequenceImporter.getTrackCount(); i++) {
      MidiTrack songTrack = new MidiTrack();
      songTrack.setName(sequenceImporter.getTrackName(i));
      songTrack.setMidiTrackIndex(i);
      songTrack.setMidiChannel(sequenceImporter.getChannel(i));
      songTrack.setInstrumentDescription(sequenceImporter.getInstrumentDescription(i));
      synthTrack.addSubtrack(songTrack);
    }
    try {
      songObject.marshal(songFile.getOutputStream());
    } catch (JAXBException ex) {
      throw new IOException(ex);
    }

    System.out.println("Result written to: " + songFile.getURL());

  }
}
