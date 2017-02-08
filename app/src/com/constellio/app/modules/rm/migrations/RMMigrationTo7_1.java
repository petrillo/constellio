package com.constellio.app.modules.rm.migrations;

import java.util.List;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.structures.BorrowHistory;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.ActionExecutorInBatch;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

public class RMMigrationTo7_1 implements MigrationScript {

	public RMMigrationTo7_1() {
	}

	@Override
	public String getVersion() {
		return "7.1";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
			AppLayerFactory appLayerFactory) throws Exception {

		SchemaFavoriteFolderAlternation alternation = new SchemaFavoriteFolderAlternation(collection, migrationResourcesProvider, appLayerFactory);
		alternation.migrate();
		
		SearchServices searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
		RecordServices recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
		CreateInitialHistoryFromOldVersion batch = new CreateInitialHistoryFromOldVersion(searchServices, "Create Initial Borrow History", 100);
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		batch.setRmSchemasRecordsServices(rm);
		batch.setRecordServices(recordServices);
		batch.execute(composeLogicalSearchCondition(rm));
	}
	
	private LogicalSearchCondition composeLogicalSearchCondition(RMSchemasRecordsServices rm) {
		return LogicalSearchQueryOperators.from(rm.containerRecord.schema()).returnAll();
	}
	
	private static class SchemaFavoriteFolderAlternation extends MetadataSchemasAlterationHelper {

		public SchemaFavoriteFolderAlternation(String collection, MigrationResourcesProvider migrationResourcesProvider,
				AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaBuilder schema = typesBuilder.getSchema(ContainerRecord.DEFAULT_SCHEMA);
			
			if (!schema.hasMetadata(ContainerRecord.BORROW_HISTORY)) {
				schema.createUndeletable(ContainerRecord.BORROW_HISTORY).setType(MetadataValueType.STRUCTURE);
			}
		}
	}
	
	private static class CreateInitialHistoryFromOldVersion extends ActionExecutorInBatch {
		private RecordServices recordServices;
		private RMSchemasRecordsServices rm;
		
		public CreateInitialHistoryFromOldVersion(SearchServices searchServices, String actionName, int batchSize) {
			super(searchServices, actionName, batchSize);
		}

		public void setRecordServices(RecordServices recordServices) {
			this.recordServices = recordServices;
		}
		
		public void setRmSchemasRecordsServices(RMSchemasRecordsServices rmSchemasRecordsServices) {
			this.rm = rmSchemasRecordsServices;
		}
		
		@Override
		public void doActionOnBatch(List<Record> records) throws Exception {
			Transaction transaction = new Transaction();
			
			for (Record record : records) {
				BorrowHistory bh = new BorrowHistory();
				
				bh.setBorrowDate(record.get(rm.containerRecord.borrowDate()))
					.setBorrowerUserName(record.get(rm.containerRecord.borrower()))
					.setPlannedBorrowDate(record.get(rm.containerRecord.planifiedReturnDate()))
					.setReturnDate(record.get(rm.containerRecord.realReturnDate()));
				
				record.set(rm.containerRecord.borrowHistory(), bh);
				
				transaction.add(record);
			}
			
			recordServices.execute(transaction);
		}
	}
}
