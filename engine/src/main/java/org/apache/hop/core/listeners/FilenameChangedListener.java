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

package org.apache.hop.core.listeners;

/**
 * A listener that will signal when the filename of an object changes.
 *
 * @author Matt Casters (mcasters@pentaho.org)
 */
public interface FilenameChangedListener {
  /**
   * The method that is executed when the filename of an object changes
   *
   * @param object      The object for which there is a filename change
   * @param oldFilename the old filename
   * @param newFilename the new filename
   */
  public void filenameChanged( Object object, String oldFilename, String newFilename );
}
