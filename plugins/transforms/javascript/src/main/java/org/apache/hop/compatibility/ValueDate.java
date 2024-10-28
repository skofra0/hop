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
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.hop.core.Const;
import org.apache.hop.core.row.IValueMeta;

/** This class contains a Value of type Date. */
public class ValueDate implements IValue, Cloneable {
  public static final String DATE_FORMAT = "yyyy/MM/dd HH:mm:ss.SSS";
  private Date date;
  public int precision;

  public ValueDate() {
    this.date = null;
    this.precision = -1;
  }

  public ValueDate(Date date) {
    this.date = date;
    this.precision = -1;
  }

  @Override
  public int getType() {
    return IValueMeta.TYPE_DATE;
  }

  @Override
  public String getTypeDesc() {
    return "Date";
  }

  @Override
  public String getString() {
    if (date == null) {
      return null;
    }
    SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT);
    return df.format(date);
  }

  @Override
  public double getNumber() {
    if (date == null) {
      return 0.0;
    }
    return date.getTime();
  }

  @Override
  public Date getDate() {
    return date;
  }

  @Override
  public boolean getBoolean() {
    return false;
  }

  @Override
  public long getInteger() {
    if (date == null) {
      return 0L;
    }
    return date.getTime();
  }

  @Override
  public void setString(String string) {
    this.date = Const.toDate(string, null);
  }

  @Override
  public void setSerializable(Serializable ser) {}

  @Override
  public void setNumber(double number) {
    this.date = new Date((long) number);
  }

  @Override
  public void setDate(Date date) {
    this.date = date;
  }

  @Override
  public void setBoolean(boolean bool) {
    this.date = null;
  }

  @Override
  public void setInteger(long number) {
    this.date = new Date(number);
  }

  @Override
  public int getLength() {
    return -1;
  }

  @Override
  public int getPrecision() {
    return precision;
  }

  @Override
  public void setLength(int length, int precision) {
    this.precision = precision;
  }

  @Override
  public void setLength(int length) {}

  @Override
  public void setPrecision(int precision) {
    this.precision = precision;
  }

  @Override
  public Object clone() {
    try {
      ValueDate retval = (ValueDate) super.clone();
      return retval;
    } catch (CloneNotSupportedException e) {
      return null;
    }
  }

  @Override
  public BigDecimal getBigNumber() {
    if (date == null) {
      return BigDecimal.ZERO;
    }
    return new BigDecimal(date.getTime());
  }

  @Override
  public void setBigNumber(BigDecimal number) {
    setInteger(number.longValue());
  }

  @Override
  public Serializable getSerializable() {
    return date;
  }

  @Override
  public byte[] getBytes() {
    return null;
  }

  @Override
  public void setBytes(byte[] b) {}
}
