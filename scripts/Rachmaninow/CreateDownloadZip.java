/*
 * Copyright 2012 harald.
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
package Rachmaninow;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * http://www.exampledepot.com/egs/java.util.zip/CreateZip.html
 *
 * @author harald
 */
public class CreateDownloadZip {

  private static final File tempDir = new File("../temp");
  private static final File rootDir = new File("/home/harald/NetBeansProjects/rachmaninow-vigil");

  private static final String contentName = "Inhalt.txt";
  private static final File contentFile = new File(tempDir, contentName);

  private static final String contentHeader
          = "Sergei Rachmaninow, Ganznächliche Vigil op. 37%n"
          + "%n"
          + "Stimme: %s%n"
          + "Letzte Änderung: %s%n"
          + "%n%n";

  private static final String contentFooter
          = "%n"
          + "%n"
          + "Seitenangaben beziehen sich auf die Chorausgabe Carus-Verlag CV 23.014/05%n"
          + "%n"
          + "Dieses Material steht unter der Creative-Commons-Lizenz Namensnennung-Nicht kommerziell 4.0 International.%n"
          + "Um eine Kopie dieser Lizenz zu sehen, besuchen Sie http://creativecommons.org/licenses/by-nc/4.0/.";

  private static class Piece {

    public final String titleE;
    public final String titleD;
    public final String page;

    public Piece(String titleE, String titleD, String page) {
      this.titleE = titleE;
      this.titleD = titleD;
      this.page = page;
    }
  }

  private static final Piece[] AllPieces = {
    new Piece("01_OComeLetUsWorship", "01_Kommt_lasst_uns_anbeten", " 1"),
    new Piece("02_BlessTheLord", "02_Lobe_den_Herrn", " 3"),
    new Piece("03_BlessedIsTheMan/1_psalm1", "03_1_Selig_ist_der_Mann_Psalm_1", " 8"),
    new Piece("03_BlessedIsTheMan/2_psalm2", "03_2_Selig_ist_der_Mann_Psalm_2", " 9"),
    new Piece("03_BlessedIsTheMan/3_psalm3", "03_3_Selig_ist_der_Mann_Psalm_3", "10"),
    new Piece("03_BlessedIsTheMan/4_gloriaPatri", "03_4_Selig_ist_der_Mann_Gloria", "11"),
    new Piece("04_OGladsomeLight", "04_Licht_der_Ruhe", "13"),
    new Piece("05_NuncDimittis", "05_Herr_lass_deinen_Knecht", "16"),
    new Piece("06_AveMaria", "06_Ave_Maria", "19"),
    new Piece("07_ShortGloria", "07_Sechster_Psalm", "21"),
    new Piece("08_IntroSixPsalms", "08_Lobet_den_Namen_des_Herrn", "23"),
    new Piece("09_BlessedArtTou", "09_Gelobt_seist_Du_o_Herr", "26"),
    new Piece("10_HymnOfTheResurection", "10_Auferstehung_Christi", "34"),
    new Piece("11_Magnificat", "11_Meine_Seele_lobpreise_den_Herrn", "37"),};

  private static class Voice {

    final public String regularVoice;
    final public String replacementVoice;
    final public String lyrics;

    public Voice(String regularVoice, String replacementVoice, String lyrics) {
      this.regularVoice = regularVoice;
      this.replacementVoice = replacementVoice;
      this.lyrics = lyrics;
    }
  }

  private static final Voice[] allVoices = {
    new Voice("01_sopranoOne", null, "sopranoLyrics"),
    new Voice("02_sopranoTwo", null, "sopranoLyrics"),
    new Voice("03_altoOne", null, "altoLyrics"),
    new Voice("04_altoTwo", null, "altoLyrics"),
    new Voice("05_tenoreOne", null, "tenoreLyrics"),
    new Voice("06_tenoreTwo", null, "tenoreLyrics"),
    new Voice("07_bassoOne", null, "bassoLyrics"),
    new Voice("08_bassoTwo", null, "bassoLyrics"),
    new Voice("09_bassoThree", "08_bassoTwo", "bassoLyrics"),
    new Voice("10_sopranoThree", "02_sopranoTwo", "sopranoLyrics")
  };

  private static class ZipItem {

    public final File source;
    public final String zipPath;

    public ZipItem(File source, String zipPath) {
      this.source = source;
      this.zipPath = zipPath;

    }
  }

