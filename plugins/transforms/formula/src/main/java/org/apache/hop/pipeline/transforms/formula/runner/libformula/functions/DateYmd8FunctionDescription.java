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
package org.apache.hop.pipeline.transforms.formula.runner.libformula.functions;

import org.pentaho.reporting.libraries.formula.function.AbstractFunctionDescription;
import org.pentaho.reporting.libraries.formula.function.FunctionCategory;
import org.pentaho.reporting.libraries.formula.function.datetime.DateTimeFunctionCategory;
import org.pentaho.reporting.libraries.formula.typing.Type;
import org.pentaho.reporting.libraries.formula.typing.coretypes.DateTimeType;
import org.pentaho.reporting.libraries.formula.typing.coretypes.NumberType;

public class DateYmd8FunctionDescription extends AbstractFunctionDescription {
  private static final long serialVersionUID = 3110217290825835653L;

  public DateYmd8FunctionDescription() {
    super(
        "DATEYMD8",
        "org.apache.hop.pipeline.transforms.formula.runner.libformula.functions.DateYmd8-Function");
  }

  @Override
  public Type getValueType() {
    return DateTimeType.DATE_TYPE;
  }

  @Override
  public int getParameterCount() {
    return 1;
  }

  @Override
  public Type getParameterType(final int position) {
    return NumberType.GENERIC_NUMBER;
  }

  @Override
  public boolean isParameterMandatory(final int position) {
    return true;
  }

  @Override
  public FunctionCategory getCategory() {
    return DateTimeFunctionCategory.CATEGORY;
  }
}
