package org.ssssssss.script;

import org.junit.Assert;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class MagicScriptTests extends BaseTest{

	@Test
	public void binary(){
		execute("binary.ms");
	}

	@Test
	public void varTest(){
		execute("var.ms");
	}

	@Test
	public void tryTest(){
		Assert.assertEquals(execute("try.ms"),0);
	}

	@Test
	public void newTest(){
		Assert.assertEquals(execute("new.ms"),new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
	}

	@Test
	public void forTest(){
		Assert.assertEquals(execute("for.ms"),2700);
	}

	@Test
	public void escapeTest() {
		execute("escape.ms");
	}

	@Test
	public void lambdaTest(){
		Assert.assertEquals(execute("lambda.ms"),8);
	}
	@Test
	public void loopListTest(){
		Assert.assertEquals(execute("loopList.ms"),15);
	}
	@Test
	public void loopMapTest(){
		Assert.assertEquals(execute("loopMap.ms"),"key1key2key3-6");
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
		Assert.assertEquals(execute("if.ms",context1),1);
		Assert.assertEquals(execute("if.ms",context2),2);
		Assert.assertEquals(execute("if.ms",context3),0);
	}
}
