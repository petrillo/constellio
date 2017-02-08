package com.constellio.model.entities.schemas;

import java.io.Serializable;

public interface StructureFactory<T extends ModifiableStructure> extends Serializable {

	T build(String string);

	String toString(T structure);

}
