package com.constellio.app.ui.pages.management.extractors.fields;

import java.util.List;

/**
 * Created by Majid on 2016-04-25.
 */
public interface MetadataPopulatorFactory {
    List<? extends GenericMetadataPopulatorField> getRegisteredTyped();
    GenericMetadataPopulatorField createInstance(Class<? extends GenericMetadataPopulatorField> type);

    String getRegisteredNameOf(Class<? extends GenericMetadataPopulatorField> type);
}
