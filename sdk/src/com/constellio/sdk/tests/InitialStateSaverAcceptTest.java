package com.constellio.sdk.tests;

import java.util.List;

import org.apache.commons.lang3.RandomUtils;
import org.joda.time.LocalDate;
import org.junit.Test;

import com.constellio.app.modules.rm.DemoTestRecords;
import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.sdk.tests.annotations.MainTest;
import com.constellio.sdk.tests.annotations.UiTest;
import com.constellio.sdk.tests.setups.Users;
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

//		newWebDriver(loggedAsUserInCollection(admin, zeCollection));
//		waitUntilICloseTheBrowsers();

		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		SearchServices searchServices = getModelLayerFactory().newSearchServices();
		RecordServices recordServices = getModelLayerFactory().newRecordServices();
		Transaction transaction = new Transaction();

		UsersExtension usersExtension = (UsersExtension) new UsersExtension().using(getModelLayerFactory().newUserServices());
		List<User> users = usersExtension.getUsers(zeCollection);

		int maxMonthIncrement = 5;

		LocalDate borrowDate = LocalDate.now().minusMonths(maxMonthIncrement);

		List<Record> search = searchServices.search(
				new LogicalSearchQuery(LogicalSearchQueryOperators.from(rm.containerRecord.schema()).returnAll()));
		for (Record record : search) {
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

			transaction.addAll(record);
		}
		
		recordServices.execute(transaction);
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
