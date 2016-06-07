package com.constellio.app.ui.pages.management.extractors.plugin;

import com.constellio.model.services.records.extractions.populator.MetadataPopulator;

public interface MetadataPopulatorToVOBuilder {
    public MetadataPopulatorVO build(MetadataPopulator populator);
}
