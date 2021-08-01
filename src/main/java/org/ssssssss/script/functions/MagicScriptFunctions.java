package org.ssssssss.script.functions;

import org.ssssssss.script.annotation.Comment;
import org.ssssssss.script.annotation.Function;

import java.util.UUID;

public class MagicScriptFunctions {

	@Comment("生成uuid字符串，不包含`-`")
	@Function
	public String uuid(){
		return UUID.randomUUID().toString().replace("-","");
	}

	@Comment("判断对象是否不是`NULL`")
	@Function
	public boolean not_null(Object value){
		return value != null;
	}

	@Comment("判断对象是否是`NULL`")
	@Function
	public boolean is_null(Object value){
		return value == null;
	}

	@Comment("打印")
	@Function
	public void print(Object target){
		System.out.print(target);
	}

	@Comment("换行打印")
	@Function
	public void println(Object target){
		System.out.println(target);
	}

	@Comment("格式化打印")
	@Function
	public void printf(String format, Object ... args){
		System.out.printf(format, args);
	}
}
