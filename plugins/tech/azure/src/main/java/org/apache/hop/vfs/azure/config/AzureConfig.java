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
package org.apache.hop.vfs.azure.config;

import java.util.LinkedHashMap;
import java.util.Map;
import no.deem.core.json.Json;
import no.deem.core.utils.Strings;
import org.apache.commons.lang3.StringUtils;
import org.apache.hop.core.logging.LogChannel;

public class AzureConfig {

  public static final String HOP_CONFIG_AZURE_CONFIG_KEY = "azure";

  private String account;
  private String key;
  private String emulatorUrl;
  private String blockIncrement = "4096"; // DEEM-MOD

  public AzureConfig() {}

  public AzureConfig(AzureConfig config) {
    setConfig(config);
  }

  // DEEM-MOD
  public void setConfig(AzureConfig config) {
    this.account = config.account;
    this.key = config.key;
    this.emulatorUrl = config.emulatorUrl;
    this.blockIncrement = config.blockIncrement;
  }

  /**
   * Gets account
   *
   * @return value of account
   */
  public String getAccount() {
    return account;
  }

  /**
   * @param account The account to set
   */
  public void setAccount(String account) {
    this.account = account;
  }

  /**
   * Gets key
   *
   * @return value of key
   */
  public String getKey() {
    return key;
  }

  /**
   * @param key The key to set
   */
  public void setKey(String key) {
    this.key = key;
  }

  /**
   * Gets emulatorUrl
   *
   * @return the url of the Azure Emulator
   */
  public String getEmulatorUrl() {
    return emulatorUrl;
  }

  /**
   * @param emulatorUrl The emulatorUrl to set
   */
  public void setEmulatorUrl(String emulatorUrl) {
    this.emulatorUrl = emulatorUrl;
  }

  /**
   * Gets blockIncrement
   *
   * @return value of blockIncrement
   */
  public String getBlockIncrement() {
    return blockIncrement;
  }

  /**
   * @param blockIncrement The blockIncrement to set
   */
  public void setBlockIncrement(String blockIncrement) {
    this.blockIncrement = blockIncrement;
  }

  public void setConnectionString(String configStr) {
    try {
      if (configStr.startsWith("{")) {
        AzureConfig azureConfig = Json.mapper().read(configStr, AzureConfig.class);
        setConfig(azureConfig);

      } else if (configStr.contains("AccountKey=") && configStr.contains("AccountName=")) {
        var values = splitToMap(configStr, ";", "=");
        setAccount(values.get("AccountName"));
        setKey(values.get("AccountKey"));
      }
    } catch (Exception e) {
      LogChannel.GENERAL.logError(
          "Error reading Azure configuration, check property '"
              + AzureConfig.HOP_CONFIG_AZURE_CONFIG_KEY
              + "' in the Hop config json file",
          e);
    }
  }

  private Map<String, String> splitToMap(
      String configStr, String lineSeparator, String keySeparator) {
    if (StringUtils.isBlank(configStr)) {
      return Map.of();
    }
    Map<String, String> values = new LinkedHashMap<>();
    for (var line : Strings.splitToList(configStr, lineSeparator)) {
      int pos = line.indexOf(keySeparator);
      if (pos >= 0) {
        String key = line.substring(0, pos);
        String value = "";
        if (pos < line.length()) {
          value = line.substring(pos + 1);
        }
        values.put(key, value);
      }
    }
    return values;
  }
}
