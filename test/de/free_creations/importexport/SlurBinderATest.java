/*
 * Copyright 2013 harald.
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
import java.net.URL;
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
 * @author harald
 */
public class SlurBinderATest {

  public SlurBinderATest() {
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
   * Test of process method, of class SlurBinderA.
   */
  @Test
  public void testProcess() throws Exception {
    System.out.println("process");


    Sequence inputSequence = MidiSystem.getSequence(new File("/home/harald/rubbish/testIn1.mid"));
    
    long startTick = 0L;
    long endTick = inputSequence.getTickLength();
    
    int noteOverlap = inputSequence.getResolution()/4;
    int minimumRest = inputSequence.getResolution()/2;
    Handler loggingHandler = null;

    Sequence result = SlurBinderA.process(inputSequence, 1, startTick, endTick, noteOverlap, minimumRest, loggingHandler);

    MidiSystem.write(result, 1, new File("/home/harald/rubbish/testOut1.mid"));
  }
}