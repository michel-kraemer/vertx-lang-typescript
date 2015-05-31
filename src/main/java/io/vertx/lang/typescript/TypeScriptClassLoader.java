package io.vertx.lang.typescript;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

public class TypeScriptClassLoader extends ClassLoader {
  private Map<String, String> cache = new HashMap<>();
  private final ScriptEngine engine;
  
  public TypeScriptClassLoader(ClassLoader parent, ScriptEngine engine) {
    super(parent);
    this.engine = engine;
  }
  
  @Override
  public InputStream getResourceAsStream(String name) {
    String lowerName = name.toLowerCase();
    if (lowerName.endsWith(".ts")) {
      return load(name);
    } else if (lowerName.endsWith(".ts.js")) {
      return load(name.substring(0, name.length() - 3));
    }
    return super.getResourceAsStream(name);
  }
  
  private InputStream load(String name) {
    String code = cache.get(name);
    
    if (code == null) {
      code = compile(name);
      cache.put(name, code);
    }
    
    try {
      return new ByteArrayInputStream(code.getBytes("UTF-8"));
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException("UTF-8 is not supported in your platform", e);
    }
  }
  
  private String compile(String name) {
    try {
      SimpleBindings bindings = new SimpleBindings(engine.getBindings(ScriptContext.ENGINE_SCOPE));
      bindings.put("__parentClassLoader", getParent());
      return (String)engine.eval("compileTypescript('" + name + "');", bindings);
    } catch (ScriptException e) {
      throw new IllegalStateException("Could not compile script", e);
    }
  }
}
