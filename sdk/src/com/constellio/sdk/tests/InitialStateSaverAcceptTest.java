package com.constellio.sdk.tests;

import java.util.List;

import org.apache.commons.lang3.RandomUtils;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.Test;

import com.constellio.app.modules.rm.DemoTestRecords;
import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.sdk.tests.annotations.MainTest;
import com.constellio.sdk.tests.annotations.UiTest;
import com.constellio.sdk.tests.setups.UsersExtension;

@UiTest
@MainTest
public class InitialStateSaverAcceptTest extends ConstellioTest {

	@Test
	public void saveCurrentInitialState() throws Exception {

		givenTransactionLogIsEnabled();
		givenCollection(zeCollection).withConstellioRMModule();

		getSaveStateFeature().saveCurrentStateToInitialStatesFolder();
	}

	@Test
	public void saveModifiedState() throws Exception {

		givenTransactionLogIsEnabled();
		RMTestRecords records = new RMTestRecords(zeCollection);

		prepareSystem(withZeCollection().withAllTestUsers().withConstellioRMModule()
				.withFoldersAndContainersOfEveryStatus().withRMTest(records).withConstellioESModule());

		getSaveStateFeature().saveStateAfterTestWithTitle("with_manual_modifications");

		ModelLayerFactory modelLayerFactory = getModelLayerFactory();
		modelLayerFactory.getSystemConfigurationsManager().setValue(RMConfigs.DOCUMENT_RETENTION_RULES, true);

		newWebDriver(loggedAsUserInCollection(admin, zeCollection));
		waitUntilICloseTheBrowsers();
	}
	
	@Test
	public void saveModifiedStateOnStart() throws Exception {

		givenTransactionLogIsEnabled();
		RMTestRecords records = new RMTestRecords(zeCollection);

		prepareSystem(withZeCollection().withAllTestUsers().withConstellioRMModule()
				.withFoldersAndContainersOfEveryStatus().withRMTest(records).withConstellioESModule());

		getSaveStateFeature().saveStateAfterTestWithTitle("with_manual_modifications");

		ModelLayerFactory modelLayerFactory = getModelLayerFactory();
		modelLayerFactory.getSystemConfigurationsManager().setValue(RMConfigs.DOCUMENT_RETENTION_RULES, true);

		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());

