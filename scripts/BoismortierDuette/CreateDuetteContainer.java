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
package BoismortierDuette;

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
public class CreateDuetteContainer {

  private static final File tempDir = new File("../temp");
  private static final String outFilename = "BoismortierDuette.fmc";
  private static final File resourceDir = new File("scripts/BoismortierDuette/resources");
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
    new File(resourceDir, "Boimortier.sf2"),
    "Boimortier.sf2",
    false),
    // ---- Rhodes Sound Font (please copy it to the temp dir)
    new ZipItem(
    new File(resourceDir, "StringPiano.sf2"),
    "StringPiano.sf2",
    false),
    //--------------------------------------------------------------------------
    // ---- 
    new ZipItem(
    new File(tempDir, "Boismortier Duette/01_Allemande.mid"),
    "Boismortier_Duette/01_Allemande.mid",
    false),
    // ---- 1 Gravement.xml (create with Create_1_Gravement.java)
    new ZipItem(
    new File(tempDir, "Boismortier Duette/01_Allemande.xml"),
    "Boismortier_Duette/01_Allemande.xml",
    true),
    //--------------------------------------------------------------------------
    // ---- 
    new ZipItem(
    new File(tempDir, "Boismortier Duette/02_Rondeau.mid"),
    "Boismortier_Duette/02_Rondeau.mid",
    false),
    // ---- 1 Gravement.xml (create with Create_1_Gravement.java)
    new ZipItem(
    new File(tempDir, "Boismortier Duette/02_Rondeau.xml"),
    "Boismortier_Duette/02_Rondeau.xml",
    true),
    //--------------------------------------------------------------------------
    // ---- 
    new ZipItem(
    new File(tempDir, "Boismortier Duette/03_Paysane.mid"),
    "Boismortier_Duette/03_Paysane.mid",
    false),
    // ---- 1 Gravement.xml (create with Create_1_Gravement.java)
    new ZipItem(
    new File(tempDir, "Boismortier Duette/03_Paysane.xml"),
    "Boismortier_Duette/03_Paysane.xml",
    true),
    //--------------------------------------------------------------------------
    // ---- 
    new ZipItem(
    new File(tempDir, "Boismortier Duette/04_Doucement.mid"),
    "Boismortier_Duette/04_Doucement.mid",
    false),
    // ---- 1 Gravement.xml (create with Create_1_Gravement.java)
    new ZipItem(
    new File(tempDir, "Boismortier Duette/04_Doucement.xml"),
    "Boismortier_Duette/04_Doucement.xml",
    true),
    //--------------------------------------------------------------------------
    // ---- 
    new ZipItem(
    new File(tempDir, "Boismortier Duette/05_Menuet_1.mid"),
    "Boismortier_Duette/05_Menuet_1.mid",
    false),
    // ---- ----------------------------------------------------
    new ZipItem(
    new File(tempDir, "Boismortier Duette/05_Menuet_1.xml"),
    "Boismortier_Duette/05_Menuet_1.xml",
    true),
    //--------------------------------------------------------------------------
    // ---- 
    new ZipItem(
    new File(tempDir, "Boismortier Duette/06_Menuet_2.mid"),
    "Boismortier_Duette/06_Menuet_2.mid",
    false),
    // ---- 
    new ZipItem(
    new File(tempDir, "Boismortier Duette/06_Menuet_2.xml"),
    "Boismortier_Duette/06_Menuet_2.xml",
    true),
    //--------------------------------------------------------------------------
    // ---- 
    new ZipItem(
    new File(tempDir, "Boismortier Duette/07_Suite_2_1.mid"),
    "Boismortier_Duette/07_Suite_2_1.mid",
    false),
    // ---- ----------------------------------------------------
    new ZipItem(
    new File(tempDir, "Boismortier Duette/07_Suite_2_1.xml"),
    "Boismortier_Duette/07_Suite_2_1.xml",
    true),
    //--------------------------------------------------------------------------
    // ---- 
    new ZipItem(
    new File(tempDir, "Boismortier Duette/08_Suite_2_2.mid"),
    "Boismortier_Duette/08_Suite_2_2.mid",
    false),
    // ---- 1 Gravement.xml (create with Create_1_Gravement.java)
    new ZipItem(
    new File(tempDir, "Boismortier Duette/08_Suite_2_2.xml"),
    "Boismortier_Duette/08_Suite_2_2.xml",
    true),
    //--------------------------------------------------------------------------
    // ---- 
    new ZipItem(
    new File(tempDir, "Boismortier Duette/09_Suite_2_3.mid"),
    "Boismortier_Duette/09_Suite_2_3.mid",
    false),
    // ---- 1 Gravement.xml (create with Create_1_Gravement.java)
    new ZipItem(
    new File(tempDir, "Boismortier Duette/09_Suite_2_3.xml"),
    "Boismortier_Duette/09_Suite_2_3.xml",
    true),
    //--------------------------------------------------------------------------
    // ---- 
    new ZipItem(
    new File(tempDir, "Boismortier Duette/11_Rigaudon_1.mid"),
    "Boismortier_Duette/11_Rigaudon_1.mid",
    false),
    // ---- 1 Gravement.xml (create with Create_1_Gravement.java)
    new ZipItem(
    new File(tempDir, "Boismortier Duette/11_Rigaudon_1.xml"),
    "Boismortier_Duette/11_Rigaudon_1.xml",
    true),
    //--------------------------------------------------------------------------
    // ---- 
    new ZipItem(
    new File(tempDir, "Boismortier Duette/12_Rigaudon_2.mid"),
    "Boismortier_Duette/12_Rigaudon_2.mid",
    false),
    // ---- 1 Gravement.xml (create with Create_1_Gravement.java)
    new ZipItem(
    new File(tempDir, "Boismortier Duette/12_Rigaudon_2.xml"),
    "Boismortier_Duette/12_Rigaudon_2.xml",
    true),
    //--------------------------------------------------------------------------
    // ---- 
    new ZipItem(
    new File(tempDir, "Boismortier Duette/13_NaudotSuite1_1.mid"),
    "Boismortier_Duette/13_NaudotSuite1_1.mid",
    false),
    // ---- 
    new ZipItem(
    new File(tempDir, "Boismortier Duette/13_NaudotSuite1_1.xml"),
    "Boismortier_Duette/13_NaudotSuite1_1.xml",
    true),
    //--------------------------------------------------------------------------
    // ---- 
    new ZipItem(
    new File(tempDir, "Boismortier Duette/14_NaudotSuite1_2.mid"),
    "Boismortier_Duette/14_NaudotSuite1_2.mid",
    false),
    // ---- 
    new ZipItem(
    new File(tempDir, "Boismortier Duette/14_NaudotSuite1_2.xml"),
    "Boismortier_Duette/14_NaudotSuite1_2.xml",
    true),
    // ---- /home/harald/NetBeansProjects/temp/Boismortier Duette/21_Allemande.mid
    new ZipItem(
    new File(tempDir, "Boismortier Duette/21_Allemande.mid"),
    "Boismortier_Duette/21_Allemande.mid",
    false),
    // ---- 
    new ZipItem(
    new File(tempDir, "Boismortier Duette/21_Allemande.xml"),
    "Boismortier_Duette/21_Allemande.xml",
    true),
    // ---- /home/harald/NetBeansProjects/temp/Boismortier Duette/22_Fanfare.mid
    new ZipItem(
    new File(tempDir, "Boismortier Duette/22_Fanfare.mid"),
    "Boismortier_Duette/22_Fanfare.mid",
    false),
    // ---- 
    new ZipItem(
    new File(tempDir, "Boismortier Duette/22_Fanfare.xml"),
    "Boismortier_Duette/22_Fanfare.xml",
    true),
    //--------------------------------------------------------------------------
    // ---- /home/harald/NetBeansProjects/temp/Boismortier Duette/23_Menuet.mid
    new ZipItem(
    new File(tempDir, "Boismortier Duette/23_Menuet.mid"),
    "Boismortier_Duette/23_Menuet.mid",
    false),
    // ---- 
    new ZipItem(
    new File(tempDir, "Boismortier Duette/23_Menuet.xml"),
    "Boismortier_Duette/23_Menuet.xml",
    true),
    //--------------------------------------------------------------------------
    // ---- /home/harald/NetBeansProjects/temp/Boismortier Duette/24_Courante.mid
    new ZipItem(
    new File(tempDir, "Boismortier Duette/24_Courante.mid"),
    "Boismortier_Duette/24_Courante.mid",
    false),
    // ---- 
    new ZipItem(
    new File(tempDir, "Boismortier Duette/24_Courante.xml"),
    "Boismortier_Duette/24_Courante.xml",
    true),
    //--------------------------------------------------------------------------
    // ---- /home/harald/NetBeansProjects/temp/Boismortier Duette/25_Rondeau.mid
    new ZipItem(
    new File(tempDir, "Boismortier Duette/25_Rondeau.mid"),
    "Boismortier_Duette/25_Rondeau.mid",
    false),
    // ---- 
    new ZipItem(
    new File(tempDir, "Boismortier Duette/25_Rondeau.xml"),
    "Boismortier_Duette/25_Rondeau.xml",
    true),
    //--------------------------------------------------------------------------
    // ---- /home/harald/NetBeansProjects/temp/Boismortier Duette/26_Pavannne.mid
    new ZipItem(
    new File(tempDir, "Boismortier Duette/26_Pavannne.mid"),
    "Boismortier_Duette/26_Pavannne.mid",
    false),
    // ---- 
    new ZipItem(
    new File(tempDir, "Boismortier Duette/26_Pavannne.xml"),
    "Boismortier_Duette/26_Pavannne.xml",
    true),
    //--------------------------------------------------------------------------
    //--------------------------------------------------------------------------
    // ---- /home/harald/NetBeansProjects/temp/Boismortier Duette/27_Doucement.mid
    new ZipItem(
    new File(tempDir, "Boismortier Duette/27_Doucement.mid"),
    "Boismortier_Duette/27_Doucement.mid",
    false),
    // ---- 
    new ZipItem(
    new File(tempDir, "Boismortier Duette/27_Doucement.xml"),
    "Boismortier_Duette/27_Doucement.xml",
    true),
    // ---- /home/harald/NetBeansProjects/temp/Boismortier Duette/28_Rigaudon_1.mid
    new ZipItem(
    new File(tempDir, "Boismortier Duette/28_Rigaudon_1.mid"),
    "Boismortier_Duette/28_Rigaudon_1.mid",
    false),
    // ---- 
    new ZipItem(
    new File(tempDir, "Boismortier Duette/28_Rigaudon_1.xml"),
    "Boismortier_Duette/28_Rigaudon_1.xml",
    true),
    // ---- /home/harald/NetBeansProjects/temp/Boismortier Duette/29_Rigaudon.mid
    new ZipItem(
    new File(tempDir, "Boismortier Duette/29_Rigaudon.mid"),
    "Boismortier_Duette/29_Rigaudon.mid",
    false),
    // ---- 
    new ZipItem(
    new File(tempDir, "Boismortier Duette/29_Rigaudon.xml"),
    "Boismortier_Duette/29_Rigaudon.xml",
    true),};

  private CreateDuetteContainer() {

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
    CreateDuetteContainer processor = new CreateDuetteContainer();
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
