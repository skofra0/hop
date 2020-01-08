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

package org.apache.hop.trans.steps.loadfileinput;

import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.apache.hop.core.Const;
import org.apache.hop.core.HopClientEnvironment;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.fileinput.FileInputList;
import org.apache.hop.core.plugins.PluginRegistry;
import org.apache.hop.core.plugins.StepPluginType;
import org.apache.hop.core.row.RowMetaInterface;
import org.apache.hop.core.row.ValueMetaInterface;
import org.apache.hop.core.row.value.ValueMetaBinary;
import org.apache.hop.core.row.value.ValueMetaString;
import org.apache.hop.core.variables.VariableSpace;
import org.apache.hop.junit.rules.RestoreHopEngineEnvironment;
import org.apache.hop.trans.Trans;
import org.apache.hop.trans.TransMeta;
import org.apache.hop.trans.step.StepDataInterface;
import org.apache.hop.trans.step.StepMeta;
import org.apache.hop.trans.step.StepMetaInterface;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.nio.charset.Charset;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

public class LoadFileInputTest {
  @ClassRule public static RestoreHopEngineEnvironment env = new RestoreHopEngineEnvironment();

  private FileSystemManager fs;
  private String filesPath;

  private String transName;
  private TransMeta transMeta;
  private Trans trans;

  private LoadFileInputMeta stepMetaInterface;
  private StepDataInterface stepDataInterface;
  private StepMeta stepMeta;
  private FileInputList stepInputFiles;
  private int stepCopyNr;

  private LoadFileInput stepLoadFileInput;

  private StepMetaInterface runtimeSMI;
  private StepDataInterface runtimeSDI;
  private LoadFileInputField inputField;
  private static String wasEncoding;

  @BeforeClass
  public static void setupBeforeClass() throws HopException {
    if ( Const.isWindows() ) {
      wasEncoding = System.getProperty( "file.encoding" );
      fiddleWithDefaultCharset( "utf8" );
    }
    HopClientEnvironment.init();
  }

  @AfterClass
  public static void teardownAfterClass() {
    if ( wasEncoding != null ) {
      fiddleWithDefaultCharset( wasEncoding );
    }
  }

  // Yeah, I don't like it much either, but it lets me set file.encoding after
  // the VM has fired up. Remove this code when the backlog ticket BACKLOG-20800 gets fixed.
  private static void fiddleWithDefaultCharset( String fiddleValue ) {
    try {
      Class<Charset> charSet = Charset.class;
      Field defaultCharsetFld = charSet.getDeclaredField( "defaultCharset" );
      defaultCharsetFld.setAccessible( true );
      defaultCharsetFld.set( null, Charset.forName( fiddleValue ) );
    } catch ( Exception ex ) {
      System.out.println( "*** Fiddling with Charset class failed" );
    }
  }

  @Before
  public void setup() throws FileSystemException {
    fs = VFS.getManager();
    filesPath = '/' + this.getClass().getPackage().getName().replace( '.', '/' ) + "/files/";

    transName = "LoadFileInput";
    transMeta = new TransMeta();
    transMeta.setName( transName );
    trans = new Trans( transMeta );

    stepMetaInterface = spy( new LoadFileInputMeta() );
    stepInputFiles = new FileInputList();
    Mockito.doReturn( stepInputFiles ).when( stepMetaInterface ).getFiles( any( VariableSpace.class ) );
    String stepId = PluginRegistry.getInstance().getPluginId( StepPluginType.class, stepMetaInterface );
    stepMeta = new StepMeta( stepId, "Load File Input", stepMetaInterface );
    transMeta.addStep( stepMeta );

    stepDataInterface = new LoadFileInputData();

    stepCopyNr = 0;

    stepLoadFileInput = new LoadFileInput( stepMeta, stepDataInterface, stepCopyNr, transMeta, trans );

    assertSame( stepMetaInterface, stepMeta.getStepMetaInterface() );

    runtimeSMI = stepMetaInterface;
    runtimeSDI = runtimeSMI.getStepData();

    inputField = new LoadFileInputField();
    ( (LoadFileInputMeta) runtimeSMI ).setInputFields( new LoadFileInputField[] { inputField } );
    stepLoadFileInput.init( runtimeSMI, runtimeSDI );
  }

