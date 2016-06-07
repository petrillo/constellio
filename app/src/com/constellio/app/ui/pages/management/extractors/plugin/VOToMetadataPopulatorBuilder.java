package com.constellio.app.ui.pages.management.extractors.plugin;

import com.constellio.model.services.records.extractions.populator.MetadataPopulator;

/**
 * Created by Majid on 2016-06-02.
 */
public interface VOToMetadataPopulatorBuilder {
    MetadataPopulator build(MetadataPopulatorVO metadataPopulatorVO);
}
