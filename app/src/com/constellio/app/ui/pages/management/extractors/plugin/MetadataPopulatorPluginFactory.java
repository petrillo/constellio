package com.constellio.app.ui.pages.management.extractors.plugin;

import com.constellio.model.services.records.extractions.populator.MetadataPopulator;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

public interface MetadataPopulatorPluginFactory extends Serializable {
    Set<String> getRegisteredPluginNames();
    MetadataPopulatorPlugin createInstance(String aPluginName);
    void register(MetadataPopulatorPlugin aPlugin);

    Map<Class<? extends MetadataPopulator>, MetadataPopulatorToVOBuilder> getMetadataPopulatorToVoBuilders();

    Map<Class<? extends MetadataPopulatorVO>, VOToMetadataPopulatorBuilder> getVoToMetadataPopulatorBuilders();
    String getPluginName(Class<? extends MetadataPopulatorVO> voClass);
}
