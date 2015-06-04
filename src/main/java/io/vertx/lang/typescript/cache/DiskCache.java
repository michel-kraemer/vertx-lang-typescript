package io.vertx.lang.typescript.cache;

import io.vertx.lang.typescript.Source;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

public class DiskCache implements Cache {
  private final File cacheDir;
  private final Cache memoryCache = new InMemoryCache();
  
  public DiskCache(File cacheDir) {
    this.cacheDir = cacheDir;
  }
  
  private File getFileOnDisk(Source source) {
    return new File(cacheDir, source.getDigest());
  }
  
  @Override
  public String get(Source src) {
    String result = memoryCache.get(src);
    if (result == null) {
      File f = getFileOnDisk(src);
      if (f.exists()) {
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
    memoryCache.put(src, value);
    
    if (!cacheDir.exists()) {
      cacheDir.mkdirs();
    }
    
    File f = getFileOnDisk(src);
    try (Writer w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f)))) {
      w.write(value);
      w.flush();
    } catch (IOException e) {
      // could not write item to disk cache
    }
  }
}
