package com.constellio.dev;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.dto.records.OptimisticLockingResolution;
import com.constellio.model.entities.records.ActionExecutorInBatch;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.extractions.RecordPopulateServices;
import com.constellio.model.services.schemas.MetadataList;
import com.constellio.model.services.schemas.MetadataSchemasManagerException;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.utils.ScriptsUtils.startLayerFactoriesWithoutBackgroundThreads;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasExcept;

/*
 * Script exemple
 *
 * Compile and copy classes on production server
 *
 */
public class ReadAllContentScript {

	static int BATCH_SIZE = 100;

	static String currentCollection;
	static AppLayerFactory appLayerFactory;
	static ModelLayerFactory modelLayerFactory;
	static SearchServices searchServices;
	static RecordServices recordServices;
	static RMSchemasRecordsServices rm;

	private static void startBackend() {
		//Only enable this line to run in production
		appLayerFactory = startLayerFactoriesWithoutBackgroundThreads();

		//Only enable this line to run on developer workstation
		//appLayerFactory = SDKScriptUtils.startApplicationWithoutBackgroundProcessesAndAuthentication();
	}

	private static LogicalSearchQuery getQuery() {
		List<MetadataSchemaType> exceptSchemas = new ArrayList<>();
		exceptSchemas.add(rm.eventSchemaType());
		return new LogicalSearchQuery(fromAllSchemasExcept(exceptSchemas).returnAll());
	}

	private static void runScriptForCurrentCollection()
			throws Exception {

		new ActionExecutorInBatch(searchServices, "Read All Documents", BATCH_SIZE) {

			@Override
			public void doActionOnBatch(List<Record> records) {
				for (Record record : records) {
					MetadataSchemaType type = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager()
							.getSchemaTypes(currentCollection)
							.getSchemaType(new SchemaUtils().getSchemaTypeCode(record.getSchemaCode()));

					MetadataList contentMetadataList = type.getAllMetadatas().onlyWithType(MetadataValueType.CONTENT);
					for (Metadata contentMetadata : contentMetadataList) {
						if (contentMetadata.isMultivalue()) {
							List<Content> contents = record.getList(contentMetadata);
							for (Content content : contents) {
								for (String hash : content.getHashOfAllVersions()) {
									System.err.println("Reading hash : " + hash);
									readHash(hash);
								}
							}
						} else {
							Content content = record.get(contentMetadata);
							if (content != null) {
								for (String hash : content.getHashOfAllVersions()) {
									System.err.println("Reading hash : " + hash);
									readHash(hash);
								}
							}
						}
					}
				}
			}

		}.execute(getQuery());
	}

	private static void readHash(String hash) {
		try (InputStream is = modelLayerFactory.getContentManager().getContentInputStream(hash, "empty")) {
			is.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String argv[])
			throws Exception {

		RecordPopulateServices.LOG_CONTENT_MISSING = false;

		startBackend();

		modelLayerFactory = appLayerFactory.getModelLayerFactory();
		searchServices = modelLayerFactory.newSearchServices();
		recordServices = modelLayerFactory.newRecordServices();

		for (String collection : modelLayerFactory.getCollectionsListManager().getCollections()) {
			currentCollection = collection;
			rm = new RMSchemasRecordsServices(collection, appLayerFactory);
			runScriptForCurrentCollection();
		}

	}

}
