package org.ssssssss.script.functions;

import org.ssssssss.script.annotation.Comment;
import org.ssssssss.script.annotation.Function;

import java.util.Iterator;

/**
 * 集合相关函数
 */
public class CollectionFunctions {

	@Function
	@Comment("区间迭代器")
	public static Iterator<Integer> range(@Comment("起始编号") int from, @Comment("结束编号") int to) {
		return new Iterator<Integer>() {
			int idx = from;

			public boolean hasNext() {
				return idx <= to;
			}

			public Integer next() {
				return idx++;
			}
		};
	}
}
