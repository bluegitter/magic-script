package org.ssssssss.script.functions;

import org.ssssssss.script.annotation.Comment;

import java.util.regex.Pattern;

/**
 * Pattern 扩展
 */
public class StringExtension {

    @Comment("校验文本是否符合正则")
    public boolean match(String source, Pattern pattern) {
        return pattern.matcher(source).find();
    }



}
