/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.hop.databases.sybase;

import org.apache.hop.core.database.DatabaseMeta;
import org.apache.hop.core.database.DatabaseMetaPlugin;
import org.apache.hop.core.gui.plugin.GuiElementType;
import org.apache.hop.core.gui.plugin.GuiWidgetElement;

@DatabaseMetaPlugin(type = "DEEM_SYBASE", typeDescription = "Deem Sybase")
public class DeemSybaseDatabaseMeta extends SybaseDatabaseMeta {

  @GuiWidgetElement(
      id = "servername",
      order = "10",
      parentId = DatabaseMeta.GUI_PLUGIN_ELEMENT_PARENT_ID,
      type = GuiElementType.TEXT,
      variables = true,
      label = "i18n:org.apache.hop.ui.core.database:DatabaseDialog.label.Servername")
  protected boolean servername;

  
  @Override
  public int[] getAccessTypeList() {
    return new int[] {DatabaseMeta.TYPE_ACCESS_NATIVE};
  }

  @Override
  public int getDefaultDatabasePort() {
    if (getAccessType() == DatabaseMeta.TYPE_ACCESS_NATIVE) {
      return 2638; // Slettvoll:8095
    }
    return -1;
  }

  @Override
  public String getDriverClass() {
    return "sap.jdbc4.sqlanywhere.IDriver";
  }

  @Override
  public String getURL(String hostname, String port, String databaseName) {
    // return "jdbc:sqlanywhere:eng=" + databaseName + ";database=" + databaseName + ";links=tcpip(host=" + hostname + ":" + port + ")"
    return "jdbc:sqlanywhere:ServerName=" + getServername() + ";DatabaseName=" + databaseName + ";Host=" + hostname + ":" + port ;
  }

}