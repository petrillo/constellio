package com.constellio.app.modules.rm.configScripts;

import com.constellio.app.modules.rm.model.enums.CalculatorWithManualMetadataChoice;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.configs.AbstractSystemConfigurationScript;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class EnableOrDisableCalculatorsManualMetadataScript extends
															AbstractSystemConfigurationScript<CalculatorWithManualMetadataChoice> {

	@Override
	public void onValueChanged(CalculatorWithManualMetadataChoice previousValue, CalculatorWithManualMetadataChoice newValue,
			ModelLayerFactory modelLayerFactory) {
		if (newValue == null) {
			newValue = CalculatorWithManualMetadataChoice.DISABLE;
		}
		if (previousValue == null) {
			previousValue = CalculatorWithManualMetadataChoice.DISABLE;
		}
		if (newValue != previousValue) {
			CollectionsListManager collectionManager = modelLayerFactory
					.getCollectionsListManager();
			for (String collection : collectionManager.getCollectionsExcludingSystem()) {
				onValueChangedForCollection(newValue, modelLayerFactory, collection);
			}
		}
	}

	private void onValueChangedForCollection(CalculatorWithManualMetadataChoice newValue, ModelLayerFactory modelLayerFactory,
			String collection) {
		final boolean disableManualMetadataInCalculators = (newValue != CalculatorWithManualMetadataChoice.DISABLE);
		modelLayerFactory
				.getMetadataSchemasManager()
				.modify(collection, new MetadataSchemaTypesAlteration() {
					@Override
					public void alter(MetadataSchemaTypesBuilder types) {
						MetadataSchemaBuilder defaultFolderSchema = types
								.getDefaultSchema(Folder.SCHEMA_TYPE);
						defaultFolderSchema
								.getMetadata(Folder.MANUAL_ARCHIVISTIC_STATUS).setEnabled(disableManualMetadataInCalculators);
						defaultFolderSchema
								.getMetadata(Folder.MANUAL_EXPECTED_DEPOSIT_DATE).setEnabled(disableManualMetadataInCalculators);
						defaultFolderSchema
								.getMetadata(Folder.MANUAL_EXPECTED_DESTRIUCTION_DATE).setEnabled(disableManualMetadataInCalculators);
						defaultFolderSchema
								.getMetadata(Folder.MANUAL_EXPECTED_TRANSFER_DATE).setEnabled(disableManualMetadataInCalculators);
					}
				});
	}
}