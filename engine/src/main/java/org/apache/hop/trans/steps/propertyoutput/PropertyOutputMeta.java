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

package org.apache.hop.trans.steps.propertyoutput;

import org.apache.commons.vfs2.FileObject;
import org.apache.hop.core.CheckResult;
import org.apache.hop.core.CheckResultInterface;
import org.apache.hop.core.Const;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.exception.HopXMLException;
import org.apache.hop.core.row.RowMetaInterface;
import org.apache.hop.core.row.ValueMetaInterface;
import org.apache.hop.core.util.Utils;
import org.apache.hop.core.variables.VariableSpace;
import org.apache.hop.core.vfs.HopVFS;
import org.apache.hop.core.xml.XMLHandler;
import org.apache.hop.i18n.BaseMessages;
import org.apache.hop.metastore.api.IMetaStore;
import org.apache.hop.resource.ResourceDefinition;
import org.apache.hop.resource.ResourceNamingInterface;
import org.apache.hop.trans.Trans;
import org.apache.hop.trans.TransMeta;
import org.apache.hop.trans.step.BaseStepMeta;
import org.apache.hop.trans.step.StepDataInterface;
import org.apache.hop.trans.step.StepInterface;
import org.apache.hop.trans.step.StepMeta;
import org.apache.hop.trans.step.StepMetaInterface;
import org.w3c.dom.Node;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Output rows to Properties file and create a file.
 *
 * @author Samatar
 * @since 13-Apr-2008
 */

public class PropertyOutputMeta extends BaseStepMeta implements StepMetaInterface {
  private static Class<?> PKG = PropertyOutputMeta.class; // for i18n purposes, needed by Translator2!!

  private String keyfield;
  private String valuefield;

  private boolean addToResult;

  /**
   * The base name of the output file
   */
  private String fileName;

  /* Specification if file name is in field */

  private boolean fileNameInField;

  private String fileNameField;

  /**
   * The file extention in case of a generated filename
   */
  private String extension;

  /**
   * Flag: add the stepnr in the filename
   */
  private boolean stepNrInFilename;

  /**
   * Flag: add the partition number in the filename
   */
  private boolean partNrInFilename;

  /**
   * Flag: add the date in the filename
   */
  private boolean dateInFilename;

  /**
   * Flag: add the time in the filename
   */
  private boolean timeInFilename;

  /**
   * Flag: create parent folder if needed
   */
  private boolean createparentfolder;

  /**
   * Comment to add in file
   */
  private String comment;

  /**
   * Flag append in file
   **/
  private boolean append;

  @Override
  public void loadXML( Node stepnode, IMetaStore metaStore ) throws HopXMLException {
    readData( stepnode );
  }

  @Override
  public Object clone() {

    PropertyOutputMeta retval = (PropertyOutputMeta) super.clone();
    return retval;

  }

  /**
   * @return Returns the extension.
   */
  public String getExtension() {
    return extension;
  }

  /**
   * @param extension The extension to set.
   */
  public void setExtension( String extension ) {
    this.extension = extension;
  }

  /**
   * @return Returns the fileName.
   */
  public String getFileName() {
    return fileName;
  }

  /**
   * @return Is the file name coded in a field?
   */
  public boolean isFileNameInField() {
    return fileNameInField;
  }

  /**
   * @param fileNameInField Is the file name coded in a field?
   */
  public void setFileNameInField( boolean fileNameInField ) {
    this.fileNameInField = fileNameInField;
  }

  /**
   * @return The field name that contains the output file name.
   */
  public String getFileNameField() {
    return fileNameField;
  }

  /**
   * @param fileNameField Name of the field that contains the file name
   */
  public void setFileNameField( String fileNameField ) {
    this.fileNameField = fileNameField;
  }

  /**
   * @return Returns the stepNrInFilename.
   */
  public boolean isStepNrInFilename() {
    return stepNrInFilename;
  }

  /**
   * @param stepNrInFilename The stepNrInFilename to set.
   */
  public void setStepNrInFilename( boolean stepNrInFilename ) {
    this.stepNrInFilename = stepNrInFilename;
  }

  /**
   * @return Returns the timeInFilename.
   */
  public boolean isTimeInFilename() {
    return timeInFilename;
  }

  /**
   * @return Returns the dateInFilename.
   */
  public boolean isDateInFilename() {
    return dateInFilename;
  }

  /**
   * @param dateInFilename The dateInFilename to set.
   */
  public void setDateInFilename( boolean dateInFilename ) {
    this.dateInFilename = dateInFilename;
  }

  /**
   * @param timeInFilename The timeInFilename to set.
   */
  public void setTimeInFilename( boolean timeInFilename ) {
    this.timeInFilename = timeInFilename;
  }

  /**
   * @param fileName The fileName to set.
   */
  public void setFileName( String fileName ) {
    this.fileName = fileName;
  }

