package com.constellio.app.modules.rm.migrations;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.allConditions;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.anyConditions;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.where;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.List;

import org.junit.Test;

import com.constellio.app.modules.rm.services.RMGeneratedSchemaRecordsServices.SchemaTypeShortcuts_containerRecord_default;
import com.constellio.app.modules.rm.services.RMGeneratedSchemaRecordsServices.SchemaTypeShortcuts_folder_default;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.SDKFoldersLocator;

public class RMMigrationTo7_1AcceptanceTest extends ConstellioTest {

	private RMSchemasRecordsServices rm;
	private SearchServices searchServices;

	@Test
	public void givenMetadataIsCreatedWhenMigrating() {
		givenPreviousSaveStateToLoad();

		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		searchServices = getModelLayerFactory().newSearchServices();

		givenSchemaHasABorrowHistoryReference(rm.containerRecord.schema());
		givenSpecificContainerRecordCasesWithoutBorrowHistory();
		
		givenSchemaHasABorrowHistoryReference(rm.folder.schema());
		givenSpecificFolderCasesWithoutBorrowHistory();
	}

	private void givenSpecificContainerRecordCasesWithoutBorrowHistory() {
		SchemaTypeShortcuts_containerRecord_default rmcr = rm.containerRecord;
		List<Record> search = searchServices.search(new LogicalSearchQuery(from(rmcr.schema()).whereAllConditions(anyConditions(
				allConditions(where(rmcr.borrowDate()).isNull(),
						where(rmcr.borrower()).isNotNull()),
				allConditions(where(rmcr.borrowDate()).isNotNull(),
						where(rmcr.borrower()).isNull())))));
		
		for (Record record : search) {
			ContainerRecord cr = rm.wrapContainerRecord(record);
			assertThat(cr.getBorrowHistory()).isNull();
		}
	}

	private void givenSpecificFolderCasesWithoutBorrowHistory() {
		SchemaTypeShortcuts_folder_default rmf = rm.folder;
		List<Record> search = searchServices.search(new LogicalSearchQuery(from(rmf.schema()).whereAllConditions(anyConditions(
				allConditions(where(rmf.borrowDate()).isNull(),
						where(rmf.borrowUser()).isNotNull()),
				allConditions(where(rmf.borrowDate()).isNotNull(),
						where(rmf.borrowUser()).isNull())))));
		
		for (Record record : search) {
			Folder f = rm.wrapFolder(record);
			assertThat(f.getBorrowHistory()).isNull();
		}
	}

	private void givenSchemaHasABorrowHistoryReference(MetadataSchema containerRecordSchema) {
		Metadata borrowHistory = containerRecordSchema.get(ContainerRecord.BORROW_HISTORY);
		assertThat(borrowHistory).isNotNull();
		assertThat(borrowHistory.getType()).isEqualTo(MetadataValueType.STRUCTURE);
	}

	private void givenPreviousSaveStateToLoad() {
		givenTransactionLogIsEnabled();

		File initialStatesFolder = new SDKFoldersLocator().getInitialStatesFolder();
		File state = new File(initialStatesFolder,
				"given_system_in_6.5.42_with_rm,es,tasks_modules__with_manual_modifications.zip");

		getCurrentTestSession().getFactoriesTestFeatures().givenSystemInState(state);
	}
}
