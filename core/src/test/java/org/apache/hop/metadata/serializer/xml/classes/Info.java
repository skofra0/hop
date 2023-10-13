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
package org.apache.hop.metadata.serializer.xml.classes;

import org.apache.hop.metadata.api.HopMetadataProperty;

public class Info {
  @HopMetadataProperty private String a;
  @HopMetadataProperty private String b;

  public Info() {
  }

  public Info(String a, String b) {
    this.a = a;
    this.b = b;
  }

  /**
   * Gets a
   *
   * @return value of a
   */
  public String getA() {
    return a;
  }

  /**
   * Sets a
   *
   * @param a value of a
   */
  public void setA(String a) {
    this.a = a;
  }

  /**
   * Gets b
   *
   * @return value of b
   */
  public String getB() {
    return b;
  }

  /**
   * Sets b
   *
   * @param b value of b
   */
  public void setB(String b) {
    this.b = b;
  }
}
