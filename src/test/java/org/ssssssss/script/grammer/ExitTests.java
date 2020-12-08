package org.ssssssss.script.grammer;

import org.junit.Assert;
import org.junit.Test;
import org.ssssssss.script.BaseTest;
import org.ssssssss.script.parsing.ast.statement.Exit;

public class ExitTests extends BaseTest {

	private Object doExecute(String filename){
		Object object = execute(filename);
		assert object instanceof Exit.Value;
		return object;
	}

	@Test
	public void exit_1(){
		Assert.assertEquals("Value{values=[123, hello, 0]}",doExecute("grammar/exit/exit_1.ms").toString());
	}

	@Test
	public void exit_2(){
		Assert.assertEquals("Value{values=[200, success]}",doExecute("grammar/exit/exit_2.ms").toString());
	}
}
