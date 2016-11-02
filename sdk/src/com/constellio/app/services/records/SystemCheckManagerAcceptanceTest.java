package com.constellio.app.services.records;

import static com.constellio.model.entities.schemas.Schemas.LOGICALLY_DELETED_ON;
import static com.constellio.model.entities.schemas.Schemas.TITLE;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.ALL;
import static com.constellio.sdk.tests.TestUtils.asMap;
import static com.constellio.sdk.tests.TestUtils.extractingSimpleCodeAndParameters;
import static com.constellio.sdk.tests.TestUtils.frenchMessages;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsMultivalue;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.joda.time.LocalDate.now;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.common.SolrInputDocument;
import org.junit.Test;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestRecord;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.AnotherSchemaMetadatas;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ZeSchemaMetadatas;

public class SystemCheckManagerAcceptanceTest extends ConstellioTest {

	TestsSchemasSetup setup = new TestsSchemasSetup(zeCollection);
	ZeSchemaMetadatas zeSchema = setup.new ZeSchemaMetadatas();
	AnotherSchemaMetadatas anotherSchema = setup.new AnotherSchemaMetadatas();

	@Test
	public void givenSystemWithBrokenSingleValueLinksWhenSystemCheckingThenFindThoseLinks()
			throws Exception {

		defineSchemasManager().using(setup.withAReferenceFromAnotherSchemaToZeSchema());
		RecordServices recordServices = getModelLayerFactory().newRecordServices();

		Transaction transaction = new Transaction();
		transaction.add(new TestRecord(zeSchema, "zeId").set(TITLE, "1"));
		transaction.add(new TestRecord(anotherSchema, "recordWithProblem1").set(TITLE, "2")
				.set(anotherSchema.referenceFromAnotherSchemaToZeSchema(), "zeId"));
		transaction.add(new TestRecord(anotherSchema, "recordWithoutProblem").set(TITLE, "3")
				.set(anotherSchema.referenceFromAnotherSchemaToZeSchema(), "zeId"));
		transaction.add(new TestRecord(anotherSchema, "recordWithProblem2").set(TITLE, "4")
				.set(anotherSchema.referenceFromAnotherSchemaToZeSchema(), "zeId"));
		recordServices.execute(transaction);

		SolrClient solrClient = getDataLayerFactory().newRecordDao().getBigVaultServer().getNestedSolrServer();
		SolrInputDocument modificationBypassingIntegrityValidations = new SolrInputDocument();
		modificationBypassingIntegrityValidations.setField("id", "recordWithProblem1");
		modificationBypassingIntegrityValidations.setField("_version_", "1");
		modificationBypassingIntegrityValidations.setField("referenceFromAnotherSchemaToZeSchemaId_s", asMap("set", "bad"));
		solrClient.add(modificationBypassingIntegrityValidations);

		modificationBypassingIntegrityValidations = new SolrInputDocument();
		modificationBypassingIntegrityValidations.setField("id", "recordWithProblem2");
		modificationBypassingIntegrityValidations.setField("_version_", "1");
		modificationBypassingIntegrityValidations
				.setField("referenceFromAnotherSchemaToZeSchemaId_s", asMap("set", "notGood"));
		solrClient.add(modificationBypassingIntegrityValidations);

		solrClient.commit();

		//rams.put("metadataCode", referenceMetadata.getCode());
		//params.put("record", recordId);
		//params.put("brokenLinkRecordId

		SystemCheckResults systemCheckResults = new SystemCheckManager(getAppLayerFactory()).runSystemCheck(false);
		assertThat(systemCheckResults.brokenReferences).isEqualTo(2);
		assertThat(systemCheckResults.checkedReferences).isEqualTo(3);
		assertThat(systemCheckResults.repairedRecords.size()).isEqualTo(0);
		assertThat(extractingSimpleCodeAndParameters(systemCheckResults.errors, "metadataCode", "record", "brokenLinkRecordId"))
				.containsOnly(
						tuple("SystemCheckResultsBuilder_brokenLink",
								"anotherSchemaType_default_referenceFromAnotherSchemaToZeSchema", "recordWithProblem1", "bad"),
						tuple("SystemCheckResultsBuilder_brokenLink",
								"anotherSchemaType_default_referenceFromAnotherSchemaToZeSchema", "recordWithProblem2", "notGood")
				);
		assertThat(frenchMessages(systemCheckResults.errors)).containsOnly(
				"La métadonnée anotherSchemaType_default_referenceFromAnotherSchemaToZeSchema de l'enregistrement recordWithProblem1 référence un enregistrement inexistant : bad",
				"La métadonnée anotherSchemaType_default_referenceFromAnotherSchemaToZeSchema de l'enregistrement recordWithProblem2 référence un enregistrement inexistant : notGood"
		);
		assertThat(recordServices.getDocumentById("recordWithProblem1").get(anotherSchema.referenceFromAnotherSchemaToZeSchema()))
				.isEqualTo("bad");
		assertThat(recordServices.getDocumentById("recordWithProblem2").get(anotherSchema.referenceFromAnotherSchemaToZeSchema()))
				.isEqualTo("notGood");

		systemCheckResults = new SystemCheckManager(getAppLayerFactory()).runSystemCheck(true);
		assertThat(systemCheckResults.brokenReferences).isEqualTo(2);
		assertThat(systemCheckResults.checkedReferences).isEqualTo(3);
		assertThat(systemCheckResults.repairedRecords.size()).isEqualTo(2);
		assertThat(extractingSimpleCodeAndParameters(systemCheckResults.errors, "metadataCode", "record", "brokenLinkRecordId"))
				.containsOnly(
						tuple("SystemCheckResultsBuilder_brokenLink",
								"anotherSchemaType_default_referenceFromAnotherSchemaToZeSchema", "recordWithProblem1", "bad"),
						tuple("SystemCheckResultsBuilder_brokenLink",
								"anotherSchemaType_default_referenceFromAnotherSchemaToZeSchema", "recordWithProblem2", "notGood")
				);

		assertThat(recordServices.getDocumentById("recordWithProblem1").get(anotherSchema.referenceFromAnotherSchemaToZeSchema()))
				.isNull();
		assertThat(recordServices.getDocumentById("recordWithProblem2").get(anotherSchema.referenceFromAnotherSchemaToZeSchema()))
				.isNull();

		systemCheckResults = new SystemCheckManager(getAppLayerFactory()).runSystemCheck(false);
		assertThat(systemCheckResults.brokenReferences).isEqualTo(0);
		assertThat(systemCheckResults.checkedReferences).isEqualTo(1);
		assertThat(systemCheckResults.repairedRecords.size()).isEqualTo(0);
		assertThat(systemCheckResults.errors.getValidationErrors()).isEmpty();
		assertThat(systemCheckResults.errors.getValidationWarnings()).isEmpty();
	}

