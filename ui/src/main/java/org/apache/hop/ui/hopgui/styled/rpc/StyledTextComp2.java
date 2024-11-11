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

import java.util.LinkedList;
import java.util.List;
import org.apache.hop.core.variables.IVariables;
import org.apache.hop.i18n.BaseMessages;
import org.apache.hop.ui.core.FormDataBuilder;
import org.apache.hop.ui.core.PropsUi;
import org.apache.hop.ui.core.gui.GuiResource;
import org.apache.hop.ui.core.widget.OsHelper;
import org.apache.hop.ui.core.widget.TextComposite;
import org.apache.hop.ui.core.widget.UndoRedoStack;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ExtendedModifyEvent;
import org.eclipse.swt.custom.ExtendedModifyListener;
import org.eclipse.swt.custom.LineStyleListener;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

// DEEM-MOD
public class StyledTextComp2 extends TextComposite {
  private static final Class<?> PKG = StyledTextComp2.class;
  private static final int MAX_STACK_SIZE = 25;

  // Modification for Undo/Redo on Styled Text
  private final StyledText textWidget;
  private final Menu styledTextPopupmenu;
  private final Composite xParent;
  private Image image;

  private List<UndoRedoStack> undoStack = new LinkedList<>();
  private List<UndoRedoStack> redoStack = new LinkedList<>();
  private boolean bFullSelection = false;
  private KeyListener kls;

  public StyledTextComp2(IVariables variables, Composite parent, int args) {
    this(variables, parent, args, true, false);
  }

  public StyledTextComp2(IVariables variables, Composite parent, int args, boolean varsSensitive) {
    this(variables, parent, args, varsSensitive, false);
  }

  public StyledTextComp2(
      IVariables variables,
      Composite parent,
      int args,
      boolean varsSensitive,
      boolean variableIconOnTop) {

    super(parent, SWT.NONE);
    textWidget = new StyledText(this, args);
    styledTextPopupmenu = new Menu(parent.getShell(), SWT.POP_UP);
    xParent = parent;
    this.setLayout(new FormLayout());

    buildingStyledTextMenu();
    addUndoRedoSupport();

    // Default layout without variables
    textWidget.setLayoutData(
        new FormDataBuilder().top().left().right(100, 0).bottom(100, 0).result());

    kls =
        new KeyAdapter() {
          @Override
          public void keyPressed(KeyEvent e) {
            if (e.keyCode == 'h' && (e.stateMask & SWT.MOD1 & SWT.SHIFT) != 0) {
              new StyledTextCompReplace(styledTextPopupmenu.getShell(), textWidget).open();
            } else if (e.keyCode == 'z' && (e.stateMask & SWT.MOD1) != 0) {
              undo();
            } else if (e.keyCode == 'y' && (e.stateMask & SWT.MOD1) != 0) {
              redo();
            } else if (e.keyCode == 'a' && (e.stateMask & SWT.MOD1) != 0) {
              bFullSelection = true;
              textWidget.selectAll();
            } else if (e.keyCode == 'f' && (e.stateMask & SWT.MOD1) != 0) {
              new StyledTextCompFind(
                      styledTextPopupmenu.getShell(),
                      textWidget,
                      BaseMessages.getString(PKG, "WidgetDialog.Styled.Find"))
                  .open();
            }
          }
        };

    textWidget.addKeyListener(kls);

    // Special layout for variables decorator
    if (varsSensitive) {
      textWidget.addKeyListener(new ControlSpaceKeyAdapter(variables, textWidget));
      image = GuiResource.getInstance().getImageVariableMini();
      if (variableIconOnTop) {
        final Label wIcon = new Label(this, SWT.RIGHT);
        PropsUi.setLook(wIcon);
        wIcon.setToolTipText(BaseMessages.getString(PKG, "StyledTextComp.tooltip.InsertVariable"));
        wIcon.setImage(image);
        wIcon.setLayoutData(new FormDataBuilder().top().right(100, 0).result());
        textWidget.setLayoutData(
            new FormDataBuilder()
                .top(new FormAttachment(wIcon, 0, 0))
                .left()
                .right(100, 0)
                .bottom(100, 0)
                .result());
      } else {
        Label controlDecoration = new Label(this, SWT.NONE);
        controlDecoration.setImage(image);
        controlDecoration.setToolTipText(
            BaseMessages.getString(PKG, "StyledTextComp.tooltip.InsertVariable"));
        PropsUi.setLook(controlDecoration);
        controlDecoration.setLayoutData(new FormDataBuilder().top().right(100, 0).result());
        textWidget.setLayoutData(
            new FormDataBuilder()
                .top()
                .left()
                .right(new FormAttachment(controlDecoration, 0, 0))
                .bottom(100, 0)
                .result());
      }
    }

    // Create the drop target on the StyledText
    DropTarget dt = new DropTarget(textWidget, DND.DROP_MOVE);
    dt.setTransfer(TextTransfer.getInstance());
    dt.addDropListener(
        new DropTargetAdapter() {
          public void dragOver(DropTargetEvent e) {
            textWidget.setFocus();
            Point location = xParent.getDisplay().map(null, textWidget, e.x, e.y);
            location.x = Math.max(0, location.x);
            location.y = Math.max(0, location.y);
            try {
              int offset = textWidget.getOffsetAtPoint(new Point(location.x, location.y));
              textWidget.setCaretOffset(offset);
            } catch (IllegalArgumentException ex) {
              int maxOffset = textWidget.getCharCount();
              Point maxLocation = textWidget.getLocationAtOffset(maxOffset);
              if (location.y >= maxLocation.y) {
                if (location.x >= maxLocation.x) {
                  textWidget.setCaretOffset(maxOffset);
                } else {
                  int offset = textWidget.getOffsetAtPoint(new Point(location.x, maxLocation.y));
                  textWidget.setCaretOffset(offset);
                }
              } else {
                textWidget.setCaretOffset(maxOffset);
              }
            }
          }

          public void drop(DropTargetEvent event) {
            // Set the buttons text to be the text being dropped
            textWidget.insert((String) event.data);
          }
        });
  }

