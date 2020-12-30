package org.ssssssss.script.functions.linq;

import org.ssssssss.script.annotation.Comment;
import org.ssssssss.script.annotation.Function;
import org.ssssssss.script.functions.NumberExtension;

public class MathFunctions {

	@Comment("四射五入保留N为小数")
	@Function
	public static double round(@Comment("目标值") Number target, @Comment("保留的小数位数") int len) {
		return NumberExtension.round(target, len);
	}

	@Comment("四射五入保留N为小数")
	@Function
	public static double round(@Comment("目标值") Number target) {
		return NumberExtension.round(target, 0);
	}

	@Comment("向上取整")
	@Function
	public static Number ceil(@Comment("目标值") Number target) {
		return NumberExtension.ceil(target);
	}

	@Comment("向下取整")
	@Function
	public static Number floor(@Comment("目标值") Number target) {
		return NumberExtension.floor(target);
	}

	@Comment("求百分比")
	@Function
	public static String precent(@Comment("目标值") Number target, @Comment("保留的小数位数") int len) {
		return NumberExtension.asPercent(target, len);
	}

	@Comment("求百分比")
	@Function
	public static String precent(@Comment("目标值") Number target) {
		return NumberExtension.asPercent(target, 0);
	}
}
