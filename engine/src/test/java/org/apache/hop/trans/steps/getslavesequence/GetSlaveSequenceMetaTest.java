/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.apache.hop.trans.steps.getslavesequence;

import org.apache.hop.core.exception.HopException;
import org.apache.hop.junit.rules.RestoreHopEngineEnvironment;
import org.apache.hop.trans.steps.loadsave.LoadSaveTester;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetSlaveSequenceMetaTest {
  @ClassRule public static RestoreHopEngineEnvironment env = new RestoreHopEngineEnvironment();

  @Test
  public void testRoundTrip() throws HopException {
    List<String> attributes =
      Arrays.asList( "valuename", "slave", "seqname", "increment" );
    Map<String, String> getterMap = new HashMap<String, String>();
    getterMap.put( "valuename", "getValuename" );
    getterMap.put( "slave", "getSlaveServerName" );
    getterMap.put( "seqname", "getSequenceName" );
    getterMap.put( "increment", "getIncrement" );

    Map<String, String> setterMap = new HashMap<String, String>();
    setterMap.put( "valuename", "setValuename" );
    setterMap.put( "slave", "setSlaveServerName" );
    setterMap.put( "seqname", "setSequenceName" );
    setterMap.put( "increment", "setIncrement" );

    LoadSaveTester tester = new LoadSaveTester( GetSlaveSequenceMeta.class, attributes, getterMap, setterMap );

    tester.testSerialization();
  }
}
