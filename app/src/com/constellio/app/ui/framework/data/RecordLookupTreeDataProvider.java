package com.constellio.app.ui.framework.data;

import static com.constellio.app.services.factories.ConstellioFactories.getInstance;
import static com.constellio.app.ui.application.ConstellioUI.getCurrentSessionContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.constellio.app.ui.framework.components.fields.lookup.LookupField.LookupTreeDataProvider;
import com.constellio.app.ui.framework.components.fields.lookup.LookupField.TextInputDataProvider;
import com.constellio.app.ui.framework.data.trees.LinkableRecordTreeNodesDataProvider;
import com.constellio.app.ui.framework.data.trees.RecordTreeNodesDataProvider;
import com.constellio.app.ui.util.FileIconUtils;
import com.constellio.app.ui.util.SchemaCaptionUtils;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.taxonomies.LinkableTaxonomySearchResponse;
import com.constellio.model.services.taxonomies.TaxonomySearchRecord;
import com.vaadin.server.Resource;

public class RecordLookupTreeDataProvider extends BaseRecordTreeDataProvider implements LookupTreeDataProvider<String> {
	private String schemaTypeCode;
	private Map<String, Boolean> selectableCache = new HashMap<>();
	private boolean ignoreLinkability;
	private boolean writeAccess;

	public RecordLookupTreeDataProvider(String schemaTypeCode, String taxonomyCode, boolean writeAccess) {
		super(new LinkableRecordTreeNodesDataProvider(taxonomyCode, schemaTypeCode, writeAccess));
		this.writeAccess = writeAccess;
		this.schemaTypeCode = schemaTypeCode;
		ignoreLinkability = false;
	}

	public RecordLookupTreeDataProvider(String schemaTypeCode, boolean writeAccess,
			RecordTreeNodesDataProvider recordTreeNodesDataProvider) {
		super(recordTreeNodesDataProvider);
		this.writeAccess = writeAccess;
		this.schemaTypeCode = schemaTypeCode;
		ignoreLinkability = false;
	}

	protected void saveResultInCache(TaxonomySearchRecord searchRecord) {
		super.saveResultInCache(searchRecord);
		boolean selectable = ignoreLinkability || searchRecord.isLinkable();
		selectableCache.put(searchRecord.getId(), selectable);
	}

	@Override
	public boolean isSelectable(String selection) {
		return selectableCache.get(selection);
	}

	@Override
	public TextInputDataProvider<String> search() {
		return new RecordTextInputDataProvider(getInstance(), getCurrentSessionContext(), schemaTypeCode, writeAccess);
	}

	public void setIgnoreLinkability(boolean ignoreLinkability) {
		this.ignoreLinkability = ignoreLinkability;
	}

}
