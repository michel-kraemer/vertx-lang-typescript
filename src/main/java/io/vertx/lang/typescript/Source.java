package io.vertx.lang.typescript;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class Source {
  private final String filename;
  private final String contents;
  private String digest;
  
  private Source(String filename, String contents) {
    this.filename = filename;
    this.contents = contents;
  }
  
  public static Source fromURL(URL url, Charset cs) throws IOException {
    String filename = basename(url.getPath());
    try (InputStream is = url.openStream()) {
      byte[] barr = readFully(is);
      String source = new String(barr, cs);
      return new Source(filename, source);
    }
  }
  
  public static Source fromFile(File f, Charset cs) throws IOException {
    String filename = f.getName();
    try (InputStream is = new FileInputStream(f)) {
      byte[] barr = readFully(is);
      String source = new String(barr, cs);
      return new Source(filename, source);
    }
  }
  
  public String getFilename() {
    return filename;
  }
  
  public String getDigest() {
    if (digest == null) {
      MessageDigest md;
      try {
        md = MessageDigest.getInstance("SHA-1");
      } catch (NoSuchAlgorithmException e) {
        throw new RuntimeException(e);
      }
      
      byte[] digestBytes = md.digest(contents.getBytes(StandardCharsets.UTF_8));
      digest = Base64.getUrlEncoder().encodeToString(digestBytes);
    }
    return digest;
  }
  
  @Override
  public String toString() {
    return contents;
  }
  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((contents == null) ? 0 : contents.hashCode());
    result = prime * result + ((filename == null) ? 0 : filename.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    
    if (obj == null) {
      return false;
    }
    
    if (getClass() != obj.getClass()) {
      return false;
    }
    
    Source other = (Source) obj;
    if (contents == null) {
      if (other.contents != null) {
        return false;
      }
    } else if (!contents.equals(other.contents)) {
      return false;
    }
    
    if (filename == null) {
      if (other.filename != null) {
        return false;
      }
    } else if (!filename.equals(other.filename)) {
      return false;
    }
    
    return true;
  }

  private static String basename(String str) {
    int sl = str.lastIndexOf('/');
    if (sl >= 0) {
      return str.substring(sl + 1);
    }
    return str;
  }
  
  private static byte[] readFully(InputStream is) throws IOException {
    if (!(is instanceof BufferedInputStream)) {
      is = new BufferedInputStream(is);
    }
    
    final byte[] buf = new byte[8192];
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
      int read;
      while ((read = is.read(buf, 0, buf.length)) > 0) {
        baos.write(buf, 0, read);
      }
      return baos.toByteArray();
    }
  }
}
