package org.ssssssss.script.runtime;

import org.ssssssss.script.compile.MagicScriptCompiler;
import org.ssssssss.script.MagicScript;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class MagicScriptClassLoader extends ClassLoader{


	public synchronized Class<MagicScriptRuntime> load(String className, byte[] bytecode) throws ClassNotFoundException {
		defineClass(className, bytecode, 0 , bytecode.length);
		return (Class<MagicScriptRuntime>) loadClass(className);
	}
}
