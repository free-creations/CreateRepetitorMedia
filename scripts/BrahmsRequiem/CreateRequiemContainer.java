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
package BrahmsRequiem;

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
public class CreateRequiemContainer {

  private static final File tempDir = new File("../temp");
  private static final File resourceDir = new File("scripts/BrahmsRequiem/resources");
  private static final String outFilename = "BrahmsRequiem.fmc";
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
    // ---- Orchestra sound font
    new ZipItem(
    new File(resourceDir, "Requiem.sf2"),
    "Requiem.sf2",
    false),
    // ---- Piano sound font
    new ZipItem(
    new File(resourceDir, "StringPiano.sf2"),
    "StringPiano.sf2",
    false),
    //--------------------------------------------------------------------------
    // ---- 1_SeligSind.mid (create with 1_SeligSind.java)
    new ZipItem(
    new File(tempDir, "BrahmsRequiem/1_SeligSind.mid"),
    "BrahmsRequiem/1_SeligSind.mid",
    false),
    // ---- 1_SeligSind.xml (create with Create_1_SeligSind.java)
    new ZipItem(
    new File(tempDir, "BrahmsRequiem/1_SeligSind.xml"),
    "BrahmsRequiem/1_SeligSind.xml",
    true),
    //--------------------------------------------------------------------------
    // ---- 2_DennAllesFleisch.mid (create with Create_2_DennAllesFleisch.java)
    new ZipItem(
    new File(tempDir, "BrahmsRequiem/2_DennAllesFleisch.mid"),
    "BrahmsRequiem/2_DennAllesFleisch.mid",
    false),
    // ---- 2_DennAllesFleisch.xml (create with Create_2_DennAllesFleisch.java)
    new ZipItem(
    new File(tempDir, "BrahmsRequiem/2_DennAllesFleisch.xml"),
    "BrahmsRequiem/2_DennAllesFleisch.xml",
    true),
    //--------------------------------------------------------------------------
    // ---- 3_HerrLehreMichDoch.mid (create with Create_3_HerrLehreMichDoch.java)
    new ZipItem(
    new File(tempDir, "BrahmsRequiem/3_HerrLehreMichDoch.mid"),
    "BrahmsRequiem/3_HerrLehreMichDoch.mid",
    false),
    // ---- 3_HerrLehreMichDoch.xml (create with Create_3_HerrLehreMichDoch.java)
    new ZipItem(
    new File(tempDir, "BrahmsRequiem/3_HerrLehreMichDoch.xml"),
    "BrahmsRequiem/3_HerrLehreMichDoch.xml",
    true),
    // ---- 4_WieLieblichSind.mid (create with Create_4_WieLieblichSind.java)
    new ZipItem(
    new File(tempDir, "BrahmsRequiem/4_WieLieblichSind.mid"),
    "BrahmsRequiem/4_WieLieblichSind.mid",
    false),
    // ---- 4_WieLieblichSind.xml (create with Create_4_WieLieblichSind.java)
    new ZipItem(
    new File(tempDir, "BrahmsRequiem/4_WieLieblichSind.xml"),
    "BrahmsRequiem/4_WieLieblichSind.xml",
    true),
    // ---- 5_IhrHabtNunTraurigkeit.mid (create with Create_5_IhrHabtNunTraurigkeit.java)
    new ZipItem(
    new File(tempDir, "BrahmsRequiem/5_IhrHabtNunTraurigkeit.mid"),
    "BrahmsRequiem/5_IhrHabtNunTraurigkeit.mid",
    false),
    // ---- 5_IhrHabtNunTraurigkeit.xml (create with Create_5_IhrHabtNunTraurigkeit.java)
    new ZipItem(
    new File(tempDir, "BrahmsRequiem/5_IhrHabtNunTraurigkeit.xml"),
    "BrahmsRequiem/5_IhrHabtNunTraurigkeit.xml",
    true),
    // ---- 6_DennWirHaben.mid (create with Create_6_DennWirHaben.java)
    new ZipItem(
    new File(tempDir, "BrahmsRequiem/6_DennWirHaben.mid"),
    "BrahmsRequiem/6_DennWirHaben.mid",
    false),
    // ---- 6_DennWirHaben.xml (create with Create_6_DennWirHaben.java)
    new ZipItem(
    new File(tempDir, "BrahmsRequiem/6_DennWirHaben.xml"),
    "BrahmsRequiem/6_DennWirHaben.xml",
    true),
    // ---- 7_SeligSindDieToten.mid (create with Create_7_SeligSindDieToten.java)
    new ZipItem(
    new File(tempDir, "BrahmsRequiem/7_SeligSindDieToten.mid"),
    "BrahmsRequiem/7_SeligSindDieToten.mid",
    false),
    // ---- 7_SeligSindDieToten.xml (create with Create_7_SeligSindDieToten.java)
    new ZipItem(
    new File(tempDir, "BrahmsRequiem/7_SeligSindDieToten.xml"),
    "BrahmsRequiem/7_SeligSindDieToten.xml",
    true),};

  private CreateRequiemContainer() {

    if (!tempDir.exists()) {
      throw new RuntimeException("No temp Directory found.");
    }
    if (!tempDir.isDirectory()) {
      throw new RuntimeException("../temp is not a directory.");
    }
    if (!resourceDir.exists()) {
      throw new RuntimeException("Resource Directory not found.");
    }
    if (!resourceDir.isDirectory()) {
      throw new RuntimeException("resourceDir is not a directory.");
    }
  }

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) throws Exception {
    CreateRequiemContainer processor = new CreateRequiemContainer();
    System.out.println("############ creating container info.");
    processor.createContainerInfo();
    System.out.println("############ packing the files.");
    processor.packFiles(files);
    System.out.println("############ Result written to " + (new File(tempDir, outFilename)).getCanonicalPath());

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
