package org.ssssssss.script;

import java.io.InputStream;

public class BaseTest {

	public static String readScript(String filename) {
		try (InputStream is = BaseTest.class.getResourceAsStream("/" + filename)) {
			byte[] buf = new byte[1024];
			StringBuilder sb = new StringBuilder();
			int len = -1;
			while ((len = is.read(buf, 0, buf.length)) != -1) {
				sb.append(new String(buf, 0, len));
			}
			return sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Object execute(String filename) {
		MagicScript script = MagicScript.create(readScript(filename), null);
		return MagicScriptEngine.execute(script, new MagicScriptContext());
	}
}
