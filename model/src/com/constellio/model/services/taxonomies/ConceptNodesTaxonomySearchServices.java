package com.constellio.model.services.taxonomies;

import static com.constellio.model.entities.schemas.Schemas.ALL_REMOVED_AUTHS;
import static com.constellio.model.entities.schemas.Schemas.ATTACHED_ANCESTORS;
import static com.constellio.model.entities.schemas.Schemas.CODE;
import static com.constellio.model.entities.schemas.Schemas.PATH_PARTS;
import static com.constellio.model.entities.schemas.Schemas.TITLE;
import static com.constellio.model.entities.schemas.Schemas.TOKENS;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromTypesInCollectionOf;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.where;

import java.util.ArrayList;
import java.util.List;

import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.search.SPEQueryResponse;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.StatusFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.DataStoreFieldLogicalSearchCondition;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.search.query.logical.ongoing.OngoingLogicalSearchCondition;

public class ConceptNodesTaxonomySearchServices {

	SearchServices searchServices;
	TaxonomiesManager taxonomiesManager;
	MetadataSchemasManager metadataSchemasManager;
	TaxonomySearchQueryConditionFactory queryFactory;
	SchemaUtils schemaUtils = new SchemaUtils();

	public ConceptNodesTaxonomySearchServices(ModelLayerFactory modelLayerFactory) {
		this.searchServices = modelLayerFactory.newSearchServices();
		this.taxonomiesManager = modelLayerFactory.getTaxonomiesManager();
		this.metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
	}

	public ConceptNodesTaxonomySearchServices(SearchServices searchServices, TaxonomiesManager taxonomiesManager,
			MetadataSchemasManager metadataSchemasManager) {
		this.searchServices = searchServices;
		this.taxonomiesManager = taxonomiesManager;
		this.metadataSchemasManager = metadataSchemasManager;
	}

	public List<Record> getRootConcept(String collection, String taxonomyCode, TaxonomiesSearchOptions options) {
		return getRootConceptResponse(collection, taxonomyCode, options).getRecords();
	}

	public SPEQueryResponse getRootConceptResponse(String collection, String taxonomyCode, TaxonomiesSearchOptions options) {
		return searchServices.query(getRootConceptsQuery(collection, taxonomyCode, options));
	}

	public LogicalSearchQuery getRootConceptsQuery(String collection, String taxonomyCode, TaxonomiesSearchOptions options) {
		Taxonomy taxonomy = taxonomiesManager.getEnabledTaxonomyWithCode(collection, taxonomyCode);
		LogicalSearchCondition condition = fromConceptsOf(taxonomy).where(PATH_PARTS).isEqualTo("R");

		LogicalSearchQuery query = new LogicalSearchQuery(condition);
		query.filteredByStatus(options.getIncludeStatus());
		query.setStartRow(options.getStartRow());
		query.setNumberOfRows(options.getRows());
		query.setReturnedMetadatas(options.getReturnedMetadatasFilter());
		query.sortAsc(CODE).sortAsc(TITLE);
		return query;
	}

	public List<Record> getChildConcept(Record record, TaxonomiesSearchOptions options) {
		return getChildNodesResponse(record, options).getRecords();
	}

	public SPEQueryResponse getChildNodesResponse(Record record, TaxonomiesSearchOptions options) {
		return searchServices.query(childNodesQuery(record, options));
	}

	public static LogicalSearchQuery childNodesQuery(Record record, TaxonomiesSearchOptions options) {
		LogicalSearchCondition condition = fromTypesInCollectionOf(record).where(directChildOf(record).andWhere(visibleInTrees));

		return new LogicalSearchQuery(condition)
				.filteredByStatus(options.getIncludeStatus())
				.setStartRow(options.getStartRow())
				.setNumberOfRows(options.getRows())
				.sortAsc(CODE).sortAsc(TITLE)
				.setReturnedMetadatas(options.getReturnedMetadatasFilter()
						.withIncludedMetadatas(TOKENS, ATTACHED_ANCESTORS, ALL_REMOVED_AUTHS));
	}