  private FileObject getFile( final String filename ) {
    try {
      return fs.resolveFile( this.getClass().getResource( filesPath + filename ) );
    } catch ( Exception e ) {
      throw new RuntimeException( "fail. " + e.getMessage(), e );
    }
  }

  @Test
  public void testOpenNextFile_noFiles() {
    assertFalse( stepMetaInterface.isIgnoreEmptyFile() ); // ensure default value

    assertFalse( stepLoadFileInput.openNextFile() );
  }

  @Test
  public void testOpenNextFile_noFiles_ignoreEmpty() {
    stepMetaInterface.setIgnoreEmptyFile( true );

    assertFalse( stepLoadFileInput.openNextFile() );
  }

  @Test
  public void testOpenNextFile_0() {
    assertFalse( stepMetaInterface.isIgnoreEmptyFile() ); // ensure default value

    stepInputFiles.addFile( getFile( "input0.txt" ) );

    assertTrue( stepLoadFileInput.openNextFile() );
    assertFalse( stepLoadFileInput.openNextFile() );
  }

  @Test
  public void testOpenNextFile_0_ignoreEmpty() {
    stepMetaInterface.setIgnoreEmptyFile( true );

    stepInputFiles.addFile( getFile( "input0.txt" ) );

    assertFalse( stepLoadFileInput.openNextFile() );
  }

  @Test
  public void testOpenNextFile_000() {
    assertFalse( stepMetaInterface.isIgnoreEmptyFile() ); // ensure default value

    stepInputFiles.addFile( getFile( "input0.txt" ) );
    stepInputFiles.addFile( getFile( "input0.txt" ) );
    stepInputFiles.addFile( getFile( "input0.txt" ) );

    assertTrue( stepLoadFileInput.openNextFile() );
    assertTrue( stepLoadFileInput.openNextFile() );
    assertTrue( stepLoadFileInput.openNextFile() );
    assertFalse( stepLoadFileInput.openNextFile() );

  }

  @Test
  public void testOpenNextFile_000_ignoreEmpty() {
    stepMetaInterface.setIgnoreEmptyFile( true );

    stepInputFiles.addFile( getFile( "input0.txt" ) );
    stepInputFiles.addFile( getFile( "input0.txt" ) );
    stepInputFiles.addFile( getFile( "input0.txt" ) );

    assertFalse( stepLoadFileInput.openNextFile() );
  }

  @Test
  public void testOpenNextFile_10() {
    assertFalse( stepMetaInterface.isIgnoreEmptyFile() ); // ensure default value

    stepInputFiles.addFile( getFile( "input1.txt" ) );
    stepInputFiles.addFile( getFile( "input0.txt" ) );

    assertTrue( stepLoadFileInput.openNextFile() );
    assertTrue( stepLoadFileInput.openNextFile() );
    assertFalse( stepLoadFileInput.openNextFile() );
  }

  @Test
  public void testOpenNextFile_10_ignoreEmpty() {
    stepMetaInterface.setIgnoreEmptyFile( true );

    stepInputFiles.addFile( getFile( "input1.txt" ) );
    stepInputFiles.addFile( getFile( "input0.txt" ) );

    assertTrue( stepLoadFileInput.openNextFile() );
    assertFalse( stepLoadFileInput.openNextFile() );
  }


  @Test
  public void testOpenNextFile_01() {
    assertFalse( stepMetaInterface.isIgnoreEmptyFile() ); // ensure default value

    stepInputFiles.addFile( getFile( "input0.txt" ) );
    stepInputFiles.addFile( getFile( "input1.txt" ) );

    assertTrue( stepLoadFileInput.openNextFile() );
    assertTrue( stepLoadFileInput.openNextFile() );
    assertFalse( stepLoadFileInput.openNextFile() );
  }

  @Test
  public void testOpenNextFile_01_ignoreEmpty() {
    stepMetaInterface.setIgnoreEmptyFile( true );

    stepInputFiles.addFile( getFile( "input0.txt" ) );
    stepInputFiles.addFile( getFile( "input1.txt" ) );

    assertTrue( stepLoadFileInput.openNextFile() );
    assertFalse( stepLoadFileInput.openNextFile() );
  }

