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

/**
 * Compile a TypeScript file to JavaScript
 * @param file the name of the file to compile
 * @returns {String} the generated JavaScript code
 */
function compileTypescript(file) {
  var output = "";
  var opts = ts.getDefaultCompilerOptions();

  // enable commonjs modules
  opts.module = 1; // 1 = CommonJS

  // prepare a host object that we can pass to the TypeScript compiler
  var host = {
    getDefaultLibFilename: function() {
      return "typescript/bin/" + (opts.target === 2 ? "lib.es6.d.ts" : "lib.d.ts");
    },

    getCurrentDirectory: function() {
      return '';
    },

    useCaseSensitiveFileNames: function() {
      return true;
    },

    getCanonicalFileName: function(name) {
      return name;
    },

    getNewLine: function() {
      return java.lang.System.lineSeparator();
    },

    getSourceFile: function(filename, languageVersion, onError) {
      // use TypeScriptClassLoader to load the given file
      var body;
      try {
        var input = __sourceFactory.getSource(filename);
        body = input.toString();
      } catch (e) {
        if (e instanceof java.io.FileNotFoundException) {
          // the original version of this method just returns 'undefined'
          // if it could not find a file
          return undefined;
        }
        if (onError) {
          onError(e.getMessage() || "Unknown error");
        }
        body = "";
      }

      return ts.createSourceFile(filename, body, opts.target, '0');
    },

    writeFile: function(filename, data, writeByteOrderMark, onError) {
      output += data;
    }
  };

  var prog = ts.createProgram([file], opts, host);

  var errs = prog.getDiagnostics();
  if (errs.length) {
    // TODO print diagnostics to errout
    throw errs;
  }

  function reportDiagnostic(diagnostic) {
    var output = "";
    if (diagnostic.file) {
        var loc = diagnostic.file.getLineAndCharacterFromPosition(diagnostic.start);
        output += diagnostic.file.filename + "(" + loc.line + "," + loc.character + "): ";
    }
    var category = ts.DiagnosticCategory[diagnostic.category].toLowerCase();
    output += category + " TS" + diagnostic.code + ": " + diagnostic.messageText + host.getNewLine();
    java.lang.System.err.println(output);
  }

  // report errors
  var checker = prog.getTypeChecker(true);
  errs = checker.getDiagnostics();
  if (errs.length) {
    errs.forEach(function(err) {
      reportDiagnostic(err);
    });
    throw "Could not compile source file " + file;
  }

  // generate code now
  checker.emitFiles();

  return output;
}
