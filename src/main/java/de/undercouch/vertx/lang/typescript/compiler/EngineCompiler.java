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

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import jdk.nashorn.api.scripting.ScriptObjectMirror;

/**
 * Compiles TypeScript sources with the TypeScript compiler hosted by a
 * JavaScript engine
 * @author Michel Kraemer
 */
public class EngineCompiler implements TypeScriptCompiler {
  /**
   * Path to the TypeScript compiler
   */
  private static final String TYPESCRIPT_JS = "typescript/lib/typescriptServices.js";
  
  /**
   * Path to a helper script calling the TypeScript compiler
   */
  private static final String COMPILE_JS = "vertx-typescript/util/compile.js";
  
  /**
   * The JavaScript engine hosting the TypeScript compiler
   */
  private ScriptEngine engine;
  
  /**
   * Creates the JavaScript engine that hosts the TypeScript compiler. Loads
   * the compiler and a helper script and evaluates them within the engine.
   * @return the engine
   */
  private synchronized ScriptEngine getEngine() {
    if (engine != null) {
      return engine;
    }
    
    // create JavaScript engine
    ScriptEngineManager mgr = new ScriptEngineManager();
    engine = mgr.getEngineByName("nashorn");
    if (engine == null) {
      throw new IllegalStateException("Could not find Nashorn JavaScript engine.");
    }
    
    // load TypeScript compiler
    loadScript(TYPESCRIPT_JS, src -> {
      // WORKAROUND for a bug in Nashorn (https://bugs.openjdk.java.net/browse/JDK-8079426)
      // Inside the TypeScript compiler `ts.Diagnostics` is defined as a literal
      // with more than 256 items. This causes all elements to be undefined.
      // The bug has been fixed but it still occurs in Java 8u45.
      
      // find function where the diagnostics are defined
      String startStr = "ts.Diagnostics = {";
      String endStr = "};";
      int start = src.indexOf(startStr);
      int end = src.indexOf(endStr, start);
      String diagnostics = src.substring(start + startStr.length(), end);
      
      // change lines so properties are set one by one
      String[] diagLines = diagnostics.split("\n");
      for (int i = 0; i < diagLines.length; ++i) {
        diagLines[i] = diagLines[i].replaceFirst("^\\s*(.+?):", "ts.Diagnostics.$1 =");
      }
      
      // replace original lines with new ones
      String newDiagnostics = startStr + "};\n" + String.join("\n", diagLines) + ";";
      src = src.substring(0, start) + newDiagnostics + src.substring(end + endStr.length());
      
      return src;
    });
    
    // load compile.js
    loadScript(COMPILE_JS, null);
    
    return engine;
  }
  
  /**
   * Loads a JavaScript file and evaluate it within {@link #engine}
   * @param name the name of the file to load
   */
  private void loadScript(String name, Function<String, String> processSource) {
    URL url = getClass().getClassLoader().getResource(name);
    if (url == null) {
      throw new IllegalStateException("Cannot find " + name + " on classpath");
    }
    
    try {
      String src = Source.fromURL(url, StandardCharsets.UTF_8).toString();
      if (processSource != null) {
        src = processSource.apply(src);
      }
      engine.eval(src);
    } catch (ScriptException | IOException e) {
      throw new IllegalStateException("Could not evaluate " + name, e);
    }
  }
  
  @Override
  public String compile(String filename, SourceFactory sourceFactory) throws IOException {
    ScriptEngine e = getEngine();
    ScriptObjectMirror o = (ScriptObjectMirror)e.get("compileTypescript");
    return (String)o.call(null, filename, sourceFactory);
  }
}
