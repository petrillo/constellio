package com.constellio.app.api.extensions;

import com.constellio.app.api.extensions.params.componentFactory.ConstellioComponentFactory;

public abstract class ComponentFactoryExtension {
	abstract public ConstellioComponentFactory newComponentFactory();
}
