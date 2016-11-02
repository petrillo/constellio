package com.constellio.app.modules.rm.migrations;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.type.DocumentType;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.systemSetup.SystemGlobalConfigsManager;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

public class RMMigrationTo6_5_20 implements MigrationScript {

	@Override
	public String getVersion() {
		return "6.5.20";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider provider, AppLayerFactory appLayerFactory)
			throws Exception {

		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		DocumentType emailDocumentType = rm.emailDocumentType();
		if (emailDocumentType.isLogicallyDeletedStatus()) {
			RecordServices recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
			recordServices.restore(emailDocumentType.getWrappedRecord(), User.GOD);
		}

	}

}