package com.constellio.app.modules.robots;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.constellio.app.entities.modules.InstallableModule;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.entities.navigation.NavigationConfig;
import com.constellio.app.modules.robots.constants.RobotsPermissionsTo;
import com.constellio.app.modules.robots.migrations.RobotsMigrationTo5_1_2;
import com.constellio.app.modules.robots.migrations.RobotsMigrationTo5_1_3;
import com.constellio.app.modules.robots.model.actions.RunExtractorsActionExecutor;
import com.constellio.app.modules.robots.services.RobotSchemaRecordServices;
import com.constellio.app.modules.robots.services.RobotsManager;
import com.constellio.app.modules.robots.ui.navigation.RobotsNavigationConfiguration;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.configs.SystemConfiguration;

public class ConstellioRobotsModule implements InstallableModule {
	public static final String ID = "robots";
	public static final String NAME = "Constellio Robots";

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public String getPublisher() {
		return "Constellio";
	}

	@Override
	public List<MigrationScript> getMigrationScripts() {
		return Arrays.asList(
				new RobotsMigrationTo5_1_2(),
				new RobotsMigrationTo5_1_3()
		);
	}

	@Override
	public void configureNavigation(NavigationConfig config) {
		new RobotsNavigationConfiguration().configureNavigation(config);
	}

	@Override
	public void start(String collection, AppLayerFactory appLayerFactory) {
		registerManagers(collection, appLayerFactory);
		RobotSchemaRecordServices robotSchemas = new RobotSchemaRecordServices(collection, appLayerFactory);
		RobotsManager robotsManager = robotSchemas.getRobotsManager();

		RunExtractorsActionExecutor.registerIn(robotsManager);
	}


	@Override
	public void stop(String collection, AppLayerFactory appLayerFactory) {

	}

	@Override
	public void addDemoData(String collection, AppLayerFactory appLayerFactory) {

	}

	private void registerManagers(String collection, AppLayerFactory appLayerFactory) {
		RobotSchemaRecordServices robotSchemas = new RobotSchemaRecordServices(collection, appLayerFactory);
		appLayerFactory.registerManager(collection, ConstellioRobotsModule.ID, RobotsManager.ID, new RobotsManager(robotSchemas));
	}

	@Override
	public boolean isComplementary() {
		return false;
	}

	@Override
	public List<String> getDependencies() {
		return new ArrayList<>();
	}

	@Override
	public List<SystemConfiguration> getConfigurations() {
		return new ArrayList<>();
	}

	@Override
	public Map<String, List<String>> getPermissions() {
		return RobotsPermissionsTo.PERMISSIONS.getGrouped();
	}

	@Override
	public List<String> getRolesForCreator() {
		return new ArrayList<>();
	}
}