	@Test
	public void givenSystemWithBrokenMultiValueLinksWhenSystemCheckingThenFindThoseLinks()
			throws Exception {

		defineSchemasManager().using(setup.withAReferenceFromAnotherSchemaToZeSchema(whichIsMultivalue));
		RecordServices recordServices = getModelLayerFactory().newRecordServices();

		Transaction transaction = new Transaction();
		transaction.add(new TestRecord(zeSchema, "recordA").set(TITLE, "A"));
		transaction.add(new TestRecord(zeSchema, "recordB").set(TITLE, "B"));
		transaction.add(new TestRecord(zeSchema, "recordC").set(TITLE, "C"));
		transaction.add(new TestRecord(anotherSchema, "recordWithProblem1").set(TITLE, "2")
				.set(anotherSchema.referenceFromAnotherSchemaToZeSchema(), asList("recordC", "recordA", "recordB")));
		transaction.add(new TestRecord(anotherSchema, "recordWithoutProblem").set(TITLE, "3")
				.set(anotherSchema.referenceFromAnotherSchemaToZeSchema(), asList("recordA", "recordB")));
		transaction.add(new TestRecord(anotherSchema, "recordWithProblem2").set(TITLE, "4")
				.set(anotherSchema.referenceFromAnotherSchemaToZeSchema(), asList("recordA", "recordC")));
		recordServices.execute(transaction);

		SolrClient solrClient = getDataLayerFactory().newRecordDao().getBigVaultServer().getNestedSolrServer();
		solrClient.deleteById("recordC");
		solrClient.commit();

		//rams.put("metadataCode", referenceMetadata.getCode());
		//params.put("record", recordId);
		//params.put("brokenLinkRecordId

		SystemCheckResults systemCheckResults = new SystemCheckManager(getAppLayerFactory()).runSystemCheck(false);
		assertThat(systemCheckResults.brokenReferences).isEqualTo(2);
		assertThat(systemCheckResults.checkedReferences).isEqualTo(7);
		assertThat(systemCheckResults.repairedRecords.size()).isEqualTo(0);
		assertThat(extractingSimpleCodeAndParameters(systemCheckResults.errors, "metadataCode", "record", "brokenLinkRecordId"))
				.containsOnly(
						tuple("SystemCheckResultsBuilder_brokenLink",
								"anotherSchemaType_default_referenceFromAnotherSchemaToZeSchema", "recordWithProblem1",
								"recordC"),
						tuple("SystemCheckResultsBuilder_brokenLink",
								"anotherSchemaType_default_referenceFromAnotherSchemaToZeSchema", "recordWithProblem2", "recordC")
				);
		assertThat(frenchMessages(systemCheckResults.errors)).containsOnly(
				"La métadonnée anotherSchemaType_default_referenceFromAnotherSchemaToZeSchema de l'enregistrement recordWithProblem1 référence un enregistrement inexistant : recordC",
				"La métadonnée anotherSchemaType_default_referenceFromAnotherSchemaToZeSchema de l'enregistrement recordWithProblem2 référence un enregistrement inexistant : recordC"
		);
		assertThat(recordServices.getDocumentById("recordWithProblem1").get(anotherSchema.referenceFromAnotherSchemaToZeSchema()))
				.isEqualTo(asList("recordC", "recordA", "recordB"));
		assertThat(recordServices.getDocumentById("recordWithProblem2").get(anotherSchema.referenceFromAnotherSchemaToZeSchema()))
				.isEqualTo(asList("recordA", "recordC"));

		systemCheckResults = new SystemCheckManager(getAppLayerFactory()).runSystemCheck(true);
		assertThat(systemCheckResults.brokenReferences).isEqualTo(2);
		assertThat(systemCheckResults.checkedReferences).isEqualTo(7);
		assertThat(systemCheckResults.repairedRecords.size()).isEqualTo(2);
		assertThat(extractingSimpleCodeAndParameters(systemCheckResults.errors, "metadataCode", "record", "brokenLinkRecordId"))
				.containsOnly(
						tuple("SystemCheckResultsBuilder_brokenLink",
								"anotherSchemaType_default_referenceFromAnotherSchemaToZeSchema", "recordWithProblem1",
								"recordC"),
						tuple("SystemCheckResultsBuilder_brokenLink",
								"anotherSchemaType_default_referenceFromAnotherSchemaToZeSchema", "recordWithProblem2", "recordC")
				);

		assertThat(recordServices.getDocumentById("recordWithProblem1").get(anotherSchema.referenceFromAnotherSchemaToZeSchema()))
				.isEqualTo(asList("recordA", "recordB"));
		assertThat(recordServices.getDocumentById("recordWithProblem2").get(anotherSchema.referenceFromAnotherSchemaToZeSchema()))
				.isEqualTo(asList("recordA"));

		systemCheckResults = new SystemCheckManager(getAppLayerFactory()).runSystemCheck(false);
		assertThat(systemCheckResults.brokenReferences).isEqualTo(0);
		assertThat(systemCheckResults.checkedReferences).isEqualTo(5);
		assertThat(systemCheckResults.repairedRecords.size()).isEqualTo(0);
		assertThat(systemCheckResults.errors.getValidationErrors()).isEmpty();
		assertThat(systemCheckResults.errors.getValidationWarnings()).isEmpty();
	}

