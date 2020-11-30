package org.ssssssss.script.functions;

import org.ssssssss.script.annotation.Comment;

import java.util.regex.Pattern;

/**
 * Pattern 扩展
 */
public class PatternExtension {

    @Comment("校验文本是否符合正则")
    public static boolean test(Pattern pattern, String source) {
        return pattern.matcher(source).find();
    }



}
