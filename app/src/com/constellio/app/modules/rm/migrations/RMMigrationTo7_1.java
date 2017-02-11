package com.constellio.app.modules.rm.migrations;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.base.BaseLocal;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.structures.BorrowHistory;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.ActionExecutorInBatch;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.Metadata;
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

		protected Date toDate(BaseLocal bl) {
			if (bl instanceof LocalDate) {
				return ((LocalDate) bl).toDate();
			} else if (bl instanceof LocalDateTime) {
				return ((LocalDateTime) bl).toDate();
			} else {
				return null;
			}
		}

		protected void setRecordBorrowHistory(Record r, String bi, BaseLocal bd, BaseLocal prd, BaseLocal rd,
				Transaction t) {

			if (StringUtils.isNotBlank(bi) && bd != null) {
				BorrowHistory bh = new BorrowHistory(null, bi, null, null, null, toDate(prd), toDate(bd), toDate(rd));

				r.set(getMetadataBorrowHistory(), bh);
				t.add(r);
			}
		}

		protected abstract MetadataSchema getSchema();

		protected abstract Metadata getMetadataBorrowHistory();
	}

	private class InitBorrowHistoryForContainerRecordFromOldVersion extends InitBorrowHistoryFromOldVersion {

		public InitBorrowHistoryForContainerRecordFromOldVersion(SearchServices ss, String an, int bs) {
			super(ss, an, bs);
		}

		protected MetadataSchema getSchema() {
			return rm.containerRecord.schema();
		}

		protected Metadata getMetadataBorrowHistory() {
			return rm.containerRecord.borrowHistory();
		}

		@Override
		public void doActionOnBatch(List<Record> records) throws Exception {
			Transaction transaction = new Transaction();

			for (Record record : records) {
				ContainerRecord container = rm.wrapContainerRecord(record);

				setRecordBorrowHistory(record, container.getBorrower(), container.getBorrowDate(),
						container.getPlanifiedReturnDate(), container.getRealReturnDate(), transaction);
			}

			if (CollectionUtils.isNotEmpty(transaction.getModifiedRecords())) {
				recordServices.execute(transaction);
			}
		}
	}

	private class InitBorrowHistoryForFolderFromOldVersion extends InitBorrowHistoryFromOldVersion {

		public InitBorrowHistoryForFolderFromOldVersion(SearchServices ss, String an, int bs) {
			super(ss, an, bs);
		}

		protected MetadataSchema getSchema() {
			return rm.folder.schema();
		}

		protected Metadata getMetadataBorrowHistory() {
			return rm.folder.borrowHistory();
		}

		@Override
		public void doActionOnBatch(List<Record> records) throws Exception {
			Transaction transaction = new Transaction();

			for (Record record : records) {
				Folder folder = rm.wrapFolder(record);

				setRecordBorrowHistory(record, folder.getBorrowUser(), folder.getBorrowDate(),
						folder.getBorrowPreviewReturnDate(), folder.getBorrowReturnDate(), transaction);
			}

			if (CollectionUtils.isNotEmpty(transaction.getModifiedRecords())) {
				recordServices.execute(transaction);
			}
		}
	}
}
