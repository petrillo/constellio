package com.constellio.app.ui.pages.management.extractors.fields;

/**
 * Created by Majid on 2016-05-05.
 */
public class PluginB implements MetadataPopulatorVO {
    public static final String NAME = "Plugin B";

    private String script = "";

    @Override
    public String getName() {
        return NAME;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }
}
