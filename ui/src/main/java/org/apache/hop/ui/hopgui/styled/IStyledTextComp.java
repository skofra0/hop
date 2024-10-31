package org.apache.hop.ui.hopgui.styled;

import org.apache.hop.core.variables.IVariables;
import org.apache.hop.ui.core.widget.TextComposite;
import org.apache.hop.ui.util.EnvironmentUtils;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Composite;

// DEEM-MOD
public interface IStyledTextComp {

  TextComposite getWrapped();

  void addModifyListener(ModifyListener lsMod);

  void setText(String text);

  String getText();

  int getLineNumber();

  int getColumnNumber();

  void setToolTipText(String string);

  String getSelectionText();

  void setSqlValuesHighlight();

  void setLayoutData(FormData fdSql);

  void addKeyListener(KeyAdapter keyAdapter);

  void addFocusListener(FocusAdapter focusAdapter);

  void addMouseListener(MouseAdapter mouseAdapter);

  public static IStyledTextComp of(IVariables variables, Composite parent, int args) {
    if (EnvironmentUtils.getInstance().isWeb()) {
      return new org.apache.hop.ui.hopgui.styled.rap.WrappedStyledTextComp(variables, parent, args);
    } else {
      return new org.apache.hop.ui.hopgui.styled.rpc.WrappedStyledTextComp(variables, parent, args);
    }
  }
}
