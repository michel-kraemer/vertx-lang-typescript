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

package de.undercouch.vertx.lang.typescript;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.undercouch.vertx.lang.typescript.cache.InMemoryCache;
import de.undercouch.vertx.lang.typescript.compiler.EngineCompiler;
import de.undercouch.vertx.lang.typescript.compiler.NodeCompiler;
import de.undercouch.vertx.lang.typescript.compiler.Source;
import de.undercouch.vertx.lang.typescript.compiler.SourceFactory;
import de.undercouch.vertx.lang.typescript.compiler.TypeScriptCompiler;

/**
 * Test if all JavaScript examples from vertx-examples can be compiled
 * @author Michel Kraemer
 */
public class TestExamplesRunner {
  private static final Set<String> DEFAULT_DIRS_TO_SKIP = new HashSet<>();
  private static final Set<String> DEFAULT_FILES_TO_SKIP = new HashSet<>();
  static {
    /////// core-examples
    
    // skip node modules
    DEFAULT_DIRS_TO_SKIP.add("node_modules");
    
    // also skip npm examples
    DEFAULT_DIRS_TO_SKIP.add("npm");
    
    // these scripts use 'string.startsWith' which is only available on
    // java.lang.String or with ES6
    DEFAULT_FILES_TO_SKIP.add("simple_form_server.js");
    DEFAULT_FILES_TO_SKIP.add("simple_form_upload_server.js");
    
    /////// web-examples
    
    // skip browser code
    DEFAULT_DIRS_TO_SKIP.add("webroot");
    
    // we don't support the eventbus-client yet
    DEFAULT_FILES_TO_SKIP.add("web/vertxbus/node/index.js");
    
    // this example calls RoutingContext.fail() with a Throwable object
    // instead of a status code (number)
    DEFAULT_FILES_TO_SKIP.add("web/custom_authorisation/server.js");
  }
  
  private Set<String> dirsToSkip;
  private Set<String> filesToSkip;
  
  private boolean containsEndsWith(Set<String> haystack, String needle) {
    for (String s : haystack) {
      if (needle.endsWith(s)) {
        return true;
      }
    }
    return false;
  }
  
  private void getAllJavaScriptFiles(File dir, List<File> result) {
    File[] files = dir.listFiles();
    for (File f : files) {
      if (dirsToSkip != null && dirsToSkip.contains(f.getName())) {
        continue;
      }

      if (f.isDirectory()) {
        getAllJavaScriptFiles(f, result);
      } else {
        if (f.getName().toLowerCase().endsWith(".js")) {
          if (filesToSkip == null || !containsEndsWith(filesToSkip,
              f.getPath().replace(File.separatorChar, '/'))) {
            result.add(f);
          }
        }
      }
    }
  }

  private List<File> getAllJavaScriptFiles(File dir) {
    List<File> result = new ArrayList<>();
    getAllJavaScriptFiles(dir, result);
    return result;
  }
  
  private void compile(File script, TypeScriptCompiler compiler,
      SourceFactory parentSourceFactory) throws IOException {
    String name = script.getName().replaceFirst("\\.js$", ".ts");
    compiler.compile(name, new SourceFactory() {
      @Override
      public Source getSource(String filename) throws IOException {
        if (filename.equals(name)) {
          Source src = Source.fromFile(script, StandardCharsets.UTF_8);
          String srcStr = src.toString();
          
          // add default type definitions
          srcStr = "/// <reference path=\"vertx-js/vertx.d.ts\" />\n" +
              "/// <reference path=\"vertx-js/java.d.ts\" />\n\n" + srcStr;
          
          // replace 'var x = require("...")' by 'import x = require("...")'
          srcStr = srcStr.replaceAll("var\\s+(.+?=\\s*require\\(.+?\\))", "import $1");
          
          return new Source(script.getName(), srcStr);
        }
        return parentSourceFactory.getSource(filename);
      }
    });
  }
  
  public void run(File pathToExamples, List<String> dirsToSkip,
      List<String> filesToSkip) throws Exception {
    this.dirsToSkip = DEFAULT_DIRS_TO_SKIP;
    if (dirsToSkip != null) {
      this.dirsToSkip = new HashSet<>(dirsToSkip);
    }
    
    this.filesToSkip = DEFAULT_FILES_TO_SKIP;
    if (filesToSkip != null) {
      this.filesToSkip = new HashSet<>(filesToSkip);
    }
    
    List<File> javaScriptFiles = getAllJavaScriptFiles(pathToExamples);
    TypeScriptCompiler compiler;
    
    if (NodeCompiler.supportsNode()) {
      System.out.println("Using NodeCompiler ...");
      compiler = new NodeCompiler();
      run(javaScriptFiles, compiler, pathToExamples);
    }
    
    // skip EngineCompiler tests on Travis CI, because they are likely to fail
    if (Boolean.parseBoolean(System.getenv("TRAVIS"))) {
      System.out.println("Travis CI environment detected. Skipping EngineCompiler tests.");
    } else {
      System.out.println("Using EngineCompiler ...");
      compiler = new EngineCompiler();
      run(javaScriptFiles, compiler, pathToExamples);
    }
  }
  
  private void run(List<File> javaScriptFiles, TypeScriptCompiler compiler,
      File pathToExamples) throws Exception {
    SourceFactory parentSourceFactory = new TypeScriptClassLoader(
        getClass().getClassLoader(), compiler, new InMemoryCache());
    
    for (File f : javaScriptFiles) {
      String name = f.getAbsolutePath();
      name = name.substring(pathToExamples.getAbsolutePath().length() + 1);
      
      System.out.print(name + " ... ");
      try {
        compile(f, compiler, parentSourceFactory);
      } catch (Exception e) {
        System.out.println("FAILED");
        throw e;
      }
      System.out.println("OK");
    }
  }
  
  public static void main(String[] args) throws Exception {
    TestExamplesRunner runner = new TestExamplesRunner();
    runner.run(new File(args[0]), null, null);
  }
}
