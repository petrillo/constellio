package com.constellio.app.ui.entities;

import com.constellio.app.ui.pages.management.extractors.fields.MetadataPopulatorVO;
import com.constellio.model.services.records.extractions.MetadataPopulator;

/**
 * Created by Majid on 2016-04-25.
 */
public class MetadataPopulatorToVOBuilder {
    public MetadataPopulatorVO build(MetadataPopulator populator) {
        return new MetadataPopulatorVO() {
            @Override
            public String getName() {
                return "builder";
            }
        };
    }
}
