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
package AusgewaehlteDuette;

import de.free_creations.mediacontainer2.ContainerInfo;
import java.io.*;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * http://www.exampledepot.com/egs/java.util.zip/CreateZip.html
 *
 * @author harald
 */
public class CreateAusgewaehlteDuetteContainer {

  private static final File tempDir = new File("../temp");
  private static final File resourceDir = new File("scripts/AusgewaehlteDuette/resources");
  private static final String outFilename = "AusgewaehlteDuette.fmc";
  private static final File tempContainerInfoFile = new File(tempDir, "container.xml");

  private static class ZipItem {

    public final File source;
    public final String zipPath;
    public final boolean isRootFile;

    public ZipItem(File source, String zipPath, boolean isRootFile) {
      this.source = source;
      this.zipPath = zipPath;
      this.isRootFile = isRootFile;
    }
  }
  private static ZipItem[] files = new ZipItem[]{
    // ---- Container Info
    new ZipItem(
    tempContainerInfoFile,
    "META-INF/container.xml",
    false),
    // ---- Hook.sf2 Sound Font (please copy it to the temp dir)
    new ZipItem(
    new File(resourceDir, "Schickardt.sf2"),
    "Schickardt.sf2",
    false),
    // ---- Rhodes Sound Font (please copy it to the temp dir)
    new ZipItem(
    new File(resourceDir, "StringPiano.sf2"),
    "StringPiano.sf2",
    false),
    //--------------------------------------------------------------------------
    // ---- 1
    new ZipItem(
    new File(tempDir, "AusgewaehlteDuette/1_Rondeau16.mid"),
    "AusgewaehlteDuette/1_Rondeau16.mid",
    false),
    // ---- 1
    new ZipItem(
    new File(tempDir, "AusgewaehlteDuette/1_Rondeau16.xml"),
    "AusgewaehlteDuette/1_Rondeau16.xml",
    true),
    //--------------------------------------------------------------------------
    // ---- 2
    new ZipItem(
    new File(tempDir, "AusgewaehlteDuette/2_Rondeau17.mid"),
    "AusgewaehlteDuette/2_Rondeau17.mid",
    false),
    // ---- 2
    new ZipItem(
    new File(tempDir, "AusgewaehlteDuette/2_Rondeau17.xml"),
    "AusgewaehlteDuette/2_Rondeau17.xml",
    true),
    //--------------------------------------------------------------------------
    // ---- 18
    new ZipItem(
    new File(tempDir, "AusgewaehlteDuette/18_Adagio.mid"),
    "AusgewaehlteDuette/18_Adagio.mid",
    false),
    // ---- 2
    new ZipItem(
    new File(tempDir, "AusgewaehlteDuette/18_Adagio.xml"),
    "AusgewaehlteDuette/18_Adagio.xml",
    true), //--------------------------------------------------------------------------
    // ---- 21 
    new ZipItem(
    new File(tempDir, "AusgewaehlteDuette/19_RondoGrazioso.mid"),
    "AusgewaehlteDuette/19_RondoGrazioso.mid",
    false),
    // ---- 2
    new ZipItem(
    new File(tempDir, "AusgewaehlteDuette/19_RondoGrazioso.xml"),
    "AusgewaehlteDuette/19_RondoGrazioso.xml",
    true),
    //--------------------------------------------------------------------------
    // ---- 21 
    new ZipItem(
    new File(tempDir, "AusgewaehlteDuette/21_Vivace.mid"),
    "AusgewaehlteDuette/21_Vivace.mid",
    false),
    // ---- 2
    new ZipItem(
    new File(tempDir, "AusgewaehlteDuette/21_Vivace.xml"),
    "AusgewaehlteDuette/21_Vivace.xml",
    true),
    //--------------------------------------------------------------------------
    // ---- 21 
    new ZipItem(
    new File(tempDir, "AusgewaehlteDuette/22_Fanfare.mid"),
    "AusgewaehlteDuette/22_Fanfare.mid",
    false),
    // ---- 2
    new ZipItem(
    new File(tempDir, "AusgewaehlteDuette/22_Fanfare.xml"),
    "AusgewaehlteDuette/22_Fanfare.xml",
    true),
    //--------------------------------------------------------------------------
    // ---- 23
    new ZipItem(
    new File(tempDir, "AusgewaehlteDuette/23_Allegretto.mid"),
    "AusgewaehlteDuette/23_Allegretto.mid",
    false),
    // ---- 2
    new ZipItem(
    new File(tempDir, "AusgewaehlteDuette/23_Allegretto.xml"),
    "AusgewaehlteDuette/23_Allegretto.xml",
    true),
    //--------------------------------------------------------------------------
    // ---- 25
    new ZipItem(
    new File(tempDir, "AusgewaehlteDuette/25_Giga.mid"),
    "AusgewaehlteDuette/25_Giga.mid",
    false),
    // ---- 2
    new ZipItem(
    new File(tempDir, "AusgewaehlteDuette/25_Giga.xml"),
    "AusgewaehlteDuette/25_Giga.xml",
    true), //
    //--------------------------------------------------------------------------
    // ---- 26
    new ZipItem(
    new File(tempDir, "AusgewaehlteDuette/26_Giga.mid"),
    "AusgewaehlteDuette/26_Giga.mid",
    false),
    // ---- 2
    new ZipItem(
    new File(tempDir, "AusgewaehlteDuette/26_Giga.xml"),
    "AusgewaehlteDuette/26_Giga.xml",
    true), //
    //--------------------------------------------------------------------------
    // ---- 27_Larghetto.mid
    new ZipItem(
    new File(tempDir, "AusgewaehlteDuette/27_Larghetto.mid"),
    "AusgewaehlteDuette/27_Larghetto.mid",
    false),
    // ---- 2
    new ZipItem(
    new File(tempDir, "AusgewaehlteDuette/27_Larghetto.xml"),
    "AusgewaehlteDuette/27_Larghetto.xml",
    true), //
    //--------------------------------------------------------------------------
    // ---- 27-2_Allegro.mid
    new ZipItem(
    new File(tempDir, "AusgewaehlteDuette/27-2_Allegro.mid"),
    "AusgewaehlteDuette/27-2_Allegro.mid",
    false),
    // ---- 2
    new ZipItem(
    new File(tempDir, "AusgewaehlteDuette/27-2_Allegro.xml"),
    "AusgewaehlteDuette/27-2_Allegro.xml",
    true), //
    //--------------------------------------------------------------------------
    // ---- 27-3_Adagio.mid
    new ZipItem(
    new File(tempDir, "AusgewaehlteDuette/27-3_Adagio.mid"),
    "AusgewaehlteDuette/27-3_Adagio.mid",
    false),
    // ---- 2
    new ZipItem(
    new File(tempDir, "AusgewaehlteDuette/27-3_Adagio.xml"),
    "AusgewaehlteDuette/27-3_Adagio.xml",
    true), //

    //--------------------------------------------------------------------------
    // ---- 27-4_Allegro.mid
    new ZipItem(
    new File(tempDir, "AusgewaehlteDuette/27-4_Allegro.mid"),
    "AusgewaehlteDuette/27-4_Allegro.mid",
    false),
    // ---- 2
    new ZipItem(
    new File(tempDir, "AusgewaehlteDuette/27-4_Allegro.xml"),
    "AusgewaehlteDuette/27-4_Allegro.xml",
    true), //
    //--------------------------------------------------------------------------
    // ---- 27p_LarghettoPiano.mid
    new ZipItem(
    new File(tempDir, "AusgewaehlteDuette/27p_LarghettoPiano.mid"),
    "AusgewaehlteDuette/27p_LarghettoPiano.mid",
    false),
    // ---- 2
    new ZipItem(
    new File(tempDir, "AusgewaehlteDuette/27p_LarghettoPiano.xml"),
    "AusgewaehlteDuette/27p_LarghettoPiano.xml",
    true), //
    //--------------------------------------------------------------------------
    // ---- 28_Allegro.mid
    new ZipItem(
    new File(tempDir, "AusgewaehlteDuette/28_Allegro.mid"),
    "AusgewaehlteDuette/28_Allegro.mid",
    false),
    // ---- 2
    new ZipItem(
    new File(tempDir, "AusgewaehlteDuette/28_Allegro.xml"),
    "AusgewaehlteDuette/28_Allegro.xml",
    true), //
    //--------------------------------------------------------------------------
    // ---- 29_Williams.mid
    new ZipItem(
    new File(tempDir, "AusgewaehlteDuette/29_Williams.mid"),
    "AusgewaehlteDuette/29_Williams.mid",
    false),
    // ---- 2
    new ZipItem(
    new File(tempDir, "AusgewaehlteDuette/29_Williams.xml"),
    "AusgewaehlteDuette/29_Williams.xml",
    true), //

    //--------------------------------------------------------------------------
    // ---- 29_Williams.mid
    new ZipItem(
    new File(tempDir, "AusgewaehlteDuette/29_1_WilliamsSimple.mid"),
    "AusgewaehlteDuette/29_1_WilliamsSimple.mid",
    false),
    // ---- 2
    new ZipItem(
    new File(tempDir, "AusgewaehlteDuette/29_1_WilliamsSimple.xml"),
    "AusgewaehlteDuette/29_1_WilliamsSimple.xml",
    true), //
    //--------------------------------------------------------------------------
    // ---- 38_Giga.mid
    new ZipItem(
    new File(tempDir, "AusgewaehlteDuette/38_Giga.mid"),
    "AusgewaehlteDuette/38_Giga.mid",
    false),
    // ---- 2
    new ZipItem(
    new File(tempDir, "AusgewaehlteDuette/38_Giga.xml"),
    "AusgewaehlteDuette/38_Giga.xml",
    true), //
    //--------------------------------------------------------------------------
    // ---- 46_Vivaldi.mid
    new ZipItem(
    new File(tempDir, "AusgewaehlteDuette/46_Vivaldi.mid"),
    "AusgewaehlteDuette/46_Vivaldi.mid",
    false),
    // ---- 2
    new ZipItem(
    new File(tempDir, "AusgewaehlteDuette/46_Vivaldi.xml"),
    "AusgewaehlteDuette/46_Vivaldi.xml",
    true), //
    //--------------------------------------------------------------------------
    // ---- 31_Preludio.mid
    new ZipItem(
    new File(tempDir, "AusgewaehlteDuette/31_Preludio.mid"),
    "AusgewaehlteDuette/31_Preludio.mid",
    false),
    // ---- 2
    new ZipItem(
    new File(tempDir, "AusgewaehlteDuette/31_Preludio.xml"),
    "AusgewaehlteDuette/31_Preludio.xml",
    true), //
    //--------------------------------------------------------------------------
    // ---- 32_Aria.mid
    // Midi file is: /home/harald/NetBeansProjects/temp/AusgewaehlteDuette/32_Aria.mid
    // Song file is: /home/harald/NetBeansProjects/temp/AusgewaehlteDuette/32_Aria.xml

    new ZipItem(
    new File(tempDir, "AusgewaehlteDuette/32_Aria.mid"),
    "AusgewaehlteDuette/32_Aria.mid",
    false),
    // ---- 2
    new ZipItem(
    new File(tempDir, "AusgewaehlteDuette/32_Aria.xml"),
    "AusgewaehlteDuette/32_Aria.xml",
    true), //
    //--------------------------------------------------------------------------
    // ---- 32_Aria.mid
    // Midi file is: /home/harald/NetBeansProjects/temp/AusgewaehlteDuette/33_Piano.mid
    // Song file is: /home/harald/NetBeansProjects/temp/AusgewaehlteDuette/33_Piano.xml

    new ZipItem(
    new File(tempDir, "AusgewaehlteDuette/33_Piano.mid"),
    "AusgewaehlteDuette/33_Piano.mid",
    false),
    // ---- 2
    new ZipItem(
    new File(tempDir, "AusgewaehlteDuette/33_Piano.xml"),
    "AusgewaehlteDuette/33_Piano.xml",
    true), //
    //--------------------------------------------------------------------------
    // ----34_Chaconne.mid
    // Midi file is: /home/harald/NetBeansProjects/temp/AusgewaehlteDuette/34_Chaconne.mid
    // Song file is: /home/harald/NetBeansProjects/temp/AusgewaehlteDuette/34_Chaconne.xml

    new ZipItem(
    new File(tempDir, "AusgewaehlteDuette/34_Chaconne.mid"),
    "AusgewaehlteDuette/34_Chaconne.mid",
    false),
    // ---- 2
    new ZipItem(
    new File(tempDir, "AusgewaehlteDuette/34_Chaconne.xml"),
    "AusgewaehlteDuette/34_Chaconne.xml",
    true), //
    //--------------------------------------------------------------------------
    // ----35_Allegro
    // Midi file is: /home/harald/NetBeansProjects/temp/AusgewaehlteDuette/35_Allegro.mid
    // Song file is: /home/harald/NetBeansProjects/temp/AusgewaehlteDuette/35_Allegro.xml
    new ZipItem(
    new File(tempDir, "AusgewaehlteDuette/35_Allegro.mid"),
    "AusgewaehlteDuette/35_Allegro.mid",
    false),
    // ---- 2
    new ZipItem(
    new File(tempDir, "AusgewaehlteDuette/35_Allegro.xml"),
    "AusgewaehlteDuette/35_Allegro.xml",
    true), //
    //--------------------------------------------------------------------------
    // ----35_Allegro
    // Midi file is: /home/harald/NetBeansProjects/temp/AusgewaehlteDuette/36_GavottaVivace.mid
    // Song file is: /home/harald/NetBeansProjects/temp/AusgewaehlteDuette/36_GavottaVivace.xml
    new ZipItem(
    new File(tempDir, "AusgewaehlteDuette/36_GavottaVivace.mid"),
    "AusgewaehlteDuette/36_GavottaVivace.mid",
    false),
    // ---- 2
    new ZipItem(
    new File(tempDir, "AusgewaehlteDuette/36_GavottaVivace.xml"),
    "AusgewaehlteDuette/36_GavottaVivace.xml",
    true), //
    //--------------------------------------------------------------------------
    // ----37_Presto
    // Midi file is: /home/harald/NetBeansProjects/temp/AusgewaehlteDuette/37_Presto.mid
    // Song file is: /home/harald/NetBeansProjects/temp/AusgewaehlteDuette/37_Presto.xml
    new ZipItem(
    new File(tempDir, "AusgewaehlteDuette/37_Presto.mid"),
    "AusgewaehlteDuette/37_Presto.mid",
    false),
    // ---- 2
    new ZipItem(
    new File(tempDir, "AusgewaehlteDuette/37_Presto.xml"),
    "AusgewaehlteDuette/37_Presto.xml",
    true), //
    //--------------------------------------------------------------------------
    // ----38_GigueEnRondeau
    // Midi file is: /home/harald/NetBeansProjects/temp/AusgewaehlteDuette/38_GigueEnRondeau.mid
    // Song file is: /home/harald/NetBeansProjects/temp/AusgewaehlteDuette/38_GigueEnRondeau.xml
    new ZipItem(
    new File(tempDir, "AusgewaehlteDuette/38_GigueEnRondeau.mid"),
    "AusgewaehlteDuette/38_GigueEnRondeau.mid",
    false),
    // ---- 2
    new ZipItem(
    new File(tempDir, "AusgewaehlteDuette/38_GigueEnRondeau.xml"),
    "AusgewaehlteDuette/38_GigueEnRondeau.xml",
    true), //
    //--------------------------------------------------------------------------
    // ----38_GigueEnRondeau
    // Midi file is: /home/harald/NetBeansProjects/temp/AusgewaehlteDuette/38_GigueEnRondeau.mid
    // Song file is: /home/harald/NetBeansProjects/temp/AusgewaehlteDuette/38_GigueEnRondeau.xml
    new ZipItem(
    new File(tempDir, "AusgewaehlteDuette/39_Grave.mid"),
    "AusgewaehlteDuette/39_Grave.mid",
    false),
    // ---- 2
    new ZipItem(
    new File(tempDir, "AusgewaehlteDuette/39_Grave.xml"),
    "AusgewaehlteDuette/39_Grave.xml",
    true), //
  };

