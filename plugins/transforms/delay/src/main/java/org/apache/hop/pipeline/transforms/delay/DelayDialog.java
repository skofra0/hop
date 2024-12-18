/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hop.pipeline.transforms.delay;

import org.apache.hop.core.util.Utils;
import org.apache.hop.core.variables.IVariables;
import org.apache.hop.i18n.BaseMessages;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.ui.core.PropsUi;
import org.apache.hop.ui.core.dialog.BaseDialog;
import org.apache.hop.ui.core.widget.LabelTextVar;
import org.apache.hop.ui.pipeline.transform.BaseTransformDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class DelayDialog extends BaseTransformDialog {
  private static final Class<?> PKG = DelayMeta.class;

  private final DelayMeta input;
  private CCombo wScaleTime;

  private LabelTextVar wTimeout;

  public DelayDialog(
      Shell parent, IVariables variables, DelayMeta transformMeta, PipelineMeta pipelineMeta) {
    super(parent, variables, transformMeta, pipelineMeta);
    input = transformMeta;
  }

  @Override
  public String open() {
    Shell parent = getParent();

    shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX);
    PropsUi.setLook(shell);
    setShellImage(shell, input);

    ModifyListener lsMod = e -> input.setChanged();
    changed = input.hasChanged();

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = PropsUi.getFormMargin();
    formLayout.marginHeight = PropsUi.getFormMargin();

    shell.setLayout(formLayout);
    shell.setText(BaseMessages.getString(PKG, "DelayDialog.Shell.Title"));

    int middle = props.getMiddlePct();
    int margin = PropsUi.getMargin();

    // TransformName line
    wlTransformName = new Label(shell, SWT.RIGHT);
    wlTransformName.setText(BaseMessages.getString(PKG, "DelayDialog.TransformName.Label"));
    PropsUi.setLook(wlTransformName);
    fdlTransformName = new FormData();
    fdlTransformName.left = new FormAttachment(0, 0);
    fdlTransformName.right = new FormAttachment(middle, -margin);
    fdlTransformName.top = new FormAttachment(0, margin);
    wlTransformName.setLayoutData(fdlTransformName);
    wTransformName = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    wTransformName.setText(transformName);
    PropsUi.setLook(wTransformName);
    wTransformName.addModifyListener(lsMod);
    fdTransformName = new FormData();
    fdTransformName.left = new FormAttachment(middle, 0);
    fdTransformName.top = new FormAttachment(0, margin);
    fdTransformName.right = new FormAttachment(100, 0);
    wTransformName.setLayoutData(fdTransformName);

    // Timeout line
    wTimeout =
        new LabelTextVar(
            variables,
            shell,
            BaseMessages.getString(PKG, "DelayDialog.Timeout.Label"),
            BaseMessages.getString(PKG, "DelayDialog.Timeout.Tooltip"));
    PropsUi.setLook(wTimeout);
    wTimeout.addModifyListener(lsMod);
    FormData fdTimeout = new FormData();
    fdTimeout.left = new FormAttachment(0, 0);
    fdTimeout.top = new FormAttachment(wTransformName, margin);
    fdTimeout.right = new FormAttachment(100, 0);
    wTimeout.setLayoutData(fdTimeout);

    // Whenever something changes, set the tooltip to the expanded version:
    wTimeout.addModifyListener(e -> wTimeout.setToolTipText(variables.resolve(wTimeout.getText())));

    wScaleTime = new CCombo(shell, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
    wScaleTime.add(BaseMessages.getString(PKG, "DelayDialog.MSScaleTime.Label"));
    wScaleTime.add(BaseMessages.getString(PKG, "DelayDialog.SScaleTime.Label"));
    wScaleTime.add(BaseMessages.getString(PKG, "DelayDialog.MnScaleTime.Label"));
    wScaleTime.add(BaseMessages.getString(PKG, "DelayDialog.HrScaleTime.Label"));
    wScaleTime.select(0); // +1: starts at -1
    PropsUi.setLook(wScaleTime);
    FormData fdScaleTime = new FormData();
    fdScaleTime.left = new FormAttachment(middle, 0);
    fdScaleTime.top = new FormAttachment(wTimeout, margin);
    fdScaleTime.right = new FormAttachment(100, 0);
    wScaleTime.setLayoutData(fdScaleTime);
    wScaleTime.addModifyListener(lsMod);

    // Some buttons
    wOk = new Button(shell, SWT.PUSH);
    wOk.setText(BaseMessages.getString(PKG, "System.Button.OK"));
    wCancel = new Button(shell, SWT.PUSH);
    wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));

    setButtonPositions(new Button[] {wOk, wCancel}, margin, wScaleTime);

    // Add listeners
    wCancel.addListener(SWT.Selection, e -> cancel());
    wOk.addListener(SWT.Selection, e -> ok());

    getData();

    BaseDialog.defaultShellHandling(shell, c -> ok(), c -> cancel());

    return transformName;
  }

  /** Copy information from the meta-data input to the dialog fields. */
  public void getData() {
    if (input.getTimeout() != null) {
      wTimeout.setText(input.getTimeout());
    }
    wScaleTime.select(input.getScaleTimeCode());
    wTransformName.selectAll();
    wTransformName.setFocus();
  }

  private void cancel() {
    transformName = null;
    input.setChanged(changed);
    dispose();
  }

  private void ok() {
    if (Utils.isEmpty(wTransformName.getText())) {
      return;
    }
    transformName = wTransformName.getText(); // return value
    input.setTimeout(wTimeout.getText());
    input.setScaleTimeCode(wScaleTime.getSelectionIndex());
    dispose();
  }
}
