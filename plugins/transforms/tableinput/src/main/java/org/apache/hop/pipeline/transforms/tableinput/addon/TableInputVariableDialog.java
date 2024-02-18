/*
 * Copyright © 2023 Deem
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.hop.pipeline.transforms.tableinput.addon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.hop.core.Const;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.exception.HopTransformException;
import org.apache.hop.core.row.IRowMeta;
import org.apache.hop.core.row.IValueMeta;
import org.apache.hop.core.variables.IVariables;
import org.apache.hop.i18n.BaseMessages;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.pipeline.transform.TransformMeta;
import org.apache.hop.pipeline.transforms.tableinput.TableInputMeta;
import org.apache.hop.ui.core.PropsUi;
import org.apache.hop.ui.core.dialog.BaseDialog;
import org.apache.hop.ui.core.dialog.ErrorDialog;
import org.apache.hop.ui.core.widget.ColumnInfo;
import org.apache.hop.ui.core.widget.TableView;
import org.apache.hop.ui.pipeline.transform.BaseTransformDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;

// DEEM-MOD
public class TableInputVariableDialog extends BaseTransformDialog {
  private static final Class<?> PKG = TableInputVariableField.class; // For Translator

  public static final String STRING_USAGE_WARNING_PARAMETER = "SetVariableUsageWarning";

  private TableView wFields;

  private TableInputMeta input;

  private Map<String, Integer> inputFields;

  private ColumnInfo[] colinf;

  public TableInputVariableDialog(Shell parent, IVariables variables, TableInputMeta in, PipelineMeta pipelineMeta, String sname) {
    super(parent, variables, in, pipelineMeta, sname);
    input = in;
    inputFields = new HashMap<>();
  }

  public String open() {
    Shell parent = getParent();

    shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN);
    PropsUi.setLook(shell);
    setShellImage(shell, input);

    ModifyListener lsMod = e -> input.setChanged();
    changed = input.hasChanged();

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = PropsUi.getFormMargin();
    formLayout.marginHeight = PropsUi.getFormMargin();

    shell.setLayout(formLayout);
    shell.setText(BaseMessages.getString(PKG, "TableInputVariableDialog.DialogTitle"));

    int margin = PropsUi.getMargin();

    var wlFields = new Label(shell, SWT.NONE);
    wlFields.setText(BaseMessages.getString(PKG, "TableInputVariableDialog.Fields.Label"));
    PropsUi.setLook(wlFields);
    var fdlFields = new FormData();
    fdlFields.left = new FormAttachment(0, 0);
    fdlFields.top = new FormAttachment(0, margin);
    wlFields.setLayoutData(fdlFields);

    final int fieldsRows = input.getVariableFields().size();
    colinf = new ColumnInfo[3];
    colinf[0] = new ColumnInfo(BaseMessages.getString(PKG, "TableInputVariableDialog.Fields.Column.FieldName"), ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] {""}, false);
    colinf[1] = new ColumnInfo(BaseMessages.getString(PKG, "TableInputVariableDialog.Fields.Column.VariableName"), ColumnInfo.COLUMN_TYPE_TEXT, false);
    colinf[2] = new ColumnInfo(BaseMessages.getString(PKG, "TableInputVariableDialog.Fields.Column.DefaultValue"), ColumnInfo.COLUMN_TYPE_TEXT, false);
    colinf[2].setUsingVariables(true);
    colinf[2].setToolTip(BaseMessages.getString(PKG, "TableInputVariableDialog.Fields.Column.DefaultValue.Tooltip"));

    wFields = new TableView(variables, shell, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, colinf, fieldsRows, lsMod, props);

    var fdFields = new FormData();
    fdFields.left = new FormAttachment(0, 0);
    fdFields.top = new FormAttachment(wlFields, margin);
    fdFields.right = new FormAttachment(100, 0);
    fdFields.bottom = new FormAttachment(100, -50);
    wFields.setLayoutData(fdFields);

    //
    // Search the fields in the background

    final Runnable runnable = () -> {
      TransformMeta transformMeta = pipelineMeta.findTransform(transformName);
      if (transformMeta != null) {
        try {
          IRowMeta row = getPreviousFields(transformMeta);

          // Remember these fields...
          for (int i = 0; i < row.size(); i++) {
            inputFields.put(row.getValueMeta(i).getName(), Integer.valueOf(i));
          }
          setComboBoxes();
        } catch (HopException e) {
          logError(BaseMessages.getString(PKG, "System.Dialog.GetFieldsFailed.Message"));
        }
      }
    };
    new Thread(runnable).start();

    // Some buttons
    wOk = new Button(shell, SWT.PUSH);
    wOk.setText(BaseMessages.getString(PKG, "System.Button.OK"));
    wGet = new Button(shell, SWT.PUSH);
    wGet.setText(BaseMessages.getString(PKG, "System.Button.GetFields"));
    wCancel = new Button(shell, SWT.PUSH);
    wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));

    setButtonPositions(new Button[] {wOk, wCancel, wGet}, margin, wFields);

    // Add listeners
    wCancel.addListener(SWT.Selection, e -> cancel());
    wGet.addListener(SWT.Selection, e -> get());
    wOk.addListener(SWT.Selection, e -> ok());

    getData();
    input.setChanged(changed);
    BaseDialog.defaultShellHandling(shell, c -> ok(), c -> cancel());
    return transformName;
  }

  protected void setComboBoxes() {
    // Something was changed in the row.
    //
    final Map<String, Integer> fields = new HashMap<>();

    // Add the currentMeta fields...
    fields.putAll(inputFields);

    Set<String> keySet = fields.keySet();
    List<String> entries = new ArrayList<>(keySet);

    String[] fieldNames = entries.toArray(new String[entries.size()]);

    Const.sortStrings(fieldNames);
    colinf[0].setComboValues(fieldNames);
  }

  /**
   * Copy information from the meta-data input to the dialog fields.
   */
  public void getData() {

    for (int i = 0; i < input.getVariableFields().size(); i++) {
      TableItem item = wFields.table.getItem(i);
      TableInputVariableField p = input.getVariableFields().get(i);
      String src = p.getFieldName();
      String tgt = p.getVariableName();
      String tvv = p.getDefaultValue();

      if (src != null) {
        item.setText(1, src);
      }
      if (tgt != null) {
        item.setText(2, tgt);
      }
      if (tvv != null) {
        item.setText(3, tvv);
      }
    }

    wFields.setRowNums();
    wFields.optWidth(true);
  }

  private void cancel() {
    transformName = null;
    input.setChanged(changed);
    dispose();
  }

  private void ok() {
    int count = wFields.nrNonEmpty();
    List<TableInputVariableField> fields = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      TableItem item = wFields.getNonEmpty(i);
      TableInputVariableField fld = new TableInputVariableField();
      fld.setFieldName(item.getText(1));
      fld.setVariableName(item.getText(2));
      fld.setDefaultValue(item.getText(3));
      fields.add(fld);
    }
    input.setVariableFields(fields);
    dispose();
  }

  private void get() {
    try {
      TransformMeta transformMeta = pipelineMeta.findTransform(transformName);
      if (transformMeta != null) {
        IRowMeta r = getPreviousFields(transformMeta);
        if (r != null && !r.isEmpty()) {
          BaseTransformDialog.getFieldsFromPrevious(r, wFields, 1, new int[] {1}, new int[] {}, -1, -1, (TableItem tableItem, IValueMeta v) -> {
            tableItem.setText(2, v.getName().toUpperCase());
            tableItem.setText(3, "");
            return true;
          });
        }
      }
    } catch (HopException ke) {
      new ErrorDialog(
          shell,
          BaseMessages.getString(PKG, "TableInputVariableDialog.FailedToGetFields.DialogTitle"),
          BaseMessages.getString(PKG, "Set.FailedToGetFields.DialogMessage"),
          ke);
    }
  }

  public IRowMeta getPreviousFields(TransformMeta transformMeta) throws HopTransformException {
    IRowMeta row = pipelineMeta.getPrevTransformFields(variables, transformMeta);
    if (row == null || row.isEmpty()) {
      TransformMeta[] infoTransform = pipelineMeta.getInfoTransform(transformMeta);
      row = pipelineMeta.getTransformFields(variables, infoTransform);
    }
    return row;
  }

}
