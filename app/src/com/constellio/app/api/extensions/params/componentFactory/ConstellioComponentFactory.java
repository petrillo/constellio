package com.constellio.app.api.extensions.params.componentFactory;

import com.vaadin.ui.Component;

public interface ConstellioComponentFactory {
	Component newComponent(ComponentFactoryParams params);
}
