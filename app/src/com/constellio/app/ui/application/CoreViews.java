package com.constellio.app.ui.application;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.constellio.app.modules.es.ui.pages.ConnectorReportView;
import com.constellio.app.ui.pages.events.EventCategory;
import com.constellio.app.ui.pages.management.taxonomy.AddEditTaxonomyConceptPresenter;
import com.constellio.app.ui.pages.management.taxonomy.TaxonomyManagementPresenter;
import com.constellio.app.ui.params.ParamUtils;
import com.google.gwt.dev.util.collect.HashMap;
import com.vaadin.navigator.Navigator;
import com.vaadin.ui.JavaScript;

public class CoreViews {
	protected Navigator navigator;

	public CoreViews(Navigator navigator) {
		this.navigator = navigator;
	}

	public String getState() {
		return navigator.getState();
	}

	public void previousView() {
		JavaScript javascript = JavaScript.getCurrent();
		javascript.execute("window.history.back();");
	}

	public void home() {
		navigator.navigateTo(NavigatorConfigurationService.HOME);
	}

	public void recordsManagement() {
		navigator.navigateTo(NavigatorConfigurationService.RECORDS_MANAGEMENT);
	}

	public void editFolder(String id) {
		Map<String, String> params = new HashMap<>();
		params.put("id", id);
		String viewPath = ParamUtils.addParams(NavigatorConfigurationService.EDIT_FOLDER, params);
		navigator.navigateTo(viewPath);
	}

	public void addDocumentWithContent(String id) {
		Map<String, String> params = new HashMap<>();
		params.put("idCopy", id);
		String viewPath = ParamUtils.addParams(NavigatorConfigurationService.ADD_DOCUMENT, params);
		navigator.navigateTo(viewPath);
	}

	public void editDocument(String id) {
		Map<String, String> params = new HashMap<>();
		params.put("id", id);
		String viewPath = ParamUtils.addParams(NavigatorConfigurationService.EDIT_DOCUMENT, params);
		navigator.navigateTo(viewPath);
	}

	public void editDocument(String id, String userDocumentId) {
		Map<String, String> params = new HashMap<>();
		params.put("id", id);
		params.put("userDocumentId", userDocumentId);
		String viewPath = ParamUtils.addParams(NavigatorConfigurationService.EDIT_DOCUMENT, params);
		navigator.navigateTo(viewPath);
	}

	public void displayDocument(String id) {
		navigator.navigateTo(NavigatorConfigurationService.DISPLAY_DOCUMENT + "/" + id);
	}

	public void appManagement() {
		navigator.navigateTo(NavigatorConfigurationService.APP_MANAGEMENT);
	}

	public void addGlobalGroup(String params) {
		navigator.navigateTo(NavigatorConfigurationService.GROUP_ADD_EDIT + "/" + params);
	}

	public void editTaxonomyConcept(String taxonomyCode, String conceptId, String schemaCode) {
		navigator.navigateTo(
				NavigatorConfigurationService.TAXONOMY_CONCEPT_ADD_EDIT + "/" + AddEditTaxonomyConceptPresenter.EDIT + "/"
						+ taxonomyCode + "/" + schemaCode + "/" + conceptId);
	}

	public void addTaxonomyConcept(String taxonomyCode, String conceptId, String schemaCode) {
		String url = NavigatorConfigurationService.TAXONOMY_CONCEPT_ADD_EDIT + "/" + AddEditTaxonomyConceptPresenter.ADD + "/"
				+ taxonomyCode + "/" + schemaCode;
		if (conceptId != null) {
			url += "/" + conceptId;
		}
		navigator.navigateTo(url);

	}

	public void addTaxonomy() {
		navigator.navigateTo(NavigatorConfigurationService.TAXONOMY_ADD_EDIT);
	}

	public void editTaxonomy(String taxonomyCode) {
		Map<String, String> params = new java.util.HashMap<>();
		params.put("taxonomyCode", taxonomyCode);
		String fragment = ParamUtils.addParams(NavigatorConfigurationService.TAXONOMY_ADD_EDIT, params);
		navigator.navigateTo(fragment);
	}

	public void listTaxonomies() {
		navigator.navigateTo(NavigatorConfigurationService.LIST_TAXONOMY);
	}

	public void taxonomyManagement(String taxonomyCode) {
		taxonomyManagement(taxonomyCode, null);
	}

