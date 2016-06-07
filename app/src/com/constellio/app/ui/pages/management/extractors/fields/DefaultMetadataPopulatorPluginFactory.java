package com.constellio.app.ui.pages.management.extractors.fields;

import com.constellio.app.ui.pages.management.extractors.plugin.*;
import com.constellio.app.ui.pages.management.extractors.plugin.regexPopulatorPlugin.RegexPlugin;
import com.constellio.model.services.records.extractions.populator.MetadataPopulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by Majid on 2016-06-02.
 */
public class DefaultMetadataPopulatorPluginFactory implements MetadataPopulatorPluginFactory {
    private final static Logger LOGGER = LoggerFactory.getLogger(DefaultMetadataPopulatorPluginFactory.class);

    private final Map<Class<? extends MetadataPopulator>, MetadataPopulatorToVOBuilder> metadataPopulatorToVOBuilderMap = new HashMap<>();
    private final Map<Class<? extends MetadataPopulatorVO>, VOToMetadataPopulatorBuilder> voToMetadataPopulatorBuilderMap = new HashMap<>();
    private final Map<String, MetadataPopulatorPlugin> plugins = new TreeMap<>();
    private final Map<Class<? extends MetadataPopulatorVO>, String> voToPluginName = new HashMap<>();

    DefaultMetadataPopulatorPluginFactory(){

    }

    public static MetadataPopulatorPluginFactory getInstance() {
        MetadataPopulatorPluginFactory instance = new DefaultMetadataPopulatorPluginFactory();
        instance.register(new RegexPlugin());
        return instance;
    }

    @Override
    public Set<String> getRegisteredPluginNames() {
        return Collections.unmodifiableSet(plugins.keySet());
    }

    @Override
    public MetadataPopulatorPlugin createInstance(String aPluginName) {
        return plugins.get(aPluginName);
    }

    @Override
    public void register(MetadataPopulatorPlugin aPlugin) {
        final MetadataPopulatorPlugin oldPlugin = plugins.put(aPlugin.getName(), aPlugin);
        if (oldPlugin != null)
            LOGGER.warn("The metadata plugin %s has been replaced with a new plugin", aPlugin.getName());

        metadataPopulatorToVOBuilderMap.put(aPlugin.getMetadataPopulatorClass(), aPlugin.getMetadataPopulatorToVOBuilder());
        voToMetadataPopulatorBuilderMap.put(aPlugin.getMetadataPopulatorVOClass(), aPlugin.getVOToMetadataPopulatorBuilder());
        voToPluginName.put(aPlugin.getMetadataPopulatorVOClass(), aPlugin.getName());
    }

    @Override
    public Map<Class<? extends MetadataPopulator>, MetadataPopulatorToVOBuilder> getMetadataPopulatorToVoBuilders() {
        return Collections.unmodifiableMap(metadataPopulatorToVOBuilderMap);
    }

    @Override
    public Map<Class<? extends MetadataPopulatorVO>, VOToMetadataPopulatorBuilder> getVoToMetadataPopulatorBuilders() {
        return Collections.unmodifiableMap(voToMetadataPopulatorBuilderMap);
    }

    @Override
    public String getPluginName(Class<? extends MetadataPopulatorVO> voClass) {
        return voToPluginName.get(voClass);
    }
}
