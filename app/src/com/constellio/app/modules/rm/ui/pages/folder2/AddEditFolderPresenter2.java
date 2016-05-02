package com.constellio.app.modules.rm.ui.pages.folder2;

import static com.constellio.app.ui.i18n.i18n.$;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningService;
import com.constellio.app.modules.rm.ui.components.folder.fields.FolderRetentionRuleField;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.components.OverridingMetadataFieldFactory.Choice;
import com.constellio.app.ui.framework.components.OverridingMetadataFieldFactory.FieldOverridePresenter;
import com.constellio.app.ui.framework.components.OverridingMetadataFieldFactory.OverrideMode;
import com.constellio.app.ui.framework.components.fields.lookup.LookupRecordField;
import com.constellio.app.ui.pages.base.SchemaPresenterUtils;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.app.ui.pages.search.SearchPresenterService;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.RecordServicesException.ValidationException;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.StatusFilter;
import com.vaadin.ui.Field;

public class AddEditFolderPresenter2 extends SingleSchemaBasePresenter<AddEditFolderView2>
		implements FieldOverridePresenter {

	private RecordToVOBuilder recordToVOBuilder = new RecordToVOBuilder();

	SearchPresenterService searchPresenterService;
	SchemasDisplayManager schemasDisplayManager;
	SearchServices searchServices;
	RMSchemasRecordsServices rm;

	private RecordVO folder;
	private boolean editMode;
	String parentId;

	public AddEditFolderPresenter2(AddEditFolderView2 view) {
		super(view, Folder.DEFAULT_SCHEMA);
		init();

	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		init();
	}

	private void init() {
		searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
		searchPresenterService = new SearchPresenterService(collection, modelLayerFactory);
		schemasDisplayManager = appLayerFactory.getMetadataSchemasDisplayManager();
		rm = new RMSchemasRecordsServices(collection, appLayerFactory);
	}

	public AddEditFolderPresenter2 forParams(String parameters) {
		Map<String, String> params = ParamUtils.getParamsMap(parameters);
		editMode = params.containsKey("id");
		if (editMode) {
			folder = loadFolder(params.get("id"));
		} else {
			parentId = params.get("parentId");
			folder = newFolder(parentId);
		}
		return this;
	}

	public void ajustFields() {
		FolderRetentionRuleField retentionRuleField = view.getForm().getField(Folder.RETENTION_RULE_ENTERED);
		LookupRecordField categoryField = view.getForm().getField(Folder.CATEGORY_ENTERED);
		LookupRecordField uniformSubdivisionField = view.getForm().getField(Folder.UNIFORM_SUBDIVISION_ENTERED);

		commitForm();
		Folder folder = rm.wrapFolder(toRecord(getFolder()));
		System.out.println(folder.getCategoryEntered());

		String currentValue = retentionRuleField.getFieldValue();
		// Discover what options are available
		List<String> availableOptions = decommissioningService().getRetentionRulesForCategory(
				folder.getCategoryEntered(), uniformSubdivisionField.getValue(), StatusFilter.ACTIVES);
		retentionRuleField.setOptions(availableOptions);
	}

	private void commitForm() {
		view.getForm().commit();
	}

	private RecordVO loadFolder(String folderId) {
		return recordToVOBuilder.build(getRecord(folderId), VIEW_MODE.FORM, view.getSessionContext());
	}

	private RecordVO newFolder(String parentId) {
		Folder folder = rm.newFolder().setParentFolder(parentId);
		return recordToVOBuilder.build(folder.getWrappedRecord(), VIEW_MODE.FORM, view.getSessionContext());
	}

	public RecordVO getFolder() {
		return folder;
	}

	public boolean isAddMode() {
		return !editMode;
	}

	public boolean isEditMode() {
		return editMode;
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return true;
	}

	@Override
	public OverrideMode getOverride(String metadataCode) {
		return OverrideMode.NONE;
	}

	@Override
	public List<Choice> getChoices(String metadataCode) {
		return null;
	}

	private Record toParametersRecord(RecordVO recordVO) {
		return new SchemaPresenterUtils(recordVO.getSchema().getCode(), view.getConstellioFactories(), view.getSessionContext())
				.toRecord(recordVO);
	}

	public void saveButtonClicked(RecordVO recordVO) {

		try {
			Transaction transaction = new Transaction().setUser(getCurrentUser());
			transaction.add(toRecord(recordVO));
			recordServices().execute(transaction);
			view.navigate().to(RMViews.class).displayFolder(recordVO.getId());

		} catch (ValidationException e) {
			view.showErrorMessage($(e.getErrors()));

		} catch (RecordServicesException e) {
			view.showErrorMessage(e.getMessage());
		}

	}

	public void backButtonClicked() {
		if (isAddMode()) {
			if (parentId != null) {
				view.navigate().to(RMViews.class).displayFolder(parentId);
			} else {
				view.navigate().to(RMViews.class).recordsManagement();
			}
		} else {
			view.navigate().to(RMViews.class).displayFolder(folder.getId());
		}
	}

	public void viewAssembled() {
		ajustFields();
	}

	private DecommissioningService decommissioningService() {
		return new DecommissioningService(collection, modelLayerFactory);
	}

	public void fieldValueChanged(Field<?> field) {
		ajustFields();
	}
}
