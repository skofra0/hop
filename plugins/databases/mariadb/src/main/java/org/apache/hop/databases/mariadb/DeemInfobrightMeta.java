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
package org.apache.hop.databases.mariadb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.hop.core.Const;
import org.apache.hop.core.database.DatabaseMeta;
import org.apache.hop.core.database.DatabaseMetaPlugin;
import org.apache.hop.core.gui.plugin.GuiPlugin;
import org.apache.hop.core.row.IValueMeta;


@DatabaseMetaPlugin(type = "DEEM_INFOBRIGHT", typeDescription = "Deem Infobright")
@GuiPlugin(id = "GUI-DeemInfobrightMeta")
public class DeemInfobrightMeta extends DeemMySqlMeta {

  public static final String DEEM_INFOBRIGHT_STRING_DBLOOKUP_LIMIT = "INFOBRIGHT_STRING_DBLOOKUP_LIMIT";
  public static final String DEEM_INFOBRIGHT_INT_DBLOOKUP_LIMIT = "INFOBRIGHT_INT_DBLOOKUP_LIMIT";
  public static final String DEEM_INFOBRIGHT_STRING_DBLOOKUP_IGNORE = "INFOBRIGHT_STRING_DBLOOKUP_IGNORE";
  public static final String DEEM_INFOBRIGHT_MAX_DECIMAL_LENGTH = "INFOBRIGHT_MAX_DECIMAL_LENGTH";
  public static final String DEEM_INFOBRIGHT_MAX_DECIMAL_PRECISION = "INFOBRIGHT_MAX_DECIMAL_PRECISION";
  public static final String DEEM_INFOBRIGHT_DECIMAL_TYPE = "INFOBRIGHT_DECIMAL_TYPE";

  public static final int INFOBRIGHT_STRING_DBLOOKUP_LIMIT = Integer.parseInt(System.getProperty(DEEM_INFOBRIGHT_STRING_DBLOOKUP_LIMIT, "9"));
  public static final int INFOBRIGHT_INT_DBLOOKUP_LIMIT = Integer.parseInt(System.getProperty(DEEM_INFOBRIGHT_INT_DBLOOKUP_LIMIT, "9"));
  public static final int INFOBRIGHT_MAX_DECIMAL_LENGTH = Integer.parseInt(System.getProperty(DEEM_INFOBRIGHT_MAX_DECIMAL_LENGTH, "18"));
  public static final int INFOBRIGHT_MAX_DECIMAL_PRECISION = Integer.parseInt(System.getProperty(DEEM_INFOBRIGHT_MAX_DECIMAL_PRECISION, "6"));
  public static final String INFOBRIGHT_DECIMAL_TYPE = System.getProperty(DEEM_INFOBRIGHT_DECIMAL_TYPE, "DOUBLE"); // DECIMAL
  public static final String INFOBRIGHT_STRING_DBLOOKUP_IGNORE = System.getProperty(DEEM_INFOBRIGHT_STRING_DBLOOKUP_IGNORE, "");
  protected static List<String> stringDbLookupIgnore = Collections.emptyList();

  public static final String COMMENT_LOOKUP = " COMMENT \"LOOKUP\"";

  static {
    if (StringUtils.isNotBlank(INFOBRIGHT_STRING_DBLOOKUP_IGNORE)) {
      String[] columns = StringUtils.split(INFOBRIGHT_STRING_DBLOOKUP_IGNORE, ",");
      stringDbLookupIgnore = new ArrayList<>();
      for (String col : columns) {
        if (StringUtils.isNotBlank(col)) {
          stringDbLookupIgnore.add(col.trim());
        }
      }
    }
  }

  @Override
  public int getDefaultDatabasePort() {
    if (getAccessType() == DatabaseMeta.TYPE_ACCESS_NATIVE) {
      return 5029;
    }
    return -1;
  }

