/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.ui.pages.rm.folder;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;
import org.openqa.selenium.By;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.tools.ButtonWebElement;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.SlowTest;
import com.constellio.sdk.tests.annotations.UiTest;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebElement;

@UiTest
public class BruteForceFolderSecurityAcceptTest extends ConstellioTest {

	SearchServices searchServices;
	RMSchemasRecordsServices rm;
	RMTestRecords records;
	RecordServices recordServices;
	ConstellioWebDriver driver;

	private String[] folderButtons = new String[] { "Ajouter un document", "Ajouter un sous-dossier", "Modifier la fiche dossier",
			"Partager ce dossier" };

	private String[] documentButtons = new String[] { "Éditer la fiche du document", "Partager ce document" };

	private void prepare()
			throws Exception {

		givenCollectionWithTitle(zeCollection, "Collection de test").withConstellioRMModule().withAllTestUsers();

		recordServices = getModelLayerFactory().newRecordServices();

		records = new RMTestRecords(zeCollection).setup(getModelLayerFactory()).withFoldersAndContainersOfEveryStatus()
				.withEvents();

		searchServices = getModelLayerFactory().newSearchServices();

		rm = new RMSchemasRecordsServices(zeCollection, getModelLayerFactory());
	}

	private boolean isEnabled() {
		String parameterValue = getCurrentTestSession().getProperty("bruteForceRMSecurityTest");
		return parameterValue != null && "true".equals(parameterValue.toLowerCase().trim());
	}

	@Test
	@SlowTest
	public void validateUserCanClickOnAllEnabledButtonsOfEveryFolders()
			throws Exception {
		if (!isEnabled()) {
			return;
		}
		prepare();
		LogicalSearchQuery allFoldersQuery = new LogicalSearchQuery()
				.setCondition(from(rm.folderSchemaType()).returnAll());

		List<String> folders = searchServices.searchRecordIds(allFoldersQuery);
		List<User> users = getModelLayerFactory().newUserServices().getAllUsersInCollection(zeCollection);

		int progression = 0;
		int total = folders.size() * users.size();

		for (User user : users) {

			logAs(user.getUsername());
			LogicalSearchQuery allFoldersWithReadQuery = new LogicalSearchQuery()
					.setCondition(from(rm.folderSchemaType()).returnAll())
					.filteredWithUser(user);
			List<String> foldersWithReadAccess = searchServices.searchRecordIds(allFoldersWithReadQuery);

			for (String folderId : folders) {

				System.out.println("" + (++progression) + " / " + total);
				driver.navigateTo().url(NavigatorConfigurationService.DISPLAY_FOLDER + "/" + folderId);
				if (foldersWithReadAccess.contains(folderId)) {
					assertThat(isOnHomePage()).describedAs("User " + user.getUsername()
							+ " should be able to view folder '" + folderId + "'")
							.isFalse();

					for (String folderButton : folderButtons) {
						ButtonWebElement button = getButtonIfEnabled(folderButton);
						if (button != null) {
							if (!clickValidateNotHomePageAndReturn(button)) {
								fail("User " + user.getUsername() + " can click on the button '" + folderButton + "' on folder '"
										+ folderId + "', but has no access to the page");
							}
						}
					}

				} else {
					assertThat(isOnHomePage()).describedAs("User " + user.getUsername()
							+ " should not be able to view folder '" + folderId + "'")
							.isTrue();
				}
			}
		}
	}

	@Test
	@SlowTest
	public void validateUserCanClickOnAllEnabledButtonsOfEveryDocuments()
			throws Exception {
		if (!isEnabled()) {
			return;
		}
		prepare();
		LogicalSearchQuery allDocumentsQuery = new LogicalSearchQuery()
				.setCondition(from(rm.documentSchemaType()).returnAll());

		List<String> documents = searchServices.searchRecordIds(allDocumentsQuery);
		List<User> users = getModelLayerFactory().newUserServices().getAllUsersInCollection(zeCollection);

		int progression = 0;
		int total = documents.size() * users.size();

		for (User user : users) {

			logAs(user.getUsername());
			LogicalSearchQuery allDocumentsWithReadQuery = new LogicalSearchQuery()
					.setCondition(from(rm.documentSchemaType()).returnAll())
					.filteredWithUser(user);
			List<String> documentsWithReadAccess = searchServices.searchRecordIds(allDocumentsWithReadQuery);

			for (String documentId : documents) {

				System.out.println("" + (++progression) + " / " + total);
				driver.navigateTo().url(NavigatorConfigurationService.DISPLAY_DOCUMENT + "/" + documentId);
				if (documentsWithReadAccess.contains(documentId)) {
					assertThat(isOnHomePage()).describedAs("User " + user.getUsername()
							+ " should be able to view document '" + documentId + "'")
							.isFalse();

					for (String folderButton : documentButtons) {
						ButtonWebElement button = getButtonIfEnabled(folderButton);
						if (button != null) {
							if (!clickValidateNotHomePageAndReturn(button)) {
								fail("User " + user.getUsername() + " can click on the button '" + folderButton
										+ "' on document '"
										+ documentId + "', but has no access to the page");
							}
						}
					}

				} else {
					assertThat(isOnHomePage()).describedAs("User " + user.getUsername()
							+ " should not be able to view document '" + documentId + "'")
							.isTrue();
				}
			}
		}
	}

	private boolean clickValidateNotHomePageAndReturn(ButtonWebElement button) {
		String currentUrl = driver.getCurrentPage();
		button.clickAndWaitForPageReload();
		boolean homePage = isOnHomePage();
		driver.navigateTo().url(currentUrl);
		return !homePage;
	}

	private boolean isOnHomePage() {
		return driver.getCurrentUrl().endsWith("/constellio/#!/lastViewedFolders");
	}

	private void logAs(String user) {
		driver = newWebDriver(loggedAsUserInCollection(user, zeCollection));
	}

	private ButtonWebElement getButtonIfEnabled(String buttonName) {
		List<ConstellioWebElement> listButtonActionMenu = driver.findAdaptElements(By.className("action-menu-button"));
		for (ConstellioWebElement buttonElement : listButtonActionMenu) {
			if (buttonElement.getText().contains(buttonName)) {
				ButtonWebElement currentButton = new ButtonWebElement(buttonElement);
				if (currentButton.isEnabled()) {
					return currentButton;
				} else {
					return null;
				}
			}
		}
		return null;
	}

}