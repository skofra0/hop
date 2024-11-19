/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.hop.ui.hopgui.styled.rpc;

import org.apache.hop.core.variables.IVariables;
import org.apache.hop.ui.core.widget.TextComposite;
import org.apache.hop.ui.core.widget.highlight.SQLValuesHighlight;
import org.apache.hop.ui.hopgui.styled.IStyledTextComp;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Composite;

// DEEM-MOD
public class WrappedStyledTextComp implements IStyledTextComp {

  protected StyledTextComp2 wrapped;

  public WrappedStyledTextComp(IVariables variables, Composite parent, int args) {
    wrapped = new StyledTextComp2(variables, parent, args);
  }

  @Override
  public TextComposite getWrapped() {
    return wrapped;
  }

  @Override
  public void addModifyListener(ModifyListener lsMod) {
    wrapped.addModifyListener(lsMod);
  }

  @Override
  public void setText(String text) {
    wrapped.setText(text);
  }

  @Override
  public String getText() {
    return wrapped.getText();
  }

  @Override
  public int getLineNumber() {
    return wrapped.getLineNumber();
  }

  @Override
  public int getColumnNumber() {
    return wrapped.getColumnNumber();
  }

  @Override
  public void setToolTipText(String string) {
    wrapped.setToolTipText(string);
  }

  @Override
  public String getSelectionText() {
    return wrapped.getSelectionText();
  }

  @Override
  public void setSqlValuesHighlight() {
    wrapped.addLineStyleListener(new SQLValuesHighlight());
  }

  @Override
  public void setLayoutData(FormData fdSql) {
    wrapped.setLayoutData(fdSql);
  }

  @Override
  public void addKeyListener(KeyAdapter keyAdapter) {
    wrapped.addKeyListener(keyAdapter);
  }

  @Override
  public void addFocusListener(FocusAdapter focusAdapter) {
    wrapped.addFocusListener(focusAdapter);
  }

  @Override
  public void addMouseListener(MouseAdapter mouseAdapter) {
    wrapped.addMouseListener(mouseAdapter);
  }
}