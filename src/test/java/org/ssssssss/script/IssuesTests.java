package org.ssssssss.script;

import org.junit.Assert;
import org.junit.Test;

public class IssuesTests extends BaseTest {

	@Test
	public void i252vy() {
		Assert.assertEquals(true, execute("issues/I252VY.ms"));
	}
}