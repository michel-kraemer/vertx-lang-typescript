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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import com.eclipsesource.v8.JavaCallback;
import com.eclipsesource.v8.JavaVoidCallback;
import com.eclipsesource.v8.V8;
import com.eclipsesource.v8.V8Array;
import com.eclipsesource.v8.V8Object;
import com.eclipsesource.v8.V8Value;

/**
 * Compiles TypeScript sources using a V8 runtime
 * @author Michel Kraemer
 */
public class V8Compiler implements TypeScriptCompiler {
  /**
   * A unique message for {@link UncheckedFileNotFoundException}
   */
  private static final String FILENOTFOUNDEXCEPTION = "__UNCHECKEDFILENOTFOUNDEXCEPTION";
  
  /**
   * An unchecked exception that represents a {@link FileNotFoundException}
   */
  private static class UncheckedFileNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 8221868491705507209L;
    
    public UncheckedFileNotFoundException() {
      super(FILENOTFOUNDEXCEPTION);
    }
  }
  
  /**
   * The V8 runtime hosting the TypeScript compiler
   */
  private V8 runtime;
  
  /**
   * Create a V8 runtime that hosts the TypeScript compiler. Load the
   * compiler and a helper script and evaluates them within the runtime.
   * @return the runtime
   */
  private V8 getRuntime() {
    if (runtime == null) {
      // create runtime
      runtime = V8.createV8Runtime();
      
      // load TypeScript compiler and helper script
      loadScript(EngineCompiler.TYPESCRIPT_JS);
      loadScript(EngineCompiler.COMPILE_JS);
      
      // define some globals
      runtime.add("__lineSeparator", System.lineSeparator());
      JavaCallback isFileNotFoundException = (V8Object receiver, V8Array parameters) -> {
        Object e = parameters.get(0);
        return e instanceof FileNotFoundException ||
            e instanceof UncheckedFileNotFoundException ||
            (e != null && String.valueOf(e).equals(FILENOTFOUNDEXCEPTION));
      };
      runtime.registerJavaMethod(isFileNotFoundException, "__isFileNotFoundException");
      JavaVoidCallback printlnErr = (V8Object receiver, V8Array parameters) ->
        java.lang.System.err.println(parameters.get(0));
      runtime.registerJavaMethod(printlnErr, "__printlnErr");
    }
    return runtime;
  }
  
  /**
   * Load a JavaScript file and evaluate it within {@link #runtime}
   * @param name the name of the file to load
   */
  private void loadScript(String name) {
    URL url = getClass().getClassLoader().getResource(name);
    if (url == null) {
      throw new IllegalStateException("Cannot find " + name + " on classpath");
    }
    
    try {
      String src = Source.fromURL(url, StandardCharsets.UTF_8).toString();
      runtime.executeScript(src);
    } catch (IOException e) {
      throw new IllegalStateException("Could not evaluate " + name, e);
    }
  }

  @Override
  public String compile(String filename, SourceFactory sourceFactory)
      throws IOException {
    JavaCallback getSource = (V8Object receiver, V8Array parameters) -> {
      String sourceFilename = parameters.get(0).toString();
      String baseFilename = parameters.get(1).toString();
      try {
        return sourceFactory.getSource(sourceFilename, baseFilename).toString();
      } catch (FileNotFoundException e) {
        throw new UncheckedFileNotFoundException();
      } catch (IOException e) {
        throw new IllegalStateException(e);
      }
    };
    
    V8 runtime = getRuntime();
    V8Object v8sourceFactory = new V8Object(runtime);
    v8sourceFactory.registerJavaMethod(getSource, "getSource");
    
    V8Array args = new V8Array(runtime);
    args.push(filename);
    try {
      // use reflection here so we can safely call #supportsV8 without having
      // to load V8Value
      Method push = V8Array.class.getMethod("push", V8Value.class);
      push.invoke(args, v8sourceFactory);
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException(e);
    }
    
    try {
      return runtime.executeStringFunction("compileTypescript", args);
    } finally {
      args.release();
      v8sourceFactory.release();
    }
  }
  
  /**
   * @return true if the V8 runtime is available, false otherwise
   */
  public static boolean supportsV8() {
    try {
      Class.forName("com.eclipsesource.v8.V8");
      return true;
    } catch (Throwable e) {
      return false;
    }
  }
}
