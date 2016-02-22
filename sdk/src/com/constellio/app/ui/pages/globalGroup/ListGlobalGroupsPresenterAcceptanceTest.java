package com.constellio.app.ui.pages.globalGroup;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.model.entities.security.global.GlobalGroup;
import com.constellio.model.entities.security.global.GlobalGroupStatus;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.entities.security.global.UserCredentialStatus;
import com.constellio.model.entities.security.global.XmlUserCredential;
import com.constellio.model.services.users.GlobalGroupsManager;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.InDevelopmentTest;
import com.constellio.sdk.tests.annotations.UiTest;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;

@UiTest
@InDevelopmentTest
public class ListGlobalGroupsPresenterAcceptanceTest extends ConstellioTest {

	ConstellioWebDriver driver;
	UserServices userServices;
	GlobalGroupsManager globalGroupsManager;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers()
		);

		userServices = getModelLayerFactory().newUserServices();
		globalGroupsManager = getModelLayerFactory().getGlobalGroupsManager();

		createGroup("Legends", "Legends group");
		createGroup("Heroes", "Heroes group");
		createGroup("BGroup", "B group");
		createGroup("AGroup", "A group");
		createGroup("DGroup", "D group");
		createGroup("CGroup", "C group");
		createGroup("FGroup", "F group");
		createGroup("EGroup", "E group");

		createUser();

		driver = newWebDriver(loggedAsUserInCollection("admin", zeCollection));
	}

	@Test
	@Ignore
	public void whenNavigateToUserCredentialsListThenOk()
			throws Exception {

		driver.navigateTo().url(NavigatorConfigurationService.USER_LIST);
		waitUntilICloseTheBrowsers();
	}

	//

	private void createUser() {
		UserCredential userCredential = new XmlUserCredential("dakota", "Dakota", "Indien", "dakota@gmail.com",
				Arrays.asList("AGroup", "BGroup"),
				Arrays.asList(zeCollection), UserCredentialStatus.ACTIVE, null, Arrays.asList(""), null);
		userServices.addUpdateUserCredential(userCredential);
	}

	private void createGroup(String code, String name) {
		GlobalGroup globalGroup = new GlobalGroup(code, name, new ArrayList<String>(), null, GlobalGroupStatus.ACTIVE);
		userServices.addUpdateGlobalGroup(globalGroup);
	}
}
