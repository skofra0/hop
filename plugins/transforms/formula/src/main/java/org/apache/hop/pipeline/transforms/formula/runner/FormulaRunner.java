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
