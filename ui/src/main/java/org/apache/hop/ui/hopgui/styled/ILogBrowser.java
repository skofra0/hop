package org.apache.hop.ui.hopgui.styled;

import org.apache.hop.core.logging.ILogParentProvided;
import org.apache.hop.ui.util.EnvironmentUtils;

public interface ILogBrowser {

  void installLogSniffer();

  boolean isPaused();

  void setPaused(boolean paused);

  public static ILogBrowser of(final IStyledText text, final ILogParentProvided logProvider) {
    if (EnvironmentUtils.getInstance().isWeb()) {
      return new org.apache.hop.ui.hopgui.styled.rap.WrappedLogBrowser(text, logProvider);
    } else {
      return new org.apache.hop.ui.hopgui.styled.rpc.WrappedLogBrowser(text, logProvider);
    }
  }

}