  private CreateAusgewaehlteDuetteContainer() {

    if (!tempDir.exists()) {
      throw new RuntimeException("No temp Directory found.");
    }
    if (!tempDir.isDirectory()) {
      throw new RuntimeException("../temp is not a directory.");
    }
  }

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) throws Exception {
    CreateAusgewaehlteDuetteContainer processor = new CreateAusgewaehlteDuetteContainer();
    System.out.println("############ creating container info.");
    processor.createContainerInfo();
    System.out.println("############ packing the files.");
    processor.packFiles(files);
  }

  private void createContainerInfo() throws FileNotFoundException, Exception {
    ArrayList<String> rootFileList = new ArrayList<>();

    for (ZipItem file : files) {
      if (file.isRootFile) {
        rootFileList.add("file:///" + file.zipPath);
      }
    }

    ContainerInfo containerInfo = new ContainerInfo(rootFileList);
    OutputStream os = new FileOutputStream(tempContainerInfoFile);
    containerInfo.writeToStream(os);
  }

  private void packFiles(ZipItem[] filestozip) throws FileNotFoundException, IOException {
    // Create a buffer for reading the files
    byte[] buf = new byte[1024];
    try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(new File(tempDir, outFilename)))) {
      for (int i = 0; i < filestozip.length; i++) {
        try (FileInputStream in = new FileInputStream(filestozip[i].source)) {
          out.putNextEntry(new ZipEntry(filestozip[i].zipPath));
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
    }

  }
}
