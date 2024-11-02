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
package org.apache.hop.www;

import static junit.framework.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.hop.core.gui.Point;
import org.apache.hop.core.logging.HopLogStore;
import org.apache.hop.core.logging.ILogChannel;
import org.apache.hop.workflow.Workflow;
import org.apache.hop.workflow.WorkflowMeta;
import org.apache.hop.workflow.engine.IWorkflowEngine;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.owasp.encoder.Encode;

public class StartWorkflowServletTest {
  private WorkflowMap mockWorkflowMap;

  private StartWorkflowServlet startJobServlet;

  @Before
  public void setup() {
    mockWorkflowMap = mock(WorkflowMap.class);
    startJobServlet = new StartWorkflowServlet(mockWorkflowMap);
  }

  @Test
  public void testStartWorkflowServletEscapesHtmlWhenPipelineNotFound()
      throws ServletException, IOException {
    HttpServletRequest mockHttpServletRequest = mock(HttpServletRequest.class);
    HttpServletResponse mockHttpServletResponse = mock(HttpServletResponse.class);

    StringWriter out = new StringWriter();
    PrintWriter printWriter = new PrintWriter(out);

    Mockito.spy(Encode.class);
    when(mockHttpServletRequest.getContextPath()).thenReturn(StartWorkflowServlet.CONTEXT_PATH);
    when(mockHttpServletRequest.getParameter(anyString()))
        .thenReturn(ServletTestUtils.BAD_STRING_TO_TEST);
    when(mockHttpServletResponse.getWriter()).thenReturn(printWriter);

    startJobServlet.doGet(mockHttpServletRequest, mockHttpServletResponse);
    assertFalse(ServletTestUtils.hasBadText(ServletTestUtils.getInsideOfTag("H1", out.toString())));
  }

  @Test
  public void testStartWorkflowServletEscapesHtmlWhenPipelineFound()
      throws ServletException, IOException {
    HopLogStore.init();
    HttpServletRequest mockHttpServletRequest = mock(HttpServletRequest.class);
    HttpServletResponse mockHttpServletResponse = mock(HttpServletResponse.class);
    IWorkflowEngine<WorkflowMeta> mockWorkflow = mock(Workflow.class);
    WorkflowMeta mockWorkflowMeta = mock(WorkflowMeta.class);
    ILogChannel mockLogChannelInterface = mock(ILogChannel.class);
    mockWorkflowMeta.setName(ServletTestUtils.BAD_STRING_TO_TEST);
    StringWriter out = new StringWriter();
    PrintWriter printWriter = new PrintWriter(out);

    Mockito.spy(Encode.class);
    when(mockHttpServletRequest.getContextPath()).thenReturn(StartWorkflowServlet.CONTEXT_PATH);
    when(mockHttpServletRequest.getParameter(anyString()))
        .thenReturn(ServletTestUtils.BAD_STRING_TO_TEST);
    when(mockHttpServletResponse.getWriter()).thenReturn(printWriter);
    when(mockWorkflowMap.getWorkflow(any(HopServerObjectEntry.class))).thenReturn(mockWorkflow);
    when(mockWorkflow.getLogChannelId()).thenReturn(ServletTestUtils.BAD_STRING_TO_TEST);
    when(mockWorkflow.getLogChannel()).thenReturn(mockLogChannelInterface);
    when(mockWorkflow.getWorkflowMeta()).thenReturn(mockWorkflowMeta);
    when(mockWorkflowMeta.getMaximum()).thenReturn(new Point(10, 10));

    startJobServlet.doGet(mockHttpServletRequest, mockHttpServletResponse);
    assertFalse(ServletTestUtils.hasBadText(ServletTestUtils.getInsideOfTag("H1", out.toString())));
  }
}
