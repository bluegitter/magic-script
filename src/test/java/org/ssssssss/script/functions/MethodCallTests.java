package org.ssssssss.script.functions;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ssssssss.script.BaseTest;
import org.ssssssss.script.reflection.AbstractReflection;

import java.util.Arrays;

public class MethodCallTests extends BaseTest {

	@BeforeClass
	public static void register() {
		AbstractReflection.getInstance().registerMethodExtension(String.class, new MethodCallTests());
	}

	public static String call(String source, int val) {
		return "call_1_" + val;
	}

	public static String call(String source, double val) {
		return "call_2_" + val;
	}

	public static String call(String source) {
		return "call_3";
	}

	public static String call(String source, int ... vals) {
		return "call_4_" + Arrays.toString(vals);
	}

	public static String call(String source, User user) {
		return "call_5_" + user;
	}

	public static String call(String source, User[] users) {
		return "call_6_" + Arrays.toString(users);
	}

	@Test
	public void method_call_1() {
		Assert.assertEquals("call_3", execute("functions/method_call_1.ms"));
	}

	@Test
	public void method_call_2() {
		Assert.assertEquals("call_1_1", execute("functions/method_call_2.ms"));
	}

	@Test
	public void method_call_3() {
		Assert.assertEquals("call_2_2.0", execute("functions/method_call_3.ms"));
	}

	@Test
	public void method_call_4() {
		Assert.assertEquals("call_5_User{age=0, weight=0.0, money=0, roles=null, name='法外狂徒'}", execute("functions/method_call_4.ms"));
	}

	@Test
	public void method_call_5() {
		Assert.assertEquals("call_4_[1, 2, 3]", execute("functions/method_call_5.ms"));
	}

}
