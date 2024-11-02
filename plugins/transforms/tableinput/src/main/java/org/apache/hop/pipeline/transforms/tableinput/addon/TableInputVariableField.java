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
package org.apache.hop.pipeline.transforms.tableinput.addon;

import java.util.Objects;
import org.apache.hop.metadata.api.HopMetadataProperty;

public class TableInputVariableField {

  @HopMetadataProperty(key = "field_name", injectionKey = "FIELD_NAME")
  private String fieldName;

  @HopMetadataProperty(key = "variable_name", injectionKey = "VARIABLE_NAME")
  private String variableName;

  @HopMetadataProperty(key = "default_value", injectionKey = "DEFAULT_VALUE")
  private String defaultValue;

  public TableInputVariableField() {}

  /**
   * @param fieldName
   * @param variableName
   * @param defaultValue
   */
  public TableInputVariableField(String fieldName, String variableName, String defaultValue) {
    this.fieldName = fieldName;
    this.variableName = variableName;
    this.defaultValue = defaultValue;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TableInputVariableField that = (TableInputVariableField) o;
    return fieldName.equals(that.fieldName) //
        && variableName.equals(that.variableName) //
        && defaultValue == that.defaultValue; //
  }

  @Override
  public int hashCode() {
    return Objects.hash(fieldName, variableName, defaultValue);
  }

  /**
   * @return Returns the fieldName.
   */
  public String getFieldName() {
    return fieldName;
  }

  /**
   * @param fieldName The fieldName to set.
   */
  public void setFieldName(String fieldName) {
    this.fieldName = fieldName;
  }

  /**
   * @return Returns the defaultValue.
   */
  public String getDefaultValue() {
    return defaultValue;
  }

  public void setDefaultValue(String defaultValue) {
    this.defaultValue = defaultValue;
  }

  /**
   * @return the variableName
   */
  public String getVariableName() {
    return variableName;
  }

  /**
   * @param variableName the variableName to set
   */
  public void setVariableName(String variableName) {
    this.variableName = variableName;
  }
}
