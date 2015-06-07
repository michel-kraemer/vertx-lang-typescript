// Copyright 2015 Michel Kraemer
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package de.undercouch.vertx.lang.typescript.cache;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import de.undercouch.vertx.lang.typescript.compiler.Source;

/**
 * A cache storing compiled code on disk
 * @author Michel Kraemer
 */
public class DiskCache implements Cache {
  /**
   * The cache directory
   */
  private final File cacheDir;
  
  /**
   * A second-level cache keeping compiled sources in memory
   */
  private final Cache memoryCache = new InMemoryCache();
  
  /**
   * Creates a new cache
   * @param cacheDir where the cache should store compiled code
   */
  public DiskCache(File cacheDir) {
    this.cacheDir = cacheDir;
  }
  
  /**
   * Get a file in the cache
   * @param source the source
   * @return the cached file
   */
  private File getFileOnDisk(Source source) {
    return new File(cacheDir, source.getDigest());
  }
  
  @Override
  public String get(Source src) {
    // check second-level cache first
    String result = memoryCache.get(src);
    if (result == null) {
      // check if the compiled code is on disk
      File f = getFileOnDisk(src);
      if (f.exists()) {
        // load cached code
        Source rs;
        try {
          rs = Source.fromFile(f, StandardCharsets.UTF_8);
          result = rs.toString();
          memoryCache.put(src, result);
        } catch (IOException e) {
          // could not read item from disk cache
        }
      }
    }
    
    return result;
  }

  @Override
  public void put(Source src, String value) {
    // also put into second-level cache
    memoryCache.put(src, value);
    
    // make sure the cache directory exists
    if (!cacheDir.exists()) {
      cacheDir.mkdirs();
    }
    
    // write compiled code to disk
    File f = getFileOnDisk(src);
    try (Writer w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f)))) {
      w.write(value);
      w.flush();
    } catch (IOException e) {
      // could not write item to disk cache
    }
  }
}
