package org.apache.hop.ui.hopgui.styled.rap;

import org.apache.hop.core.logging.ILogParentProvided;
import org.apache.hop.ui.hopgui.file.pipeline.HopGuiLogBrowser;
import org.apache.hop.ui.hopgui.styled.ILogBrowser;
import org.apache.hop.ui.hopgui.styled.IStyledText;

public class WrappedLogBrowser implements ILogBrowser {

  private HopGuiLogBrowser wrapped;

  public WrappedLogBrowser(final IStyledText text, final ILogParentProvided logProvider) {
    wrapped = new HopGuiLogBrowser(text, logProvider);
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
