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
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;

import de.undercouch.vertx.lang.typescript.cache.InMemoryCache;
import de.undercouch.vertx.lang.typescript.compiler.EngineCompiler;
import de.undercouch.vertx.lang.typescript.compiler.NodeCompiler;
import de.undercouch.vertx.lang.typescript.compiler.Source;
import de.undercouch.vertx.lang.typescript.compiler.SourceFactory;
import de.undercouch.vertx.lang.typescript.compiler.TypeScriptCompiler;
import de.undercouch.vertx.lang.typescript.compiler.V8Compiler;

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
    DEFAULT_DIRS_TO_SKIP.add("vertxbus");
    
    // this example calls RoutingContext.fail() with a Throwable object
    // instead of a status code (number)
    DEFAULT_FILES_TO_SKIP.add("web/custom_authorisation/server.js");
    
    // no valid TypeScript (variable email needs to be of type 'any')
    DEFAULT_FILES_TO_SKIP.add("mail_headers.js");
    DEFAULT_FILES_TO_SKIP.add("mail_login.js");
  }
  
  private Set<String> dirsToSkip;
  private Set<String> filesToSkip;
  
  private TypeScriptCompiler nodeCompiler;
  private TypeScriptCompiler engineCompiler;
  private TypeScriptCompiler v8Compiler;
  
  private TypeScriptCompiler getNodeCompiler() {
    if (nodeCompiler == null) {
      nodeCompiler = new NodeCompiler();
    }
    return nodeCompiler;
  }
  
  private TypeScriptCompiler getEngineCompiler() {
    if (engineCompiler == null) {
      engineCompiler = new EngineCompiler();
    }
    return engineCompiler;
  }
  
  private TypeScriptCompiler getV8Compiler() {
    if (v8Compiler == null) {
      v8Compiler = new V8Compiler();
    }
    return v8Compiler;
  }
  
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
              FilenameUtils.separatorsToUnix(f.getPath()))) {
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
      SourceFactory parentSourceFactory, File pathToTypings) throws IOException {
    String name = FilenameUtils.separatorsToUnix(script.getPath().replaceFirst("\\.js$", ".ts"));
    compiler.compile(name, new SourceFactory() {
      @Override
      public Source getSource(String filename, String baseFilename) throws IOException {
        if (FilenameUtils.equalsNormalized(filename, name)) {
          Source src = Source.fromFile(script, StandardCharsets.UTF_8);
          String srcStr = src.toString();
          
          // find all required vertx modules
          Pattern requireVertx = Pattern.compile("var\\s+.+?=\\s*require\\s*\\(\\s*\"(vertx-.+?)\"\\s*\\)");
          Matcher requireVertxMatcher = requireVertx.matcher(srcStr);
          List<String> modules = new ArrayList<>();
          modules.add("vertx-js/vertx.d.ts");
          modules.add("vertx-js/java.d.ts");
          while (requireVertxMatcher.find()) {
            String mod = requireVertxMatcher.group(1);
            modules.add(mod);
          }
          
          // add default type definitions
          Path relPathToTypings = script.toPath().getParent().relativize(pathToTypings.toPath());
          for (String mod : modules) {
            srcStr = "/// <reference path=\"" + FilenameUtils.separatorsToUnix(
                relPathToTypings.resolve(mod).toString()) + "\" />\n" + srcStr;
          }
          
          // replace 'var x = require("...")' by 'import x = require("...")'
          srcStr = srcStr.replaceAll("var\\s+(.+?=\\s*require\\s*\\(.+?\\))", "import $1");
          
          return new Source(script.toURI(), srcStr);
        }
        return parentSourceFactory.getSource(filename, baseFilename);
      }
    });
  }
  
  public void run(File pathToExamples, File pathToTypings, List<String> dirsToSkip,
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
    
    if (V8Compiler.supportsV8()) {
      System.out.println("Using V8Compiler ...");
      compiler = getV8Compiler();
      run(javaScriptFiles, compiler, pathToExamples, pathToTypings);
    }
    
    if (NodeCompiler.supportsNode()) {
      System.out.println("Using NodeCompiler ...");
      compiler = getNodeCompiler();
      run(javaScriptFiles, compiler, pathToExamples, pathToTypings);
    }
    
    // skip EngineCompiler tests on Travis CI, because they are likely to fail
    if (Boolean.parseBoolean(System.getenv("TRAVIS"))) {
      System.out.println("Travis CI environment detected. Skipping EngineCompiler tests.");
    } else {
      System.out.println("Using EngineCompiler ...");
      compiler = getEngineCompiler();
      run(javaScriptFiles, compiler, pathToExamples, pathToTypings);
    }
  }
  
  private void run(List<File> javaScriptFiles, TypeScriptCompiler compiler,
      File pathToExamples, File pathToTypings) throws Exception {
    SourceFactory parentSourceFactory = new TypeScriptClassLoader(
        getClass().getClassLoader(), compiler, new InMemoryCache());
    
    for (File f : javaScriptFiles) {
      String name = f.getAbsolutePath();
      name = name.substring(pathToExamples.getAbsolutePath().length() + 1);
      
      System.out.print(name + " ... ");
      long start = System.currentTimeMillis();
      try {
        compile(f, compiler, parentSourceFactory, pathToTypings);
      } catch (Exception e) {
        System.out.println("FAILED (" + (System.currentTimeMillis() - start) + " ms)");
        throw e;
      }
      System.out.println("OK (" + (System.currentTimeMillis() - start) + " ms)");
    }
  }
  
  public static void main(String[] args) throws Exception {
    TestExamplesRunner runner = new TestExamplesRunner();
    runner.run(new File(args[0]), new File(args[1]), null, null);
  }
}