	public void taxonomyManagement(String taxonomyCode, String conceptId) {
		Map<String, String> params = new HashMap<>();
		params.put(TaxonomyManagementPresenter.TAXONOMY_CODE, taxonomyCode);
		if (conceptId != null) {
			params.put(TaxonomyManagementPresenter.CONCEPT_ID, conceptId);
		}
		String viewPath = ParamUtils.addParams(NavigatorConfigurationService.TAXONOMY_MANAGEMENT, params);
		navigator.navigateTo(viewPath);
	}

	public void collectionSecurity() {
		navigator.navigateTo(NavigatorConfigurationService.COLLECTION_USER_LIST);
	}

	public void displayCollectionUser(String entityId) {
		navigator.navigateTo(NavigatorConfigurationService.COLLECTION_USER + "/" + entityId);
	}

	public void editCollectionUserRoles(String enityId) {
		navigator.navigateTo(NavigatorConfigurationService.COLLECTION_USER_ROLES + "/" + enityId);
	}

	public void displayCollectionGroup(String entityId) {
		navigator.navigateTo(NavigatorConfigurationService.COLLECTION_GROUP + "/" + entityId);
	}

	public void editCollectionGroupRoles(String entityId) {
		navigator.navigateTo(NavigatorConfigurationService.COLLECTION_GROUP_ROLES + "/" + entityId);
	}

	public void permissionManagement() {
		navigator.navigateTo(NavigatorConfigurationService.PERMISSION_MANAGEMENT);
	}

	public void listValueDomains() {
		navigator.navigateTo(NavigatorConfigurationService.LIST_VALUE_DOMAINS);
	}

	public void searchSchemaRecords(String schemaCode, String queryExpression) {
		Map<String, String> params = new HashMap<>();
		params.put("q", queryExpression);
		params.put("schemaCode", schemaCode);
		String viewPath = ParamUtils.addParams(NavigatorConfigurationService.SEARCH_SCHEMA_RECORDS, params);
		navigator.navigateTo(viewPath);
	}

	public void simpleSearch(String queryExpression) {
		navigator.navigateTo(NavigatorConfigurationService.SIMPLE_SEARCH + "/q/" + queryExpression);
	}

	public void simpleSearch(String queryExpression, int pageNumber) {
		navigator.navigateTo(NavigatorConfigurationService.SIMPLE_SEARCH + "/q/" + queryExpression + "/" + pageNumber);
	}

	public void simpleSearchReplay(String searchId) {
		navigator.navigateTo(NavigatorConfigurationService.SIMPLE_SEARCH + "/s/" + searchId);
	}

	public void advancedSearch() {
		navigator.navigateTo(NavigatorConfigurationService.ADVANCED_SEARCH);
	}

	public void advancedSearchReplay(String searchId) {
		navigator.navigateTo(NavigatorConfigurationService.ADVANCED_SEARCH + "/s/" + searchId);
	}

	public void taxonomySearch(String taxonomyCode, String queryExpression) {
		Map<String, String> params = new HashMap<>();
		params.put("taxonomyCode", taxonomyCode);
		params.put("q", queryExpression);
		String viewPath = ParamUtils.addParams(NavigatorConfigurationService.TAXONOMY_SEARCH, params);
		navigator.navigateTo(viewPath);
	}

	public void retentionRulesSearch(String queryExpression) {
		navigator.navigateTo(NavigatorConfigurationService.RETENTION_RULES_SEARCH + "/" + queryExpression);
	}

	public void listGlobalGroups() {
		navigator.navigateTo(NavigatorConfigurationService.GROUP_LIST);
	}

	public void editGlobalGroup(String params) {
		navigator.navigateTo(NavigatorConfigurationService.GROUP_ADD_EDIT + "/" + params);
	}

	public void displayGlobalGroup(String params) {
		navigator.navigateTo(NavigatorConfigurationService.GROUP_DISPLAY + "/" + params);
	}

	public void listUserCredentials() {
		navigator.navigateTo(NavigatorConfigurationService.USER_LIST);
	}

	public void displayUserCredential(String params) {
		navigator.navigateTo(NavigatorConfigurationService.USER_DISPLAY + "/" + params);
	}

	public void showEventCategory(EventCategory specificEvent) {
		navigator.navigateTo(NavigatorConfigurationService.EVENT_CATEGORY + "/" + specificEvent);
	}

