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
package org.apache.hop.ui.hopgui.dialog;

import org.apache.hop.core.Const;
import org.apache.hop.core.NotePadMeta;
import org.apache.hop.core.Props;
import org.apache.hop.core.variables.IVariables;
import org.apache.hop.i18n.BaseMessages;
import org.apache.hop.ui.core.PropsUi;
import org.apache.hop.ui.core.dialog.BaseDialog;
import org.apache.hop.ui.core.gui.GuiResource;
import org.apache.hop.ui.core.gui.WindowProperty;
import org.apache.hop.ui.core.widget.StyledTextComp;
import org.apache.hop.ui.pipeline.transform.BaseTransformDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;

/** Dialog to enter a text. (descriptions etc.) */
public class NotePadDialog extends Dialog {
  private static final Class<?> PKG = NotePadDialog.class;
  public static final String CONST_NOTE_PAD_DIALOG_FONT_COLOR_DIALOG_LABEL =
      "NotePadDialog.Font.Color.Dialog.Label";

  private NotePadMeta notePadMeta;

  private StyledTextComp wDesc;

  private Shell shell;
  private String title;
  private PropsUi props;

  private CTabFolder wNoteFolder;

  private CCombo wFontName;

  private Spinner wFontSize;

  private Button wFontBold;

  private Button wFontItalic;

  private Label wBackGroundColor;

  private Label wFontColor;

  private Label wBorderColor;

  private GuiResource guiresource = GuiResource.getInstance();

  public RGB colorRgbBlack = guiresource.getColorBlack().getRGB();
  public RGB colorRgbYellow = guiresource.getColorYellow().getRGB();
  public RGB colorRgbGray = guiresource.getColorGray().getRGB();

  private Color fontColor;
  private Color bgColor;
  private Color borderColor;

  private Font font;

  private IVariables variables;

  /** Dialog to allow someone to show or enter a text in variable width font */
  public NotePadDialog(IVariables variables, Shell parent, String title, NotePadMeta nMeta) {
    super(parent, SWT.NONE);
    props = PropsUi.getInstance();
    this.title = title;
    if (nMeta != null) {
      notePadMeta = nMeta;
    }
    this.variables = variables;
  }

  public NotePadDialog(IVariables variables, Shell parent, String title) {
    this(variables, parent, title, null);
  }

  public NotePadMeta open() {
    Shell parent = getParent();

    shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN | SWT.NONE);
    PropsUi.setLook(shell);
    shell.setImage(guiresource.getImageNote());

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = PropsUi.getFormMargin();
    formLayout.marginHeight = PropsUi.getFormMargin();

    shell.setLayout(formLayout);
    shell.setText(title);

    int margin = PropsUi.getMargin();
    int middle = 30;

