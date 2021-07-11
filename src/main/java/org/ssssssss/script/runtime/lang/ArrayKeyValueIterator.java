package org.ssssssss.script.runtime.lang;

public class ArrayKeyValueIterator extends ArrayValueIterator implements KeyIterator{

	public ArrayKeyValueIterator(Object target) {
		super(target);
	}

	public Object getKey(){
		return index;
	}
}