  @Override 
  public String getFieldDefinition(IValueMeta v, String tk, String pk, boolean useAutoinc, boolean addFieldname, boolean addCr) {
    String retval = "";
    String fieldname = v.getName();
    int length = v.getLength();
    int precision = v.getPrecision();
    if (precision < 0) {
      precision = 0;
    }
    if (addFieldname) {
      retval += fieldname + " ";
    }
    int type = v.getType();
    switch (type) {
      case IValueMeta.TYPE_TIMESTAMP:
      case IValueMeta.TYPE_DATE:
        if (length >= 8 && length <= 10) { // DEEM-MOD
          retval += "DATE"; // SKOFA
        } else {
          retval += "DATETIME";
        }
        break;
      case IValueMeta.TYPE_BOOLEAN:
        if (isSupportsBooleanDataType()) {
          retval += "BOOLEAN" + COMMENT_LOOKUP;
        } else {
          retval += "CHAR(1)" + COMMENT_LOOKUP;
        }
        break;

      case IValueMeta.TYPE_NUMBER:
      case IValueMeta.TYPE_INTEGER:
      case IValueMeta.TYPE_BIGNUMBER:
        if (fieldname.equalsIgnoreCase(tk) || // Technical key
            fieldname.equalsIgnoreCase(pk) // Primary key
        ) {
          if (useAutoinc) {
            retval += "BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY";
          } else {
            retval += "BIGINT NOT NULL PRIMARY KEY";
          }
        } else {
          // Integer values...
          if (precision == 0) {
            String commentLookup = COMMENT_LOOKUP;
            if (length > INFOBRIGHT_INT_DBLOOKUP_LIMIT) {
              commentLookup = "";
            }
            if (length > 9) {
              if (length < 19) {
                // can hold signed values between -9223372036854775808 and
                // 9223372036854775807
                // 18 significant digits
                retval += "BIGINT(" + length + ")" + commentLookup;
              } else {
                retval += "BIGINT(18) /* MAX LENGTH=18 */";
              }
            } else {
              if (length > 0) {
                retval += "INT(" + length + ")" + commentLookup;
              } else {
                retval += "INT " + commentLookup;
              }
            }
          }
          // Floating point values...
          else {
            // Infobright fix (do not support DECIMAL>18)
            if (length < 19) {
              if (length < 0) {
                retval += INFOBRIGHT_DECIMAL_TYPE;
              } else {
                retval += INFOBRIGHT_DECIMAL_TYPE + "(" + length; // PROBLEM MIXING TYPES IN INFOBRIGHT
                if (precision > 0) {
                  retval += ", " + precision;
                } else {
                  retval += ", 0";
                }
                retval += ")";
              }
            } else {
              // A double-precision floating-point number is accurate to approximately
              // 15 decimal places.
              // http://mysql.mirrors-r-us.net/doc/refman/5.1/en/numeric-type-overview.html
              if (precision > 0) {
                retval += "DOUBLE(" + length + ", " + precision + ")";
              } else {
                retval += "DOUBLE";
              }
            }
          }
        }
        break;
      case IValueMeta.TYPE_STRING:
        if (length > 0) {
          if (length == 1) {
            retval += "CHAR(1)" + COMMENT_LOOKUP;
          } else if (length <= INFOBRIGHT_STRING_DBLOOKUP_LIMIT && !stringDbLookupIgnore.contains(fieldname)) {
            retval += "VARCHAR(" + length + ")" + COMMENT_LOOKUP;
          } else if (length < 256) {
            retval += "VARCHAR(" + length + ")";
            // DEEM-MOD INFOBRIGHT TESTED WITH MEDIUMTEXT (ERROR IN DDL SCRIPT)
          } else {
            retval += "TEXT";
          }
        } else {
          if (length > 2000) {
            // retval += "TINYTEXT";
            retval += "TEXT";
          } else if (length > 255) {
            retval += "VARCHAR(" + length + ")";
          } else {
            retval += "VARCHAR(255)";
          }
        }
        break;
      case IValueMeta.TYPE_BINARY:
        retval += "LONGBLOB";
        break;
      default:
        retval += " UNKNOWN";
        break;
    }
    if (addCr) {
      retval += Const.CR;
    }
    return retval;
  }

}
