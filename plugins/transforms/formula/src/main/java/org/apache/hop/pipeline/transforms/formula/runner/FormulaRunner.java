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
package org.apache.hop.pipeline.transforms.formula.runner;

import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.row.IRowMeta;
import org.apache.hop.pipeline.transforms.formula.FormulaData;
import org.apache.hop.pipeline.transforms.formula.FormulaMeta;
import org.apache.hop.pipeline.transforms.formula.FormulaMetaFunction;

/**
 * DEEM-MOD
 */
public abstract class FormulaRunner {

  protected FormulaMeta meta;
  protected FormulaData data;
  protected boolean first;

  public void init(FormulaMeta meta, FormulaData data) {
    first = true;
    this.meta = meta;
    this.data = data;

  }

  public void initRow(Object[] outputRowData) throws HopException {
    first = false;
  }

  public void dispose() throws HopException {
    // nop
  }

  public abstract Object evaluate(FormulaMetaFunction formula, IRowMeta inputRowMeta, Object[] outputRowData, int i) throws HopException;

}
