package org.apache.hop.ui.hopgui.styled;

import org.apache.hop.ui.util.EnvironmentUtils;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Widget;

public interface IStyledText {

  Widget getWidget();

  void setLayoutData(Object layoutData);

  void setText(String string);

  boolean isDisposed();

  String getText();

  String getSelectionText();

  public static IStyledText of(Composite parent, int style) {
    if (EnvironmentUtils.getInstance().isWeb()) {
      return new org.apache.hop.ui.hopgui.styled.rap.WrappedStyledText(parent, style);
    } else {
      return new org.apache.hop.ui.hopgui.styled.rpc.WrappedStyledText(parent, style);
    }
  }

}
