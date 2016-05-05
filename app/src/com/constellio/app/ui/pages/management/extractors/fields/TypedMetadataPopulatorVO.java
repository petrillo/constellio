package com.constellio.app.ui.pages.management.extractors.fields;

import java.io.Serializable;

/**
 * Created by Majid on 2016-04-26.
 */
public class TypedMetadataPopulatorVO implements Serializable{
    String type;
    MetadataPopulatorVO metadataPopulatorVO;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public MetadataPopulatorVO getMetadataPopulatorVO() {
        return metadataPopulatorVO;
    }

    public void setMetadataPopulatorVO(MetadataPopulatorVO metadataPopulatorVO) {
        this.metadataPopulatorVO = metadataPopulatorVO;
    }
}