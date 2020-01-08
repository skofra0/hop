/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.apache.hop.trans.steps.scriptvalues_mod;

import org.apache.hop.core.CheckResultInterface;
import org.apache.hop.core.HopEnvironment;
import org.apache.hop.core.RowMetaAndData;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.plugins.PluginRegistry;
import org.apache.hop.core.plugins.StepPluginType;
import org.apache.hop.core.row.ValueMetaInterface;
import org.apache.hop.trans.TransHopMeta;
import org.apache.hop.trans.TransMeta;
import org.apache.hop.trans.step.StepMeta;
import org.apache.hop.trans.steps.dummytrans.DummyTransMeta;
import org.apache.hop.trans.steps.injector.InjectorMeta;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Tests setting the optimization level of ScriptValuesMetaMod. This unit test will call the ScriptValuesMetaMod.check()
 * to test the setting.
 * <p>
 * This class depends on JavaScriptSpecialTest for the creation of test data.
 * <p>
 * The java script optimization is described here: https://developer.mozilla.org/en/Rhino_Optimization
 * <p>
 * If Rhino's error message is changed in future releases then this unit test will break. In that case
 * RANGE_ERROR_MESSAGE_PREFIX will have to be modified accordingly.
 *
 * @author sflatley
 */
public class OptimizationLevelIT {

  /**
   * The error message that Rhino will give when an out or range optimization level is set will start with this.
   * <p>
   * If we update Ketle with a version of Rhino
   */
  private static final String RANGE_ERROR_MESSAGE_PREFIX = "Optimization level outside [-1..9]";

  /**
   * Returns a true of the list of CheckResultInterface contains a message that starts with RANGE_ERROR_MESSAGE_PREFIX.
   *
   * @param checkResultInterfaces
   * @param thisString
   * @return boolean
   */
  private boolean containsErrorMessage( List<CheckResultInterface> checkResultInterfaces ) {

    for ( CheckResultInterface checkResultInterface : checkResultInterfaces ) {
      if ( checkResultInterface.getText().startsWith( RANGE_ERROR_MESSAGE_PREFIX ) ) {
        return true;
      }
    }

    return false;
  }

  /**
   * Runs a test for valid values a a few invalid ones.
   */
  @Test
  public void testOptimizationLevelminus1Through9() {

    // positive tests
    test( "-1", false );
    test( "0", false );
    test( "1", false );
    test( "2", false );
    test( "3", false );
    test( "4", false );
    test( "5", false );
    test( "6", false );
    test( "7", false );
    test( "8", false );
    test( "9", false );

    // negative tests
    test( "-9", true );
    test( "10", true );
  }

  /**
   * Performs the JUnit assertEquals for the passed optimization level. The return value of testOptimizationLevel is
   * asserted with containsErrorMessage.
   *
   * @param optimizationLevel    the optimization level to test
   * @param containsErrorMessage True if the check contains the error message, false if not.
   */
  private void test( String optimizationLevel, boolean containsErrorMessage ) {
    try {
      List<CheckResultInterface> remarks = testOptimizationLevel( optimizationLevel );
      assertEquals( containsErrorMessage( remarks ), containsErrorMessage );
    } catch ( HopException ke ) {
      ke.printStackTrace();
      fail( ke.getMessage() );
    }
  }

  /**
   * Creates the transformation needed to test the java script step with an optimization level set.
   *
   * @param optimizationLevel
   * @return
   * @throws HopException
   */
  private List<CheckResultInterface> testOptimizationLevel( String optimizationLevel ) throws HopException {

    HopEnvironment.init();

    // Create a new transformation...
    TransMeta transMeta = new TransMeta();
    transMeta.setName( "Test optimization level exception handling" );

    PluginRegistry registry = PluginRegistry.getInstance();

    // create an injector step.../
    String injectorStepname = "injector step";
    InjectorMeta im = new InjectorMeta();

    // Set the information of the injector.
    String injectorPid = registry.getPluginId( StepPluginType.class, im );
    StepMeta injectorStep = new StepMeta( injectorPid, injectorStepname, im );
    transMeta.addStep( injectorStep );

    // Create a javascript step
    String javaScriptStepname = "javascript step";

    // Create the meta and populate
    ScriptValuesMetaMod scriptValuesMetaMod = new ScriptValuesMetaMod();
    ScriptValuesScript[] js =
      new ScriptValuesScript[] { new ScriptValuesScript(
        ScriptValuesScript.TRANSFORM_SCRIPT, "script", "var str = string;\n" + "var bool = LuhnCheck(str);" ) };
    scriptValuesMetaMod.setJSScripts( js );
    scriptValuesMetaMod.setFieldname( new String[] { "bool" } );
    scriptValuesMetaMod.setRename( new String[] { "" } );
    scriptValuesMetaMod.setType( new int[] { ValueMetaInterface.TYPE_BOOLEAN } );
    scriptValuesMetaMod.setLength( new int[] { -1 } );
    scriptValuesMetaMod.setPrecision( new int[] { -1 } );
    scriptValuesMetaMod.setReplace( new boolean[] { false } );
    scriptValuesMetaMod.setCompatible( false );
    scriptValuesMetaMod.setOptimizationLevel( optimizationLevel );

    // Create the step meta
    String javaScriptStepPid = registry.getPluginId( StepPluginType.class, scriptValuesMetaMod );
    StepMeta javaScriptStep = new StepMeta( javaScriptStepPid, javaScriptStepname, scriptValuesMetaMod );

    // Create a dummy step
    String dummyStepname = "dummy step";
    DummyTransMeta dm = new DummyTransMeta();
    String dummyPid = registry.getPluginId( StepPluginType.class, dm );
    StepMeta dummyStep = new StepMeta( dummyPid, dummyStepname, dm );
    transMeta.addStep( dummyStep );

    // hop the steps that were created
    TransHopMeta hi2 = new TransHopMeta( javaScriptStep, dummyStep );
    transMeta.addTransHop( hi2 );

    // We use an existing test that creates data: we'll use that data here
    JavaScriptSpecialIT javaScriptSpecialTest = new JavaScriptSpecialIT();
    List<RowMetaAndData> inputList = javaScriptSpecialTest.createData1();
    // RowMetaInterface rowMetaInterface = null;

    // This is the collection of error messages that may be generated
    // and other things that the check method will need
    List<CheckResultInterface> remarks = new ArrayList<CheckResultInterface>();
    String[] input = new String[] { injectorStepname };
    String[] output = new String[] {};

    // We get the row meta and data....
    Iterator<RowMetaAndData> it = inputList.iterator();
    if ( it.hasNext() ) {
      RowMetaAndData rowMetaAndData = it.next();

      // .... and then call the scriptValuesMetaMod's check method
      scriptValuesMetaMod.check(
        remarks, transMeta, javaScriptStep, rowMetaAndData.getRowMeta(), input, output, null, transMeta, null,
        null );
    } else {
      fail( "No data in the inputList" );
    }

    // we then return the remarks made by scriptValuesMetaMod.check(....);
    return remarks;
  }
}
