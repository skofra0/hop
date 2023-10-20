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
package org.apache.hop.pipeline.transforms.formula.runner.libformula.functions;

import org.pentaho.reporting.libraries.formula.EvaluationException;
import org.pentaho.reporting.libraries.formula.FormulaContext;
import org.pentaho.reporting.libraries.formula.LibFormulaErrorValue;
import org.pentaho.reporting.libraries.formula.function.Function;
import org.pentaho.reporting.libraries.formula.function.ParameterCallback;
import org.pentaho.reporting.libraries.formula.lvalues.TypeValuePair;
import org.pentaho.reporting.libraries.formula.typing.Type;
import org.pentaho.reporting.libraries.formula.typing.TypeRegistry;
import org.pentaho.reporting.libraries.formula.typing.coretypes.DateTimeType;
import java.util.Date;

public class DateYmd8Function implements Function {
  private static final long serialVersionUID = -906531902889630172L;

  public DateYmd8Function() {
    // NOP
  }

  @Override
  public String getCanonicalName() {
    return "DATEYMD8";
  }

  @Override
  public TypeValuePair evaluate(final FormulaContext context, final ParameterCallback parameters) throws EvaluationException {
    if (parameters.getParameterCount() != 1) {
      throw EvaluationException.getInstance(LibFormulaErrorValue.ERROR_ARGUMENTS_VALUE);
    }

    final TypeRegistry typeRegistry = context.getTypeRegistry();
    final Type type = parameters.getType(0);
    final Object value = parameters.getValue(0);

    final Number date1 = typeRegistry.convertToNumber(type, value);
    final Date date = DateYmd8Util.byYMD8(date1);

    return new TypeValuePair(DateTimeType.DATE_TYPE, date);

  }
}
