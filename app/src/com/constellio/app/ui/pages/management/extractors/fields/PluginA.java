package com.constellio.app.ui.pages.management.extractors.fields;

/**
 * Created by Majid on 2016-04-26.
 */
public class PluginA implements MetadataPopulatorVO {
    private String name = "Plugin A";

    private String config = "Test Config";

    @Override
    public String getName() {
        return name;
    }

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }
}
