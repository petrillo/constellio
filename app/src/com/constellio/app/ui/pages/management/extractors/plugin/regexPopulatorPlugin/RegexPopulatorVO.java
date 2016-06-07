package com.constellio.app.ui.pages.management.extractors.plugin.regexPopulatorPlugin;

import com.constellio.app.ui.pages.management.extractors.plugin.MetadataPopulatorVO;

/**
 * Created by Majid on 2016-06-02.
 */
public class RegexPopulatorVO implements MetadataPopulatorVO{
    //supplier
    private String metadataCode;

    //extractor
    private String regexPattern;
    private String value;
    private boolean substitute;


    public String getMetadataCode() {
        return metadataCode;
    }

    public void setMetadataCode(String metadataCode) {
        this.metadataCode = metadataCode;
    }

    public String getRegexPattern() {
        return regexPattern;
    }

    public void setRegexPattern(String regexPattern) {
        this.regexPattern = regexPattern;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isSubstitute() {
        return substitute;
    }

    public void setSubstitute(boolean substitute) {
        this.substitute = substitute;
    }
}
