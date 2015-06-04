package io.vertx.lang.typescript;

import io.vertx.lang.typescript.cache.Cache;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

public class TypeScriptClassLoader extends ClassLoader {
  private Map<String, Source> sourceCache = new HashMap<>();
  private final Cache codeCache;
  private final ScriptEngine engine;
  
  public TypeScriptClassLoader(ClassLoader parent, ScriptEngine engine, Cache codeCache) {
    super(parent);
    this.engine = engine;
    this.codeCache = codeCache;
  }
  
  @Override
  public InputStream getResourceAsStream(String name) {
    try {
      String lowerName = name.toLowerCase();
      if (lowerName.endsWith(".ts")) {
        return load(name);
      } else if (lowerName.endsWith(".ts.js")) {
        return load(name.substring(0, name.length() - 3));
      }
      return super.getResourceAsStream(name);
    } catch (IOException e) {
      return null;
    }
  }
  
  public Source getSource(String name) throws IOException {
    Source result = sourceCache.get(name);
    if (result == null) {
      URL u = getParent().getResource(name);
      if (u != null) {
        result = Source.fromURL(u, StandardCharsets.UTF_8);
      }
      if (result == null) {
        result = Source.fromFile(new File(name), StandardCharsets.UTF_8);
      }
      sourceCache.put(name, result);
    }
    return result;
  }
  
  private InputStream load(String name) throws IOException {
    Source src = getSource(name);
    String code = codeCache.get(src);
    if (code == null) {
      code = compile(name);
      codeCache.put(src, code);
    }
    return new ByteArrayInputStream(code.getBytes(StandardCharsets.UTF_8));
  }
  
  private String compile(String name) {
    try {
      SimpleBindings bindings = new SimpleBindings(engine.getBindings(ScriptContext.ENGINE_SCOPE));
      bindings.put("__typeScriptClassLoader", this);
      return (String)engine.eval("compileTypescript('" + name + "');", bindings);
    } catch (ScriptException e) {
      throw new IllegalStateException("Could not compile script", e);
    }
  }
}
