package de.free_creations.importexport;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import javax.sound.midi.InvalidMidiDataException;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;

public class FileImporterTest extends NbTestCase {

  private FileObject midiInputFileObject;
  private FileObject songFileObject;
  private FileObject soundbankFileObject;

  public FileImporterTest(java.lang.String testName) {
    super(testName);
  }

  @Override
  protected void setUp() throws IOException {
    clearWorkDir();

    File songFile = new File(getWorkDir(), "testSong.xml");
    songFileObject = FileUtil.createData(songFile);
    assertNotNull(songFileObject);

    URL inputMidiFileURL = this.getClass().getResource("resources/SanctusOrchestra.mid");
    midiInputFileObject = URLMapper.findFileObject(inputMidiFileURL);
    assertNotNull(midiInputFileObject);

    URL soundbankFileURL = this.getClass().getResource("resources/Claudio_Piano.SF2");
    soundbankFileObject = URLMapper.findFileObject(soundbankFileURL);
    assertNotNull(soundbankFileObject);


  }

  public void testPrepareFilesObjects() throws IOException {
    System.out.println("testPrepareFilesObjects");

    FileImporter.prepareFilesObjects(midiInputFileObject, soundbankFileObject, songFileObject);

    File midiOutputFile = new File(getWorkDir(), "testSong.mid");
    File soundbankOutputFile = new File(getWorkDir(), "Claudio_Piano.SF2");

    assertTrue(midiOutputFile.exists());
    assertTrue(soundbankOutputFile.exists());
  }

  public void testPrepareFilesObjects_Filenameclash() throws MalformedURLException, IOException {
    try {
      System.out.println("testPrepareFilesObjects_Filenameclash");


      File midiFile = new File(getWorkDir(), "testSong.mid");
      FileObject midiFileObject = FileUtil.createData(midiFile);
      assertNotNull(midiFileObject);

      FileImporter.prepareFilesObjects(midiFileObject, null, songFileObject);
      fail("Expected  IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      System.out.println("... expected error message was:" + e.getMessage());
    }
  }

  public void testProcessFiles() throws IOException, InvalidMidiDataException {
    System.out.println("testProcessFiles");

    File midiOutputFile = new File(getWorkDir(), "testSong.mid");
    FileObject midiOutputFileObject = FileUtil.createData(midiOutputFile);
    assertNotNull(midiOutputFileObject);

    FileImporter.inputMidiFile = midiInputFileObject;
    FileImporter.outputMidiFile = midiOutputFileObject;
    FileImporter.soundBankFile = null;
    FileImporter.songFile = songFileObject;

    assertEquals(0L, midiOutputFileObject.getSize()); //before processing

    FileImporter.processFiles();
    assertTrue(midiOutputFileObject.getSize() > 0L); //after processing
    assertTrue(songFileObject.getSize() > 0L); //after processing
  }
  
  public void testDoitAll() throws IOException, InvalidMidiDataException {
    FileImporter.prepareFilesObjects(midiInputFileObject, soundbankFileObject, songFileObject);
    FileImporter.processFiles();
    System.out.println("...Result written to "+getWorkDir().getPath());
    
  }
}
