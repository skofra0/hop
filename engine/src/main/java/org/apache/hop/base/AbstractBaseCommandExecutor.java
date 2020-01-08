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

package org.apache.hop.base;

import org.apache.hop.core.Const;
import org.apache.hop.core.Result;
import org.apache.hop.core.logging.LogChannelInterface;
import org.apache.hop.core.util.Utils;
import org.apache.hop.i18n.BaseMessages;
import org.apache.hop.metastore.MetaStoreConst;
import org.apache.hop.metastore.api.exceptions.MetaStoreException;
import org.apache.hop.metastore.stores.delegate.DelegatingMetaStore;
import org.apache.hop.version.BuildVersion;

import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class AbstractBaseCommandExecutor {

  private SimpleDateFormat dateFormat = new SimpleDateFormat( "yyyy/MM/dd HH:mm:ss.SSS" );

  public static final String YES = "Y";

  private LogChannelInterface log;
  private Class<?> pkgClazz;
  DelegatingMetaStore metaStore;

  private Result result = new Result();

  protected Result exitWithStatus( final int exitStatus ) {
    getResult().setExitStatus( exitStatus );
    return getResult();
  }

  public DelegatingMetaStore createDefaultMetastore() throws MetaStoreException {
    DelegatingMetaStore metaStore = new DelegatingMetaStore();
    metaStore.addMetaStore( MetaStoreConst.openLocalPentahoMetaStore() );
    metaStore.setActiveMetaStoreName( metaStore.getName() );
    return metaStore;
  }

  protected void logDebug( final String messageKey ) {
    if ( getLog().isDebug() ) {
      getLog().logDebug( BaseMessages.getString( getPkgClazz(), messageKey ) );
    }
  }

  protected void logDebug( final String messageKey, String... messageTokens ) {
    if ( getLog().isDebug() ) {
      getLog().logDebug( BaseMessages.getString( getPkgClazz(), messageKey, messageTokens ) );
    }
  }

  protected int calculateAndPrintElapsedTime( Date start, Date stop, String startStopMsgTkn, String processingEndAfterMsgTkn,
                                              String processingEndAfterLongMsgTkn, String processingEndAfterLongerMsgTkn,
                                              String processingEndAfterLongestMsgTkn ) {

    String begin = getDateFormat().format( start ).toString();
    String end = getDateFormat().format( stop ).toString();

    getLog().logMinimal( BaseMessages.getString( getPkgClazz(), startStopMsgTkn, begin, end ) );

    long millis = stop.getTime() - start.getTime();
    int seconds = (int) ( millis / 1000 );
    if ( seconds <= 60 ) {
      getLog().logMinimal( BaseMessages.getString( getPkgClazz(), processingEndAfterMsgTkn, String.valueOf( seconds ) ) );
    } else if ( seconds <= 60 * 60 ) {
      int min = ( seconds / 60 );
      int rem = ( seconds % 60 );
      getLog().logMinimal( BaseMessages.getString( getPkgClazz(), processingEndAfterLongMsgTkn, String.valueOf( min ),
        String.valueOf( rem ), String.valueOf( seconds ) ) );
    } else if ( seconds <= 60 * 60 * 24 ) {
      int rem;
      int hour = ( seconds / ( 60 * 60 ) );
      rem = ( seconds % ( 60 * 60 ) );
      int min = rem / 60;
      rem = rem % 60;
      getLog().logMinimal( BaseMessages.getString( getPkgClazz(), processingEndAfterLongerMsgTkn, String.valueOf( hour ),
        String.valueOf( min ), String.valueOf( rem ), String.valueOf( seconds ) ) );
    } else {
      int rem;
      int days = ( seconds / ( 60 * 60 * 24 ) );
      rem = ( seconds % ( 60 * 60 * 24 ) );
      int hour = rem / ( 60 * 60 );
      rem = rem % ( 60 * 60 );
      int min = rem / 60;
      rem = rem % 60;
      getLog().logMinimal( BaseMessages.getString( getPkgClazz(), processingEndAfterLongestMsgTkn, String.valueOf( days ),
        String.valueOf( hour ), String.valueOf( min ), String.valueOf( rem ), String.valueOf( seconds ) ) );
    }

    return seconds;
  }

  protected void printVersion( String kettleVersionMsgTkn ) {
    BuildVersion buildVersion = BuildVersion.getInstance();
    getLog().logBasic( BaseMessages.getString( getPkgClazz(), kettleVersionMsgTkn, buildVersion.getVersion(),
      buildVersion.getRevision(), buildVersion.getBuildDate() ) );
  }

  protected void printParameter( String name, String value, String defaultValue, String description ) {
    if ( Utils.isEmpty( defaultValue ) ) {
      System.out.println( "Parameter: " + name + "=" + Const.NVL( value, "" ) + " : " + Const.NVL( description, "" ) );
    } else {
      System.out.println( "Parameter: " + name + "=" + Const.NVL( value, "" ) + ", default=" + defaultValue + " : " + Const.NVL( description, "" ) );
    }
  }

  public boolean isEnabled( final String value ) {
    return YES.equalsIgnoreCase( value ) || Boolean.parseBoolean( value ); // both are NPE safe, both are case-insensitive
  }

  public LogChannelInterface getLog() {
    return log;
  }

  public void setLog( LogChannelInterface log ) {
    this.log = log;
  }

  public Class<?> getPkgClazz() {
    return pkgClazz;
  }

  public void setPkgClazz( Class<?> pkgClazz ) {
    this.pkgClazz = pkgClazz;
  }

  public DelegatingMetaStore getMetaStore() {
    return metaStore;
  }

  public void setMetaStore( DelegatingMetaStore metaStore ) {
    this.metaStore = metaStore;
  }

  public SimpleDateFormat getDateFormat() {
    return dateFormat;
  }

  public void setDateFormat( SimpleDateFormat dateFormat ) {
    this.dateFormat = dateFormat;
  }

  public Result getResult() {
    return result;
  }

  public void setResult( Result result ) {
    this.result = result;
  }
}
