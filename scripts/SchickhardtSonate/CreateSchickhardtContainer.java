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
package SchickhardtSonate;

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
public class CreateSchickhardtContainer {

  private static final File tempDir = new File("../temp");
  private static final File resourceDir = new File("scripts/SchickhardtSonate/resources");
  private static final String outFilename = "SchickhardtSonate.fmc";
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
    new File(tempDir, "SchickhardtSonate/1_Prelude.mid"),
    "SchickhardtSonate/1_Prelude.mid",
    false),
    // ---- 1
    new ZipItem(
    new File(tempDir, "SchickhardtSonate/1_Prelude.xml"),
    "SchickhardtSonate/1_Prelude.xml",
    true),
    // ---- 2
    new ZipItem(
    new File(tempDir, "SchickhardtSonate/2_Courante.mid"),
    "SchickhardtSonate/2_Courante.mid",
    false),
    // ---- 2
    new ZipItem(
    new File(tempDir, "SchickhardtSonate/2_Courante.xml"),
    "SchickhardtSonate/2_Courante.xml",
    true),
    // ---- 3
    new ZipItem(
    new File(tempDir, "SchickhardtSonate/3_Allemande.mid"),
    "SchickhardtSonate/3_Allemande.mid",
    false),
    // ---- 3
    new ZipItem(
    new File(tempDir, "SchickhardtSonate/3_Allemande.xml"),
    "SchickhardtSonate/3_Allemande.xml",
    true),
    // ---- 4
    new ZipItem(
    new File(tempDir, "SchickhardtSonate/4_Sarabande.mid"),
    "SchickhardtSonate/4_Sarabande.mid",
    false),
    // ---- 4
    new ZipItem(
    new File(tempDir, "SchickhardtSonate/4_Sarabande.xml"),
    "SchickhardtSonate/4_Sarabande.xml",
    true),
    // ---- 5
    new ZipItem(
    new File(tempDir, "SchickhardtSonate/5_Gigue.mid"),
    "SchickhardtSonate/5_Gigue.mid",
    false),
    // ---- 5
    new ZipItem(
    new File(tempDir, "SchickhardtSonate/5_Gigue.xml"),
    "SchickhardtSonate/5_Gigue.xml",
    true),};

  private CreateSchickhardtContainer() {

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
    CreateSchickhardtContainer processor = new CreateSchickhardtContainer();
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
