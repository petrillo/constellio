package com.constellio.app.ui.pages.management.extractors.plugin;


import com.constellio.model.services.records.extractions.populator.MetadataPopulator;

public interface MetadataPopulatorPlugin {
    String getName();
    Class<? extends MetadataPopulatorVO> getMetadataPopulatorVOClass();
    Class<? extends MetadataPopulator> getMetadataPopulatorClass();
    MetadataPopulatorField getMetadataPopulatorField();
    MetadataPopulatorToVOBuilder getMetadataPopulatorToVOBuilder();
    VOToMetadataPopulatorBuilder getVOToMetadataPopulatorBuilder();
}
