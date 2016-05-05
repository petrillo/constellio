package com.constellio.app.ui.pages.management.extractors.fields;

import com.constellio.app.ui.framework.components.fields.list.ListAddRemoveField;

/**
 * Created by Majid on 2016-04-25.
 */
public class ListAddRemoveMetadataPopulatorField extends ListAddRemoveField<TypedMetadataPopulatorVO, GenericMetadataPopulatorField> {

    @Override
    protected GenericMetadataPopulatorField newAddEditField() {
        return new GenericMetadataPopulatorField();
    }

    @Override
    protected String getItemCaption(Object itemId) {
        return ((TypedMetadataPopulatorVO)itemId).getType();
    }
}