	@Test
	public void givenLogicallyDeletedAdministrativeUnitsAndCategoriesThenRepairRestoreThem()
			throws Exception {
		RMTestRecords records = new RMTestRecords(zeCollection);
		prepareSystem(withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(records));
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());

		RecordServices recordServices = getModelLayerFactory().newRecordServices();
		recordServices.add(rm.newFolder().setTitle("My folder").setAdministrativeUnitEntered(records.unitId_20)
				.setOpenDate(now()).setCategoryEntered(records.categoryId_Z100).setRetentionRuleEntered(records.ruleId_1));
		for (Category category : rm.searchCategorys(ALL)) {
			recordServices.update(category.set(Schemas.LOGICALLY_DELETED_STATUS.getLocalCode(), true));
		}
		recordServices.logicallyDelete(records.getUnit12c().getWrappedRecord(), User.GOD);
		recordServices.logicallyDelete(records.getUnit20d().getWrappedRecord(), User.GOD);
		recordServices.logicallyDelete(records.getUnit20e().getWrappedRecord(), User.GOD);
		recordServices.update(records.getUnit20().set(Schemas.LOGICALLY_DELETED_STATUS.getLocalCode(), true));
		assertThat(records.getUnit20().isLogicallyDeletedStatus()).isTrue();
		assertThat(records.getUnit12c().isLogicallyDeletedStatus()).isTrue();
		assertThat(records.getCategory_X13().isLogicallyDeletedStatus()).isTrue();
		assertThat(records.getCategory_Z().isLogicallyDeletedStatus()).isTrue();
		assertThat(records.getCategory_Z100().isLogicallyDeletedStatus()).isTrue();
		assertThat(records.getCategory_Z110().isLogicallyDeletedStatus()).isTrue();
		assertThat(records.getCategory_Z112().isLogicallyDeletedStatus()).isTrue();

