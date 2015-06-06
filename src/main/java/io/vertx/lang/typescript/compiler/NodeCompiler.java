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

package io.vertx.lang.typescript.compiler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;

/**
 * Compiles TypeScript files using Node.js
 * @author Michel Kraemer
 */
public class NodeCompiler implements TypeScriptCompiler {
  /**
   * Path to the TypeScript compiler
   */
  private static final String TYPESCRIPT_JS = "typescript/bin/tsc.js";
  
  /**
   * Path to a script setting a special system for the TypeScript compiler.
   */
  private static final String NODE_COMPILER_SYS_JS = "vertx-typescript/util/node_compiler_sys.js";
  
  /**
   * A temporary file holding the actual TypeScript compiler
   */
  private File temporaryCompiler;
  
  private String getTemporaryCompiler(SourceFactory sourceFactory) throws IOException {
    if (temporaryCompiler == null) {
      Source tscSrc = sourceFactory.getSource(TYPESCRIPT_JS);
      Source nodeCompilerSysSrc = sourceFactory.getSource(NODE_COMPILER_SYS_JS);
      String tscSrcStr = tscSrc.toString();
      tscSrcStr = tscSrcStr.replaceFirst("(?m)^ts\\.executeCommandLine",
          Matcher.quoteReplacement(nodeCompilerSysSrc.toString()) + "\n$0");
      temporaryCompiler = File.createTempFile("VERTX_TYPESCRIPT_", ".js");
      temporaryCompiler.deleteOnExit();
      try (Writer w = new OutputStreamWriter(new FileOutputStream(temporaryCompiler))) {
        w.write(tscSrcStr);
        w.flush();
      }
    }
    return temporaryCompiler.getAbsolutePath();
  }
  
  @Override
  public String compile(String filename, SourceFactory sourceFactory)
      throws IOException {
    String temporaryCompilerPath = getTemporaryCompiler(sourceFactory);
    
    ProcessBuilder processBuilder = new ProcessBuilder("node", temporaryCompilerPath, filename);
    processBuilder.redirectErrorStream(true);
    
    Process process = processBuilder.start();
    InputStream pis = process.getInputStream();
    OutputStream pos = process.getOutputStream();
    
    BufferedReader pr = new BufferedReader(new InputStreamReader(pis, StandardCharsets.UTF_8));
    Writer pw = new OutputStreamWriter(pos, StandardCharsets.UTF_8);
    
    StringBuffer out = new StringBuffer();
    String line;
    while ((line = pr.readLine()) != null) {
      if (line.startsWith("VERTX_TYPESCRIPT_READFILE")) {
        // compiler wants us to read a file
        String fileToRead = line.substring("VERTX_TYPESCRIPT_READFILE".length());
        Source src = sourceFactory.getSource(fileToRead);
        
        // send length of file contents, a space character and then the file contents
        String contents = src.toString();
        pw.append(String.valueOf(contents.length()));
        pw.append(' ');
        pw.append(contents);
        pw.flush();
      } else {
        out.append(line + "\n");
      }
    }
    
    int code;
    try {
      code = process.waitFor();
    } catch (InterruptedException e) {
      throw new IllegalStateException("Node.js died prematurely", e);
    }
    
    if (code != 0) {
      throw new IllegalStateException("Could not compile script. Exit code: " +
          code + "\n" + out.toString());
    }
    
    return out.toString();
  }
  
  public static boolean supportsNode() {
    try {
      ProcessBuilder processBuilder = new ProcessBuilder("node", "-v");
      processBuilder.redirectErrorStream(true);
      Process process = processBuilder.start();
      InputStream pis = process.getInputStream();
      BufferedReader pr = new BufferedReader(new InputStreamReader(pis, StandardCharsets.UTF_8));
      String version = null;
      String line;
      while ((line = pr.readLine()) != null) {
        if (version == null) {
          version = line;
        }
      }
      int code = process.waitFor();
      return (code == 0);
    } catch (IOException | InterruptedException e) {
      return false;
    }
  }
}
