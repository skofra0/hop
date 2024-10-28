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
