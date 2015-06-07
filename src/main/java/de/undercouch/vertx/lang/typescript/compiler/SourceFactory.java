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

/**
 * Loads source files
 * @author Michel Kraemer
 */
public interface SourceFactory {
  /**
   * Loads a source file
   * @param filename the name of the file to load
   * @return the source object
   * @throws IOException if the source file could not be loaded
   */
  Source getSource(String filename) throws IOException;
}
