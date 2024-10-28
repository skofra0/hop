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
import org.apache.hop.core.row.IValueMeta;

/**
 * This class contains a Value of type Binary. It's supposed to contain CLOBS, LOBS, ... GIF data,
 * jpg's, ...
 */
public class ValueBinary implements IValue, Cloneable {

  protected byte[] bytes;
  private int length;

  public ValueBinary() {
    this.bytes = null;
    this.length = -1;
  }

  public ValueBinary(byte[] bytes) {
    this.bytes = bytes;
    this.length = -1;
  }

  @Override
  public byte[] getBytes() {
    return bytes;
  }

  @Override
  public void setBytes(byte[] b) {
    this.bytes = b;
  }

  @Override
  public int getPrecision() {
    return 0;
  }

  @Override
  public String getString() {
    return (bytes != null) ? new String(bytes) : null;
  }

  @Override
  public int getType() {
    return IValueMeta.TYPE_BINARY;
  }

  @Override
  public String getTypeDesc() {
    return "Binary";
  }

  @Override
  public Object clone() {
    try {
      ValueBinary retval = (ValueBinary) super.clone();
      return retval;
    } catch (CloneNotSupportedException e) {
      return null;
    }
  }

  @Override
  public void setLength(int length) {
    this.length = length;
  }

  @Override
  public void setLength(int length, int precision) {
    this.length = length;
  }

  @Override
  public int getLength() {
    return length;
  }

  // These dont do anything but are needed for the IValue
  @Override
  public Serializable getSerializable() {
    return null;
  }

  @Override
  public void setBigNumber(BigDecimal number) {}

  @Override
  public void setBoolean(boolean bool) {}

  @Override
  public void setDate(Date date) {}

  @Override
  public void setInteger(long number) {}

  @Override
  public void setNumber(double number) {}

  @Override
  public void setPrecision(int precision) {}

  @Override
  public void setString(String string) {}

  @Override
  public void setSerializable(Serializable ser) {}

  @Override
  public BigDecimal getBigNumber() {
    return null;
  }

  @Override
  public boolean getBoolean() {
    return false;
  }

  @Override
  public Date getDate() {
    return null;
  }

  @Override
  public long getInteger() {
    return 0;
  }

  @Override
  public double getNumber() {
    return 0;
  }
}
