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

}
