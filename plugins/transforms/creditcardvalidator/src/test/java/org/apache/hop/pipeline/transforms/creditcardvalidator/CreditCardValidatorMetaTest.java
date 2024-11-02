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
package org.apache.hop.pipeline.transforms.creditcardvalidator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.hop.core.exception.HopTransformException;
import org.apache.hop.core.row.IValueMeta;
import org.apache.hop.core.row.RowMeta;
import org.apache.hop.core.variables.Variables;
import org.apache.hop.pipeline.transform.TransformSerializationTestUtil;
import org.junit.Assert;
import org.junit.Test;

public class CreditCardValidatorMetaTest {
  @Test
  public void testLoadSave() throws Exception {
    CreditCardValidatorMeta meta =
        TransformSerializationTestUtil.testSerialization(
            "/credit-card-validator-transform.xml", CreditCardValidatorMeta.class);
    Assert.assertNotNull(meta.getFieldName());
    Assert.assertNotNull(meta.getResultFieldName());
    Assert.assertNotNull(meta.getNotValidMessage());
    Assert.assertTrue(meta.isOnlyDigits());
    Assert.assertNotNull(meta.getCardType());
  }

  @Test
  public void testSupportsErrorHandling() {
    assertTrue(new CreditCardValidatorMeta().supportsErrorHandling());
  }

  @Test
  public void testDefaults() {
    CreditCardValidatorMeta meta = new CreditCardValidatorMeta();
    meta.setDefault();
    assertEquals("result", meta.getResultFieldName());
    assertFalse(meta.isOnlyDigits());
    assertEquals("card type", meta.getCardType());
    assertEquals("not valid message", meta.getNotValidMessage());
  }

  @Test
  public void testGetFields() throws HopTransformException {
    CreditCardValidatorMeta meta = new CreditCardValidatorMeta();
    meta.setDefault();
    meta.setResultFieldName("The Result Field");
    meta.setCardType("The Card Type Field");
    meta.setNotValidMessage("Is Card Valid");

    RowMeta rowMeta = new RowMeta();
    meta.getFields(rowMeta, "this transform", null, null, new Variables(), null);
    assertEquals(3, rowMeta.size());
    assertEquals("The Result Field", rowMeta.getValueMeta(0).getName());
    assertEquals(IValueMeta.TYPE_BOOLEAN, rowMeta.getValueMeta(0).getType());
    assertEquals("this transform", rowMeta.getValueMeta(0).getOrigin());
    assertEquals("The Card Type Field", rowMeta.getValueMeta(1).getName());
    assertEquals(IValueMeta.TYPE_STRING, rowMeta.getValueMeta(1).getType());
    assertEquals("this transform", rowMeta.getValueMeta(1).getOrigin());
    assertEquals("Is Card Valid", rowMeta.getValueMeta(2).getName());
    assertEquals(IValueMeta.TYPE_STRING, rowMeta.getValueMeta(2).getType());
    assertEquals("this transform", rowMeta.getValueMeta(2).getOrigin());
  }
}