	public void showEventCategory(Map<String, Object> params) {
		String viewWithParamsString = ParamUtils.addParams(NavigatorConfigurationService.EVENT_CATEGORY, params);
		navigator.navigateTo(viewWithParamsString);
	}

	public void showEvent(Map<String, Object> params) {
		String viewWithParamsString = ParamUtils.addParams(NavigatorConfigurationService.EVENT_DISPLAY, params);
		navigator.navigateTo(viewWithParamsString);
	}

	public void editUserCredential(String params) {
		navigator.navigateTo(NavigatorConfigurationService.USER_ADD_EDIT + "/" + params);
	}

	public void addSchemaRecord(String schema) {
		navigator.navigateTo(NavigatorConfigurationService.ADD_EDIT_SCHEMA_RECORD + "/" + schema);
	}

	public void editSchemaRecord(String schemaName, String recordId) {
		navigator.navigateTo(NavigatorConfigurationService.ADD_EDIT_SCHEMA_RECORD + "/" + schemaName + "/" + recordId);
	}

	public void displaySchemaRecord(String recordId) {
		navigator.navigateTo(NavigatorConfigurationService.DISPLAY_SCHEMA_RECORD + "/" + recordId);
	}

	public void listSchemaRecords(String schema) {
		navigator.navigateTo(NavigatorConfigurationService.LIST_SCHEMA_RECORDS + "/" + schema);
	}

	public void addUserCredential(String params) {
		navigator.navigateTo(NavigatorConfigurationService.USER_ADD_EDIT + "/" + params);
	}

	public void url(String url) {
		navigator.navigateTo(url);
	}

	public void adminModule() {
		navigator.navigateTo(NavigatorConfigurationService.ADMIN_MODULE);
	}

	public void reports() {
		navigator.navigateTo(NavigatorConfigurationService.REPORTS);
	}

	public void listPrincipalAccessAuthorizations(String entityId) {
		navigator.navigateTo(NavigatorConfigurationService.LIST_PRINCIPAL_ACCESS_AUTHORIZATIONS + "/" + entityId);
	}

	public void listObjectAccessAuthorizations(String entityId) {
		navigator.navigateTo(NavigatorConfigurationService.LIST_OBJECT_ACCESS_AUTHORIZATIONS + "/" + entityId);
	}

	public void listObjectRoleAuthorizations(String entityId) {
		navigator.navigateTo(NavigatorConfigurationService.LIST_OBJECT_ROLE_AUTHORIZATIONS + "/" + entityId);
	}

	public void shareContent(String entityId) {
		navigator.navigateTo(NavigatorConfigurationService.SHARE_CONTENT + "/" + entityId);
	}

	public void decommissioning() {
		navigator.navigateTo(NavigatorConfigurationService.DECOMMISSIONING);
	}

	public void decommissioningListBuilder(String type) {
		navigator.navigateTo(NavigatorConfigurationService.DECOMMISSIONING_LIST_BUILDER + "/" + type);
	}

	public void displayDecommissioningList(String entityId) {
		navigator.navigateTo(NavigatorConfigurationService.DECOMMISSIONING_LIST_DISPLAY + "/" + entityId);
	}

	public void displayDocumentDecommissioningList(String entityId) {
		navigator.navigateTo(NavigatorConfigurationService.DOCUMENT_DECOMMISSIONING_LIST_DISPLAY + "/" + entityId);
	}

	public void editDecommissioningList(String entityId) {
		navigator.navigateTo(NavigatorConfigurationService.DECOMMISSIONING_LIST_EDIT + "/" + entityId);
	}

	public void createContainerForDecommissioningList(String entityId) {
		navigator.navigateTo(NavigatorConfigurationService.DECOMMISSIONING_LIST_ADD_NEW_CONTAINER + "/" + entityId);
	}

	public void searchContainerForDecommissioningList(String entityId) {
		navigator.navigateTo(NavigatorConfigurationService.DECOMMISSIONING_LIST_ADD_EXISTING_CONTAINER + "/" + entityId);
	}

	public void configManagement() {
		navigator.navigateTo(NavigatorConfigurationService.CONFIG_MANAGEMENT);
	}

	public void pluginManagement() {
		navigator.navigateTo(NavigatorConfigurationService.PLUGIN_MANAGEMENT);
	}

