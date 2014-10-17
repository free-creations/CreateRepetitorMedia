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
package AlleTastenImSchrank;

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
public class CreateAlleTastenContainer {

  private static final File tempDir = new File("../temp");
  private static final String outFilename = "AlleTasten.fmc";
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
    // ----  Sound Font (please copy it to the temp dir)
    new ZipItem(
    new File(tempDir, "Chorium.sf2"),
    "Chorium.sf2",
    false),
    // ----  Sound Font (please copy it to the temp dir)
    new ZipItem(
    new File(tempDir, "mk_1_rhodes.sf2"),
    "mk_1_rhodes.sf2",
    false),
    // ---- Rhodes Sound Font (please copy it to the temp dir)
    new ZipItem(
    new File(tempDir, "StringPiano.sf2"),
    "StringPiano.sf2",
    false),
    //--------------------------------------------------------------------------
    //--------------------------------------------------------------------------
    // ---- 20 Kookaburra.
    new ZipItem(
    new File(tempDir, "AlleTastenImSchrank/20_Kookaburra.mid"),
    "AlleTastenImSchrank/20_Kookaburra.mid",
    false),
    // ---- 
    new ZipItem(
    new File(tempDir, "AlleTastenImSchrank/20_Kookaburra.xml"),
    "AlleTastenImSchrank/20_Kookaburra.xml",
    true), //--
    // ---- 21_WieWohl.
    new ZipItem(
    new File(tempDir, "AlleTastenImSchrank/21_WieWohl.mid"),
    "AlleTastenImSchrank/21_WieWohl.mid",
    false),
    // ---- 
    new ZipItem(
    new File(tempDir, "AlleTastenImSchrank/21_WieWohl.xml"),
    "AlleTastenImSchrank/21_WieWohl.xml",
    true), //--
    // ---- 22_Heissa.
    new ZipItem(
    new File(tempDir, "AlleTastenImSchrank/22_Heissa.mid"),
    "AlleTastenImSchrank/22_Heissa.mid",
    false),
    // ---- 
    new ZipItem(
    new File(tempDir, "AlleTastenImSchrank/22_Heissa.xml"),
    "AlleTastenImSchrank/22_Heissa.xml",
    true), //--
    // ---- 24_WinterAde.
    new ZipItem(
    new File(tempDir, "AlleTastenImSchrank/24_WinterAde.mid"),
    "AlleTastenImSchrank/24_WinterAde.mid",
    false),
    // ---- 
    new ZipItem(
    new File(tempDir, "AlleTastenImSchrank/24_WinterAde.xml"),
    "AlleTastenImSchrank/24_WinterAde.xml",
    true), //--
    // ---- 42_IchHabDieNachtGetraeumet.
    new ZipItem(
    new File(tempDir, "AlleTastenImSchrank/42_IchHabDieNachtGetraeumet.mid"),
    "AlleTastenImSchrank/42_IchHabDieNachtGetraeumet.mid",
    false),
    // ---- 
    new ZipItem(
    new File(tempDir, "AlleTastenImSchrank/42_IchHabDieNachtGetraeumet.xml"),
    "AlleTastenImSchrank/42_IchHabDieNachtGetraeumet.xml",
    true), //--
    // ---- 42_IchHabDieNachtGetraeumetAmoll.
    new ZipItem(
    new File(tempDir, "AlleTastenImSchrank/42_IchHabDieNachtGetraeumetAmoll.mid"),
    "AlleTastenImSchrank/42_IchHabDieNachtGetraeumetAmoll.mid",
    false),
    // ---- 
    new ZipItem(
    new File(tempDir, "AlleTastenImSchrank/42_IchHabDieNachtGetraeumetAmoll.xml"),
    "AlleTastenImSchrank/42_IchHabDieNachtGetraeumetAmoll.xml",
    true), //--
  };

  private CreateAlleTastenContainer() {

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
    CreateAlleTastenContainer processor = new CreateAlleTastenContainer();
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
