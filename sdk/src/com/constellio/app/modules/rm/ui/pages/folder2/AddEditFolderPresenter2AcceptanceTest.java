package com.constellio.app.modules.rm.ui.pages.folder2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.SDKViewNavigation;
import com.constellio.sdk.tests.setups.Users;

public class AddEditFolderPresenter2AcceptanceTest extends ConstellioTest {

	@Mock AddEditFolderView2 view;
	AddEditFolderPresenter2 presenter;
	Users users = new Users();
	RMTestRecords records = new RMTestRecords(zeCollection);
	RMSchemasRecordsServices rm;
	SDKViewNavigation viewNavigation;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(withZeCollection().withConstellioRMModule().withAllTest(users).withRMTest(records)
				.withFoldersAndContainersOfEveryStatus());
		when(view.getCollection()).thenReturn(zeCollection);
		when(view.getConstellioFactories()).thenReturn(getConstellioFactories());
		SessionContext sessionContext = FakeSessionContext.adminInCollection(zeCollection);
		sessionContext.setCurrentLocale(Locale.FRENCH);
		when(view.getSessionContext()).thenReturn(sessionContext);

		presenter = new AddEditFolderPresenter2(view);
		viewNavigation = new SDKViewNavigation(view);

		//Validate initial state
		assertThat(presenter.getFolder()).isNull();

		rm = new RMSchemasRecordsServices(zeCollection, getModelLayerFactory());
	}

	@Test
	public void whenInAddModeThenCreateFolder()
			throws Exception {

		presenter.forParams("");

		assertThat(presenter.isAddMode()).isTrue();
		assertThat(presenter.isEditMode()).isFalse();
		assertThat(presenter.getFolder()).isNotNull();
		assertThat(presenter.getFolder().getSchema().getCode()).isEqualTo(rm.folder.schema().getCode());

	}

	@Test
	public void whenInEditModeThenLoadFolderWithId()
			throws Exception {

		presenter.forParams("id=A16");

		assertThat(presenter.isAddMode()).isFalse();
		assertThat(presenter.isEditMode()).isTrue();
		assertThat(presenter.getFolder()).isNotNull();
		assertThat(presenter.getFolder().getId()).isEqualTo("A16");

	}

	@Test
	public void whenInAddModeWithParentFolderThenCreateSubFolderWithParentId()
			throws Exception {

		presenter.forParams("parentId=A16");

		assertThat(presenter.isAddMode()).isTrue();
		assertThat(presenter.isEditMode()).isFalse();
		assertThat(presenter.getFolder()).isNotNull();
		assertThat(presenter.getFolder().getId()).isNotEqualTo("A16");
		assertThat(presenter.getFolder().get(Folder.PARENT_FOLDER)).isEqualTo("A16");

	}

	@Test
	public void givenInAddModeWhenCancelButtonClickedThenReturnToHome()
			throws Exception {

		presenter.forParams("");

		presenter.backButtonClicked();

		verify(viewNavigation.rmViews).recordsManagement();
	}

	@Test
	public void givenInEditModeWhenCancelButtonClickedThenReturnToFolderDisplay()
			throws Exception {

		presenter.forParams("id=A16");

		presenter.backButtonClicked();

		verify(viewNavigation.rmViews).displayFolder("A16");
	}

	@Test
	public void givenInAddModeWithParentFolderWhenCancelButtonClickedThenReturnToParentFolderDisplay()
			throws Exception {

		presenter.forParams("parentId=A15");

		presenter.backButtonClicked();

		verify(viewNavigation.rmViews).displayFolder("A15");
	}
}
