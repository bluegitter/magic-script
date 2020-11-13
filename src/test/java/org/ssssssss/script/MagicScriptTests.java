package org.ssssssss.script;

import org.junit.Assert;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;

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
		Assert.assertEquals(0, execute("try.ms"));
	}

	@Test
	public void newTest(){
		Assert.assertEquals(new SimpleDateFormat("yyyy-MM-dd").format(new Date()), execute("new.ms"));
	}

	@Test
	public void forTest(){
		Assert.assertEquals(2700, execute("for.ms"));
	}

	@Test
	public void escapeTest() {
		execute("escape.ms");
	}

	@Test
	public void lambdaTest(){
		Assert.assertEquals(8, execute("lambda.ms"));
	}
	@Test
	public void loopListTest(){
		Assert.assertEquals(execute("loopList.ms"),15);
	}

	@Test
	public void loop1() {
		Assert.assertEquals(2700, execute("loop_1.ms"));
	}

	@Test
	public void async() {
		Assert.assertEquals(5050.0, execute("async.ms"));
	}
	@Test
	public void loopMapTest(){
		Assert.assertEquals("key1key2key3-6", execute("loopMap.ms"));
	}

}