		computeRecords(new ContainerRecordBorrower(rm));
		computeRecords(new FolderRecordBorrower(rm));
	}

	private void computeRecords(RecordBorrower rb) throws RecordServicesException {
		SearchServices searchServices = getModelLayerFactory().newSearchServices();
		RecordServices recordServices = getModelLayerFactory().newRecordServices();
		Transaction transaction = new Transaction();
		
		List<Record> search = searchServices.search(
				new LogicalSearchQuery(LogicalSearchQueryOperators.from(rb.getSchema()).returnAll()));
		for (Record record : search) {
			rb.visit(record);

			transaction.addAll(record);
		}
		
		recordServices.execute(transaction);
	}
	
	private abstract class RecordBorrower {
		protected final RMSchemasRecordsServices rm;
		protected int maxMonthIncrement = 5;
		protected LocalDate borrowDate = LocalDate.now().minusMonths(maxMonthIncrement);
		protected List<User> users;

		public RecordBorrower(RMSchemasRecordsServices rm) {
			this.rm = rm;
			
			UsersExtension usersExtension = (UsersExtension) new UsersExtension().using(getModelLayerFactory().newUserServices());
			users = usersExtension.getUsers(zeCollection);
		}
		
		public abstract MetadataSchema getSchema();
		
		public abstract void visit(Record record);
	}
	
	private class ContainerRecordBorrower extends RecordBorrower {

		public ContainerRecordBorrower(RMSchemasRecordsServices rm) {
			super(rm);
		}

		@Override
		public MetadataSchema getSchema() {
			return rm.containerRecord.schema();
		}

		@Override
		public void visit(Record record) {
			ContainerRecord container = rm.wrapContainerRecord(record);

			LocalDate planifiedReturnDate = borrowDate.plusMonths(RandomUtils.nextInt(0, maxMonthIncrement));

			switch (RandomUtils.nextInt(0, 7)) {
			case 0:
				container.setBorrowDate(borrowDate);
				container.setBorrower(users.get(RandomUtils.nextInt(0, users.size())));
				container.setPlanifiedReturnDate(planifiedReturnDate);
				container.setRealReturnDate(planifiedReturnDate.plusDays(RandomUtils.nextInt(0, 10)));
				break;

			case 1:
				container.setBorrowDate(borrowDate);
				container.setBorrower(users.get(RandomUtils.nextInt(0, users.size())));
				container.setPlanifiedReturnDate(planifiedReturnDate);
				container.setRealReturnDate(planifiedReturnDate.minusDays(RandomUtils.nextInt(0, 10)));
				break;

			case 2:
				container.setBorrowDate(borrowDate);
				container.setBorrower(users.get(RandomUtils.nextInt(0, users.size())));
				container.setPlanifiedReturnDate(planifiedReturnDate);
				container.setBorrowed(true);
				break;

			case 3:
				container.setBorrowDate(borrowDate);
				container.setPlanifiedReturnDate(planifiedReturnDate);
				container.setRealReturnDate(planifiedReturnDate.plusDays(RandomUtils.nextInt(0, 10)));
				break;

			case 4:
				container.setBorrower(users.get(RandomUtils.nextInt(0, users.size())));
				container.setPlanifiedReturnDate(planifiedReturnDate);
				container.setRealReturnDate(planifiedReturnDate.plusDays(RandomUtils.nextInt(0, 10)));
				break;

			default:
				
				break;
			}
		}
	}
	
	private class FolderRecordBorrower extends RecordBorrower {

		public FolderRecordBorrower(RMSchemasRecordsServices rm) {
			super(rm);
		}

		@Override
		public MetadataSchema getSchema() {
			return rm.folder.schema();
		}

		@Override
		public void visit(Record record) {
			Folder container = rm.wrapFolder(record);

			LocalDate planifiedReturnDate = borrowDate.plusMonths(RandomUtils.nextInt(0, maxMonthIncrement));
			
			switch (RandomUtils.nextInt(0, 7)) {
			case 0:
				container.setBorrowDate(toLocalDateTime(borrowDate));
				container.setBorrowUserEntered(users.get(RandomUtils.nextInt(0, users.size())).getId());
				container.setBorrowPreviewReturnDate(planifiedReturnDate);
				container.setBorrowReturnDate(toLocalDateTime(planifiedReturnDate.plusDays(RandomUtils.nextInt(0, 10))));
				break;

			case 1:
				container.setBorrowDate(toLocalDateTime(borrowDate));
				container.setBorrowUserEntered(users.get(RandomUtils.nextInt(0, users.size())).getId());
				container.setBorrowPreviewReturnDate(planifiedReturnDate);
				container.setBorrowReturnDate(toLocalDateTime(planifiedReturnDate.minusDays(RandomUtils.nextInt(0, 10))));
				break;

			case 2:
				container.setBorrowDate(toLocalDateTime(borrowDate));
				container.setBorrowUserEntered(users.get(RandomUtils.nextInt(0, users.size())).getId());
				container.setBorrowPreviewReturnDate(planifiedReturnDate);
				container.setBorrowed(true);
				break;

			case 3:
				container.setBorrowDate(toLocalDateTime(borrowDate));
				container.setBorrowPreviewReturnDate(planifiedReturnDate);
				container.setBorrowReturnDate(toLocalDateTime(planifiedReturnDate.plusDays(RandomUtils.nextInt(0, 10))));
				break;

			case 4:
				container.setBorrowUserEntered(users.get(RandomUtils.nextInt(0, users.size())).getId());
				container.setBorrowPreviewReturnDate(planifiedReturnDate);
				container.setBorrowReturnDate(toLocalDateTime(planifiedReturnDate.plusDays(RandomUtils.nextInt(0, 10))));
				break;

			default:
				
				break;
			}
		}
		
		private LocalDateTime toLocalDateTime(LocalDate localDate) {
			return LocalDateTime.fromDateFields(localDate.toDate());
		}
	}

	@Test
	public void saveWithEnterpriseSearchModule() throws Exception {

		givenTransactionLogIsEnabled();
		givenCollection(zeCollection).withConstellioESModule().withAllTestUsers();

		getSaveStateFeature().saveStateAfterTestWithTitle("with_enterprise_search_module");

		newWebDriver(loggedAsUserInCollection(admin, zeCollection));
		waitUntilICloseTheBrowsers();

	}

	@Test
	public void saveModifiedStateStartingFromNewSystem() throws Exception {

		givenTransactionLogIsEnabled();
		// givenCollection(zeCollection).withConstellioRMModule().withAllTestUsers();
		//
		getSaveStateFeature().saveStateAfterTestWithTitle("from_new_system");

		newWebDriver();
		waitUntilICloseTheBrowsers();

	}

	@Test
	public void saveStateWithTestRecords() throws Exception {

		givenTransactionLogIsEnabled();
		givenCollection(zeCollection).withConstellioRMModule().withAllTestUsers();
		RMTestRecords records = new RMTestRecords(zeCollection);
		records.setup(getAppLayerFactory()).withFoldersAndContainersOfEveryStatus();
		givenCollection("zeDeuxiemeCollection").withConstellioRMModule().withAllTestUsers();
		DemoTestRecords records2 = new DemoTestRecords("zeDeuxiemeCollection");
		records2.setup(getAppLayerFactory()).withFoldersAndContainersOfEveryStatus();

		getSaveStateFeature().saveStateAfterTestWithTitle("with_document_rules");

		ModelLayerFactory modelLayerFactory = getModelLayerFactory();
		modelLayerFactory.getSystemConfigurationsManager().setValue(RMConfigs.DOCUMENT_RETENTION_RULES, true);

		newWebDriver(loggedAsUserInCollection(admin, zeCollection));
		waitUntilICloseTheBrowsers();

	}

}
