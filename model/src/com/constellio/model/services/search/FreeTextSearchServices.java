package com.constellio.model.services.search;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.data.dao.services.records.RecordDao;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.query.FilterUtils;
import com.constellio.model.services.search.query.logical.FreeTextQuery;
import com.constellio.model.services.security.SecurityTokenManager;
import com.constellio.model.services.users.UserServices;

public class FreeTextSearchServices {
	Logger LOGGER = LoggerFactory.getLogger(FreeTextSearchServices.class);

	RecordDao recordDao;
	RecordDao eventsDao;
	RecordServices recordServices;
	UserServices userServices;
	SecurityTokenManager securityTokenManager;
	MetadataSchemasManager metadataSchemasManager;

	public FreeTextSearchServices(ModelLayerFactory modelLayerFactory) {
		super();
		this.recordDao = modelLayerFactory.getDataLayerFactory().newRecordDao();
		this.eventsDao = modelLayerFactory.getDataLayerFactory().newEventsDao();
		this.metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
		this.userServices = modelLayerFactory.newUserServices();
		this.securityTokenManager = modelLayerFactory.getSecurityTokenManager();
	}

	public QueryResponse search(FreeTextQuery query) {
		ModifiableSolrParams modifiableSolrParams = new ModifiableSolrParams(query.getSolrParams());

		if (query.getUserFilter() != null && isSecurityEnabled(modifiableSolrParams)) {
			String filter = FilterUtils.multiCollectionUserReadFilter(query.getUserFilter(), userServices, securityTokenManager);
			modifiableSolrParams.add("fq", filter);
		}

		if (query.isSearchingEvents()) {
			modifiableSolrParams.add("fq", "schema_s:event*");
		} else {
			modifiableSolrParams.add("fq", "-schema_s:event*");
		}
		//LOGGER.info(LoggerUtils.toParamsString(modifiableSolrParams));
		return recordDao.nativeQuery(modifiableSolrParams);
	}

	public boolean isSecurityEnabled(SolrParams params) {
		String collection = null;
		String schemaType = null;
		if (params.getParams("fq") != null) {
			for (String filterQuery : params.getParams("fq")) {
				if (filterQuery.startsWith("schema_s:")) {
					schemaType = StringUtils.substringBefore(filterQuery.substring("schema_s:".length()), "_");
				}
				if (filterQuery.startsWith("collection_s:")) {
					collection = StringUtils.substringBefore(filterQuery.substring("collection_s:".length()), "_");
				}
			}
		}

		String q = params.get("q");
		if (q != null && q.startsWith("schema_s:")) {
			schemaType = q.substring("schema_s:".length());
			schemaType = StringUtils.substringBefore(schemaType, "_");
			schemaType = StringUtils.substringBefore(schemaType, "*");
		}
		if (q != null && q.startsWith("collection_s:")) {
			collection = StringUtils.substringBefore(q.substring("collection_s:".length()), "_");
		}

		boolean security = true;
		if (collection != null && schemaType != null && security) {
			try {
				MetadataSchemaTypes types = metadataSchemasManager.getSchemaTypes(collection);
				security = types.getSchemaType(schemaType).hasSecurity();
			} catch (Exception e) {
				//OK
			}
		}
		return security;
	}
}
