/*
 **************************************************************************
 *
 * Copyright 2021 - Nexus
 *
 * Based upon code from Pentaho Data Integration
 *
 **************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
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

package org.apache.hop.pipeline.transforms.formula.runner.libformula.functions;

import java.util.Calendar;
import java.util.Date;

public class DateYmd8Util {

  public static Date byYMD8(Number m3Date) {
    Date result = null;
    Calendar calendar = Calendar.getInstance();

    if (m3Date != null) {
      String date = Long.toString(m3Date.longValue());
      if (date.length() == 8) {
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Integer.valueOf(date.substring(0, 4)), Integer.valueOf(date.substring(4, 6)) - 1, Integer.valueOf(date.substring(6, 8)), 0, 0, 0);
        result = calendar.getTime();
      }
    }
    return result;
  }

  public static int toYMD8(Date date) {

    if (date == null) {
      return 0;
    }

    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    return calendar.get(Calendar.YEAR) * 10000 + (calendar.get(Calendar.MONTH) + 1) * 100 + calendar.get(Calendar.DAY_OF_MONTH);
  }

}
