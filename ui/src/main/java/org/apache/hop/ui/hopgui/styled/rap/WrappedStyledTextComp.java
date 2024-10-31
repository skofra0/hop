package org.apache.hop.ui.hopgui.styled.rap;

import org.apache.hop.core.variables.IVariables;
import org.apache.hop.ui.core.widget.StyledTextComp;
import org.apache.hop.ui.core.widget.TextComposite;
import org.apache.hop.ui.hopgui.styled.IStyledTextComp;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Composite;

// DEEM-MOD
public class WrappedStyledTextComp implements IStyledTextComp {

  protected StyledTextComp wrapped;

  public WrappedStyledTextComp(IVariables variables, Composite parent, int args) {
    wrapped = new StyledTextComp(variables, parent, args);
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
  public void setSqlValuesHighlight() {}

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