  /**
   * @return Returns the Add to result filesname flag.
   * @deprecated use {@link #isAddToResult()}
   */
  @Deprecated
  public boolean addToResult() {
    return isAddToResult();
  }

  public boolean isAddToResult() {
    return addToResult;
  }

  /**
   * @param addToResult The Add file to result to set.
   */
  public void setAddToResult( boolean addToResult ) {
    this.addToResult = addToResult;
  }

  /**
   * @return Returns the create parent folder flag.
   */
  public boolean isCreateParentFolder() {
    return createparentfolder;
  }

  /**
   * @param createparentfolder The create parent folder flag to set.
   */
  public void setCreateParentFolder( boolean createparentfolder ) {
    this.createparentfolder = createparentfolder;
  }

  /**
   * @return Returns the append flag.
   */
  public boolean isAppend() {
    return append;
  }

  /**
   * @param append The append to set.
   */
  public void setAppend( boolean append ) {
    this.append = append;
  }

  public String getComment() {
    return comment;
  }

  public void setComment( String commentin ) {
    this.comment = commentin;
  }

  public String[] getFiles( VariableSpace space ) {
    int copies = 1;
    int parts = 1;

    if ( stepNrInFilename ) {
      copies = 3;
    }

    if ( partNrInFilename ) {
      parts = 3;
    }

    int nr = copies * parts;
    if ( nr > 1 ) {
      nr++;
    }

    String[] retval = new String[ nr ];

    int i = 0;
    for ( int copy = 0; copy < copies; copy++ ) {
      for ( int part = 0; part < parts; part++ ) {

        retval[ i ] = buildFilename( space, copy );
        i++;

      }
    }
    if ( i < nr ) {
      retval[ i ] = "...";
    }

    return retval;
  }

  public String buildFilename( VariableSpace space, int stepnr ) {

    SimpleDateFormat daf = new SimpleDateFormat();

    // Replace possible environment variables...
    String retval = space.environmentSubstitute( fileName );

    Date now = new Date();

    if ( dateInFilename ) {
      daf.applyPattern( "yyyMMdd" );
      String d = daf.format( now );
      retval += "_" + d;
    }
    if ( timeInFilename ) {
      daf.applyPattern( "HHmmss" );
      String t = daf.format( now );
      retval += "_" + t;
    }
    if ( stepNrInFilename ) {
      retval += "_" + stepnr;
    }

    if ( extension != null && extension.length() != 0 ) {
      retval += "." + extension;
    }

    return retval;
  }

