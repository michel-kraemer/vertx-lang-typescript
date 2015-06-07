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

package de.undercouch.vertx.lang.typescript.compiler;

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

/**
 * Represents a source file
 * @author Michel Kraemer
 */
public class Source {
  /**
   * The file's name
   */
  private final String filename;
  
  /**
   * The file's contents
   */
  private final String contents;
  
  /**
   * The SHA-1 digest of the file's contents
   * @see #getDigest()
   */
  private String digest;
  
  /**
   * Creates a new source object
   * @param filename the file name
   * @param contents the file contents
   */
  public Source(String filename, String contents) {
    this.filename = filename;
    this.contents = contents;
  }
  
  /**
   * Creates a new source object from a URL
   * @param url the URL to read
   * @param cs the character set to use when reading
   * @return the new source object
   * @throws IOException if reading from the given URL failed
   */
  public static Source fromURL(URL url, Charset cs) throws IOException {
    String filename = basename(url.getPath());
    try (InputStream is = url.openStream()) {
      return fromStream(is, filename, cs);
    }
  }
  
  /**
   * Creates a new source object from a file
   * @param f the file to read
   * @param cs the character set to use when reading
   * @return the new source object
   * @throws IOException if reading from the given file failed
   */
  public static Source fromFile(File f, Charset cs) throws IOException {
    String filename = f.getName();
    try (InputStream is = new FileInputStream(f)) {
      return fromStream(is, filename, cs);
    }
  }
  
  /**
   * Creates a new source object from a stream. Does not close the given stream.
   * @param is the input stream to read
   * @param filename the name of the file the new source should represent
   * @param cs the character set to use when reading
   * @return the new source object
   * @throws IOException if reading from the given stream failed
   */
  public static Source fromStream(InputStream is, String filename, Charset cs) throws IOException {
    byte[] barr = readFully(is);
    String source = new String(barr, cs);
    return new Source(filename, source);
  }
  
  /**
   * @return the file name
   */
  public String getFilename() {
    return filename;
  }
  
  /**
   * Calculates the SHA-1 digest of the file's contents
   * @return the digest
   */
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

  /**
   * Gets the last element (i.e. the file name) from the given path
   * @param str the path
   * @return the last path element
   */
  private static String basename(String str) {
    int sl = str.lastIndexOf('/');
    if (sl >= 0) {
      return str.substring(sl + 1);
    }
    return str;
  }
  
  /**
   * Reads an input stream completely into a byte array. Does not close the
   * given stream.
   * @param is the input stream to read from
   * @return the bytes read from the stream
   * @throws IOException if reading failed
   */
  private static byte[] readFully(InputStream is) throws IOException {
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
