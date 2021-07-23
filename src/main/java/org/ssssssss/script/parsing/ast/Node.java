package org.ssssssss.script.parsing.ast;

import org.ssssssss.script.asm.Opcodes;
import org.ssssssss.script.compile.MagicScriptCompiler;
import org.ssssssss.script.parsing.Span;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 节点
 */
public abstract class Node implements Opcodes {
	/**
	 * 对应的文本
	 */
	private final Span span;

	/**
	 * 在Linq中
	 */
	private boolean inLinq;

	public Node(Span span) {
		this.span = span;
	}

	public static List<Span> mergeSpans(Object... nodes) {
		List<Span> dest = new ArrayList<>();
		for (Object node : nodes) {
			if (node instanceof List) {
				List<Node> nodeList = (List<Node>) node;
				nodeList.forEach(it -> dest.addAll(it.visitSpan()));
			} else if (node instanceof Node) {
				dest.addAll(((Node) node).visitSpan());
			} else if (node instanceof Span) {
				dest.add((Span) node);
			}
		}
		return dest;
	}

	public Span getSpan() {
		return span;
	}

	public boolean isInLinq() {
		return inLinq;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + ":" + span.getText();
	}

	public List<Span> visitSpan() {
		return Collections.singletonList(getSpan());
	}

	public void visitMethod(MagicScriptCompiler compiler) {

	}

	public void compile(MagicScriptCompiler compiler) {
		throw new UnsupportedOperationException(this.getClass().getSimpleName() + "不支持编译");
	}

}