package com.constellio.model.services.records.extractions;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.schemas.MetadataSchemasManager;

public interface MetadataPopulator {
    void init(ContentManager contentManager, MetadataSchema schema, boolean multiValue);
    Object getPopulationValue(Record record);

    String toXml();
}