	public void emailServerManagement() {
		navigator.navigateTo(NavigatorConfigurationService.EMAIL_SERVER_MANAGEMENT);
	}

	public void ldapConfigManagement() {
		navigator.navigateTo(NavigatorConfigurationService.LDAP_CONFIG_MANAGEMENT);
	}

	public void listSchema(String params) {
		navigator.navigateTo(NavigatorConfigurationService.DISPLAY_SCHEMA + "/" + params);
	}

	public void listSchemaTypes() {
		navigator.navigateTo(NavigatorConfigurationService.DISPLAY_SCHEMA_TYPE);
	}

	public void editMetadata(String params) {
		navigator.navigateTo(NavigatorConfigurationService.ADD_EDIT_METADATA + "/" + params);
	}

	public void addMetadata(String params) {
		navigator.navigateTo(NavigatorConfigurationService.ADD_EDIT_METADATA + "/" + params);
	}

	public void listSchemaMetadata(String params) {
		navigator.navigateTo(NavigatorConfigurationService.ADD_EDIT_SCHEMA_METADATA + "/" + params);
	}

	public void addSchema(String params) {
		navigator.navigateTo(NavigatorConfigurationService.ADD_EDIT_SCHEMA + "/" + params);
	}

	public void editSchema(String params) {
		navigator.navigateTo(NavigatorConfigurationService.ADD_EDIT_SCHEMA + "/" + params);
	}

	public void editDisplayForm(String schemaCode) {
		navigator.navigateTo(NavigatorConfigurationService.EDIT_DISPLAY_FORM + "/" + schemaCode);
	}

	public void searchDisplayForm(String schemaCode) {
		navigator.navigateTo(NavigatorConfigurationService.SEARCH_DISPLAY_FORM + "/" + schemaCode);
	}

	public void tableDisplayForm(String schemaCode) {
		navigator.navigateTo(NavigatorConfigurationService.TABLE_DISPLAY_FORM + "/" + schemaCode);
	}

	public void reportDisplayForm(String schemaCode) {
		navigator.navigateTo(NavigatorConfigurationService.REPORT_DISPLAY_FORM + "/" + schemaCode);
	}

	public void formDisplayForm(String schemaCode) {
		navigator.navigateTo(NavigatorConfigurationService.FORM_DISPLAY_FORM + "/" + schemaCode);
	}

	public void containersByAdministrativeUnits() {
		navigator.navigateTo(NavigatorConfigurationService.CONTAINERS_BY_ADMIN_UNITS);
	}

	public void displayAdminUnitWithContainers(String tabName, String entityId) {
		navigator
				.navigateTo(NavigatorConfigurationService.DISPLAY_ADMIN_UNIT_WITH_CONTAINERS + "/" + tabName + "/" + entityId);
	}

	public void displayFilingSpaceWithContainers(String tabName, String adminUnitId, String filingSpaceId) {
		navigator.navigateTo(
				NavigatorConfigurationService.DISPLAY_FILING_SPACE_WITH_CONTAINERS + "/" + tabName + "/" + adminUnitId + "/"
						+ filingSpaceId);
	}

	public void displayContainer(String containerId) {
		navigator.navigateTo(NavigatorConfigurationService.DISPLAY_CONTAINER + "/" + containerId);
	}

	public void addContainer() {
		navigator.navigateTo(NavigatorConfigurationService.EDIT_CONTAINER);
	}

	public void editContainer(String containerId) {
		navigator.navigateTo(NavigatorConfigurationService.EDIT_CONTAINER + "/" + containerId);
	}

	public void displayRetentionRule(String id) {
		navigator.navigateTo(NavigatorConfigurationService.DISPLAY_RETENTION_RULE + "/" + id);
	}

	public void addRetentionRule() {
		navigator.navigateTo(NavigatorConfigurationService.ADD_RETENTION_RULE);
	}

	public void editRetentionRule(String id) {
		navigator.navigateTo(NavigatorConfigurationService.EDIT_RETENTION_RULE + "/" + id);
	}

	public void declareUserDocument(String userDocumentId, String folderId) {
		Map<String, String> params = new HashMap<>();
		params.put("userDocumentId", userDocumentId);
		if (folderId != null) {
			params.put("parentId", folderId);
		}

		String viewPath = ParamUtils.addParams(NavigatorConfigurationService.ADD_DOCUMENT, params);
		navigator.navigateTo(viewPath);
	}

