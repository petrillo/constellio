package com.constellio.app.ui.pages.management.extractors.fields;

import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.framework.components.fields.list.ListAddRemoveField;
import com.constellio.app.ui.pages.management.extractors.plugin.MetadataPopulatorPluginFactory;
import com.constellio.app.ui.pages.management.extractors.plugin.MetadataPopulatorVO;

import java.util.List;

/**
 * Created by Majid on 2016-04-25.
 */
public class ListAddRemoveMetadataPopulatorField extends ListAddRemoveField<MetadataPopulatorVO, GenericMetadataPopulatorField> {
    private final MetadataPopulatorPluginFactory factory;

    public ListAddRemoveMetadataPopulatorField(MetadataPopulatorPluginFactory factory){
        this.factory = factory;
    }

    @Override
    protected GenericMetadataPopulatorField newAddEditField() {
        return new GenericMetadataPopulatorField(factory);
    }

    @Override
    protected String getItemCaption(Object itemId) {
        return factory.getPluginName((Class<? extends MetadataPopulatorVO>) itemId.getClass());
    }

    public void setMetadataOptions(List<MetadataVO> metadataOptions) {
        getAddEditField().setMetadataOptions(metadataOptions);
    }
}
