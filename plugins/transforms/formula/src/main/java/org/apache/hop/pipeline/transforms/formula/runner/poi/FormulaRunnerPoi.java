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
package org.apache.hop.pipeline.transforms.formula.runner.poi;

import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.exception.HopValueException;
import org.apache.hop.core.row.IRowMeta;
import org.apache.hop.core.row.IValueMeta;
import org.apache.hop.pipeline.transforms.formula.FormulaData;
import org.apache.hop.pipeline.transforms.formula.FormulaMeta;
import org.apache.hop.pipeline.transforms.formula.FormulaMetaFunction;
import org.apache.hop.pipeline.transforms.formula.runner.FormulaRunner;
import org.apache.hop.pipeline.transforms.formula.util.FormulaParser;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.IOException;
import java.util.Map;

/**
 * DEEM-MOD
 */
public class FormulaRunnerPoi extends FormulaRunner {

  private XSSFWorkbook workBook;
  private XSSFSheet workSheet;
  private Row sheetRow;

  @Override
  public void init(FormulaMeta meta, FormulaData data) {
    super.init(meta, data);
    workBook = new XSSFWorkbook();
    workSheet = workBook.createSheet();
    sheetRow = workSheet.createRow(0);
  }

  @Override
  public void dispose() throws HopException {
    try {
      if (workBook != null) {
        workBook.close();
      }
    } catch (IOException e) {
      throw new HopException(e);
    }
  }

  @Override
  public void initRow(Object[] outputRowData) throws HopException {
    super.initRow(outputRowData);
    if (sheetRow != null) {
      workSheet.removeRow(sheetRow);
    }
    sheetRow = workSheet.createRow(0);
  }

  @Override
  public Object evaluate(FormulaMetaFunction formula, IRowMeta inputRowMeta, Object[] outputRowData, int i, Map<String, String> replaceMap) throws HopValueException {
    Object outputValue = null;
    FormulaParser parser = new FormulaParser(formula, inputRowMeta, outputRowData, sheetRow, null, replaceMap);
    CellValue cellValue = parser.getFormulaValue();
    CellType cellType = cellValue.getCellType();

    switch (cellType) {
      case BLANK:
        // should never happen.
        break;
      case NUMERIC:
        outputValue = cellValue.getNumberValue();
        int outputValueType = formula.getValueType();

        switch (outputValueType) {
          case IValueMeta.TYPE_NUMBER:
            data.returnType[i] = FormulaData.RETURN_TYPE_NUMBER;
            formula.setNeedDataConversion(formula.getValueType() != IValueMeta.TYPE_NUMBER);
            break;
          case IValueMeta.TYPE_INTEGER:
            data.returnType[i] = FormulaData.RETURN_TYPE_INTEGER;
            formula.setNeedDataConversion(formula.getValueType() != IValueMeta.TYPE_NUMBER);
            break;
          case IValueMeta.TYPE_BIGNUMBER:
            data.returnType[i] = FormulaData.RETURN_TYPE_BIGDECIMAL;
            formula.setNeedDataConversion(formula.getValueType() != IValueMeta.TYPE_NUMBER);
            break;
          case IValueMeta.TYPE_DATE:
            outputValue = DateUtil.getJavaDate(cellValue.getNumberValue());
            data.returnType[i] = FormulaData.RETURN_TYPE_DATE;
            formula.setNeedDataConversion(formula.getValueType() != IValueMeta.TYPE_NUMBER);
            break;
          case IValueMeta.TYPE_TIMESTAMP:
            data.returnType[i] = FormulaData.RETURN_TYPE_TIMESTAMP;
            formula.setNeedDataConversion(formula.getValueType() != IValueMeta.TYPE_NUMBER);
            break;
          default:
            break;
        }
        // get cell value
        break;
      case BOOLEAN:
        outputValue = cellValue.getBooleanValue();
        data.returnType[i] = FormulaData.RETURN_TYPE_BOOLEAN;
        formula.setNeedDataConversion(formula.getValueType() != IValueMeta.TYPE_BOOLEAN);
        break;
      case STRING:
        outputValue = cellValue.getStringValue();
        data.returnType[i] = FormulaData.RETURN_TYPE_STRING;
        formula.setNeedDataConversion(formula.getValueType() != IValueMeta.TYPE_STRING);
        break;
      default:
        break;
    }
    return outputValue;
  }

}
