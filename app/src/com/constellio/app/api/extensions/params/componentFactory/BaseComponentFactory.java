package com.constellio.app.api.extensions.params.componentFactory;

import com.sun.star.uno.RuntimeException;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;

public class BaseComponentFactory implements ConstellioComponentFactory {
	@Override
	public Component newComponent(ComponentFactoryParams params) {
		switch (params.componentType) {
		case HORIZONTAL_LAYOUT:
			return new HorizontalLayout();
		case TABLE:
			return new Table();
		default:
			throw new RuntimeException("Unsupported type " + params.componentType);
		}
	}
}
