package com.constellio.app.modules.rm.migrations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.sdk.tests.ConstellioTest;

public class RMMigrationTo7_1AcceptanceTest extends ConstellioTest {

	private RMTestRecords records;
	private RMSchemasRecordsServices rm;
	private SearchServices searchServices;
	private RecordServices recordServices;

	@Before
	public void setUp() {
		records = new RMTestRecords(zeCollection);

		prepareSystem(withZeCollection().withAllTestUsers().withConstellioRMModule().withConstellioESModule()
				.withRMTest(records).withFoldersAndContainersOfEveryStatus().withDocumentsDecommissioningList()
				.withDocumentsHavingContent());

		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
	}

	@Test
	public void givenMetadataIsCreatedWhenMigrating() {
		MetadataSchema containerRecordSchema = rm.containerRecord.schema();

		Metadata borrowHistory = containerRecordSchema.get(ContainerRecord.BORROW_HISTORY);
		assertThat(borrowHistory).isNotNull();
		assertThat(borrowHistory.getType()).isEqualTo(MetadataValueType.STRUCTURE);
	}

}