    // Some buttons at the bottom
    //
    Button wOk = new Button(shell, SWT.PUSH);
    wOk.setText(BaseMessages.getString(PKG, "System.Button.OK"));
    wOk.addListener(SWT.Selection, e -> ok());
    Button wCancel = new Button(shell, SWT.PUSH);
    wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));
    wCancel.addListener(SWT.Selection, e -> cancel());
    BaseTransformDialog.positionBottomButtons(shell, new Button[] {wOk, wCancel}, margin, null);

    wNoteFolder = new CTabFolder(shell, SWT.BORDER);
    PropsUi.setLook(wNoteFolder, Props.WIDGET_STYLE_TAB);

    // ////////////////////////
    // START OF NOTE CONTENT TAB///
    // /
    CTabItem wNoteContentTab = new CTabItem(wNoteFolder, SWT.NONE);
    wNoteContentTab.setFont(GuiResource.getInstance().getFontDefault());
    wNoteContentTab.setText(BaseMessages.getString(PKG, "NotePadDialog.ContentTab.Note"));
    Composite wNoteContentComp = new Composite(wNoteFolder, SWT.NONE);
    PropsUi.setLook(wNoteContentComp);

    FormLayout fileLayout = new FormLayout();
    fileLayout.marginWidth = 3;
    fileLayout.marginHeight = 3;
    wNoteContentComp.setLayout(fileLayout);

    // From transform line
    Label wlDesc = new Label(wNoteContentComp, SWT.NONE);
    wlDesc.setText(BaseMessages.getString(PKG, "NotePadDialog.ContentTab.Note.Label"));
    PropsUi.setLook(wlDesc);
    FormData fdlDesc = new FormData();
    fdlDesc.left = new FormAttachment(0, 0);
    fdlDesc.top = new FormAttachment(0, margin);
    wlDesc.setLayoutData(fdlDesc);
    wDesc =
        new StyledTextComp(
            variables,
            wNoteContentComp,
            SWT.MULTI | SWT.LEFT | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);

    wDesc.setText("");
    FormData fdDesc = new FormData();
    fdDesc.left = new FormAttachment(0, 0);
    fdDesc.top = new FormAttachment(wlDesc, margin);
    fdDesc.right = new FormAttachment(100, -10);
    fdDesc.bottom = new FormAttachment(100, -margin);
    wDesc.setLayoutData(fdDesc);

    FormData fdNoteContentComp = new FormData();
    fdNoteContentComp.left = new FormAttachment(0, 0);
    fdNoteContentComp.top = new FormAttachment(0, 0);
    fdNoteContentComp.right = new FormAttachment(100, 0);
    fdNoteContentComp.bottom = new FormAttachment(100, 0);
    wNoteContentComp.setLayoutData(fdNoteContentComp);
    wNoteContentComp.layout();
    wNoteContentTab.setControl(wNoteContentComp);

    // ///////////////////////////////////////////////////////////
    // / END OF NOTE CONTENT TAB
    // ///////////////////////////////////////////////////////////

    // ////////////////////////
    // START OF NOTE FONT TAB///
    // /
    CTabItem wNoteFontTab = new CTabItem(wNoteFolder, SWT.NONE);
    wNoteFontTab.setFont(GuiResource.getInstance().getFontDefault());
    wNoteFontTab.setText(BaseMessages.getString(PKG, "NotePadDialog.Font.Label"));
    Composite wNoteFontComp = new Composite(wNoteFolder, SWT.NONE);
    PropsUi.setLook(wNoteFontComp);

    FormLayout notefontLayout = new FormLayout();
    fileLayout.marginWidth = 3;
    fileLayout.marginHeight = 3;
    wNoteFontComp.setLayout(notefontLayout);

    // Font name
    Label wlFontName = new Label(wNoteFontComp, SWT.RIGHT);
    wlFontName.setText(BaseMessages.getString(PKG, "NotePadDialog.Font.Name.Label"));
    PropsUi.setLook(wlFontName);
    FormData fdlFontName = new FormData();
    fdlFontName.left = new FormAttachment(margin, margin);
    fdlFontName.top = new FormAttachment(0, 3 * margin);
    fdlFontName.right = new FormAttachment(middle, -margin);
    wlFontName.setLayoutData(fdlFontName);
    wFontName = new CCombo(wNoteFontComp, SWT.BORDER | SWT.READ_ONLY);
    wFontName.setItems(Const.getAvailableFontNames());
    PropsUi.setLook(wFontName);
    FormData fdFontName = new FormData();
    fdFontName.left = new FormAttachment(middle, 0);
    fdFontName.top = new FormAttachment(0, 3 * margin);
    fdFontName.right = new FormAttachment(100, -margin);
    wFontName.setLayoutData(fdFontName);
    wFontName.select(0);
    wFontName.addSelectionListener(
        new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent arg0) {
            refreshTextNote();
          }
        });

    // FontSize line
    Label wlFontSize = new Label(wNoteFontComp, SWT.RIGHT);
    wlFontSize.setText(BaseMessages.getString(PKG, "NotePadDialog.Font.Size.Label"));
    PropsUi.setLook(wlFontSize);
    FormData fdlFontSize = new FormData();
    fdlFontSize.left = new FormAttachment(margin, margin);
    fdlFontSize.top = new FormAttachment(wFontName, margin);
    fdlFontSize.right = new FormAttachment(middle, -margin);
    wlFontSize.setLayoutData(fdlFontSize);
    wFontSize = new Spinner(wNoteFontComp, SWT.BORDER);
    wFontSize.setMinimum(0);
    wFontSize.setMaximum(70);
    wFontSize.setIncrement(1);
    FormData fdFontSize = new FormData();
    fdFontSize.left = new FormAttachment(middle, 0);
    fdFontSize.top = new FormAttachment(wFontName, margin);
    fdFontSize.right = new FormAttachment(100, -margin);
    wFontSize.setLayoutData(fdFontSize);
    wFontSize.addSelectionListener(
        new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent arg0) {
            refreshTextNote();
          }
        });

    // Font bold?
    Label wlFontBold = new Label(wNoteFontComp, SWT.RIGHT);
    wlFontBold.setText(BaseMessages.getString(PKG, "NotePadDialog.Font.Bold.Label"));
    PropsUi.setLook(wlFontBold);
    FormData fdlFontBold = new FormData();
    fdlFontBold.left = new FormAttachment(margin, margin);
    fdlFontBold.top = new FormAttachment(wFontSize, margin);
    fdlFontBold.right = new FormAttachment(middle, -margin);
    wlFontBold.setLayoutData(fdlFontBold);
    wFontBold = new Button(wNoteFontComp, SWT.CHECK);
    PropsUi.setLook(wFontBold);
    FormData fdFontBold = new FormData();
    fdFontBold.left = new FormAttachment(middle, 0);
    fdFontBold.top = new FormAttachment(wlFontBold, 0, SWT.CENTER);
    fdFontBold.right = new FormAttachment(100, -margin);
    wFontBold.setLayoutData(fdFontBold);
    wFontBold.addSelectionListener(
        new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent arg0) {
            refreshTextNote();
          }
        });
    // Font Italic?
    Label wlFontItalic = new Label(wNoteFontComp, SWT.RIGHT);
    wlFontItalic.setText(BaseMessages.getString(PKG, "NotePadDialog.Font.Italic.Label"));
    PropsUi.setLook(wlFontItalic);
    FormData fdlFontItalic = new FormData();
    fdlFontItalic.left = new FormAttachment(margin, margin);
    fdlFontItalic.top = new FormAttachment(wlFontBold, margin);
    fdlFontItalic.right = new FormAttachment(middle, -margin);
    wlFontItalic.setLayoutData(fdlFontItalic);
    wFontItalic = new Button(wNoteFontComp, SWT.CHECK);
    PropsUi.setLook(wFontItalic);
    FormData fdFontItalic = new FormData();
    fdFontItalic.left = new FormAttachment(middle, 0);
    fdFontItalic.top = new FormAttachment(wlFontItalic, 0, SWT.CENTER);
    fdFontItalic.right = new FormAttachment(100, -margin);
    wFontItalic.setLayoutData(fdFontItalic);
    wFontItalic.addSelectionListener(
        new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent arg0) {
            refreshTextNote();
          }
        });
    // Font color line
    Label wlFontColor = new Label(wNoteFontComp, SWT.RIGHT);
    wlFontColor.setText(BaseMessages.getString(PKG, "NotePadDialog.Font.Color.Label"));
    PropsUi.setLook(wlFontColor);
    FormData fdlFontColor = new FormData();
    fdlFontColor.left = new FormAttachment(margin, margin);
    fdlFontColor.top = new FormAttachment(wFontItalic, 2 * margin);
    fdlFontColor.right = new FormAttachment(middle, -margin);
    wlFontColor.setLayoutData(fdlFontColor);

    // Change font color
    Button wbFontColorChange = new Button(wNoteFontComp, SWT.PUSH);
    wbFontColorChange.setImage(guiresource.getImageColor());
    wbFontColorChange.setToolTipText(
        BaseMessages.getString(PKG, "NotePadDialog.Font.Color.Change.Tooltip"));
    PropsUi.setLook(wbFontColorChange);
    FormData fdFontColorChange = new FormData();
    fdFontColorChange.top = new FormAttachment(wlFontItalic, 2 * margin);
    fdFontColorChange.right = new FormAttachment(100, -margin);
    wbFontColorChange.setLayoutData(fdFontColorChange);
    wbFontColorChange.addSelectionListener(
        new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            ColorDialog cd = new ColorDialog(shell);
            cd.setText(BaseMessages.getString(PKG, CONST_NOTE_PAD_DIALOG_FONT_COLOR_DIALOG_LABEL));
            cd.setRGB(wFontColor.getBackground().getRGB());
            RGB newColor = cd.open();
            if (newColor == null) {
              return;
            }
            fontColor.dispose();
            fontColor = new Color(shell.getDisplay(), newColor);
            wFontColor.setBackground(fontColor);
            refreshTextNote();
          }
        });

    // Font color
    wFontColor = new Label(wNoteFontComp, SWT.NONE);
    wFontColor.setToolTipText(BaseMessages.getString(PKG, "NotePadDialog.Font.Color.Tooltip"));
    PropsUi.setLook(wFontColor);
    wFontColor.setEnabled(false);
    FormData fdFontColor = new FormData();
    fdFontColor.left = new FormAttachment(wlFontColor, margin);
    fdFontColor.top = new FormAttachment(wFontItalic, 2 * margin);
    fdFontColor.right = new FormAttachment(wbFontColorChange, -margin);
    wFontColor.setLayoutData(fdFontColor);

    // Background color line
    Label wlBackGroundColor = new Label(wNoteFontComp, SWT.RIGHT);
    wlBackGroundColor.setText(
        BaseMessages.getString(PKG, "NotePadDialog.Font.BackGroundColor.Label"));
    PropsUi.setLook(wlBackGroundColor);
    FormData fdlBackGroundColor = new FormData();
    fdlBackGroundColor.left = new FormAttachment(margin, margin);
    fdlBackGroundColor.top = new FormAttachment(wFontColor, 2 * margin);
    fdlBackGroundColor.right = new FormAttachment(middle, -margin);
    wlBackGroundColor.setLayoutData(fdlBackGroundColor);

    // Change Background color
    Button wbBackGroundColorChange = new Button(wNoteFontComp, SWT.PUSH);
    wbBackGroundColorChange.setImage(guiresource.getImageColor());
    wbBackGroundColorChange.setToolTipText(
        BaseMessages.getString(PKG, "NotePadDialog.Font.BackGroundColor.Change.Tooltip"));
    PropsUi.setLook(wbBackGroundColorChange);
    FormData fdBackGroundColorChange = new FormData();
    fdBackGroundColorChange.top = new FormAttachment(wFontColor, 2 * margin);
    fdBackGroundColorChange.right = new FormAttachment(100, -margin);
    fdBackGroundColorChange.right = new FormAttachment(100, -margin);
    wbBackGroundColorChange.setLayoutData(fdBackGroundColorChange);
    wbBackGroundColorChange.addSelectionListener(
        new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            ColorDialog cd = new ColorDialog(shell);
            cd.setText(BaseMessages.getString(PKG, CONST_NOTE_PAD_DIALOG_FONT_COLOR_DIALOG_LABEL));
            cd.setRGB(wBackGroundColor.getBackground().getRGB());
            RGB newColor = cd.open();
            if (newColor == null) {
              return;
            }
            bgColor.dispose();
            bgColor = new Color(shell.getDisplay(), newColor);
            wBackGroundColor.setBackground(bgColor);
            refreshTextNote();
          }
        });

    // Background color
    wBackGroundColor = new Label(wNoteFontComp, SWT.BORDER);
    wBackGroundColor.setToolTipText(
        BaseMessages.getString(PKG, "NotePadDialog.Font.BackGroundColor.Tooltip"));
    PropsUi.setLook(wBackGroundColor);
    wBackGroundColor.setEnabled(false);
    FormData fdBackGroundColor = new FormData();
    fdBackGroundColor.left = new FormAttachment(wlBackGroundColor, margin);
    fdBackGroundColor.top = new FormAttachment(wFontColor, 2 * margin);
    fdBackGroundColor.right = new FormAttachment(wbBackGroundColorChange, -margin);
    wBackGroundColor.setLayoutData(fdBackGroundColor);

    // Border color line
    Label wlBorderColor = new Label(wNoteFontComp, SWT.RIGHT);
    wlBorderColor.setText(BaseMessages.getString(PKG, "NotePadDialog.Font.BorderColor.Label"));
    PropsUi.setLook(wlBorderColor);
    FormData fdlBorderColor = new FormData();
    fdlBorderColor.left = new FormAttachment(margin, margin);
    fdlBorderColor.top = new FormAttachment(wBackGroundColor, 2 * margin);
    fdlBorderColor.right = new FormAttachment(middle, -margin);
    wlBorderColor.setLayoutData(fdlBorderColor);

    // Change border color
    Button wbBorderColorChange = new Button(wNoteFontComp, SWT.PUSH);
    wbBorderColorChange.setImage(guiresource.getImageColor());
    wbBorderColorChange.setToolTipText(
        BaseMessages.getString(PKG, "NotePadDialog.Font.BorderColor.Change.Tooltip"));
    PropsUi.setLook(wbBorderColorChange);
    FormData fdBorderColorChange = new FormData();
    fdBorderColorChange.top = new FormAttachment(wBackGroundColor, 2 * margin);
    fdBorderColorChange.right = new FormAttachment(100, -margin);
    wbBorderColorChange.setLayoutData(fdBorderColorChange);
    wbBorderColorChange.addSelectionListener(
        new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            ColorDialog cd = new ColorDialog(shell);
            cd.setText(BaseMessages.getString(PKG, CONST_NOTE_PAD_DIALOG_FONT_COLOR_DIALOG_LABEL));
            cd.setRGB(wBorderColor.getBackground().getRGB());
            RGB newColor = cd.open();
            if (newColor == null) {
              return;
            }
            borderColor.dispose();
            borderColor = new Color(shell.getDisplay(), newColor);
            wBorderColor.setBackground(borderColor);
          }
        });

    // border color
    wBorderColor = new Label(wNoteFontComp, SWT.BORDER);
    wBorderColor.setToolTipText(
        BaseMessages.getString(PKG, "NotePadDialog.Font.BorderColor.Tooltip"));
    PropsUi.setLook(wBorderColor);
    wBorderColor.setEnabled(false);
    FormData fdBorderColor = new FormData();
    fdBorderColor.left = new FormAttachment(wlBorderColor, margin);
    fdBorderColor.top = new FormAttachment(wBackGroundColor, 2 * margin);
    fdBorderColor.right = new FormAttachment(wbBorderColorChange, -margin);
    wBorderColor.setLayoutData(fdBorderColor);

    FormData fdNoteFontComp = new FormData();
    fdNoteFontComp.left = new FormAttachment(0, 0);
    fdNoteFontComp.top = new FormAttachment(0, 0);
    fdNoteFontComp.right = new FormAttachment(100, 0);
    fdNoteFontComp.bottom = new FormAttachment(100, 0);
    wNoteFontComp.setLayoutData(fdNoteFontComp);
    wNoteFontComp.layout();
    wNoteFontTab.setControl(wNoteFontComp);

    // ///////////////////////////////////////////////////////////
    // / END OF NOTE FONT TAB
    // ///////////////////////////////////////////////////////////

    FormData fdNoteFolder = new FormData();
    fdNoteFolder.left = new FormAttachment(0, 0);
    fdNoteFolder.top = new FormAttachment(0, margin);
    fdNoteFolder.right = new FormAttachment(100, 0);
    fdNoteFolder.bottom = new FormAttachment(wOk, -2 * margin);
    wNoteFolder.setLayoutData(fdNoteFolder);

    getData();

    BaseDialog.defaultShellHandling(shell, c -> ok(), c -> cancel());

    return notePadMeta;
  }

  public void dispose() {
    props.setScreen(new WindowProperty(shell));
    fontColor.dispose();
    bgColor.dispose();
    borderColor.dispose();
    if (font != null && !font.isDisposed()) {
      font.dispose();
    }
    shell.dispose();
  }

  public void getData() {
    if (notePadMeta != null) {
      wDesc.setText(Const.NVL(notePadMeta.getNote(), ""));
      wFontName.setText(
          notePadMeta.getFontName() == null
              ? props.getNoteFont().getName()
              : notePadMeta.getFontName());
      wFontSize.setSelection(
          notePadMeta.getFontSize() == -1
              ? props.getNoteFont().getHeight()
              : notePadMeta.getFontSize());
      wFontBold.setSelection(notePadMeta.isFontBold());
      wFontItalic.setSelection(notePadMeta.isFontItalic());
      fontColor =
          new Color(
              shell.getDisplay(),
              props.contrastColor(
                  notePadMeta.getFontColorRed(),
                  notePadMeta.getFontColorGreen(),
                  notePadMeta.getFontColorBlue()));
      bgColor =
          new Color(
              shell.getDisplay(),
              props.contrastColor(
                  notePadMeta.getBackGroundColorRed(),
                  notePadMeta.getBackGroundColorGreen(),
                  notePadMeta.getBackGroundColorBlue()));
      borderColor =
          new Color(
              shell.getDisplay(),
              props.contrastColor(
                  notePadMeta.getBorderColorRed(),
                  notePadMeta.getBorderColorGreen(),
                  notePadMeta.getBorderColorBlue()));
    } else {
      wFontName.setText(props.getNoteFont().getName());
      wFontSize.setSelection(props.getNoteFont().getHeight());
      wFontBold.setSelection(false);
      wFontItalic.setSelection(false);
      fontColor =
          new Color(
              shell.getDisplay(),
              props.contrastColor(
                  NotePadMeta.COLOR_RGB_BLACK_RED,
                  NotePadMeta.COLOR_RGB_BLACK_GREEN,
                  NotePadMeta.COLOR_RGB_BLACK_BLUE));
      bgColor =
          new Color(
              shell.getDisplay(),
              props.contrastColor(
                  NotePadMeta.COLOR_RGB_DEFAULT_BG_RED,
                  NotePadMeta.COLOR_RGB_DEFAULT_BG_GREEN,
                  NotePadMeta.COLOR_RGB_DEFAULT_BG_BLUE));
      borderColor =
          new Color(
              shell.getDisplay(),
              props.contrastColor(
                  NotePadMeta.COLOR_RGB_DEFAULT_BORDER_RED,
                  NotePadMeta.COLOR_RGB_DEFAULT_BORDER_GREEN,
                  NotePadMeta.COLOR_RGB_DEFAULT_BORDER_BLUE));
    }

    wFontColor.setBackground(fontColor);
    wBackGroundColor.setBackground(bgColor);
    wBorderColor.setBackground(borderColor);

    wNoteFolder.setSelection(0);
    wDesc.setFocus();
    wDesc.setSelection(wDesc.getText().length());

    refreshTextNote();
  }

  private void cancel() {
    notePadMeta = null;
    dispose();
  }

  private void ok() {
    notePadMeta = new NotePadMeta();
    if (wDesc.getText() != null) {
      notePadMeta.setNote(wDesc.getText());
    }
    if (wFontName.getText() != null) {
      notePadMeta.setFontName(wFontName.getText());
    }
    notePadMeta.setFontSize(wFontSize.getSelection());
    notePadMeta.setFontBold(wFontBold.getSelection());
    notePadMeta.setFontItalic(wFontItalic.getSelection());
    // font color
    notePadMeta.setFontColorRed(wFontColor.getBackground().getRed());
    notePadMeta.setFontColorGreen(wFontColor.getBackground().getGreen());
    notePadMeta.setFontColorBlue(wFontColor.getBackground().getBlue());
    // background color
    notePadMeta.setBackGroundColorRed(wBackGroundColor.getBackground().getRed());
    notePadMeta.setBackGroundColorGreen(wBackGroundColor.getBackground().getGreen());
    notePadMeta.setBackGroundColorBlue(wBackGroundColor.getBackground().getBlue());
    // border color
    notePadMeta.setBorderColorRed(wBorderColor.getBackground().getRed());
    notePadMeta.setBorderColorGreen(wBorderColor.getBackground().getGreen());
    notePadMeta.setBorderColorBlue(wBorderColor.getBackground().getBlue());
    dispose();
  }

  private void refreshTextNote() {
    int swt = SWT.NORMAL;
    if (wFontBold.getSelection()) {
      swt = SWT.BOLD;
    }
    if (wFontItalic.getSelection()) {
      swt = swt | SWT.ITALIC;
    }
    // dispose of old font only after setting it on wDesc
    Font oldFont = font;
    font = new Font(shell.getDisplay(), wFontName.getText(), wFontSize.getSelection(), swt);
    wDesc.setFont(font);
    if (oldFont != null && !oldFont.isDisposed()) {
      oldFont.dispose();
    }
    for (Control control : wDesc.getChildren()) {
      control.setBackground(bgColor);
    }

    wFontColor.setBackground(fontColor);
    wBackGroundColor.setBackground(bgColor);
    wBorderColor.setBackground(borderColor);
  }
}
