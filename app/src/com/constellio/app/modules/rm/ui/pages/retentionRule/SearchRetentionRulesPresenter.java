package com.constellio.app.modules.rm.ui.pages.retentionRule;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.StatusFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

public class SearchRetentionRulesPresenter extends SingleSchemaBasePresenter<SearchRetentionRulesView> {
	private RecordToVOBuilder voBuilder = new RecordToVOBuilder();
	private MetadataSchemaVO schemaVO;
	private String queryExpression;
	private RecordVODataProvider dataProvider;

	public SearchRetentionRulesPresenter(SearchRetentionRulesView view) {
		super(view, RetentionRule.DEFAULT_SCHEMA);
		schemaVO = new MetadataSchemaToVOBuilder().build(defaultSchema(), VIEW_MODE.TABLE, view.getSessionContext());
	}

	public void forParams(String parameters) {
		String[] splitParams = parameters.split("/");
		queryExpression = splitParams[0];
	}

	public void viewAssembled() {
		dataProvider = new RecordVODataProvider(schemaVO, voBuilder, modelLayerFactory, view.getSessionContext()) {
			@Override
			protected LogicalSearchQuery getQuery() {
				MetadataSchema schema = defaultSchema();
				return new LogicalSearchQuery(from(schema).returnAll())
						.filteredByStatus(StatusFilter.ACTIVES)
						.setFreeTextQuery(queryExpression)
						.sortAsc(schema.getMetadata(RetentionRule.CODE));
			}
		};
		view.setDataProvider(dataProvider);
	}

	public void backButtonClicked() {
		view.navigateTo().listRetentionRules();
	}

	public void displayButtonClicked(RecordVO recordVO) {
		view.navigateTo().displayRetentionRule(recordVO.getId());
	}

	public void editButtonClicked(RecordVO recordVO) {
		view.navigateTo().editRetentionRule(recordVO.getId());
	}

	public void deleteButtonClicked(RecordVO recordVO) {
		if (isDeletable(recordVO)) {
			Record record = getRecord(recordVO.getId());
			delete(record, false);
			view.navigateTo().listRetentionRules();
		} else {
			view.showErrorMessage($("ListRetentionRulesView.cannotDelete"));
		}
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.has(RMPermissionsTo.MANAGE_RETENTIONRULE).globally();
	}

	@Override
	public boolean isDeletable(RecordVO entity) {
		RecordServices recordService = modelLayerFactory.newRecordServices();
		Record record = getRecord(entity.getId());
		User user = getCurrentUser();
		return recordService.isLogicallyDeletable(record, user);
	}

	public String getDefaultOrderField() {
		return Schemas.CODE.getLocalCode();
	}

	public void search(String freeText) {
		view.navigateTo().retentionRulesSearch(freeText);
	}

	public String getQueryExpression() {
		return queryExpression;
	}

	public RecordVODataProvider getDataProvider() {
		return dataProvider;
	}
}