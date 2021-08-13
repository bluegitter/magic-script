package org.ssssssss.script;

import org.ssssssss.script.exception.MagicExitException;
import org.ssssssss.script.runtime.MagicScriptRuntime;

import java.io.InputStream;
import java.util.Arrays;

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
		String str = readScript(filename);
		long t = System.currentTimeMillis();
		MagicScript script = MagicScript.create(str, null);
		MagicScriptRuntime runtime = script.compile();
		System.out.println("编译耗时：" + (System.currentTimeMillis() - t) + "ms");
		Object value = null;
		t = System.currentTimeMillis();
		MagicScriptContext context = new MagicScriptContext();
		try {
			MagicScriptContext.set(context);
			value = runtime.execute(context);
		} catch(MagicExitException mee){
			value = mee.getExitValue();
		} catch (Exception e) {
			MagicScriptError.transfer(runtime, e);
		} finally {
			MagicScriptContext.remove();
		}
		System.out.println("执行耗时：" + (System.currentTimeMillis() - t) + "ms");
		System.out.println("执行结果：" + value);
		System.out.println(Arrays.toString(context.getVars()));
		return value;
	}
}
