package org.ssssssss.script.functions;

import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.annotation.Comment;
import org.ssssssss.script.annotation.Function;

import java.util.Arrays;
import java.util.UUID;

public class MagicScriptFunctions {

	@Comment("生成uuid字符串，不包含`-`")
	@Function
	public String uuid(){
		return UUID.randomUUID().toString().replace("-","");
	}

	@Comment("导出当前变量信息")
	@Function
	public static void dump(){
		MagicScriptContext context = MagicScriptContext.get();
		if(context != null){
			System.out.println(Arrays.toString(context.getVars()));
		}

	}

}
