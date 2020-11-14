package org.ssssssss.script;

import org.junit.Assert;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;

public class GrammarTests extends BaseTest {

	@Test
	public void binary() {
		execute("grammar/binary.ms");
	}

	@Test
	public void varTest() {
		execute("grammar/var.ms");
	}

	@Test
	public void tryTest() {
		Assert.assertEquals(0, execute("grammar/try.ms"));
	}

	@Test
	public void newTest() {
		Assert.assertEquals(new SimpleDateFormat("yyyy-MM-dd").format(new Date()), execute("grammar/new.ms"));
	}

	@Test
	public void forTest() {
		Assert.assertEquals(2700, execute("grammar/for.ms"));
	}

	@Test
	public void escapeTest() {
		execute("grammar/escape.ms");
	}

	@Test
	public void lambdaTest() {
		Assert.assertEquals(8, execute("grammar/lambda.ms"));
	}

	@Test
	public void loopListTest() {
		Assert.assertEquals(execute("grammar/loopList.ms"), 15);
	}

	@Test
	public void loop1() {
		Assert.assertEquals(2700, execute("grammar/loop_1.ms"));
	}

	@Test
	public void async() {
		Assert.assertEquals(5050.0, execute("grammar/async.ms"));
	}

	@Test
	public void loopMapTest() {
		Assert.assertEquals("key1key2key3-6", execute("grammar/loopMap.ms"));
	}

	@Test
	public void recursionTest() {
		Assert.assertEquals(55, execute("grammar/recursion.ms"));
	}

	@Test
	public void var_scope_1_test() {
		Assert.assertEquals(3, execute("grammar/var_scope_1.ms"));
	}

	@Test
	public void var_scope_2_test() {
		Assert.assertEquals(13, execute("grammar/var_scope_2.ms"));
	}

}