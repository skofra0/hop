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
package org.apache.hop.compatibility;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * This interface provides a way to look at a Number, String, Integer, Date... the same way. The
 * methods mentioned in
 * this interface are common to all Value types.
 */
public interface IValue {
  int getType();

  String getTypeDesc();

  String getString();

  double getNumber();

  Date getDate();

  boolean getBoolean();

  long getInteger();

  BigDecimal getBigNumber();

  Serializable getSerializable();

  byte[] getBytes();

  void setString(String string);

  void setNumber(double number);

  void setDate(Date date);

  void setBoolean(boolean bool);

  void setInteger(long number);

  void setBigNumber(BigDecimal number);

  void setSerializable(Serializable ser);

  void setBytes(byte[] b);

  int getLength();

  int getPrecision();

  void setLength(int length);

  void setPrecision(int precision);

  void setLength(int length, int precision);

  Object clone();
}
