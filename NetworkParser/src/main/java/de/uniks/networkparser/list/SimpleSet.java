package de.uniks.networkparser.list;

import java.util.Set;

public class SimpleSet<V> extends AbstractList<V> implements Set<V> {
	@Override
	public AbstractList<V> getNewInstance() {
		return new SimpleSet<V>();
	}

	@Override
	public boolean remove(Object o) {
		// TODO Auto-generated method stub
		return false;
	}
}
