/*
 * Copyright 2013 Harald <Harald at free-creations.de>.
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
package de.free_creations.importexport;

import java.io.File;
import java.util.logging.Handler;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Harald <Harald at free-creations.de>
 */
public class MetronomeCreatorTest {
  
  public MetronomeCreatorTest() {
  }
  
  @BeforeClass
  public static void setUpClass() {
  }
  
  @AfterClass
  public static void tearDownClass() {
  }
  
  @Before
  public void setUp() {
  }
  
  @After
  public void tearDown() {
  }



  /**
   * Test of process method, of class MetronomeCreator.
   */
  @Test
  public void testProcess() throws Exception {
    System.out.println("process");
        Sequence inputSequence = MidiSystem.getSequence(new File("/home/harald/rubbish/testIn.mid"));

    Handler loggingHandler = null;
    Sequence result = MetronomeCreator.process(inputSequence, loggingHandler);
    
    MidiSystem.write(result, 1, new File("/home/harald/rubbish/testOut.mid"));

  }
}