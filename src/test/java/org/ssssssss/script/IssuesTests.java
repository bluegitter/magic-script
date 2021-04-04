package org.ssssssss.script;

import org.junit.Assert;
import org.junit.Test;

public class IssuesTests extends BaseTest {

	@Test
	public void i252vy() {
		Assert.assertEquals(true, execute("issues/I252VY.ms"));
	}

	@Test
	public void i29lqg() {
		Assert.assertEquals(true, execute("issues/I29LQG.ms"));
	}

	@Test
	public void bug_function_call() {
		Assert.assertEquals(123, execute("issues/bug_function_call.ms"));
	}

	@Test
	public void bug_var() {
		Assert.assertEquals(15, execute("issues/bug_var.ms"));
	}

	@Test
	public void bug_assigment() {
		Assert.assertEquals(1, execute("issues/bug_assigment.ms"));
	}

	@Test
	public void i398nd() {
		Assert.assertEquals(true, execute("issues/I398ND.ms"));
	}
}
