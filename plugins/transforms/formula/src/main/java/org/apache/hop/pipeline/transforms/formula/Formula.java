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

package org.apache.hop.pipeline.transforms.formula;

import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.row.IValueMeta;
import org.apache.hop.core.row.RowDataUtil;
import org.apache.hop.core.row.value.ValueMetaFactory;
import org.apache.hop.core.util.Utils;
import org.apache.hop.pipeline.Pipeline;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.pipeline.transform.BaseTransform;
import org.apache.hop.pipeline.transform.TransformMeta;
import org.apache.hop.pipeline.transforms.formula.runner.libformula.FormulaRunnerPentaho;
import java.util.Arrays;
import java.util.HashMap;

public class Formula extends BaseTransform<FormulaMeta, FormulaData> {

  // private XSSFWorkbook workBook; // DEEM-MOD
  // private XSSFSheet workSheet; // DEEM-MOD
  // private Row sheetRow; // DEEM-MOD
  private HashMap<String, String> replaceMap;

  @Override
  public boolean init() {
    if (super.init()) {
      // DEEM-MOD
      data.runner = new FormulaRunnerPentaho();
      data.runner.init(meta, data);
      replaceMap = new HashMap<>();

      data.returnType = new int[meta.getFormulas().size()];
      for (int i = 0; i < meta.getFormulas().size(); i++) {
        data.returnType[i] = -1;
      }
      return true;
    }
    return false;
  }

  @Override
  public void dispose() {
    try {
      data.runner.dispose(); // DEEM-MOD
    } catch (HopException e) {
      logError("Unable to close temporary workbook", e);
    }
    super.dispose();
  }

  @Override
  public boolean processRow() throws HopException {

    Object[] r = getRow();
    if (r == null) {
      setOutputDone();
      return false;
    }

    int tempIndex = getInputRowMeta().size(); // DEEM-MOD
    if (first) {
      first = false;

      data.outputRowMeta = getInputRowMeta().clone();
      meta.getFields(data.outputRowMeta, getTransformName(), null, null, this, metadataProvider);

      // Calculate replace indexes...
      //
      data.replaceIndex = new int[meta.getFormulas().size()];
      for (int j = 0; j < meta.getFormulas().size(); j++) {
        FormulaMetaFunction fn = meta.getFormulas().get(j);
        if (!Utils.isEmpty(fn.getReplaceField())) {
          data.replaceIndex[j] = getInputRowMeta().indexOfValue(fn.getReplaceField());

          // keep track of the formula fields and the fields they replace for formula parsing later on.
          replaceMap.put(fn.getFieldName(), fn.getReplaceField());
          if (data.replaceIndex[j] < 0) {
            throw new HopException("Unknown field specified to replace with a formula result: [" + fn.getReplaceField() + "]");
          }
        } else {
          data.replaceIndex[j] = -1;
        }
      }
    }

    if (log.isRowLevel()) {
      logRowlevel("Read row #" + getLinesRead() + " : " + Arrays.toString(r));
    }

    Object[] outputRowData = RowDataUtil.resizeArray(r, data.outputRowMeta.size());
    data.runner.initRow(outputRowData);

    int i = 0;
    for (var formula : meta.getFormulas()) {
      Object outputValue = data.runner.evaluate(formula, getInputRowMeta(), r, i, replaceMap);
      int realIndex = (data.replaceIndex[i] < 0) ? tempIndex++ : data.replaceIndex[i];
      outputRowData[realIndex] = getReturnValue(outputValue, data.returnType[i], realIndex, formula);
      i++;
    }
    putRow(data.outputRowMeta, outputRowData);
    if (log.isRowLevel()) {
      logRowlevel("Wrote row #" + getLinesWritten() + " : " + Arrays.toString(r));
    }
    if (checkFeedback(getLinesRead())) {
      logBasic("Linenr " + getLinesRead());
    }

    return true;
  }

  /**
   * This is the base transform that forms that basis for all transforms. You can derive from this
   * class to implement your own transforms.
   *
   * @param transformMeta The TransformMeta object to run.
   * @param meta
   * @param data the data object to store temporary data, database connections, caches, result sets,
   *        hashtables etc.
   * @param copyNr The copynumber for this transform.
   * @param pipelineMeta The PipelineMeta of which the transform transformMeta is part of.
   * @param pipeline The (running) pipeline to obtain information shared among the transforms.
   */
  public Formula(TransformMeta transformMeta, FormulaMeta meta, FormulaData data, int copyNr, PipelineMeta pipelineMeta, Pipeline pipeline) {
    super(transformMeta, meta, data, copyNr, pipelineMeta, pipeline);
  }

  protected Object getReturnValue(Object formulaResult, int returnType, int realIndex, FormulaMetaFunction fn) throws HopException {
    if (formulaResult == null) {
      return null;
    }
    Object value = null;
    switch (returnType) {
      case FormulaData.RETURN_TYPE_STRING:
        if (fn.isNeedDataConversion()) {
          value = convertDataToTargetValueMeta(realIndex, formulaResult);
        } else {
          value = formulaResult.toString();
        }
        break;
      case FormulaData.RETURN_TYPE_NUMBER:
        if (fn.isNeedDataConversion()) {
          value = convertDataToTargetValueMeta(realIndex, formulaResult);
        } else {
          value = ((Number) formulaResult).doubleValue();
        }
        break;
      case FormulaData.RETURN_TYPE_INTEGER:
        if (fn.isNeedDataConversion()) {
          value = convertDataToTargetValueMeta(realIndex, formulaResult);
        } else {
          value = formulaResult;
        }
        break;
      case FormulaData.RETURN_TYPE_LONG:
        if (fn.isNeedDataConversion()) {
          value = convertDataToTargetValueMeta(realIndex, formulaResult);
        } else {
          value = formulaResult;
        }
        break;
      case FormulaData.RETURN_TYPE_DATE:
        if (fn.isNeedDataConversion()) {
          value = convertDataToTargetValueMeta(realIndex, formulaResult);
        } else {
          value = formulaResult;
        }
        break;
      case FormulaData.RETURN_TYPE_BIGDECIMAL:
        if (fn.isNeedDataConversion()) {
          value = convertDataToTargetValueMeta(realIndex, formulaResult);
        } else {
          value = formulaResult;
        }
        break;
      case FormulaData.RETURN_TYPE_BYTE_ARRAY:
        value = formulaResult;
        break;
      case FormulaData.RETURN_TYPE_BOOLEAN:
        value = formulaResult;
        break;
      case FormulaData.RETURN_TYPE_TIMESTAMP:
        if (fn.isNeedDataConversion()) {
          value = convertDataToTargetValueMeta(realIndex, formulaResult);
        } else {
          value = formulaResult;
        }
        break;
    } // if none case is caught - null is returned.
    return value;
  }

  private Object convertDataToTargetValueMeta(int i, Object formulaResult) throws HopException {
    if (formulaResult == null) {
      return formulaResult;
    }
    IValueMeta target = data.outputRowMeta.getValueMeta(i);
    IValueMeta actual = ValueMetaFactory.guessValueMetaInterface(formulaResult);
    return target.convertData(actual, formulaResult);
  }
}
