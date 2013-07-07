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
package JamesHookTrios;

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
public class CreateHookContainer {

  private static final File tempDir = new File("../temp");
  private static final String outFilename = "Hook_Trios.fmc";
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
    new File(tempDir, "Hook.sf2"),
    "Hook.sf2",
    false),
    // ---- Rhodes Sound Font (please copy it to the temp dir)
    new ZipItem(
    new File(tempDir, "mk_1_rhodes.sf2"),
    "mk_1_rhodes.sf2",
    false),
    //--------------------------------------------------------------------------
    // ---- 1_Allegro.mid (create with Create_1_Allegro.java)
    new ZipItem(
    new File(tempDir, "Hook_Trios/1_Allegro.mid"),
    "Hook_Trios/1_Allegro.mid",
    false),
    // ---- 1_Allegro.xml (create with Create_1_Allegro.java)
    new ZipItem(
    new File(tempDir, "Hook_Trios/1_Allegro.xml"),
    "Hook_Trios/1_Allegro.xml",
    true),
    //--------------------------------------------------------------------------
    // ---- 2_Menuetto.mid
    new ZipItem(
    new File(tempDir, "Hook_Trios/2_Menuetto.mid"),
    "Hook_Trios/2_Menuetto.mid",
    false),
    // ---- 2_Menuetto.xml 
    new ZipItem(
    new File(tempDir, "Hook_Trios/2_Menuetto.xml"),
    "Hook_Trios/2_Menuetto.xml",
    true),
    // ---- 3_Rondo.mid (create with Create_3Rondo.java)
    new ZipItem(
    new File(tempDir, "Hook_Trios/3_Rondo.mid"),
    "Hook_Trios/3_Rondo.mid",
    false),
    // ---- 3_Rondo.xml (create with Create_3Rondo.java)
    new ZipItem(
    new File(tempDir, "Hook_Trios/3_Rondo.xml"),
    "Hook_Trios/3_Rondo.xml",
    true),};
//    // ---- 3_Gravement.mid (create with Create_3_Gravement.java)
//    new ZipItem(
//    new File(tempDir, "Sonate I/3_Gravement.mid"),
//    "Sonate1/3_Gravement.mid",
//    false),
//    // ---- 3_Gravement.xml (create with Create_3_Gravement.java)
//    new ZipItem(
//    new File(tempDir, "Sonate I/3_Gravement.xml"),
//    "Sonate1/3_Gravement.xml",
//    true),
//    // ---- 4_GavotteEnRondeau.mid (create with Create_4_GavotteEnRondeau.java)
//    new ZipItem(
//    new File(tempDir, "Sonate I/4_GavotteEnRondeau.mid"),
//    "Sonate1/4_GavotteEnRondeau.mid",
//    false),
//    // ---- 4_GavotteEnRondeau.xml (create with Create_4_GavotteEnRondeau.java)
//    new ZipItem(
//    new File(tempDir, "Sonate I/4_GavotteEnRondeau.xml"),
//    "Sonate1/4_GavotteEnRondeau.xml",
//    true),
//    // ---- 5 Gayment.mid (create with Create_5_Gayment.java)
//    new ZipItem(
//    new File(tempDir, "Sonate I/5_Gayment.mid"),
//    "Sonate1/5_Gayment.mid",
//    false),
//    // ---- 5 Gayment.xml (create with Create_5_Gayment.java)
//    new ZipItem(
//    new File(tempDir, "Sonate I/5_Gayment.xml"),
//    "Sonate1/5_Gayment.xml",
//    true),};

  private CreateHookContainer() {

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
    CreateHookContainer processor = new CreateHookContainer();
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
