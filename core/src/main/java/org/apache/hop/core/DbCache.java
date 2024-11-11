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
package org.apache.hop.core;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import org.apache.hop.core.exception.HopEofException;
import org.apache.hop.core.exception.HopFileException;
import org.apache.hop.core.logging.ILogChannel;
import org.apache.hop.core.logging.LogChannel;
import org.apache.hop.core.row.IRowMeta;
import org.apache.hop.core.row.RowMeta;

/**
 * This class caches database queries so that the same query doesn't get called twice. Queries are
 * often launched to the databases to get information on tables etc.
 */
public class DbCache {
  private static DbCache dbCache;

  private Hashtable<DbCacheEntry, IRowMeta> cache;
  private boolean usecache;

  private ILogChannel log;

  public void setActive() {
    setActive(true);
  }

  public void setInactive() {
    setActive(false);
  }

  public void setActive(boolean act) {
    usecache = act;
  }

  public boolean isActive() {
    return usecache;
  }

  public void put(DbCacheEntry entry, IRowMeta fields) {
    if (!usecache) {
      return;
    }

    IRowMeta copy = fields.clone();
    cache.put(entry, copy);
  }

  /**
   * Get the fields as a row generated by a database cache entry
   *
   * @param entry the entry to look for
   * @return the fields as a row generated by a database cache entry
   */
  public IRowMeta get(DbCacheEntry entry) {
    if (!usecache) {
      return null;
    }

    IRowMeta fields = cache.get(entry);
    if (fields != null) {
      fields = fields.clone(); // Copy it again!
    }

    return fields;
  }

  public int size() {
    return cache.size();
  }

  /**
   * Clear out all entries of database with a certain name
   *
   * @param dbname The name of the database for which we want to clear the cache or null if we want
   *     to clear it all.
   */
  public void clear(String dbname) {
    if (dbname == null) {
      cache = new Hashtable<>();
      setActive();
    } else {
      Enumeration<DbCacheEntry> keys = cache.keys();
      while (keys.hasMoreElements()) {
        DbCacheEntry entry = keys.nextElement();
        if (entry.sameDB(dbname)) {
          // Same name: remove it!
          cache.remove(entry);
        }
      }
    }
  }

  public String getFilename() {
    return Const.HOP_AUDIT_FOLDER + Const.FILE_SEPARATOR + "db.cache";
  }

  private DbCache() throws HopFileException {
    try {
      clear(null);

      // Serialization support for the DB cache
      //
      log = new LogChannel("DbCache");

      String filename = getFilename();
      File file = new File(filename);
      if (file.canRead()) {
        log.logDetailed("Loading database cache from file: [" + filename + "]");

        try (FileInputStream fis = new FileInputStream(file);
            DataInputStream dis = new DataInputStream(fis)) {
          int counter = 0;
          try {
            while (true) {
              DbCacheEntry entry = new DbCacheEntry(dis);
              IRowMeta row = new RowMeta(dis);
              cache.put(entry, row);
              counter++;
            }
          } catch (HopEofException eof) {
            log.logDetailed("We read " + counter + " cached rows from the database cache!");
          }
        } catch (Exception e) {
          throw new Exception(e);
        }
      } else {
        log.logDetailed("The database cache doesn't exist yet.");
      }
    } catch (Exception e) {
      throw new HopFileException("Couldn't read the database cache", e);
    }
  }

  public void saveCache() throws HopFileException {
    try {
      // Serialization support for the DB cache
      //
      String filename = getFilename();
      File file = new File(filename);
      if (!file.exists() || file.canWrite()) {

        try (FileOutputStream fos = new FileOutputStream(file);
            DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(fos, 10000))) {

          int counter = 0;

          Enumeration<DbCacheEntry> keys = cache.keys();
          while (keys.hasMoreElements()) {
            // Save the database cache entry
            DbCacheEntry entry = keys.nextElement();
            entry.write(dos);

            // Save the corresponding row as well.
            IRowMeta rowMeta = get(entry);
            if (rowMeta != null) {
              rowMeta.writeMeta(dos);
              counter++;
            } else {
              throw new HopFileException(
                  "The database cache contains an empty row. We can't save this!");
            }
          }

          log.logDetailed("We wrote " + counter + " cached rows to the database cache!");
        } catch (Exception e) {
          throw new Exception(e);
        }
      } else {
        throw new HopFileException("We can't write to the cache file: " + filename);
      }
    } catch (Exception e) {
      throw new HopFileException("Couldn't write to the database cache", e);
    }
  }

  /**
   * Create the database cache instance by loading it from disk
   *
   * @return the database cache instance.
   */
  public static final DbCache getInstance() {
    if (dbCache != null) {
      return dbCache;
    }
    try {
      dbCache = new DbCache();
    } catch (HopFileException kfe) {
      throw new RuntimeException("Unable to create the database cache: " + kfe.getMessage());
    }
    return dbCache;
  }
}
