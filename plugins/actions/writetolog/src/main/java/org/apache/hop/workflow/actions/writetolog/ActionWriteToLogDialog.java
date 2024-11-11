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
package org.apache.hop.workflow.actions.writetolog;

import org.apache.hop.core.Const;
import org.apache.hop.core.Props;
import org.apache.hop.core.logging.LogLevel;
import org.apache.hop.core.util.Utils;
import org.apache.hop.core.variables.IVariables;
import org.apache.hop.i18n.BaseMessages;
import org.apache.hop.ui.core.PropsUi;
import org.apache.hop.ui.core.dialog.BaseDialog;
import org.apache.hop.ui.core.dialog.MessageBox;
import org.apache.hop.ui.core.widget.TextVar;
import org.apache.hop.ui.pipeline.transform.BaseTransformDialog;
import org.apache.hop.ui.workflow.action.ActionDialog;
import org.apache.hop.ui.workflow.dialog.WorkflowDialog;
import org.apache.hop.workflow.WorkflowMeta;
import org.apache.hop.workflow.action.IAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/** This dialog allows you to edit a ActionWriteToLog object. */
public class ActionWriteToLogDialog extends ActionDialog {
  private static final Class<?> PKG = ActionWriteToLog.class;
  public static final String CONST_WRITE_TO_LOG_NAME_DEFAULT = "WriteToLog.Name.Default";

  private Text wName;

  private TextVar wLogMessage;

  private ActionWriteToLog action;

  private boolean changed;

  private TextVar wLogSubject;

  private Combo wLoglevel;

  public ActionWriteToLogDialog(
      Shell parent, ActionWriteToLog action, WorkflowMeta workflowMeta, IVariables variables) {
    super(parent, workflowMeta, variables);
    this.action = action;
    if (this.action.getName() == null) {
      this.action.setName(BaseMessages.getString(PKG, CONST_WRITE_TO_LOG_NAME_DEFAULT));
    }
  }