  public String getSelectionText() {
    return textWidget.getSelectionText();
  }

  public int getCaretOffset() {
    return textWidget.getCaretOffset();
  }

  public String getText() {
    return textWidget.getText();
  }

  public void setText(String text) {
    textWidget.setText(text);
  }

  public int getLineAtOffset(int iOffset) {
    return textWidget.getLineAtOffset(iOffset);
  }

  public void insert(String strInsert) {
    textWidget.insert(strInsert);
  }

  @Override
  public void addListener(int eventType, Listener listener) {
    textWidget.addListener(eventType, listener);
  }

  public void addModifyListener(ModifyListener lsMod) {
    textWidget.addModifyListener(lsMod);
  }

  @Override
  public void addLineStyleListener() {
    // No listener required
  }

  @Override
  public void addLineStyleListener(List<String> sqlKeywords) {
    // No listener required
  }

  public void addLineStyleListener(LineStyleListener lineStyler) {
    textWidget.addLineStyleListener(lineStyler);
  }

  public void addKeyListener(KeyAdapter keyAdapter) {
    textWidget.addKeyListener(keyAdapter);
  }

  public void addFocusListener(FocusAdapter focusAdapter) {
    textWidget.addFocusListener(focusAdapter);
  }

  public void addMouseListener(MouseAdapter mouseAdapter) {
    textWidget.addMouseListener(mouseAdapter);
  }

  public int getSelectionCount() {
    return textWidget.getSelectionCount();
  }

  public void setSelection(int arg0) {
    textWidget.setSelection(arg0);
  }

  public void setSelection(int arg0, int arg1) {
    textWidget.setSelection(arg0, arg1);
  }

  @Override
  public void setBackground(Color color) {
    super.setBackground(color);
    textWidget.setBackground(color);
  }

  @Override
  public void setForeground(Color color) {
    super.setForeground(color);
    textWidget.setForeground(color);
  }

  @Override
  public void setFont(Font fnt) {
    textWidget.setFont(fnt);
  }

