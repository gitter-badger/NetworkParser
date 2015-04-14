package de.uniks.networkparser.gui;

import de.uniks.networkparser.interfaces.SendableEntityCreator;

@FunctionalInterface
public interface CellHandler {
	public boolean onAction(Object entity, SendableEntityCreator creator, double x, double y);
}