	public void declareUserDocument(String userDocumentId) {
		Map<String, String> params = new HashMap<>();
		params.put("userDocumentId", userDocumentId);
		String viewPath = ParamUtils.addParams(NavigatorConfigurationService.ADD_DOCUMENT, params);
		navigator.navigateTo(viewPath);
	}

	public void manageCollections() {
		navigator.navigateTo(NavigatorConfigurationService.COLLECTION_MANAGEMENT);
	}

	public void addEditCollection(String params) {
		navigator.navigateTo(NavigatorConfigurationService.COLLECTION_ADD_EDIT + "/" + params);
	}

	public void listTabDisplayForm(String params) {
		navigator.navigateTo(NavigatorConfigurationService.LIST_ONGLET + "/" + params);
	}

	public void modifyProfil(String params) {
		String path = NavigatorConfigurationService.MODIFY_PROFILE;
		if (params != null) {
			path += "/" + params;
		}
		navigator.navigateTo(path);
	}

	public void updateManager() {
		navigator.navigateTo(NavigatorConfigurationService.UPDATE_MANAGER);
	}

	public void importRecords() {
		navigator.navigateTo(NavigatorConfigurationService.IMPORT_FILE);
	}

	public void importUsers() {
		navigator.navigateTo(NavigatorConfigurationService.IMPORT_USERS);
	}

	public void importAuthorizations() {
		navigator.navigateTo(NavigatorConfigurationService.IMPORT_AUTHORIZATIONS);
	}

	public void importGroups() {
		navigator.navigateTo(NavigatorConfigurationService.IMPORT_GROUPS);
	}

	public void importSchemaTypes() {
		navigator.navigateTo(NavigatorConfigurationService.IMPORT_SCHEMA_TYPES);
	}

	public void exporter() {
		navigator.navigateTo(NavigatorConfigurationService.EXPORTER);
	}

	public void listAgentLogs() {
		navigator.navigateTo(NavigatorConfigurationService.LIST_AGENT_LOGS);
	}

	public void editFacetConfiguration(String params) {
		navigator.navigateTo(NavigatorConfigurationService.EDIT_FACET_CONFIGURATION + "/" + params);
	}

	public void addFacetConfiguration() {
		navigator.navigateTo(NavigatorConfigurationService.ADD_FACET_CONFIGURATION);
	}

	public void orderFacetConfiguration() {
		navigator.navigateTo(NavigatorConfigurationService.ORDER_FACET_CONFIGURATION);
	}

	public void listFacetConfiguration() {
		navigator.navigateTo(NavigatorConfigurationService.LIST_FACET_CONFIGURATION);
	}

	public void displayFacetConfiguration(String params) {
		navigator.navigateTo(NavigatorConfigurationService.DISPLAY_FACET_CONFIGURATION + "/" + params);
	}

	public void listSavedSearches() {
		navigator.navigateTo(NavigatorConfigurationService.LIST_SAVED_SEARCHES);
	}

	public void searchBoostByMetadatas() {
		navigator.navigateTo(NavigatorConfigurationService.SEARCH_BOOST_BY_METADATAS);
	}

	public void searchBoostByQuerys() {
		navigator.navigateTo(NavigatorConfigurationService.SEARCH_BOOST_BY_QUERYS);
	}

	//Module ES

	public void displayConnectorInstance(String params) {
		navigator.navigateTo(NavigatorConfigurationService.DISPLAY_CONNECTOR_INSTANCE + "/" + params);
	}

	public void wizardConnectorInstance() {
		navigator.navigateTo(NavigatorConfigurationService.WIZARD_CONNECTOR_INSTANCE);
	}

	public void editConnectorInstance(String params) {
		navigator.navigateTo(NavigatorConfigurationService.EDIT_CONNECTOR_INSTANCE + "/" + params);
	}

	public void connectorIndexationReport(String connectorId) {
		Map<String, String> params = new HashMap<>();
		if (connectorId != null) {
			params.put(ConnectorReportView.CONNECTOR_ID, connectorId);
		}
		params.put(ConnectorReportView.REPORT_MODE, ConnectorReportView.INDEXATION);
		String viewPath = ParamUtils.addParams(NavigatorConfigurationService.CONNECTOR_REPORT, params);
		navigator.navigateTo(viewPath);
	}

