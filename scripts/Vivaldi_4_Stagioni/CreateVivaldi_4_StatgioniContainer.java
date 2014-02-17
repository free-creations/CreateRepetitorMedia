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
package Vivaldi_4_Stagioni;


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
public class CreateVivaldi_4_StatgioniContainer {

  private static final File tempDir = new File("../temp");
    private static final File resourceDir = new File("scripts/Vivaldi_4_Stagioni/resources");

  private static final String outFilename = "Vivaldi4Stagioni.fmc";
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
    // ---- Chorium Sound Font (please copy it to the temp dir)
    new ZipItem(
    new File(resourceDir, "Vivaldi4Stagioni.sf2"),
    "Vivaldi4Stagioni.sf2",
    false),
    // ---- Rhodes Sound Font (please copy it to the temp dir)
    new ZipItem(
    new File(resourceDir, "StringPiano.sf2"),
    "StringPiano.sf2",
    false),
    //--------------------------------------------------------------------------
    // ---- 
    new ZipItem(
    new File(tempDir, "Vivaldi_4_Stagioni/1_Primavera1.mid"),
    "Vivaldi_4_Stagioni/1_Primavera1.mid",
    false),
    // ---- 
    new ZipItem(
    new File(tempDir, "Vivaldi_4_Stagioni/1_Primavera1.xml"),
    "Vivaldi_4_Stagioni/1_Primavera1.xml",
    true),
    //--------------------------------------------------------------------------
    // ---- 
    new ZipItem(
    new File(tempDir, "Vivaldi_4_Stagioni/2_Primavera3.mid"),
    "Vivaldi_4_Stagioni/2_Primavera3.mid",
    false),
    // ----
    new ZipItem(
    new File(tempDir, "Vivaldi_4_Stagioni/2_Primavera3.xml"),
    "Vivaldi_4_Stagioni/2_Primavera3.xml",
    true),
    
    
      
    //--------------------------------------------------------------------------
    // ---- Sanctus midi file 
    new ZipItem(
    new File(tempDir, "3_Estate/3_Estate.mid"),
    "3_Estate/3_Estate.mid",
    false),
    // ---- Sanctus xml file (generated with "Create_3_Sanctus.java")
    new ZipItem(
    new File(tempDir, "3_Estate/3_Estate.xml"),
    "3_Estate/3_Estate.xml",
    true),
    //--------------------------------------------------------------------------
    // ---- 
    new ZipItem(
    new File(tempDir, "4_Autunno/4_Autunno.mid"),
    "4_Autunno/4_Autunno.mid",
    false),
    // ---- Benedictus xml file (generated with "Create_4_Benedictus.java")
    new ZipItem(
    new File(tempDir, "4_Autunno/4_Autunno.xml"),
    "4_Autunno/4_Autunno.xml",
    true),
    //--------------------------------------------------------------------------
    // ---- 
    new ZipItem(
    new File(tempDir, "5_Inverno/5_Inverno.mid"),
    "5_Inverno/5_Inverno.mid",
    false),
    // ---- Agnus_Dei xml file (generated with "Create_5_Agnus_Dei.java")
    new ZipItem(
    new File(tempDir, "5_Inverno/5_Inverno.xml"),
    "5_Inverno/5_Inverno.xml",
    true),
    
  };

  private CreateVivaldi_4_StatgioniContainer() {

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
    CreateVivaldi_4_StatgioniContainer processor = new CreateVivaldi_4_StatgioniContainer();
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
