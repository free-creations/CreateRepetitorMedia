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
package Blues;

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
public class CreateBluesContainer {

  private static final File tempDir = new File("../temp");
  private static final String outFilename = "Blues.fmc";
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
    // ---- Boimortier.sf2 Sound Font (please copy it to the temp dir)
    new ZipItem(
    new File(tempDir, "Chorium.sf2"),
    "Chorium.sf2",
    false),
    //--------------------------------------------------------------------------
    // ---- 1 BluesInD.mid (create with BluesInD.java)
    new ZipItem(
    new File(tempDir, "Blues/1_BluesInD.mid"),
    "Blues/1_BluesInD.mid",
    false),
    // ---- 1 Gravement.xml (create with Create_1_Gravement.java)
    new ZipItem(
    new File(tempDir, "Blues/1_BluesInD.xml"),
    "Blues/1_BluesInD.xml",
    true),
    //--------------------------------------------------------------------------
    // ---- 2 BluesInA.mid (create with BluesInA.java)
    new ZipItem(
    new File(tempDir, "Blues/2_BluesInA.mid"),
    "Blues/2_BluesInA.mid",
    false),
    // ---- 1 Gravement.xml (create with Create_1_Gravement.java)
    new ZipItem(
    new File(tempDir, "Blues/2_BluesInA.xml"),
    "Blues/2_BluesInA.xml",
    true), //--------------------------------------------------------------------------
    // ---- 3 Chuck Berry Blues in D
    new ZipItem(
    new File(tempDir, "Blues/3_ChuckBerryD.mid"),
    "Blues/3_ChuckBerryD.mid",
    false),
    // ---- 3
    new ZipItem(
    new File(tempDir, "Blues/3_ChuckBerryD.xml"),
    "Blues/3_ChuckBerryD.xml",
    true),};

  private CreateBluesContainer() {

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
    CreateBluesContainer processor = new CreateBluesContainer();
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
