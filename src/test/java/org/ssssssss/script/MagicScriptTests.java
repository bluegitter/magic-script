package org.ssssssss.script;

import org.junit.Test;

import java.util.HashMap;

public class MagicScriptTests extends BaseTest{

	@Test
	public void binary(){
		execute("binary.ms");
	}

	@Test
	public void ifTest(){
		MagicScriptContext context1 = new MagicScriptContext(new HashMap<String,Object>() {
			{
				put("a", 1);
			}
		});
		MagicScriptContext context2 = new MagicScriptContext(new HashMap<String,Object>() {
			{
				put("a", 2);
			}
		});
		MagicScriptContext context3 = new MagicScriptContext(new HashMap<String,Object>() {
			{
				put("a", 3);
			}
		});
		execute("if.ms",context1);
		execute("if.ms",context2);
		execute("if.ms",context3);
	}
}