  private void buildingStyledTextMenu() {
    final MenuItem undoItem = new MenuItem(styledTextPopupmenu, SWT.PUSH);
    undoItem.setText(
        OsHelper.customizeMenuitemText(BaseMessages.getString(PKG, "WidgetDialog.Styled.Undo")));
    undoItem.addListener(SWT.Selection, e -> undo());

    final MenuItem redoItem = new MenuItem(styledTextPopupmenu, SWT.PUSH);
    redoItem.setText(
        OsHelper.customizeMenuitemText(BaseMessages.getString(PKG, "WidgetDialog.Styled.Redo")));
    redoItem.addListener(SWT.Selection, e -> redo());

    new MenuItem(styledTextPopupmenu, SWT.SEPARATOR);
    final MenuItem cutItem = new MenuItem(styledTextPopupmenu, SWT.PUSH);
    cutItem.setText(
        OsHelper.customizeMenuitemText(BaseMessages.getString(PKG, "WidgetDialog.Styled.Cut")));
    cutItem.addListener(SWT.Selection, e -> textWidget.cut());

    final MenuItem copyItem = new MenuItem(styledTextPopupmenu, SWT.PUSH);
    copyItem.setText(
        OsHelper.customizeMenuitemText(BaseMessages.getString(PKG, "WidgetDialog.Styled.Copy")));
    copyItem.addListener(SWT.Selection, e -> textWidget.copy());

    final MenuItem pasteItem = new MenuItem(styledTextPopupmenu, SWT.PUSH);
    pasteItem.setText(
        OsHelper.customizeMenuitemText(BaseMessages.getString(PKG, "WidgetDialog.Styled.Paste")));
    pasteItem.addListener(SWT.Selection, e -> textWidget.paste());

    MenuItem selectAllItem = new MenuItem(styledTextPopupmenu, SWT.PUSH);
    selectAllItem.setText(
        OsHelper.customizeMenuitemText(
            BaseMessages.getString(PKG, "WidgetDialog.Styled.SelectAll")));
    selectAllItem.addListener(SWT.Selection, e -> textWidget.selectAll());

    new MenuItem(styledTextPopupmenu, SWT.SEPARATOR);
    final MenuItem findItem = new MenuItem(styledTextPopupmenu, SWT.PUSH);
    findItem.setText(
        OsHelper.customizeMenuitemText(BaseMessages.getString(PKG, "WidgetDialog.Styled.Find")));
    findItem.addListener(
        SWT.Selection,
        e -> {
          StyledTextCompFind stFind =
              new StyledTextCompFind(
                  textWidget.getShell(),
                  textWidget,
                  BaseMessages.getString(PKG, "WidgetDialog.Styled.FindString"));
          stFind.open();
        });
    MenuItem replaceItem = new MenuItem(styledTextPopupmenu, SWT.PUSH);
    replaceItem.setText(
        OsHelper.customizeMenuitemText(BaseMessages.getString(PKG, "WidgetDialog.Styled.Replace")));
    replaceItem.setAccelerator(SWT.MOD1 | 'H');
    replaceItem.addListener(
        SWT.Selection,
        e -> {
          StyledTextCompReplace stReplace =
              new StyledTextCompReplace(textWidget.getShell(), textWidget);
          stReplace.open();
        });

    textWidget.addMenuDetectListener(
        e -> {
          styledTextPopupmenu.getItem(0).setEnabled(!undoStack.isEmpty());
          styledTextPopupmenu.getItem(1).setEnabled(!redoStack.isEmpty());

          styledTextPopupmenu.getItem(5).setEnabled(checkPaste());
          styledTextPopupmenu.getItem(3).setEnabled(textWidget.getSelectionCount() > 0);
          styledTextPopupmenu.getItem(4).setEnabled(textWidget.getSelectionCount() > 0);
        });
    textWidget.setMenu(styledTextPopupmenu);
  }

  // Check if something is stored inside the Clipboard
  private boolean checkPaste() {
    try {
      Clipboard clipboard = new Clipboard(xParent.getDisplay());
      TextTransfer transfer = TextTransfer.getInstance();
      String text = (String) clipboard.getContents(transfer);
      if (text != null && text.length() > 0) {
        return true;
      } else {
        return false;
      }
    } catch (Exception e) {
      return false;
    }
  }

  public Image getImage() {
    return image;
  }

  public StyledText getTextWidget() {
    return textWidget;
  }

  public boolean isEditable() {
    return textWidget.getEditable();
  }

  public void setEditable(boolean canEdit) {
    textWidget.setEditable(canEdit);
  }

  @Override
  public void setEnabled(boolean enabled) {
    textWidget.setEnabled(enabled);
    if (Display.getDefault() != null) {
      Color foreground =
          Display.getDefault().getSystemColor(enabled ? SWT.COLOR_BLACK : SWT.COLOR_DARK_GRAY);
      Color background =
          Display.getDefault()
              .getSystemColor(enabled ? SWT.COLOR_WHITE : SWT.COLOR_WIDGET_BACKGROUND);
      GuiResource guiResource = GuiResource.getInstance();
      textWidget.setForeground(
          guiResource.getColor(foreground.getRed(), foreground.getGreen(), foreground.getBlue()));
      textWidget.setBackground(
          guiResource.getColor(background.getRed(), background.getGreen(), background.getBlue()));
    }
  }

  /**
   * @return The caret line number, starting from 1.
   */
  public int getLineNumber() {
    return getLineAtOffset(getCaretOffset()) + 1;
  }