	public static LogicalSearchQuery childConceptsQuery(Record record, Taxonomy taxonomy, TaxonomiesSearchOptions options) {
		LogicalSearchCondition condition = fromTypeIn(taxonomy).where(directChildOf(record)).andWhere(visibleInTrees);

		return new LogicalSearchQuery(condition)
				.filteredByStatus(options.getIncludeStatus())
				.setStartRow(options.getStartRow())
				.setNumberOfRows(options.getRows())
				.sortAsc(CODE).sortAsc(TITLE)
				.setReturnedMetadatas(options.getReturnedMetadatasFilter()
						.withIncludedMetadatas(TOKENS, ATTACHED_ANCESTORS, ALL_REMOVED_AUTHS));
	}

	OngoingLogicalSearchCondition fromConceptsOf(Taxonomy taxonomy) {
		return from(metadataSchemasManager.getSchemaTypes(taxonomy, taxonomy.getSchemaTypes()));
	}

	static DataStoreFieldLogicalSearchCondition directChildOf(Record record) {
		return (DataStoreFieldLogicalSearchCondition) where(Schemas.PATH_PARTS).isEqualTo("_LAST_" + record.getId());
	}

	static DataStoreFieldLogicalSearchCondition notDirectChildOf(Record record) {
		return (DataStoreFieldLogicalSearchCondition) where(Schemas.PATH_PARTS).isNotEqual("_LAST_" + record.getId());
	}

	static DataStoreFieldLogicalSearchCondition recordInHierarchyOf(Record record) {
		return (DataStoreFieldLogicalSearchCondition) where(Schemas.PATH_PARTS).isEqualTo(record.getId());
	}

	public static DataStoreFieldLogicalSearchCondition visibleInTrees = (DataStoreFieldLogicalSearchCondition) where(
			Schemas.VISIBLE_IN_TREES).isTrueOrNull();

	public LogicalSearchQuery getChildNodesQuery(String taxonomyCode, Record record, TaxonomiesSearchOptions options) {
		Taxonomy taxonomy = taxonomiesManager.getEnabledTaxonomyWithCode(record.getCollection(), taxonomyCode);
		LogicalSearchCondition condition = fromConceptsOf(taxonomy).where(directChildOf(record)).andWhere(visibleInTrees);

		return new LogicalSearchQuery(condition)
				.filteredByStatus(options.getIncludeStatus())
				.setStartRow(options.getStartRow())
				.setNumberOfRows(options.getRows())
				.sortAsc(CODE).sortAsc(TITLE)
				.setReturnedMetadatas(options.getReturnedMetadatasFilter()
						.withIncludedMetadatas(TOKENS, ATTACHED_ANCESTORS, ALL_REMOVED_AUTHS));
	}

	public static OngoingLogicalSearchCondition fromTypeIn(Taxonomy taxonomy) {
		return from(taxonomy.getSchemaTypes(), taxonomy.getCollection());
	}

	public static LogicalSearchCondition childrenCondition(Taxonomy taxonomy, Record inRecord) {
		return fromTypeIn(taxonomy).where(directChildOf(inRecord));
	}

	public List<String> getAllConceptIdsHierarchyOf(Taxonomy taxonomy, Record record) {
		List<String> newReponse = new ArrayList<>();
		newReponse.add(record.getId());
		newReponse.addAll(searchServices.searchRecordIds(fromConceptsOf(taxonomy).where(recordInHierarchyOf(record))));
		return newReponse;

	}

	public List<Record> getAllConceptHierarchyOf(Taxonomy taxonomy, Record record) {
		return searchServices.search(new LogicalSearchQuery(fromConceptsOf(taxonomy).where(directChildOf(record))));
	}

	public List<String> getAllPrincipalConceptIdsAvailableTo(Taxonomy taxonomy, User user, StatusFilter statusFilter) {
		return searchServices.searchRecordIds(new LogicalSearchQuery(fromConceptsOf(taxonomy).returnAll())
				.sortAsc(CODE).sortAsc(TITLE)
				.filteredWithUser(user)
				.filteredByStatus(statusFilter));
	}

	public List<String> getAllPrincipalConceptIdsAvailableTo(Taxonomy taxonomy, User user) {
		return getAllPrincipalConceptIdsAvailableTo(taxonomy, user, StatusFilter.ALL);
	}

	public List<Record> getAllPrincipalConceptsAvailableTo(Taxonomy taxonomy, User user) {
		return searchServices.search(new LogicalSearchQuery(fromConceptsOf(taxonomy).returnAll())
				.sortAsc(CODE).sortAsc(TITLE)
				.filteredWithUser(user));
	}
}
