package org.ssssssss.script.runtime;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class MagicScriptClassLoader extends ClassLoader{


	public synchronized Class<MagicScriptRuntime> load(String className, byte[] bytecode) throws ClassNotFoundException {
		defineClass(className, bytecode, 0 , bytecode.length);
		try {
			Files.write(Paths.get("E:\\idea\\MagicAPI\\magic-script\\src\\test\\java\\org\\ssssssss\\script","bytecode.class"), bytecode);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return (Class<MagicScriptRuntime>) loadClass(className);
	}
}
