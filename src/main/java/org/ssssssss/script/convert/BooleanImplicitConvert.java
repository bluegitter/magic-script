package org.ssssssss.script.convert;

import org.ssssssss.script.parsing.ast.literal.BooleanLiteral;

public class BooleanImplicitConvert implements ClassImplicitConvert{
	@Override
	public boolean support(Class<?> from, Class<?> to) {
		return to == Boolean.class || to == boolean.class;
	}

	@Override
	public Object convert(Object source, Class<?> target) {
		return BooleanLiteral.isTrue(source);
	}
}
