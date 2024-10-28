package org.apache.hop.ui.hopgui.styled.rpc;

import org.apache.hop.core.logging.ILogParentProvided;
import org.apache.hop.ui.hopgui.styled.ILogBrowser;
import org.apache.hop.ui.hopgui.styled.IStyledText;

// DEEM-MOD
public class WrappedLogBrowser implements ILogBrowser {

  private HopGuiLogBrowserStyled wrapped;

  public WrappedLogBrowser(final IStyledText text, final ILogParentProvided logProvider) {
    wrapped = new HopGuiLogBrowserStyled(text, logProvider);
  }

  @Override
  public void installLogSniffer() {
    wrapped.installLogSniffer();
  }

  @Override
  public boolean isPaused() {
    return wrapped.isPaused();
  }

  @Override
  public void setPaused(boolean paused) {
    wrapped.setPaused(paused);
  }
}
