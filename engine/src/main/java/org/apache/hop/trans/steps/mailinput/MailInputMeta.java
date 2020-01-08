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

package org.apache.hop.trans.steps.mailinput;

import org.apache.hop.core.CheckResult;
import org.apache.hop.core.CheckResultInterface;
import org.apache.hop.core.Const;
import org.apache.hop.core.encryption.Encr;
import org.apache.hop.core.exception.HopStepException;
import org.apache.hop.core.exception.HopXMLException;
import org.apache.hop.core.row.RowMetaInterface;
import org.apache.hop.core.row.ValueMetaInterface;
import org.apache.hop.core.row.value.ValueMetaBoolean;
import org.apache.hop.core.row.value.ValueMetaDate;
import org.apache.hop.core.row.value.ValueMetaInteger;
import org.apache.hop.core.row.value.ValueMetaString;
import org.apache.hop.core.variables.VariableSpace;
import org.apache.hop.core.xml.XMLHandler;
import org.apache.hop.i18n.BaseMessages;
import org.apache.hop.job.entries.getpop.MailConnectionMeta;
import org.apache.hop.metastore.api.IMetaStore;
import org.apache.hop.trans.Trans;
import org.apache.hop.trans.TransMeta;
import org.apache.hop.trans.step.BaseStepMeta;
import org.apache.hop.trans.step.StepDataInterface;
import org.apache.hop.trans.step.StepInterface;
import org.apache.hop.trans.step.StepMeta;
import org.apache.hop.trans.step.StepMetaInterface;
import org.w3c.dom.Node;

import java.util.List;

public class MailInputMeta extends BaseStepMeta implements StepMetaInterface {
  private static Class<?> PKG = MailInputMeta.class; // for i18n purposes, needed by Translator2!!

  public static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";
  public static int DEFAULT_BATCH_SIZE = 500;

  public int conditionReceivedDate;

  public int valueimaplist;

  private String servername;
  private String username;
  private String password;
  private boolean usessl;
  private String sslport;
  private String firstmails;
  public int retrievemails;
  private boolean delete;
  private String protocol;
  private String imapfirstmails;
  private String imapfolder;
  // search term
  private String senderSearch;
  private boolean notTermSenderSearch;
  private String recipientSearch;
  private String subjectSearch;
  private String receivedDate1;
  private String receivedDate2;
  private boolean notTermSubjectSearch;
  private boolean notTermRecipientSearch;
  private boolean notTermReceivedDateSearch;
  private boolean includesubfolders;
  private boolean useproxy;
  private String proxyusername;
  private String folderfield;
  private boolean usedynamicfolder;
  private String rowlimit;

  /**
   * The fields ...
   */
  private MailInputField[] inputFields;

  private boolean useBatch;
  private String start;
  private String end;

  private Integer batchSize = DEFAULT_BATCH_SIZE;

  private boolean stopOnError;

  public MailInputMeta() {
    super(); // allocate BaseStepMeta
  }

  @Override
  public void loadXML( Node stepnode, IMetaStore metaStore ) throws HopXMLException {
    readData( stepnode );
  }

  public void allocate( int nrfields ) {
    inputFields = new MailInputField[ nrfields ];
  }

  @Override
  public Object clone() {
    MailInputMeta retval = (MailInputMeta) super.clone();
    int nrFields = inputFields.length;
    retval.allocate( nrFields );
    for ( int i = 0; i < nrFields; i++ ) {
      if ( inputFields[ i ] != null ) {
        retval.inputFields[ i ] = (MailInputField) inputFields[ i ].clone();
      }
    }

    return retval;
  }

