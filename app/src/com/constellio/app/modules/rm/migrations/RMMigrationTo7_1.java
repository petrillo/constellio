package com.constellio.app.modules.rm.migrations;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.services.RMGeneratedSchemaRecordsServices.SchemaTypeShortcuts_containerRecord_default;
import com.constellio.app.modules.rm.services.RMGeneratedSchemaRecordsServices.SchemaTypeShortcuts_folder_default;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.structures.BorrowHistory;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.ActionExecutorInBatch;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.MetadataSchema;
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

		SchemaAlternation alternation = new SchemaAlternation(collection, migrationResourcesProvider, appLayerFactory);
		alternation.migrate();

		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		SearchServices searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
		RecordServices recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();

		InitBorrowHistoryForContainerRecordFromOldVersion batchContainer = new InitBorrowHistoryForContainerRecordFromOldVersion(
				searchServices, "Init Containers Borrow History", 100);
		batchContainer.setRmSchemasRecordsServices(rm);
		batchContainer.setRecordServices(recordServices);
		batchContainer.execute();

		InitBorrowHistoryForFolderFromOldVersion batchFolder = new InitBorrowHistoryForFolderFromOldVersion(
				searchServices, "Init Folders Borrow History", 100);
		batchFolder.setRmSchemasRecordsServices(rm);
		batchFolder.setRecordServices(recordServices);
		batchFolder.execute();
	}

	private class SchemaAlternation extends MetadataSchemasAlterationHelper {

		public SchemaAlternation(String collection, MigrationResourcesProvider migrationResourcesProvider,
				AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaBuilder schema = typesBuilder.getSchema(ContainerRecord.DEFAULT_SCHEMA);

			if (!schema.hasMetadata(ContainerRecord.BORROW_HISTORY)) {
				schema.createUndeletable(ContainerRecord.BORROW_HISTORY).setType(MetadataValueType.STRUCTURE);
			}

			schema = typesBuilder.getSchema(Folder.DEFAULT_SCHEMA);

			if (!schema.hasMetadata(ContainerRecord.BORROW_HISTORY)) {
				schema.createUndeletable(ContainerRecord.BORROW_HISTORY).setType(MetadataValueType.STRUCTURE);
			}
		}
	}

	private abstract class InitBorrowHistoryFromOldVersion extends ActionExecutorInBatch {
		protected RecordServices recordServices;
		protected RMSchemasRecordsServices rm;

		public InitBorrowHistoryFromOldVersion(SearchServices searchServices, String actionName, int batchSize) {
			super(searchServices, actionName, batchSize);
		}

		public void setRecordServices(RecordServices recordServices) {
			this.recordServices = recordServices;
		}

		public void setRmSchemasRecordsServices(RMSchemasRecordsServices rmSchemasRecordsServices) {
			this.rm = rmSchemasRecordsServices;
		}

		public void execute() throws Exception {
			execute(composeLogicalSearchCondition(getSchema()));
		}

		protected LogicalSearchCondition composeLogicalSearchCondition(MetadataSchema schema) {
			return LogicalSearchQueryOperators.from(schema).returnAll();
		}

		protected abstract MetadataSchema getSchema();
	}

	private class InitBorrowHistoryForContainerRecordFromOldVersion extends InitBorrowHistoryFromOldVersion {

		public InitBorrowHistoryForContainerRecordFromOldVersion(SearchServices searchServices, String actionName,
				int batchSize) {
			super(searchServices, actionName, batchSize);
		}

		protected MetadataSchema getSchema() {
			return getSchemaType().schema();
		}

		private SchemaTypeShortcuts_containerRecord_default getSchemaType() {
			return rm.containerRecord;
		}

		@Override
		public void doActionOnBatch(List<Record> records) throws Exception {
			Transaction transaction = new Transaction();

			for (Record record : records) {
				String borrowerId = record.get(getSchemaType().borrower());
				Date borrowDate = record.get(getSchemaType().borrowDate());

				Date planifiedReturnDate = record.get(getSchemaType().planifiedReturnDate());
				Date returnDate = record.get(getSchemaType().realReturnDate());

				if (StringUtils.isNotBlank(borrowerId) && borrowDate != null) {
					BorrowHistory bh = new BorrowHistory(null, borrowerId, null, null, null, planifiedReturnDate,
							borrowDate, returnDate);

					record.set(getSchemaType().borrowHistory(), bh);
					transaction.add(record);
				}
			}

			recordServices.execute(transaction);
		}
	}

	private class InitBorrowHistoryForFolderFromOldVersion extends InitBorrowHistoryFromOldVersion {

		public InitBorrowHistoryForFolderFromOldVersion(SearchServices searchServices, String actionName,
				int batchSize) {
			super(searchServices, actionName, batchSize);
		}

		protected MetadataSchema getSchema() {
			return getSchemaType().schema();
		}

		private SchemaTypeShortcuts_folder_default getSchemaType() {
			return rm.folder;
		}

		@Override
		public void doActionOnBatch(List<Record> records) throws Exception {
			Transaction transaction = new Transaction();

			for (Record record : records) {
				String borrowerId = record.get(getSchemaType().borrowUser());
				Date borrowDate = record.get(getSchemaType().borrowDate());

				Date planifiedReturnDate = record.get(getSchemaType().borrowPreviewReturnDate());
				Date returnDate = record.get(getSchemaType().borrowReturnDate());

				if (StringUtils.isNotBlank(borrowerId) && borrowDate != null) {
					BorrowHistory bh = new BorrowHistory(null, borrowerId, null, null, null, planifiedReturnDate,
							borrowDate, returnDate);

					record.set(getSchemaType().borrowHistory(), bh);
					transaction.add(record);
				}
			}

			recordServices.execute(transaction);
		}
	}
}