  @Override
  public IAction open() {

    shell = new Shell(getParent(), SWT.DIALOG_TRIM | SWT.MIN | SWT.MAX | SWT.RESIZE);
    shell.setMinimumSize(350, 250);
    PropsUi.setLook(shell);
    WorkflowDialog.setShellImage(shell, action);

    ModifyListener lsMod = e -> action.setChanged();
    changed = action.hasChanged();

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = PropsUi.getFormMargin();
    formLayout.marginHeight = PropsUi.getFormMargin();

    shell.setLayout(formLayout);
    shell.setText(BaseMessages.getString(PKG, "WriteToLog.Title"));

    int middle = props.getMiddlePct();
    int margin = PropsUi.getMargin();

    // Buttons go at the very bottom
    //
    Button wOk = new Button(shell, SWT.PUSH);
    wOk.setText(BaseMessages.getString(PKG, "System.Button.OK"));
    wOk.addListener(SWT.Selection, e -> ok());
    Button wCancel = new Button(shell, SWT.PUSH);
    wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));
    wCancel.addListener(SWT.Selection, e -> cancel());
    BaseTransformDialog.positionBottomButtons(shell, new Button[] {wOk, wCancel}, margin, null);

    // Action name
    Label wlName = new Label(shell, SWT.RIGHT);
    wlName.setText(BaseMessages.getString(PKG, "WriteToLog.ActionName.Label"));
    PropsUi.setLook(wlName);
    FormData fdlName = new FormData();
    fdlName.left = new FormAttachment(0, 0);
    fdlName.right = new FormAttachment(middle, -margin);
    fdlName.top = new FormAttachment(0, margin);
    wlName.setLayoutData(fdlName);
    wName = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    PropsUi.setLook(wName);
    wName.addModifyListener(lsMod);
    FormData fdName = new FormData();
    fdName.left = new FormAttachment(middle, 0);
    fdName.top = new FormAttachment(0, margin);
    fdName.right = new FormAttachment(100, 0);
    wName.setLayoutData(fdName);

    // Log Level
    Label wlLoglevel = new Label(shell, SWT.RIGHT);
    wlLoglevel.setText(BaseMessages.getString(PKG, "WriteToLog.Loglevel.Label"));
    PropsUi.setLook(wlLoglevel);
    FormData fdlLoglevel = new FormData();
    fdlLoglevel.left = new FormAttachment(0, 0);
    fdlLoglevel.right = new FormAttachment(middle, -margin);
    fdlLoglevel.top = new FormAttachment(wName, margin);
    wlLoglevel.setLayoutData(fdlLoglevel);
    wLoglevel = new Combo(shell, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
    wLoglevel.setItems(LogLevel.getLogLevelDescriptions());
    PropsUi.setLook(wLoglevel);
    FormData fdLoglevel = new FormData();
    fdLoglevel.left = new FormAttachment(middle, 0);
    fdLoglevel.top = new FormAttachment(wName, margin);
    fdLoglevel.right = new FormAttachment(100, 0);
    wLoglevel.setLayoutData(fdLoglevel);

    // Log subject
    Label wlLogSubject = new Label(shell, SWT.RIGHT);
    wlLogSubject.setText(BaseMessages.getString(PKG, "WriteToLog.LogSubject.Label"));
    PropsUi.setLook(wlLogSubject);
    FormData fdlLogSubject = new FormData();
    fdlLogSubject.left = new FormAttachment(0, 0);
    fdlLogSubject.top = new FormAttachment(wLoglevel, margin);
    fdlLogSubject.right = new FormAttachment(middle, -margin);
    wlLogSubject.setLayoutData(fdlLogSubject);

    wLogSubject = new TextVar(variables, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    wLogSubject.setText(BaseMessages.getString(PKG, CONST_WRITE_TO_LOG_NAME_DEFAULT));
    PropsUi.setLook(wLogSubject);
    wLogSubject.addModifyListener(lsMod);
    FormData fdLogSubject = new FormData();
    fdLogSubject.left = new FormAttachment(middle, 0);
    fdLogSubject.top = new FormAttachment(wLoglevel, margin);
    fdLogSubject.right = new FormAttachment(100, 0);
    wLogSubject.setLayoutData(fdLogSubject);

    // Log message to display
    Label wlLogMessage = new Label(shell, SWT.RIGHT);
    wlLogMessage.setText(BaseMessages.getString(PKG, "WriteToLog.LogMessage.Label"));
    PropsUi.setLook(wlLogMessage);
    FormData fdlLogMessage = new FormData();
    fdlLogMessage.left = new FormAttachment(0, 0);
    fdlLogMessage.top = new FormAttachment(wLogSubject, margin);
    fdlLogMessage.right = new FormAttachment(middle, -margin);
    wlLogMessage.setLayoutData(fdlLogMessage);

    wLogMessage =
        new TextVar(
            variables, shell, SWT.MULTI | SWT.LEFT | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
    wLogMessage.setText(BaseMessages.getString(PKG, CONST_WRITE_TO_LOG_NAME_DEFAULT));
    PropsUi.setLook(wLogMessage, Props.WIDGET_STYLE_FIXED);
    wLogMessage.addModifyListener(lsMod);
    FormData fdLogMessage = new FormData();
    fdLogMessage.left = new FormAttachment(middle, 0);
    fdLogMessage.top = new FormAttachment(wLogSubject, margin);
    fdLogMessage.right = new FormAttachment(100, 0);
    fdLogMessage.bottom = new FormAttachment(wOk, -margin);
    wLogMessage.setLayoutData(fdLogMessage);

    getData();

    BaseDialog.defaultShellHandling(shell, c -> ok(), c -> cancel());

    return action;
  }

  /** Copy information from the meta-data input to the dialog fields. */
  public void getData() {
    wName.setText(Const.nullToEmpty(action.getName()));
    wLogMessage.setText(Const.nullToEmpty(action.getLogMessage()));
    wLogSubject.setText(Const.nullToEmpty(action.getLogSubject()));
    if (action.getActionLogLevel() != null) {
      wLoglevel.select(action.getActionLogLevel().getLevel());
    }

    wName.selectAll();
    wName.setFocus();
  }

  private void cancel() {
    action.setChanged(changed);
    action = null;
    dispose();
  }

  private void ok() {
    if (Utils.isEmpty(wName.getText())) {
      MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
      mb.setText(BaseMessages.getString(PKG, "System.TransformActionNameMissing.Title"));
      mb.setMessage(BaseMessages.getString(PKG, "System.ActionNameMissing.Msg"));
      mb.open();
      return;
    }
    action.setName(wName.getText());
    action.setLogMessage(wLogMessage.getText());
    action.setLogSubject(wLogSubject.getText());
    if (wLoglevel.getSelectionIndex() != -1) {
      action.setActionLogLevel(LogLevel.values()[wLoglevel.getSelectionIndex()]);
    }
    dispose();
  }
}