		SystemCheckResults systemCheckResults = new SystemCheckManager(getAppLayerFactory()).runSystemCheck(false);

		assertThat(systemCheckResults.repairedRecords.size()).isEqualTo(0);
		assertThat(extractingSimpleCodeAndParameters(systemCheckResults.errors, "schemaType", "recordId")).containsOnly(
				SystemCheckManagerAcceptanceTestResources.expectedErrorsWhenLogicallyDeletedCategoriesAndUnits);
		assertThat(frenchMessages(systemCheckResults.errors)).containsOnly(
				SystemCheckManagerAcceptanceTestResources.expectedErrorsWhenLogicallyDeletedCategoriesAndUnitsErrorMessages
		);
		assertThat(statusOf(records.unitId_20)).isEqualTo(RecordStatus.LOGICALLY_DELETED);
		assertThat(statusOf(records.unitId_12c)).isEqualTo(RecordStatus.LOGICALLY_DELETED);
		assertThat(statusOf(records.categoryId_X13)).isEqualTo(RecordStatus.LOGICALLY_DELETED);
		assertThat(statusOf(records.categoryId_Z)).isEqualTo(RecordStatus.LOGICALLY_DELETED);
		assertThat(statusOf(records.categoryId_Z100)).isEqualTo(RecordStatus.LOGICALLY_DELETED);
		assertThat(statusOf(records.categoryId_Z110)).isEqualTo(RecordStatus.LOGICALLY_DELETED);
		assertThat(statusOf(records.categoryId_Z112)).isEqualTo(RecordStatus.LOGICALLY_DELETED);

		systemCheckResults = new SystemCheckManager(getAppLayerFactory()).runSystemCheck(true);
		assertThat(systemCheckResults.repairedRecords.size()).isEqualTo(18);
		assertThat(extractingSimpleCodeAndParameters(systemCheckResults.errors, "schemaType", "recordId")).containsOnly(
				SystemCheckManagerAcceptanceTestResources.expectedErrorsWhenLogicallyDeletedCategoriesAndUnits);
		assertThat(statusOf(records.unitId_20)).isEqualTo(RecordStatus.ACTIVE);
		assertThat(statusOf(records.unitId_12c)).isEqualTo(RecordStatus.DELETED);
		assertThat(statusOf(records.categoryId_X13)).isEqualTo(RecordStatus.DELETED);
		assertThat(statusOf(records.categoryId_Z100)).isEqualTo(RecordStatus.ACTIVE);
		assertThat(statusOf(records.categoryId_Z)).isEqualTo(RecordStatus.ACTIVE);
		assertThat(statusOf(records.categoryId_Z110)).isEqualTo(RecordStatus.DELETED);
		assertThat(statusOf(records.categoryId_Z112)).isEqualTo(RecordStatus.DELETED);

		systemCheckResults = new SystemCheckManager(getAppLayerFactory()).runSystemCheck(false);
		assertThat(systemCheckResults.repairedRecords.size()).isEqualTo(0);
		assertThat(extractingSimpleCodeAndParameters(systemCheckResults.errors, "schemaType", "recordId")).isEmpty();
	}

	private static enum RecordStatus {ACTIVE, LOGICALLY_DELETED, DELETED}

	private RecordStatus statusOf(String id) {
		try {
			Record record = getModelLayerFactory().newRecordServices().getDocumentById(id);
			return record.<Boolean>get(Schemas.LOGICALLY_DELETED_STATUS) ? RecordStatus.LOGICALLY_DELETED : RecordStatus.ACTIVE;
		} catch (Exception e) {
			return RecordStatus.DELETED;
		}

	}
}