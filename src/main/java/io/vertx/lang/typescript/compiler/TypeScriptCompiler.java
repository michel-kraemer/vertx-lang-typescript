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

import java.io.IOException;

/**
 * Compiles TypeScript source files
 * @author Michel Kraemer
 */
public interface TypeScriptCompiler {
  /**
   * Compiles the given TypeScript file
   * @param filename the name of the file to compile
   * @param sourceFactory the factory that loads source files
   * @return the generated code
   * @throws IOException if one of the source files to compile could not be loaded
   */
  String compile(String filename, SourceFactory sourceFactory) throws IOException;
}