  @Test
  public void testOpenNextFile_010() {
    assertFalse( stepMetaInterface.isIgnoreEmptyFile() ); // ensure default value

    stepInputFiles.addFile( getFile( "input0.txt" ) );
    stepInputFiles.addFile( getFile( "input1.txt" ) );
    stepInputFiles.addFile( getFile( "input0.txt" ) );

    assertTrue( stepLoadFileInput.openNextFile() );
    assertTrue( stepLoadFileInput.openNextFile() );
    assertTrue( stepLoadFileInput.openNextFile() );
    assertFalse( stepLoadFileInput.openNextFile() );
  }

  @Test
  public void testOpenNextFile_010_ignoreEmpty() {
    stepMetaInterface.setIgnoreEmptyFile( true );

    stepInputFiles.addFile( getFile( "input0.txt" ) );
    stepInputFiles.addFile( getFile( "input1.txt" ) );
    stepInputFiles.addFile( getFile( "input0.txt" ) );

    assertTrue( stepLoadFileInput.openNextFile() );
    assertFalse( stepLoadFileInput.openNextFile() );
  }

  @Test
  public void testGetOneRow() throws Exception {
    // string without specified encoding
    stepInputFiles.addFile( getFile( "input1.txt" ) );

    assertNotNull( stepLoadFileInput.getOneRow() );
    assertEquals( "input1 - not empty", new String( stepLoadFileInput.data.filecontent ) );
  }

  @Test
  public void testUTF8Encoding() throws HopException, FileSystemException {
    stepMetaInterface.setIncludeFilename( true );
    stepMetaInterface.setFilenameField( "filename" );
    stepMetaInterface.setIncludeRowNumber( true );
    stepMetaInterface.setRowNumberField( "rownumber" );
    stepMetaInterface.setShortFileNameField( "shortname" );
    stepMetaInterface.setExtensionField( "extension" );
    stepMetaInterface.setPathField( "path" );
    stepMetaInterface.setIsHiddenField( "hidden" );
    stepMetaInterface.setLastModificationDateField( "lastmodified" );
    stepMetaInterface.setUriField( "uri" );
    stepMetaInterface.setRootUriField( "root uri" );

    // string with UTF-8 encoding
    ( (LoadFileInputMeta) runtimeSMI ).setEncoding( "UTF-8" );
    stepInputFiles.addFile( getFile( "UTF-8.txt" ) );
    Object[] result = stepLoadFileInput.getOneRow();
    assertEquals( " UTF-8 string ÕÕÕ€ ", result[ 0 ] );
    assertEquals( 1L, result[ 2 ] );
    assertEquals( "UTF-8.txt", result[ 3 ] );
    assertEquals( "txt", result[ 4 ] );
    assertEquals( false, result[ 6 ] );
    assertEquals( getFile( "UTF-8.txt" ).getURL().toString(), result[ 8 ] );
    assertEquals( getFile( "UTF-8.txt" ).getName().getRootURI(), result[ 9 ] );
  }

  @Test
  public void testUTF8TrimLeft() throws HopException {
    ( (LoadFileInputMeta) runtimeSMI ).setEncoding( "UTF-8" );
    inputField.setTrimType( ValueMetaInterface.TRIM_TYPE_LEFT );
    stepInputFiles.addFile( getFile( "UTF-8.txt" ) );
    assertEquals( "UTF-8 string ÕÕÕ€ ", stepLoadFileInput.getOneRow()[ 0 ] );
  }

  @Test
  public void testUTF8TrimRight() throws HopException {
    ( (LoadFileInputMeta) runtimeSMI ).setEncoding( "UTF-8" );
    inputField.setTrimType( ValueMetaInterface.TRIM_TYPE_RIGHT );
    stepInputFiles.addFile( getFile( "UTF-8.txt" ) );
    assertEquals( " UTF-8 string ÕÕÕ€", stepLoadFileInput.getOneRow()[ 0 ] );
  }

  @Test
  public void testUTF8Trim() throws HopException {
    ( (LoadFileInputMeta) runtimeSMI ).setEncoding( "UTF-8" );
    inputField.setTrimType( ValueMetaInterface.TRIM_TYPE_BOTH );
    stepInputFiles.addFile( getFile( "UTF-8.txt" ) );
    assertEquals( "UTF-8 string ÕÕÕ€", stepLoadFileInput.getOneRow()[ 0 ] );
  }

