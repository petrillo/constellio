package com.constellio.app.ui.framework.data.trees;

import com.constellio.model.entities.records.Record;
import com.constellio.model.services.taxonomies.LinkableTaxonomySearchResponse;

public interface RecordTreeNodesDataProvider {

	LinkableTaxonomySearchResponse getChildrenNodes(String recordId, int start, int maxSize);

	LinkableTaxonomySearchResponse getRootNodes(int start, int maxSize);

	String getTaxonomyCode();

}