  private void readData( Node stepnode ) throws HopXMLException {
    try {

      keyfield = XMLHandler.getTagValue( stepnode, "keyfield" );
      valuefield = XMLHandler.getTagValue( stepnode, "valuefield" );
      comment = XMLHandler.getTagValue( stepnode, "comment" );

      fileName = XMLHandler.getTagValue( stepnode, "file", "name" );

      createparentfolder =
        "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "file", "create_parent_folder" ) );
      extension = XMLHandler.getTagValue( stepnode, "file", "extention" );
      stepNrInFilename = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "file", "split" ) );
      partNrInFilename = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "file", "haspartno" ) );
      dateInFilename = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "file", "add_date" ) );
      timeInFilename = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "file", "add_time" ) );
      addToResult = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "file", "AddToResult" ) );
      append = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "file", "append" ) );
      fileName = XMLHandler.getTagValue( stepnode, "file", "name" );
      fileNameInField = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "fileNameInField" ) );
      fileNameField = XMLHandler.getTagValue( stepnode, "fileNameField" );

    } catch ( Exception e ) {
      throw new HopXMLException( "Unable to load step info from XML", e );
    }
  }

  @Override
  public void setDefault() {
    append = false;
    createparentfolder = false;
    // Items ...
    keyfield = null;
    valuefield = null;
    comment = null;
  }

  @Override
  public String getXML() {
    StringBuilder retval = new StringBuilder();

    // Items ...

    retval.append( "    " + XMLHandler.addTagValue( "keyfield", keyfield ) );
    retval.append( "    " + XMLHandler.addTagValue( "valuefield", valuefield ) );
    retval.append( "    " + XMLHandler.addTagValue( "comment", comment ) );

    retval.append( "    " + XMLHandler.addTagValue( "fileNameInField", fileNameInField ) );
    retval.append( "    " + XMLHandler.addTagValue( "fileNameField", fileNameField ) );
    retval.append( "    <file>" + Const.CR );

    retval.append( "      " + XMLHandler.addTagValue( "name", fileName ) );
    retval.append( "      " + XMLHandler.addTagValue( "extention", extension ) );
    retval.append( "      " + XMLHandler.addTagValue( "split", stepNrInFilename ) );
    retval.append( "      " + XMLHandler.addTagValue( "haspartno", partNrInFilename ) );
    retval.append( "      " + XMLHandler.addTagValue( "add_date", dateInFilename ) );
    retval.append( "      " + XMLHandler.addTagValue( "add_time", timeInFilename ) );

    retval.append( "      " + XMLHandler.addTagValue( "create_parent_folder", createparentfolder ) );
    retval.append( "    " + XMLHandler.addTagValue( "addtoresult", addToResult ) );
    retval.append( "    " + XMLHandler.addTagValue( "append", append ) );
    retval.append( "      </file>" + Const.CR );

    return retval.toString();
  }

  @Override
  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta,
                     RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info, VariableSpace space,
                     IMetaStore metaStore ) {

    CheckResult cr;
    // Now see what we can find as previous step...
    if ( prev != null && prev.size() > 0 ) {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "PropertyOutputMeta.CheckResult.FieldsReceived", "" + prev.size() ), stepMeta );
      remarks.add( cr );
    } else {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "PropertyOutputMeta.CheckResult.NoFields" ), stepMeta );
      remarks.add( cr );
    }

    // See if we have input streams leading to this step!
    if ( input.length > 0 ) {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "PropertyOutputMeta.CheckResult.ExpectedInputOk" ), stepMeta );
      remarks.add( cr );
    } else {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "PropertyOutputMeta.CheckResult.ExpectedInputError" ), stepMeta );
      remarks.add( cr );
    }

    // Check if filename is given
    if ( !Utils.isEmpty( fileName ) ) {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "PropertyOutputMeta.CheckResult.FilenameOk" ), stepMeta );
      remarks.add( cr );
    } else {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "PropertyOutputMeta.CheckResult.FilenameError" ), stepMeta );
      remarks.add( cr );
    }

    // Check for Key field

    ValueMetaInterface v = prev.searchValueMeta( keyfield );
    if ( v == null ) {
      cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "PropertyOutputMeta.CheckResult.KeyFieldMissing" ), stepMeta );
      remarks.add( cr );
    } else {
      cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "PropertyOutputMeta.CheckResult.KeyFieldOk" ), stepMeta );
      remarks.add( cr );
    }

    // Check for Value field

    v = prev.searchValueMeta( valuefield );
    if ( v == null ) {
      cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "PropertyOutputMeta.CheckResult.ValueFieldMissing" ), stepMeta );
      remarks.add( cr );
    } else {
      cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "PropertyOutputMeta.CheckResult.ValueFieldOk" ), stepMeta );
      remarks.add( cr );
    }

  }

  @Override
  public StepDataInterface getStepData() {
    return new PropertyOutputData();
  }

  /**
   * @return the keyfield
   */
  public String getKeyField() {
    return keyfield;
  }

  /**
   * @return the valuefield
   */
  public String getValueField() {
    return valuefield;
  }

  /**
   * @param KeyField the keyfield to set
   */
  public void setKeyField( String KeyField ) {
    this.keyfield = KeyField;
  }

  /**
   * @param valuefield the valuefield to set
   */
  public void setValueField( String valuefield ) {
    this.valuefield = valuefield;
  }

  @Override
  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr,
                                Trans trans ) {
    return new PropertyOutput( stepMeta, stepDataInterface, cnr, tr, trans );
  }

  @Override
  public boolean supportsErrorHandling() {
    return true;
  }

  /**
   * Since the exported transformation that runs this will reside in a ZIP file, we can't reference files relatively. So
   * what this does is turn the name of files into absolute paths OR it simply includes the resource in the ZIP file.
   * For now, we'll simply turn it into an absolute path and pray that the file is on a shared drive or something like
   * that.
   *
   * @param space                   the variable space to use
   * @param definitions
   * @param resourceNamingInterface
   * @param metaStore               the metaStore in which non-kettle metadata could reside.
   * @return the filename of the exported resource
   */
  @Override
  public String exportResources( VariableSpace space, Map<String, ResourceDefinition> definitions,
                                 ResourceNamingInterface resourceNamingInterface, IMetaStore metaStore ) throws HopException {
    try {
      // The object that we're modifying here is a copy of the original!
      // So let's change the filename from relative to absolute by grabbing the file object...
      //
      // From : ${Internal.Transformation.Filename.Directory}/../foo/bar.data
      // To : /home/matt/test/files/foo/bar.data
      //
      // In case the name of the file comes from previous steps, forget about this!
      if ( !fileNameInField ) {
        FileObject fileObject = HopVFS.getFileObject( space.environmentSubstitute( fileName ), space );

        // If the file doesn't exist, forget about this effort too!
        //
        if ( fileObject.exists() ) {
          // Convert to an absolute path...
          //
          fileName = resourceNamingInterface.nameResource( fileObject, space, true );
          return fileName;
        }
      }
      return null;
    } catch ( Exception e ) {
      throw new HopException( e );
    }
  }

}
