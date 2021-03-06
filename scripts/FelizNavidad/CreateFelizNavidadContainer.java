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
package FelizNavidad;

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
public class CreateFelizNavidadContainer {

  private static final File tempDir = new File("../temp");
  private static final String outFilename = "FelizeNavidad.fmc";
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

    // ----  /home/harald/NetBeansProjects/temp/FelizNavidad/01_FelizNavidad.mid
    new ZipItem(
    new File(tempDir, "FelizNavidad/01_FelizNavidad.mid"),
    "FelizNavidad/01_FelizNavidad.mid",
    false),
    // ---- 
    new ZipItem(
    new File(tempDir, "FelizNavidad/01_FelizNavidad.xml"),
    "FelizNavidad/01_FelizNavidad.xml",
    true),
    // ------ FelizNavidad/02_IchSteh.mid
    new ZipItem(
    new File(tempDir, "FelizNavidad/02_IchSteh.mid"),
    "FelizNavidad/02_IchSteh.mid",
    false),
    // ---- 
    new ZipItem(
    new File(tempDir, "FelizNavidad/02_IchSteh.xml"),
    "FelizNavidad/02_IchSteh.xml",
    true),
    // ------ FelizNavidad/03_IchSteh_Am.mid
    new ZipItem(
    new File(tempDir, "FelizNavidad/03_IchSteh_Am.mid"),
    "FelizNavidad/03_IchSteh_Am.mid",
    false),
    // ---- 
    new ZipItem(
    new File(tempDir, "FelizNavidad/03_IchSteh_Am.xml"),
    "FelizNavidad/03_IchSteh_Am.xml",
    true), //--
  };

  private CreateFelizNavidadContainer() {

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
    CreateFelizNavidadContainer processor = new CreateFelizNavidadContainer();
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
