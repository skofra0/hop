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
package org.apache.hop.ui.hopgui.styled.rap;

import org.apache.hop.ui.hopgui.styled.IStyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;

// DEEM-MOD
public class WrappedStyledText implements IStyledText {

  protected Text wrapped;

  public WrappedStyledText(Composite parent, int style) {
    wrapped = new Text(parent, style);
  }

  public Text getWrappedText() {
    return wrapped;
  }

  @Override
  public Widget getWidget() {
    return wrapped;
  }

  @Override
  public void setLayoutData(Object layoutData) {
    wrapped.setLayoutData(layoutData);
  }

  @Override
  public void setText(String string) {
    wrapped.setText(string);
  }

  @Override
  public boolean isDisposed() {
    return wrapped.isDisposed();
  }

  @Override
  public String getText() {
    return wrapped.getText();
  }

  @Override
  public String getSelectionText() {
    return wrapped.getSelectionText();
  }
}