  void createZipFiles() throws IOException {
    for (Voice voice : allVoices) {
      PrintStream content = prepareContentList(voice.regularVoice);
      File zipFile = new File(rootDir, "/downloads/rachmaninow" + voice.regularVoice + ".zip");
      List<ZipItem> zipList = createZipList(voice, content);
      closeContent(content);
      packFiles(voice.regularVoice, zipList, zipFile);
    }
  }

  List<ZipItem> createZipList(Voice voice, PrintStream content) {
    ArrayList<ZipItem> zipList = new ArrayList<>();
    zipList.add(new ZipItem(contentFile, contentName));
    int count = 0;
    for (Piece piece : AllPieces) {
      count++;
      ZipItem musicItem = createMusicItem(piece, voice);
      zipList.add(musicItem);
      addContentEntry(content, count, musicItem.zipPath, piece.page);

      ZipItem lyricItem = createLyricItem(piece, voice);
      if (lyricItem != null) {
        count++;
        zipList.add(lyricItem);
        addContentEntry(content, count, lyricItem.zipPath, piece.page);
      }
    }
    return zipList;
  }

  void addContentEntry(PrintStream content, int count, String fileName, String page) {
    content.printf("%2d)  %-52s Seite %s%n", count, fileName, page);
  }

  private ZipItem createMusicItem(Piece piece, Voice voice) {
    File sourceDir = new File(rootDir, piece.titleE + "/audio");
    File sourceFile = new File(sourceDir, voice.regularVoice + ".mp3");
    if (!sourceFile.exists()) {
      sourceFile = new File(sourceDir, voice.replacementVoice + ".mp3");
    }
    if (!sourceFile.exists()) {
      throw new RuntimeException(sourceFile + " not found.");
    }
    return new ZipItem(sourceFile, piece.titleD + ".mp3");
  }

  private ZipItem createLyricItem(Piece piece, Voice voice) {
    File sourceDir = new File(rootDir, piece.titleE + "/lyrics");
    File sourceFile = new File(sourceDir, voice.lyrics + ".mp3");
    if (!sourceFile.exists()) {
      return null;
    }
    return new ZipItem(sourceFile, piece.titleD + "_Ausprache.mp3");
  }

  boolean checkFiles() {
    boolean ok = true;
    for (Piece piece : AllPieces) {
      File dir = new File(rootDir, piece.titleE);
      ok = checkFilesDir(dir) & ok;
    }
    return ok;
  }

  boolean checkFilesDir(File dir) {
    try {
      File audioDir = new File(dir, "audio");
      if (!audioDir.exists()) {
        System.err.println(audioDir.getCanonicalPath() + " not found.");
        return false;
      }
      if (!audioDir.isDirectory()) {
        System.err.println(audioDir.getCanonicalPath() + " is not a directory.");
        return false;
      }
      boolean ok = true;
      for (Voice voice : allVoices) {
        File voiceFile = new File(audioDir, voice.regularVoice + ".mp3");
        if (!voiceFile.exists()) {
          if (voice.replacementVoice == null) {
            ok = false;
            System.err.println(voiceFile.getCanonicalPath() + " not found.");
          } else {
            voiceFile = new File(audioDir, voice.replacementVoice + ".mp3");
            if (!voiceFile.exists()) {
              ok = false;
              System.err.println(voiceFile.getCanonicalPath() + " not found.");
            }
          }
        }
      }
      return ok;

    } catch (IOException ex) {
      System.err.println(ex.getMessage());
      return false;
    }
  }

  private PrintStream prepareContentList(String voice) throws FileNotFoundException {

    PrintStream stream = new PrintStream(contentFile);
    stream.printf(contentHeader, voice, new Date());

    return stream;

  }

  private void closeContent(PrintStream content) {
    content.printf(contentFooter);
    content.close();
  }

  //create
  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) throws Exception {
    CreateDownloadZip processor = new CreateDownloadZip();

    if (!processor.checkFiles()) {
      return;
    }

    // force line separators to windows line endings
    System.setProperty("line.separator", "\r\n");

    processor.createZipFiles();

  }

  private void packFiles(String voice, List<ZipItem> filestozip, File zipfile) throws FileNotFoundException, IOException {

    // Create a buffer for reading the files
    byte[] buf = new byte[1024];
    try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipfile))) {

      for (ZipItem zipItem : filestozip) {
        try (FileInputStream in = new FileInputStream(zipItem.source)) {
          out.putNextEntry(new ZipEntry(zipItem.zipPath));
          // Transfer bytes from the file to the ZIP file
          int len;
          while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
          }
          // Complete the entry 
          out.closeEntry();

        }
      }
      out.close();
      System.out.println(" ####  " + voice + " done.");
    }

  }
}
