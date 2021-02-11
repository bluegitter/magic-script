package org.ssssssss.script.functions.linq;

import org.ssssssss.script.annotation.Comment;
import org.ssssssss.script.annotation.Function;
import org.ssssssss.script.functions.DateExtension;

import java.util.Date;

/**
 * Linq中的函数
 */
public class LinqFunctions {

	@Function
	@Comment("判断值是否为空")
	public Object ifnull(@Comment("目标值") Object target, @Comment("为空的值") Object trueValue) {
		return target == null ? trueValue : target;
	}

	@Function
	@Comment("日期格式化")
	public String date_format(@Comment("目标日期") Date target, @Comment("格式") String pattern) {
		return target == null ? null : DateExtension.format(target, pattern);
	}

	@Function
	@Comment("日期格式化")
	public String date_format(@Comment("目标日期") Date target) {
		return target == null ? null : DateExtension.format(target, "yyyy-MM-dd HH:mm:ss");
	}

	@Function
	@Comment("取当前时间")
	public Date now() {
		return new Date();
	}


}
