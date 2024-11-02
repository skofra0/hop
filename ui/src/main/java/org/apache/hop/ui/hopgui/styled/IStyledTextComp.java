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
package org.apache.hop.ui.hopgui.styled;

import org.apache.hop.core.variables.IVariables;
import org.apache.hop.ui.core.widget.TextComposite;
import org.apache.hop.ui.util.EnvironmentUtils;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Composite;

// DEEM-MOD
public interface IStyledTextComp {

  TextComposite getWrapped();

  void addModifyListener(ModifyListener lsMod);

  void setText(String text);

  String getText();

  int getLineNumber();

  int getColumnNumber();

  void setToolTipText(String string);

  String getSelectionText();

  void setSqlValuesHighlight();

  void setLayoutData(FormData fdSql);

  void addKeyListener(KeyAdapter keyAdapter);

  void addFocusListener(FocusAdapter focusAdapter);

  void addMouseListener(MouseAdapter mouseAdapter);

  public static IStyledTextComp of(IVariables variables, Composite parent, int args) {
    if (EnvironmentUtils.getInstance().isWeb()) {
      return new org.apache.hop.ui.hopgui.styled.rap.WrappedStyledTextComp(variables, parent, args);
    } else {
      return new org.apache.hop.ui.hopgui.styled.rpc.WrappedStyledTextComp(variables, parent, args);
    }
  }
}
