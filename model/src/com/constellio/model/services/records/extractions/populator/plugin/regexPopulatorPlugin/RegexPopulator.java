package com.constellio.model.services.records.extractions.populator.plugin.regexPopulatorPlugin;

import com.constellio.model.services.records.extractions.populator.DefaultMetadataPopulator;
import com.constellio.model.services.records.extractions.populator.plugin.MetadataToText;

public class RegexPopulator extends DefaultMetadataPopulator{

    public RegexPopulator(){
        super(new RegexExtractor(), new MetadataToText());
    }

}
