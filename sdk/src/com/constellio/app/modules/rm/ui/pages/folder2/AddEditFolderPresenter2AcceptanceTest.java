package com.constellio.app.modules.rm.ui.pages.folder2;

import static com.constellio.app.modules.rm.wrappers.Folder.ADMINISTRATIVE_UNIT_ENTERED;
import static com.constellio.app.modules.rm.wrappers.Folder.CATEGORY_ENTERED;
import static com.constellio.app.modules.rm.wrappers.Folder.COPY_STATUS_ENTERED;
import static com.constellio.app.modules.rm.wrappers.Folder.MAIN_COPY_RULE_ID_ENTERED;
import static com.constellio.app.modules.rm.wrappers.Folder.PARENT_FOLDER;
import static com.constellio.app.modules.rm.wrappers.Folder.RETENTION_RULE_ENTERED;
import static com.constellio.app.modules.rm.wrappers.Folder.UNIFORM_SUBDIVISION_ENTERED;
import static com.constellio.sdk.tests.TestUtils.getBoolean;
import static com.constellio.sdk.tests.TestUtils.setBoolean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.endsWith;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.ui.components.folder.FolderForm;
import com.constellio.app.modules.rm.ui.components.folder.fields.CustomFolderField;
import com.constellio.app.modules.rm.ui.components.folder.fields.FolderAdministrativeUnitField;
import com.constellio.app.modules.rm.ui.components.folder.fields.FolderCategoryField;
import com.constellio.app.modules.rm.ui.components.folder.fields.FolderCopyRuleField;
import com.constellio.app.modules.rm.ui.components.folder.fields.FolderCopyStatusEnteredField;
import com.constellio.app.modules.rm.ui.components.folder.fields.FolderParentFolderField;
import com.constellio.app.modules.rm.ui.components.folder.fields.FolderRetentionRuleField;
import com.constellio.app.modules.rm.ui.components.folder.fields.FolderUniformSubdivisionField;
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

	@Mock FolderForm folderForm;
	@Mock FolderCategoryField categoryField;
	@Mock FolderRetentionRuleField retentionRuleField;
	@Mock FolderCopyRuleField copyRuleField;
	@Mock FolderCopyStatusEnteredField copyStatusField;
	@Mock FolderAdministrativeUnitField administrativeUnitField;
	@Mock FolderParentFolderField parentFolderField;
	@Mock FolderUniformSubdivisionField uniformSubdivisionField;

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

		doReturn(folderForm).when(view).getForm();

		configure(categoryField, retentionRuleField, copyRuleField, copyStatusField, administrativeUnitField, parentFolderField,
				uniformSubdivisionField);
		doReturn(categoryField).when(folderForm).getCustomField(endsWith(CATEGORY_ENTERED));
		doReturn(retentionRuleField).when(folderForm).getCustomField(endsWith(RETENTION_RULE_ENTERED));
		doReturn(copyRuleField).when(folderForm).getCustomField(endsWith(MAIN_COPY_RULE_ID_ENTERED));
		doReturn(copyStatusField).when(folderForm).getCustomField(endsWith(COPY_STATUS_ENTERED));
		doReturn(administrativeUnitField).when(folderForm).getCustomField(endsWith(ADMINISTRATIVE_UNIT_ENTERED));
		doReturn(parentFolderField).when(folderForm).getCustomField(endsWith(PARENT_FOLDER));
		doReturn(uniformSubdivisionField).when(folderForm).getCustomField(endsWith(UNIFORM_SUBDIVISION_ENTERED));
	}

	private void configure(CustomFolderField... fields) {
		for (CustomFolderField field : fields) {

			final AtomicBoolean visible = new AtomicBoolean();
			doAnswer(setBoolean(visible)).when(field).setVisible(anyBoolean());
			doAnswer(getBoolean(visible)).when(field).isVisible();
		}
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
		assertThat(presenter.getFolder().get(PARENT_FOLDER)).isEqualTo("A16");

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

	@Test
	public void whenCreateNewFolderThenValidFieldVisibility()
			throws Exception {

		presenter.forParams("");

		assertThat(administrativeUnitField.isVisible()).isTrue();
		assertThat(categoryField.isVisible()).isTrue();
		assertThat(retentionRuleField.isVisible()).isFalse();
		assertThat(parentFolderField.isVisible()).isTrue();
		assertThat(copyRuleField.isVisible()).isFalse();
		assertThat(copyStatusField.isVisible()).isFalse();
		assertThat(uniformSubdivisionField.isVisible()).isFalse();

	}

	@Test
	public void givenCustomisableCopyTypeWhenCreateNewFolderThenFieldInvisible()
			throws Exception {

		givenConfig(RMConfigs.COPY_RULE_TYPE_ALWAYS_MODIFIABLE, true);

		presenter.forParams("");

		assertThat(administrativeUnitField.isVisible()).isTrue();
		assertThat(categoryField.isVisible()).isTrue();
		assertThat(retentionRuleField.isVisible()).isFalse();
		assertThat(parentFolderField.isVisible()).isTrue();
		assertThat(copyRuleField.isVisible()).isFalse();
		assertThat(copyStatusField.isVisible()).isFalse();
		assertThat(uniformSubdivisionField.isVisible()).isFalse();

	}

	@Test
	public void whenCreateNewFolderWithParentFolderThenValidFieldVisibility()
			throws Exception {

		presenter.forParams("parentId=A15");

		assertThat(administrativeUnitField.isVisible()).isFalse();
		assertThat(categoryField.isVisible()).isFalse();
		assertThat(retentionRuleField.isVisible()).isFalse();
		assertThat(parentFolderField.isVisible()).isTrue();
		assertThat(copyRuleField.isVisible()).isFalse();
		assertThat(copyStatusField.isVisible()).isFalse();
		assertThat(uniformSubdivisionField.isVisible()).isFalse();

	}

	@Test
	public void givenRootFolderWithEveryFieldsDeducedWhenModifyThenValidFieldVisibility()
			throws Exception {

		//TODO
		presenter.forParams("");

		assertThat(administrativeUnitField.isVisible()).isTrue();
		assertThat(categoryField.isVisible()).isTrue();
		assertThat(retentionRuleField.isVisible()).isFalse();
		assertThat(parentFolderField.isVisible()).isTrue();
		assertThat(copyRuleField.isVisible()).isFalse();
		assertThat(copyStatusField.isVisible()).isFalse();
		assertThat(uniformSubdivisionField.isVisible()).isFalse();

	}

	@Test
	public void givenCopyAlwaysCustomisableAndRootFolderWhenModifyThenValidFieldVisibility()
			throws Exception {

		givenConfig(RMConfigs.COPY_RULE_TYPE_ALWAYS_MODIFIABLE, true);

		//TODO
		presenter.forParams("");

		assertThat(administrativeUnitField.isVisible()).isTrue();
		assertThat(categoryField.isVisible()).isTrue();
		assertThat(retentionRuleField.isVisible()).isFalse();
		assertThat(parentFolderField.isVisible()).isTrue();
		assertThat(copyRuleField.isVisible()).isTrue();
		assertThat(copyStatusField.isVisible()).isTrue();
		assertThat(uniformSubdivisionField.isVisible()).isFalse();

	}

	@Test
	public void givenRootFolderWithoutDeducableFieldsWhenModifyThenValidFieldVisibility()
			throws Exception {

		//TODO
		presenter.forParams("");

		assertThat(administrativeUnitField.isVisible()).isTrue();
		assertThat(categoryField.isVisible()).isTrue();
		assertThat(retentionRuleField.isVisible()).isTrue();
		assertThat(parentFolderField.isVisible()).isTrue();
		assertThat(copyRuleField.isVisible()).isTrue();
		assertThat(copyStatusField.isVisible()).isTrue();
		assertThat(uniformSubdivisionField.isVisible()).isFalse();

	}

	@Test
	public void givenRootFolderWithUndeducableCopyTypeWhenModifyThenCopyVisible()
			throws Exception {

		//TODO
		presenter.forParams("");

		assertThat(administrativeUnitField.isVisible()).isTrue();
		assertThat(categoryField.isVisible()).isTrue();
		assertThat(retentionRuleField.isVisible()).isFalse();
		assertThat(parentFolderField.isVisible()).isTrue();
		assertThat(copyRuleField.isVisible()).isTrue();
		assertThat(copyStatusField.isVisible()).isFalse();
		assertThat(uniformSubdivisionField.isVisible()).isFalse();

	}

	@Test
	public void givenRootFolderWithUndeducableCopyStatusWhenModifyThenCopyVisible()
			throws Exception {

		//TODO
		presenter.forParams("");

		assertThat(administrativeUnitField.isVisible()).isTrue();
		assertThat(categoryField.isVisible()).isTrue();
		assertThat(retentionRuleField.isVisible()).isFalse();
		assertThat(parentFolderField.isVisible()).isTrue();
		assertThat(copyRuleField.isVisible()).isFalse();
		assertThat(copyStatusField.isVisible()).isTrue();
		assertThat(uniformSubdivisionField.isVisible()).isFalse();

	}

	@Test
	public void givenRootFolderWithUndeducableRuleWhenModifyThenRuleVisible()
			throws Exception {

		//TODO
		presenter.forParams("");

		assertThat(administrativeUnitField.isVisible()).isTrue();
		assertThat(categoryField.isVisible()).isTrue();
		assertThat(retentionRuleField.isVisible()).isTrue();
		assertThat(parentFolderField.isVisible()).isTrue();
		assertThat(copyRuleField.isVisible()).isFalse();
		assertThat(copyStatusField.isVisible()).isFalse();
		assertThat(uniformSubdivisionField.isVisible()).isFalse();

	}

	@Test
	public void whenModifySubFolderThenValidFieldVisibility()
			throws Exception {

		//TODO
		presenter.forParams("");

		assertThat(administrativeUnitField.isVisible()).isFalse();
		assertThat(categoryField.isVisible()).isFalse();
		assertThat(retentionRuleField.isVisible()).isFalse();
		assertThat(parentFolderField.isVisible()).isTrue();
		assertThat(copyRuleField.isVisible()).isFalse();
		assertThat(copyStatusField.isVisible()).isFalse();
		assertThat(uniformSubdivisionField.isVisible()).isFalse();

	}

	@Test
	public void givenCustomisableCopyTypeWhenModifySubFolderThenCopyTypeStillInvisible()
			throws Exception {

		givenConfig(RMConfigs.COPY_RULE_TYPE_ALWAYS_MODIFIABLE, true);

		//TODO
		presenter.forParams("");

		assertThat(administrativeUnitField.isVisible()).isFalse();
		assertThat(categoryField.isVisible()).isFalse();
		assertThat(retentionRuleField.isVisible()).isFalse();
		assertThat(parentFolderField.isVisible()).isTrue();
		assertThat(copyRuleField.isVisible()).isFalse();
		assertThat(copyStatusField.isVisible()).isFalse();
		assertThat(uniformSubdivisionField.isVisible()).isFalse();

	}

	@Test
	public void givenEnableUniformSubdivisionWhenCreateRootFolderThenFieldVisible()
			throws Exception {
		givenUniformSubdivisionEnabled();

		//TODO
		presenter.forParams("");

		assertThat(administrativeUnitField.isVisible()).isTrue();
		assertThat(categoryField.isVisible()).isTrue();
		assertThat(retentionRuleField.isVisible()).isFalse();
		assertThat(parentFolderField.isVisible()).isTrue();
		assertThat(copyRuleField.isVisible()).isFalse();
		assertThat(copyStatusField.isVisible()).isFalse();
		assertThat(uniformSubdivisionField.isVisible()).isTrue();

	}

	@Test
	public void givenEnableUniformSubdivisionWhenCreateSubFolderThenFieldVisible()
			throws Exception {
		givenUniformSubdivisionEnabled();

		//TODO
		presenter.forParams("");

		assertThat(administrativeUnitField.isVisible()).isFalse();
		assertThat(categoryField.isVisible()).isFalse();
		assertThat(retentionRuleField.isVisible()).isFalse();
		assertThat(parentFolderField.isVisible()).isTrue();
		assertThat(copyRuleField.isVisible()).isFalse();
		assertThat(copyStatusField.isVisible()).isFalse();
		assertThat(uniformSubdivisionField.isVisible()).isTrue();

	}

	@Test
	public void givenEnableUniformSubdivisionWhenModifyRootFolderWithDeducedCopyAndRuleThenFieldVisible()
			throws Exception {
		givenUniformSubdivisionEnabled();

		//TODO
		presenter.forParams("");

		assertThat(administrativeUnitField.isVisible()).isTrue();
		assertThat(categoryField.isVisible()).isTrue();
		assertThat(retentionRuleField.isVisible()).isFalse();
		assertThat(parentFolderField.isVisible()).isTrue();
		assertThat(copyRuleField.isVisible()).isFalse();
		assertThat(copyStatusField.isVisible()).isFalse();
		assertThat(uniformSubdivisionField.isVisible()).isTrue();

	}

	@Test
	public void givenEnableUniformSubdivisionWhenModifySubFolderThenFieldVisible()
			throws Exception {
		givenUniformSubdivisionEnabled();

		//TODO
		presenter.forParams("");

		assertThat(administrativeUnitField.isVisible()).isFalse();
		assertThat(categoryField.isVisible()).isFalse();
		assertThat(retentionRuleField.isVisible()).isFalse();
		assertThat(parentFolderField.isVisible()).isTrue();
		assertThat(copyRuleField.isVisible()).isFalse();
		assertThat(copyStatusField.isVisible()).isFalse();
		assertThat(uniformSubdivisionField.isVisible()).isTrue();

	}

	private void givenUniformSubdivisionEnabled() {

	}
}