	public void connectorErrorsReport(String connectorId) {
		Map<String, String> params = new HashMap<>();
		if (connectorId != null) {
			params.put(ConnectorReportView.CONNECTOR_ID, connectorId);
		}
		params.put(ConnectorReportView.REPORT_MODE, ConnectorReportView.ERRORS);
		String viewPath = ParamUtils.addParams(NavigatorConfigurationService.CONNECTOR_REPORT, params);
		navigator.navigateTo(viewPath);
	}

	//tasks module

	public void editTask(String taskId, String workflowId, boolean completeTask) {
		Map<String, String> params = new HashMap<>();
		params.put("id", taskId);
		if (completeTask) {
			params.put("completeTask", "" + true);
		}
		if (StringUtils.isNotBlank(workflowId)) {
			params.put("workflowId", workflowId);
		}
		String viewPath = ParamUtils.addParams(NavigatorConfigurationService.EDIT_TASK, params);
		navigator.navigateTo(viewPath);
	}

	public void editTask(String taskId, boolean completeTask) {
		editTask(taskId, null, completeTask);
	}

	public void editTask(String taskId, String workflowId) {
		editTask(taskId, workflowId, false);
	}

	public void editTask(String taskId) {
		editTask(taskId, null);
	}

	public void addWorkflow() {
		navigator.navigateTo(NavigatorConfigurationService.ADD_WORKFLOW);
	}

	public void editWorkflow(String id) {
		navigator.navigateTo(NavigatorConfigurationService.EDIT_WORKFLOW + "/" + id);
	}

	public void displayWorkflowInstance(String id) {
		navigator.navigateTo(NavigatorConfigurationService.DISPLAY_WORKFLOW_INSTANCE + "/" + id);
	}

	public void displayConnectorMappings(String entityId) {
		navigator.navigateTo(NavigatorConfigurationService.DISPLAY_CONNECTOR_MAPPINGS + "/" + entityId);
	}

	public void addConnectorMapping(String connectorInstance, String documentType) {
		navigator.navigateTo(NavigatorConfigurationService.ADD_CONNECTOR_MAPPING +
				"/" + connectorInstance + "/" + documentType);
	}

	public void editConnectorMapping(String connectorInstance, String documentType, String metadata) {
		navigator.navigateTo(NavigatorConfigurationService.ADD_CONNECTOR_MAPPING +
				"/" + connectorInstance + "/" + documentType + "/" + metadata);
	}

	public void listTasksLogs() {
		navigator.navigateTo(NavigatorConfigurationService.LIST_TASKS_LOGS);
	}

	public void listMetadataExtractors() {
		navigator.navigateTo(NavigatorConfigurationService.LIST_METADATA_EXTRACTORS);
	}

	public void addMetadataExtractor() {
		navigator.navigateTo(NavigatorConfigurationService.ADD_METADATA_EXTRACTOR);
	}

	public void editMetadataExtractor(String metadataCode) {
		navigator.navigateTo(NavigatorConfigurationService.EDIT_METADATA_EXTRACTOR + "/" + metadataCode);
	}

	// Robots module



	public void addRobot(String parentId) {
		Map<String, String> params = new HashMap<>();
		params.put("pageMode", "add");
		if (parentId != null) {
			params.put("parentId", parentId);
		}
		String viewPath = ParamUtils.addParams(NavigatorConfigurationService.ADD_EDIT_ROBOT, params);
		navigator.navigateTo(viewPath);
	}

	public void editRobot(String robotId) {
		Map<String, String> params = new HashMap<>();
		params.put("pageMode", "edit");
		params.put("robotId", robotId);
		String viewPath = ParamUtils.addParams(NavigatorConfigurationService.ADD_EDIT_ROBOT, params);
		navigator.navigateTo(viewPath);
	}

	public void displayRobotLogs(String entityId) {
		navigator.navigateTo(NavigatorConfigurationService.ROBOT_LOGS + "/" + entityId);
	}

	public void addEmailAttachmentsToFolder(String userDocumentId) {
		Map<String, String> params = new HashMap<>();
		params.put("userDocumentId", userDocumentId);
		String viewPath = ParamUtils.addParams(NavigatorConfigurationService.ADD_EMAIL_ATTACHMENTS_TO_FOLDER, params);
		navigator.navigateTo(viewPath);
	}
}