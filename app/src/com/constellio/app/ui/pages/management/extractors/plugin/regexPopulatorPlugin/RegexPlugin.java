package com.constellio.app.ui.pages.management.extractors.plugin.regexPopulatorPlugin;

import com.constellio.app.ui.pages.management.extractors.plugin.*;
import com.constellio.model.services.records.extractions.populator.MetadataPopulator;
import com.constellio.model.services.records.extractions.populator.plugin.regexPopulatorPlugin.RegexPopulator;

/**
 * Created by Majid on 2016-04-26.
 */
public class RegexPlugin implements MetadataPopulatorPlugin {
    private String name = "Regex";

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Class<? extends MetadataPopulatorVO> getMetadataPopulatorVOClass() {
        return RegexPopulatorVO.class;
    }

    @Override
    public Class<? extends MetadataPopulator> getMetadataPopulatorClass() {
        return RegexPopulator.class;
    }

    @Override
    public MetadataPopulatorField getMetadataPopulatorField() {
        return new RegexPluginField();
    }

    @Override
    public MetadataPopulatorToVOBuilder getMetadataPopulatorToVOBuilder() {
        return new RegexPopulatorToVOBuilder();
    }

    @Override
    public VOToMetadataPopulatorBuilder getVOToMetadataPopulatorBuilder() {
        return new VOToRegexPopulatorBuilder();
    }

}
