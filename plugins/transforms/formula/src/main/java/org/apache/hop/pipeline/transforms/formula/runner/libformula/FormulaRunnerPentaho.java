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
package org.apache.hop.pipeline.transforms.formula.runner.libformula;

import java.math.BigDecimal;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.apache.hop.core.Const;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.exception.HopValueException;
import org.apache.hop.core.row.IRowMeta;
import org.apache.hop.core.row.IValueMeta;
import org.apache.hop.pipeline.transforms.formula.FormulaData;
import org.apache.hop.pipeline.transforms.formula.FormulaMetaFunction;
import org.apache.hop.pipeline.transforms.formula.runner.FormulaRunner;
import org.pentaho.reporting.libraries.formula.EvaluationException;
import org.pentaho.reporting.libraries.formula.Formula;
import org.pentaho.reporting.libraries.formula.LibFormulaErrorValue;
import org.pentaho.reporting.libraries.formula.parser.ParseException;

/**
 * DEEM-MOD
 */
public class FormulaRunnerPentaho extends FormulaRunner {

  private RowForumulaContext context;
  private Formula[] formulas;
  private FormulaMetaFunction currentFormula;

  @Override
  public void initRow(Object[] outputRowData) throws HopException {
    if (formulas == null) {
      context = new RowForumulaContext(data.outputRowMeta);

      // Create a set of LValues to put the parsed results in...
      formulas = new org.pentaho.reporting.libraries.formula.Formula[meta.getFormulas().size()];
      for (int i = 0; i < meta.getFormulas().size(); i++) {
        FormulaMetaFunction fn = meta.getFormulas().get(i);
        currentFormula = fn; // DEEM-MOD
        if (!StringUtils.isEmpty(fn.getFieldName())) {
          formulas[i] = createFormula(meta.getFormulas().get(i).getFormula());
        } else {
          throw new HopException("Unable to find field name for formula [" + Const.NVL(fn.getFormula(), "") + "]");
        }
      }
    }
    context.setRowData(outputRowData);
  }

  @Override
  public Object evaluate(FormulaMetaFunction formula, IRowMeta inputRowMeta, Object[] outputRowData, int i) throws HopException {
    return calcFields(formula, i);
  }

  private Object calcFields(FormulaMetaFunction fn, int i) throws HopValueException {
    try {
      Object formulaResult = null;

      if (!StringUtils.isEmpty(fn.getFieldName())) {

        // this is main part of all this step: calculate formula
        formulaResult = formulas[i].evaluate();
        if (formulaResult instanceof LibFormulaErrorValue) {
          // inspect why it is happens to get clear error message.
          throw new HopException("Error calculate formula. Formula " + fn.getFormula() + " output field: " + fn.getFieldName() + ", error is: " + formulaResult.toString());
        }

        // Calculate the return type on the first row...
        // for most cases we can try to convert data on a fly.
        if (data.returnType[i] < 0) {
          if (formulaResult instanceof String) {
            data.returnType[i] = FormulaData.RETURN_TYPE_STRING;
            fn.setNeedDataConversion(fn.getValueType() != IValueMeta.TYPE_STRING);
          } else if (formulaResult instanceof Integer) {
            data.returnType[i] = FormulaData.RETURN_TYPE_INTEGER;
            fn.setNeedDataConversion(fn.getValueType() != IValueMeta.TYPE_INTEGER);
          } else if (formulaResult instanceof Long) {
            data.returnType[i] = FormulaData.RETURN_TYPE_LONG;
            fn.setNeedDataConversion(fn.getValueType() != IValueMeta.TYPE_INTEGER);
          } else if (formulaResult instanceof Date) {
            data.returnType[i] = FormulaData.RETURN_TYPE_DATE;
            fn.setNeedDataConversion(fn.getValueType() != IValueMeta.TYPE_DATE);
          } else if (formulaResult instanceof BigDecimal) {
            data.returnType[i] = FormulaData.RETURN_TYPE_BIGDECIMAL;
            fn.setNeedDataConversion(fn.getValueType() != IValueMeta.TYPE_BIGNUMBER);
          } else if (formulaResult instanceof Number) {
            data.returnType[i] = FormulaData.RETURN_TYPE_NUMBER;
            fn.setNeedDataConversion(fn.getValueType() != IValueMeta.TYPE_NUMBER);
            // this types we will not make attempt to
            // auto-convert
          } else if (formulaResult instanceof byte[]) {
            data.returnType[i] = FormulaData.RETURN_TYPE_BYTE_ARRAY;
            if (fn.getValueType() != IValueMeta.TYPE_BINARY) {
              throw new HopValueException("Please specify a Binary type for field [" + fn.getFieldName() + "] as a result of formula [" + fn.getFormula() + "]");
            }
          } else if (formulaResult instanceof Boolean) {
            data.returnType[i] = FormulaData.RETURN_TYPE_BOOLEAN;
            if (fn.getValueType() != IValueMeta.TYPE_BOOLEAN) {
              throw new HopValueException("Please specify a Boolean type for field [" + fn.getFieldName() + "] as a result of formula [" + fn.getFormula() + "]");
            }
          } else {
            data.returnType[i] = FormulaData.RETURN_TYPE_STRING;
            fn.setNeedDataConversion(fn.getValueType() != IValueMeta.TYPE_STRING);
          }
        }
      }
      currentFormula = null;
      return formulaResult;
    } catch (Exception e) {
      String msg = "Formula Error:";
      if (currentFormula != null) {
        msg = currentFormula.getFieldName() + " = " + currentFormula.getFormula();
      }
      throw new HopValueException(msg, e);
    }
  }

  private Formula createFormula(String formulaText) throws HopException {
    try {
      Formula result = new Formula(formulaText);
      result.initialize(context);
      return result;
    } catch (ParseException | EvaluationException e) {
      throw new HopException(e);
    }
  }

}
