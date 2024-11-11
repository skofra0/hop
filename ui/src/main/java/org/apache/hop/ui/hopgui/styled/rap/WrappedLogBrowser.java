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
package org.apache.hop.ui.hopgui.styled.rap;

import org.apache.hop.core.logging.ILogParentProvided;
import org.apache.hop.ui.hopgui.file.pipeline.HopGuiLogBrowser;
import org.apache.hop.ui.hopgui.styled.ILogBrowser;
import org.apache.hop.ui.hopgui.styled.IStyledText;

// DEEM-MOD
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
