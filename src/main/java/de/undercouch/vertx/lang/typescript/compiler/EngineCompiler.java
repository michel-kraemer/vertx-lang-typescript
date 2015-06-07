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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

/**
 * Compiles TypeScript sources with the TypeScript compiler hosted by a
 * JavaScript engine
 * @author Michel Kraemer
 */
public class EngineCompiler implements TypeScriptCompiler {
  /**
   * Path to the TypeScript compiler
   */
  private static final String TYPESCRIPT_JS = "typescript/bin/typescriptServices.js";
  
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
    loadScript(TYPESCRIPT_JS);
    
    // load compile.js
    loadScript(COMPILE_JS);
    
    return engine;
  }
  
  /**
   * Loads a JavaScript file and evaluate it within {@link #engine}
   * @param name the name of the file to load
   */
  private void loadScript(String name) {
    URL url = getClass().getClassLoader().getResource(name);
    if (url == null) {
      throw new IllegalStateException("Cannot find " + name + " on classpath");
    }

    try (Reader r = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"))) {
      engine.eval(r);
    } catch (ScriptException | IOException e) {
      throw new IllegalStateException("Could not evaluate " + name, e);
    }
  }
  
  @Override
  public String compile(String filename, SourceFactory sourceFactory) throws IOException {
    try {
      ScriptEngine e = getEngine();
      SimpleBindings bindings = new SimpleBindings(e.getBindings(ScriptContext.ENGINE_SCOPE));
      bindings.put("__sourceFactory", sourceFactory);
      return (String)e.eval("compileTypescript('" + filename + "');", bindings);
    } catch (ScriptException e) {
      throw new IllegalStateException("Could not compile \"" + filename + "\"", e);
    }
  }
}
