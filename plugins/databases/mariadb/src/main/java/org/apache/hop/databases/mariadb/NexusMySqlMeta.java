/*
 **************************************************************************
 *
 * Copyright 2021 - Nexus
 *
 * Based upon code from Pentaho Data Integration
 *
 **************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **************************************************************************
 */

package org.apache.hop.databases.mariadb;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.hop.core.Const;
import org.apache.hop.core.database.DatabaseMetaPlugin;
import org.apache.hop.core.exception.HopDatabaseException;
import org.apache.hop.core.gui.plugin.GuiPlugin;
import org.apache.hop.core.row.IValueMeta;


@DatabaseMetaPlugin(type = "NEXUS_MYSQL", typeDescription = "Nexus MySQL")
@GuiPlugin(id = "GUI-NexusMySqlDatabaseMeta")
public class NexusMySqlMeta extends MariaDBDatabaseMeta {

  @Override
  public String getFieldDefinition(IValueMeta v, String tk, String pk, boolean useAutoinc, boolean addFieldname, boolean addCr) {
    String retval = "";

    String fieldname = v.getName();
    int length = v.getLength();
    int precision = v.getPrecision();

    if (addFieldname) {
      retval += fieldname + " ";
    }

    int type = v.getType();
    switch (type) {
      case IValueMeta.TYPE_TIMESTAMP:
      case IValueMeta.TYPE_DATE:
        if (length >= 8 && length <= 10) { // DEEM-MOD
          retval += "DATE"; // DEEM-MOD
        } else {
          retval += "DATETIME";
        }
        break;
      case IValueMeta.TYPE_BOOLEAN:
        if (isSupportsBooleanDataType()) {
          retval += "BOOLEAN";
        } else {
          retval += "CHAR(1)";
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
            if (length > 9) {
              if (length < 19) {
                // can hold signed values between -9223372036854775808 and 9223372036854775807
                // 18 significant digits
                retval += "BIGINT(" + length + ")";
              } else {
                retval += "DECIMAL(" + length + ")";
              }
            } else {
              if (length > 0) {
                retval += "INT(" + length + ")";
              } else {
                retval += "INT";
              }
            }
          }
          // Floating point values...
          else {
            if (length > 15) {
              retval += "DECIMAL(" + length;
              if (precision > 0) {
                retval += ", " + precision;
              }
              retval += ")";
            } else {
              // A double-precision floating-point number is accurate to approximately 15 decimal places.
              // http://mysql.mirrors-r-us.net/doc/refman/5.1/en/numeric-type-overview.html
              retval += "DOUBLE";
            }
          }
        }
        break;
      case IValueMeta.TYPE_STRING:
        if (length > 0) {
          if (length == 1) {
            retval += "CHAR(1)";
          } else if (length < 256) {
            retval += "VARCHAR(" + length + ")";
          } else if (length < 65536) {
            retval += "TEXT";
          } else if (length < 16777216) {
            retval += "MEDIUMTEXT";
          } else {
            retval += "LONGTEXT";
          }
        } else {
          retval += "TINYTEXT";
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

  /**
   * @return true if the database is streaming results (normally this is an option just for MySQL).
   */
  @Override
  public boolean isStreamingResults() {
    return true;
  }

  /**
   * This method allows a database dialect to convert database specific data types to Kettle data types.
   *
   * @param resultSet
   *        The result set to use
   * @param valueMeta
   *        The description of the value to retrieve
   * @param index
   *        the index on which we need to retrieve the value, 0-based.
   * @return The correctly converted Kettle data type corresponding to the valueMeta description.
   * @throws KettleDatabaseException
   */
  // DEEM-MOD (Problem with newer MariaDB jdbc drivers Date/Timestamp)
  @Override
  public Object getValueFromResultSet(ResultSet rs, IValueMeta val, int i) throws HopDatabaseException {
    Object data = null;
    try {
      switch (val.getType()) {
        case IValueMeta.TYPE_DATE:
        case IValueMeta.TYPE_TIMESTAMP:
          if (val.getOriginalColumnType() == java.sql.Types.DATE) {
            return rs.getDate(i + 1);
          } else if (val.getOriginalColumnType() == java.sql.Types.TIME) {
            return rs.getTime(i + 1);
          } else if (val.getPrecision() != 1 && isSupportsTimeStampToDateConversion()) {
            data = rs.getTimestamp(i + 1);
            break; // Timestamp extends java.util.Date
          } else {
            data = rs.getDate(i + 1);
            break;
          }
        default:
          return super.getValueFromResultSet(rs, val, i);
      }
      if (rs.wasNull()) {
        data = null;
      }
    } catch (SQLException e) {
      throw new HopDatabaseException("Unable to get value '" + val.toStringMeta() + "' from database resultset, index " + i, e);
    }

    return data;
  }

}