  private void readData( Node stepnode ) {
    servername = XMLHandler.getTagValue( stepnode, "servername" );
    username = XMLHandler.getTagValue( stepnode, "username" );
    password = Encr.decryptPasswordOptionallyEncrypted( XMLHandler.getTagValue( stepnode, "password" ) );
    usessl = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "usessl" ) );
    sslport = XMLHandler.getTagValue( stepnode, "sslport" );
    retrievemails = Const.toInt( XMLHandler.getTagValue( stepnode, "retrievemails" ), -1 );
    firstmails = XMLHandler.getTagValue( stepnode, "firstmails" );
    delete = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "delete" ) );

    protocol = Const.NVL( XMLHandler.getTagValue( stepnode, "protocol" ), MailConnectionMeta.PROTOCOL_STRING_POP3 );
    valueimaplist =
      MailConnectionMeta.getValueImapListByCode( Const.NVL(
        XMLHandler.getTagValue( stepnode, "valueimaplist" ), "" ) );
    imapfirstmails = XMLHandler.getTagValue( stepnode, "imapfirstmails" );
    imapfolder = XMLHandler.getTagValue( stepnode, "imapfolder" );
    // search term
    senderSearch = XMLHandler.getTagValue( stepnode, "sendersearch" );
    notTermSenderSearch = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "nottermsendersearch" ) );
    recipientSearch = XMLHandler.getTagValue( stepnode, "recipientsearch" );
    notTermRecipientSearch = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "notTermRecipientSearch" ) );
    subjectSearch = XMLHandler.getTagValue( stepnode, "subjectsearch" );
    notTermSubjectSearch = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "nottermsubjectsearch" ) );
    conditionReceivedDate =
      MailConnectionMeta.getConditionByCode( Const.NVL( XMLHandler.getTagValue(
        stepnode, "conditionreceiveddate" ), "" ) );
    notTermReceivedDateSearch =
      "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "nottermreceiveddatesearch" ) );
    receivedDate1 = XMLHandler.getTagValue( stepnode, "receivedDate1" );
    receivedDate2 = XMLHandler.getTagValue( stepnode, "receivedDate2" );
    includesubfolders = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "includesubfolders" ) );
    usedynamicfolder = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "usedynamicfolder" ) );
    folderfield = XMLHandler.getTagValue( stepnode, "folderfield" );
    proxyusername = XMLHandler.getTagValue( stepnode, "proxyusername" );
    useproxy = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "useproxy" ) );
    useBatch = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, Tags.USE_BATCH ) );
    try {
      batchSize = Integer.parseInt( XMLHandler.getTagValue( stepnode, Tags.BATCH_SIZE ) );
    } catch ( NumberFormatException e ) {
      batchSize = DEFAULT_BATCH_SIZE;
    }
    start = XMLHandler.getTagValue( stepnode, Tags.START_MSG );
    end = XMLHandler.getTagValue( stepnode, Tags.END_MSG );
    stopOnError = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, Tags.STOP_ON_ERROR ) );

    rowlimit = XMLHandler.getTagValue( stepnode, "rowlimit" );
    Node fields = XMLHandler.getSubNode( stepnode, "fields" );
    int nrFields = XMLHandler.countNodes( fields, "field" );

    allocate( nrFields );
    for ( int i = 0; i < nrFields; i++ ) {
      Node fnode = XMLHandler.getSubNodeByNr( fields, "field", i );
      inputFields[ i ] = new MailInputField();
      inputFields[ i ].setName( XMLHandler.getTagValue( fnode, "name" ) );
      inputFields[ i ].setColumn( MailInputField.getColumnByCode( XMLHandler.getTagValue( fnode, "column" ) ) );
    }
  }

  @Override
  public void setDefault() {
    servername = null;
    username = null;
    password = null;
    usessl = false;
    sslport = null;
    retrievemails = 0;
    firstmails = null;
    delete = false;
    protocol = MailConnectionMeta.PROTOCOL_STRING_POP3;
    imapfirstmails = "0";
    valueimaplist = MailConnectionMeta.VALUE_IMAP_LIST_ALL;
    imapfolder = null;
    // search term
    senderSearch = null;
    notTermSenderSearch = false;
    notTermRecipientSearch = false;
    notTermSubjectSearch = false;
    receivedDate1 = null;
    receivedDate2 = null;
    notTermReceivedDateSearch = false;
    recipientSearch = null;
    subjectSearch = null;
    includesubfolders = false;
    useproxy = false;
    proxyusername = null;
    folderfield = null;
    usedynamicfolder = false;
    rowlimit = "0";

    batchSize = DEFAULT_BATCH_SIZE;
    useBatch = false;
    start = null;
    end = null;
    stopOnError = true;

    int nrFields = 0;
    allocate( nrFields );

    for ( int i = 0; i < nrFields; i++ ) {
      inputFields[ i ] = new MailInputField( "field" + ( i + 1 ) );
    }

  }

  private static final class Tags {
    static final String USE_BATCH = "useBatch";
    static final String BATCH_SIZE = "batchSize";
    static final String START_MSG = "startMsg";
    static final String END_MSG = "endMsg";
    static final String STOP_ON_ERROR = "stopOnError";
  }

  @Override
  public String getXML() {
    StringBuilder retval = new StringBuilder();
    String tab = "      ";
    retval.append( "      " ).append( XMLHandler.addTagValue( "servername", servername ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "username", username ) );
    retval.append( "      " ).append(
      XMLHandler.addTagValue( "password", Encr.encryptPasswordIfNotUsingVariables( password ) ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "usessl", usessl ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "sslport", sslport ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "retrievemails", retrievemails ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "firstmails", firstmails ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "delete", delete ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "protocol", protocol ) );
    retval.append( "      " ).append(
      XMLHandler.addTagValue( "valueimaplist", MailConnectionMeta.getValueImapListCode( valueimaplist ) ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "imapfirstmails", imapfirstmails ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "imapfolder", imapfolder ) );
    // search term
    retval.append( "      " ).append( XMLHandler.addTagValue( "sendersearch", senderSearch ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "nottermsendersearch", notTermSenderSearch ) );

    retval.append( "      " ).append( XMLHandler.addTagValue( "recipientsearch", recipientSearch ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "notTermRecipientSearch", notTermRecipientSearch ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "subjectsearch", subjectSearch ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "nottermsubjectsearch", notTermSubjectSearch ) );
    retval.append( "      " ).append(
      XMLHandler.addTagValue( "conditionreceiveddate", MailConnectionMeta
        .getConditionDateCode( conditionReceivedDate ) ) );
    retval.append( "      " ).append(
      XMLHandler.addTagValue( "nottermreceiveddatesearch", notTermReceivedDateSearch ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "receiveddate1", receivedDate1 ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "receiveddate2", receivedDate2 ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "includesubfolders", includesubfolders ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "useproxy", useproxy ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "proxyusername", proxyusername ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "usedynamicfolder", usedynamicfolder ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "folderfield", folderfield ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "rowlimit", rowlimit ) );
    retval.append( tab ).append( XMLHandler.addTagValue( Tags.USE_BATCH, useBatch ) );
    retval.append( tab ).append( XMLHandler.addTagValue( Tags.BATCH_SIZE, batchSize ) );
    retval.append( tab ).append( XMLHandler.addTagValue( Tags.START_MSG, start ) );
    retval.append( tab ).append( XMLHandler.addTagValue( Tags.END_MSG, end ) );
    retval.append( tab ).append( XMLHandler.addTagValue( Tags.STOP_ON_ERROR, stopOnError ) );

    /*
     * Describe the fields to read
     */
    retval.append( "    <fields>" ).append( Const.CR );
    for ( int i = 0; i < inputFields.length; i++ ) {
      retval.append( "      <field>" ).append( Const.CR );
      retval.append( "        " ).append( XMLHandler.addTagValue( "name", inputFields[ i ].getName() ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "column", inputFields[ i ].getColumnCode() ) );
      retval.append( "      </field>" ).append( Const.CR );
    }
    retval.append( "    </fields>" ).append( Const.CR );
    return retval.toString();
  }

  @Override
  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta,
                     RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info, VariableSpace space,
                     IMetaStore metaStore ) {
    CheckResult cr;
    // See if we get input...
    if ( input.length > 0 ) {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "MailInputMeta.CheckResult.NoInputExpected" ), stepMeta );
      remarks.add( cr );
    } else {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "MailInputMeta.CheckResult.NoInput" ), stepMeta );
      remarks.add( cr );
    }
  }

  public String getPort() {
    return sslport;
  }

  public void setPort( String sslport ) {
    this.sslport = sslport;
  }

  public void setFirstMails( String firstmails ) {
    this.firstmails = firstmails;
  }

  public String getFirstMails() {
    return firstmails;
  }

  public boolean isIncludeSubFolders() {
    return includesubfolders;
  }

  public void setIncludeSubFolders( boolean includesubfolders ) {
    this.includesubfolders = includesubfolders;
  }

  /**
   * @return Returns the useproxy.
   */
  public boolean isUseProxy() {
    return this.useproxy;
  }

  public void setUseProxy( boolean useprox ) {
    this.useproxy = useprox;
  }

  public void setProxyUsername( String username ) {
    this.proxyusername = username;
  }

  public String getProxyUsername() {
    return this.proxyusername;
  }

  /**
   * @return Returns the usedynamicfolder.
   */
  public boolean isDynamicFolder() {
    return this.usedynamicfolder;
  }

  public void setDynamicFolder( boolean usedynamicfolder ) {
    this.usedynamicfolder = usedynamicfolder;
  }

  public void setRowLimit( String rowlimit ) {
    this.rowlimit = rowlimit;
  }

  public String getRowLimit() {
    return this.rowlimit;
  }

  public void setFolderField( String folderfield ) {
    this.folderfield = folderfield;
  }

  public String getFolderField() {
    return this.folderfield;
  }

  public void setFirstIMAPMails( String firstmails ) {
    this.imapfirstmails = firstmails;
  }

  public String getFirstIMAPMails() {
    return imapfirstmails;
  }

  public void setSenderSearchTerm( String senderSearch ) {
    this.senderSearch = senderSearch;
  }

  public String getSenderSearchTerm() {
    return this.senderSearch;
  }

  public void setNotTermSenderSearch( boolean notTermSenderSearch ) {
    this.notTermSenderSearch = notTermSenderSearch;
  }

  public boolean isNotTermSenderSearch() {
    return this.notTermSenderSearch;
  }

  public void setNotTermSubjectSearch( boolean notTermSubjectSearch ) {
    this.notTermSubjectSearch = notTermSubjectSearch;
  }

  public boolean isNotTermSubjectSearch() {
    return this.notTermSubjectSearch;
  }

  public void setNotTermReceivedDateSearch( boolean notTermReceivedDateSearch ) {
    this.notTermReceivedDateSearch = notTermReceivedDateSearch;
  }

  public boolean isNotTermReceivedDateSearch() {
    return this.notTermReceivedDateSearch;
  }

  public void setNotTermRecipientSearch( boolean notTermRecipientSearch ) {
    this.notTermRecipientSearch = notTermRecipientSearch;
  }

  public boolean isNotTermRecipientSearch() {
    return this.notTermRecipientSearch;
  }

  public void setRecipientSearch( String recipientSearch ) {
    this.recipientSearch = recipientSearch;
  }

  public String getRecipientSearch() {
    return this.recipientSearch;
  }

  public void setSubjectSearch( String subjectSearch ) {
    this.subjectSearch = subjectSearch;
  }

  public String getSubjectSearch() {
    return this.subjectSearch;
  }

  public String getReceivedDate1() {
    return this.receivedDate1;
  }

  public void setReceivedDate1( String inputDate ) {
    this.receivedDate1 = inputDate;
  }

  public String getReceivedDate2() {
    return this.receivedDate2;
  }

  public void setReceivedDate2( String inputDate ) {
    this.receivedDate2 = inputDate;
  }

  public void setConditionOnReceivedDate( int conditionReceivedDate ) {
    this.conditionReceivedDate = conditionReceivedDate;
  }

  public int getConditionOnReceivedDate() {
    return this.conditionReceivedDate;
  }

  public void setServerName( String servername ) {
    this.servername = servername;
  }

  public String getServerName() {
    return servername;
  }

  public void setUserName( String username ) {
    this.username = username;
  }

  public String getUserName() {
    return username;
  }

  /**
   * <li>0 = retrieve all <li>2 = retrieve unread
   *
   * @param nr
   * @see {@link #setValueImapList(int)}
   */
  public void setRetrievemails( int nr ) {
    retrievemails = nr;
  }

  public int getRetrievemails() {
    return this.retrievemails;
  }

  public int getValueImapList() {
    return valueimaplist;
  }

  public void setValueImapList( int value ) {
    this.valueimaplist = value;
  }

  /**
   * @return Returns the input fields.
   */
  public MailInputField[] getInputFields() {
    return inputFields;
  }

  /**
   * @param inputFields The input fields to set.
   */
  public void setInputFields( MailInputField[] inputFields ) {
    this.inputFields = inputFields;
  }

  /**
   * @return Returns the password.
   */
  public String getPassword() {
    return password;
  }

  /**
   * @param delete The delete to set.
   */
  public void setDelete( boolean delete ) {
    this.delete = delete;
  }

  /**
   * @return Returns the delete.
   */
  public boolean getDelete() {
    return delete;
  }

  public String getProtocol() {
    return protocol;
  }

  public void setProtocol( String protocol ) {
    this.protocol = protocol;
  }

  public String getIMAPFolder() {
    return imapfolder;
  }

  public void setIMAPFolder( String folder ) {
    this.imapfolder = folder;
  }

  /**
   * @param usessl The usessl to set.
   */
  public void setUseSSL( boolean usessl ) {
    this.usessl = usessl;
  }

  /**
   * @return Returns the usessl.
   */
  public boolean isUseSSL() {
    return usessl;
  }

  /**
   * @param password The password to set.
   */
  public void setPassword( String password ) {
    this.password = password;
  }

  @Override
  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr,
                                Trans trans ) {
    return new MailInput( stepMeta, stepDataInterface, cnr, tr, trans );
  }

  @Override
  public StepDataInterface getStepData() {
    return new MailInputData();
  }

  @Override
  public void getFields( RowMetaInterface r, String name, RowMetaInterface[] info, StepMeta nextStep,
                         VariableSpace space, IMetaStore metaStore ) throws HopStepException {
    int i;
    for ( i = 0; i < inputFields.length; i++ ) {
      MailInputField field = inputFields[ i ];
      ValueMetaInterface v = new ValueMetaString( space.environmentSubstitute( field.getName() ) );
      switch ( field.getColumn() ) {
        case MailInputField.COLUMN_MESSAGE_NR:
        case MailInputField.COLUMN_SIZE:
        case MailInputField.COLUMN_ATTACHED_FILES_COUNT:
          v = new ValueMetaInteger( space.environmentSubstitute( field.getName() ) );
          v.setLength( ValueMetaInterface.DEFAULT_INTEGER_LENGTH, 0 );
          break;
        case MailInputField.COLUMN_RECEIVED_DATE:
        case MailInputField.COLUMN_SENT_DATE:
          v = new ValueMetaDate( space.environmentSubstitute( field.getName() ) );
          break;
        case MailInputField.COLUMN_FLAG_DELETED:
        case MailInputField.COLUMN_FLAG_DRAFT:
        case MailInputField.COLUMN_FLAG_FLAGGED:
        case MailInputField.COLUMN_FLAG_NEW:
        case MailInputField.COLUMN_FLAG_READ:
          v = new ValueMetaBoolean( space.environmentSubstitute( field.getName() ) );
          break;
        default:
          // STRING
          v.setLength( 250 );
          v.setPrecision( -1 );
          break;
      }
      v.setOrigin( name );
      r.addValueMeta( v );
    }

  }

  public boolean useBatch() {
    return useBatch;
  }

  public Integer getBatchSize() {
    return batchSize;
  }

  public boolean isStopOnError() {
    return stopOnError;
  }

  public void setStopOnError( boolean breakOnError ) {
    this.stopOnError = breakOnError;
  }

  public boolean isUseBatch() {
    return useBatch;
  }

  public void setUseBatch( boolean useBatch ) {
    this.useBatch = useBatch;
  }

  public String getStart() {
    return start;
  }

  public void setStart( String start ) {
    this.start = start;
  }

  public String getEnd() {
    return end;
  }

  public void setEnd( String end ) {
    this.end = end;
  }

  public void setBatchSize( Integer batchSize ) {
    this.batchSize = batchSize;
  }

}