  /**
   * @return The caret column number, starting from 1.
   */
  public int getColumnNumber() {
    String scr = getText();
    int colnr = 0;
    int posnr = getCaretOffset();
    while (posnr > 0 && scr.charAt(posnr - 1) != '\n' && scr.charAt(posnr - 1) != '\r') {
      posnr--;
      colnr++;
    }
    return colnr + 1;
  }

  // Start Functions for Undo / Redo on wSrcipt
  private void addUndoRedoSupport() {

    textWidget.addSelectionListener(
        new SelectionListener() {
          public void widgetSelected(SelectionEvent event) {
            if (textWidget.getSelectionCount() == textWidget.getCharCount()) {
              bFullSelection = true;
              try {
                event.wait(2);
              } catch (Exception e) {
                // Ignore errors
              }
            }
          }

          public void widgetDefaultSelected(SelectionEvent event) {}
        });

    textWidget.addExtendedModifyListener(
        new ExtendedModifyListener() {
          public void modifyText(ExtendedModifyEvent event) {
            int iEventLength = event.length;
            int iEventStartPostition = event.start;

            // Unterscheidung um welche Art es sich handelt Delete or Insert
            String newText = textWidget.getText();
            String repText = event.replacedText;
            String oldText = "";
            int iEventType = -1;

            // if((event.length!=newText.length()) || newText.length()==1){
            if ((event.length != newText.length()) || (bFullSelection)) {
              if (repText != null && repText.length() > 0) {
                oldText =
                    newText.substring(0, event.start)
                        + repText
                        + newText.substring(event.start + event.length);
                iEventType = UndoRedoStack.DELETE;
                iEventLength = repText.length();
              } else {
                oldText =
                    newText.substring(0, event.start)
                        + newText.substring(event.start + event.length);
                iEventType = UndoRedoStack.INSERT;
              }

              if ((oldText != null && oldText.length() > 0)
                  || (iEventStartPostition == event.length)) {
                UndoRedoStack urs =
                    new UndoRedoStack(
                        iEventStartPostition, newText, oldText, iEventLength, iEventType);
                if (undoStack.size() == MAX_STACK_SIZE) {
                  undoStack.remove(undoStack.size() - 1);
                }
                undoStack.add(0, urs);
              }
            }
            bFullSelection = false;
          }
        });
  }

  private void undo() {
    if (!undoStack.isEmpty()) {
      UndoRedoStack urs = undoStack.remove(0);
      if (redoStack.size() == MAX_STACK_SIZE) {
        redoStack.remove(redoStack.size() - 1);
      }
      UndoRedoStack rro =
          new UndoRedoStack(
              urs.getCursorPosition(),
              urs.getReplacedText(),
              textWidget.getText(),
              urs.getEventLength(),
              urs.getType());
      bFullSelection = false;
      textWidget.setText(urs.getReplacedText());
      if (urs.getType() == UndoRedoStack.INSERT) {
        textWidget.setCaretOffset(urs.getCursorPosition());
      } else if (urs.getType() == UndoRedoStack.DELETE) {
        textWidget.setCaretOffset(urs.getCursorPosition() + urs.getEventLength());
        textWidget.setSelection(
            urs.getCursorPosition(), urs.getCursorPosition() + urs.getEventLength());
        if (textWidget.getSelectionCount() == textWidget.getCharCount()) {
          bFullSelection = true;
        }
      }
      redoStack.add(0, rro);
    }
  }

  private void redo() {
    if (!redoStack.isEmpty()) {
      UndoRedoStack urs = redoStack.remove(0);
      if (undoStack.size() == MAX_STACK_SIZE) {
        undoStack.remove(undoStack.size() - 1);
      }
      UndoRedoStack rro =
          new UndoRedoStack(
              urs.getCursorPosition(),
              urs.getReplacedText(),
              textWidget.getText(),
              urs.getEventLength(),
              urs.getType());
      bFullSelection = false;
      textWidget.setText(urs.getReplacedText());
      if (urs.getType() == UndoRedoStack.INSERT) {
        textWidget.setCaretOffset(urs.getCursorPosition());
      } else if (urs.getType() == UndoRedoStack.DELETE) {
        textWidget.setCaretOffset(urs.getCursorPosition() + urs.getEventLength());
        textWidget.setSelection(
            urs.getCursorPosition(), urs.getCursorPosition() + urs.getEventLength());
        if (textWidget.getSelectionCount() == textWidget.getCharCount()) {
          bFullSelection = true;
        }
      }
      undoStack.add(0, rro);
    }
  }
}