  @Test
  public void testWindowsEncoding() throws HopException {
    ( (LoadFileInputMeta) runtimeSMI ).setEncoding( "Windows-1252" );
    inputField.setTrimType( ValueMetaInterface.TRIM_TYPE_NONE );
    stepInputFiles.addFile( getFile( "Windows-1252.txt" ) );
    assertEquals( " Windows-1252 string ÕÕÕ€ ", stepLoadFileInput.getOneRow()[ 0 ] );
  }

  @Test
  public void testWithNoEncoding() throws HopException, UnsupportedEncodingException {
    // string with Windows-1252 encoding but with no encoding set
    ( (LoadFileInputMeta) runtimeSMI ).setEncoding( null );
    stepInputFiles.addFile( getFile( "Windows-1252.txt" ) );
    assertNotEquals( " Windows-1252 string ÕÕÕ€ ", stepLoadFileInput.getOneRow()[ 0 ] );
    assertEquals( " Windows-1252 string ÕÕÕ€ ", new String( stepLoadFileInput.data.filecontent, "Windows-1252" ) );
  }

  @Test
  public void testByteArray() throws Exception {
    RowMetaInterface mockedRowMetaInterface = mock( RowMetaInterface.class );
    stepLoadFileInput.data.outputRowMeta = mockedRowMetaInterface;
    stepLoadFileInput.data.convertRowMeta = mockedRowMetaInterface;
    Mockito.doReturn( new ValueMetaString() ).when( mockedRowMetaInterface ).getValueMeta( anyInt() );

    // byte array
    Mockito.doReturn( new ValueMetaBinary() ).when( mockedRowMetaInterface ).getValueMeta( anyInt() );
    ( (LoadFileInputMeta) runtimeSMI ).setEncoding( "UTF-8" );
    stepInputFiles.addFile( getFile( "pentaho_splash.png" ) );
    inputField = new LoadFileInputField();
    inputField.setType( ValueMetaInterface.TYPE_BINARY );
    ( (LoadFileInputMeta) runtimeSMI ).setInputFields( new LoadFileInputField[] { inputField } );

    assertNotNull( stepLoadFileInput.getOneRow() );
    assertArrayEquals( IOUtils.toByteArray( getFile( "pentaho_splash.png" ).getContent().getInputStream() ), stepLoadFileInput.data.filecontent );
  }

  @Test
  public void testCopyOrCloneArrayFromLoadFileWithSmallerSizedReadRowArray() {
    int size = 5;
    Object[] rowData = new Object[ size ];
    Object[] readrow = new Object[ size - 1 ];
    LoadFileInput loadFileInput = mock( LoadFileInput.class );

    Mockito.when( loadFileInput.copyOrCloneArrayFromLoadFile( rowData, readrow ) ).thenCallRealMethod();

    assertEquals( 5, loadFileInput.copyOrCloneArrayFromLoadFile( rowData, readrow ).length );
  }

  @Test
  public void testCopyOrCloneArrayFromLoadFileWithBiggerSizedReadRowArray() {
    int size = 5;
    Object[] rowData = new Object[ size ];
    Object[] readrow = new Object[ size + 1 ];
    LoadFileInput loadFileInput = mock( LoadFileInput.class );

    Mockito.when( loadFileInput.copyOrCloneArrayFromLoadFile( rowData, readrow ) ).thenCallRealMethod();

    assertEquals( 6, loadFileInput.copyOrCloneArrayFromLoadFile( rowData, readrow ).length );
  }

  @Test
  public void testCopyOrCloneArrayFromLoadFileWithSameSizedReadRowArray() {
    int size = 5;
    Object[] rowData = new Object[ size ];
    Object[] readrow = new Object[ size ];
    LoadFileInput loadFileInput = mock( LoadFileInput.class );

    Mockito.when( loadFileInput.copyOrCloneArrayFromLoadFile( rowData, readrow ) ).thenCallRealMethod();

    assertEquals( 5, loadFileInput.copyOrCloneArrayFromLoadFile( rowData, readrow ).length );
  }

}
