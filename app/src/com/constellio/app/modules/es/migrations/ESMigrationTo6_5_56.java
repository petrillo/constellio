package com.constellio.app.modules.es.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbInstance;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemaTypesDisplayTransactionBuilder;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

import static com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbInstance.FORCE_SYNC_TREE;

public class ESMigrationTo6_5_56 extends MigrationHelper implements MigrationScript {

	MigrationResourcesProvider migrationResourcesProvider;

	@Override
	public String getVersion() {
		return "6.5.56";
	}
	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory)
			throws Exception {
		this.migrationResourcesProvider = migrationResourcesProvider;

		new SchemaAlterationFor6_5_56(collection, migrationResourcesProvider, appLayerFactory).migrate();
		updateFormAndDisplay(collection, appLayerFactory);
	}

	private void updateFormAndDisplay(String collection, AppLayerFactory appLayerFactory) {
		SchemasDisplayManager manager = appLayerFactory.getMetadataSchemasDisplayManager();

		String advancedTab = "connectors.advanced";
		SchemaTypesDisplayTransactionBuilder transaction = manager.newTransactionBuilderFor(collection);
		transaction.add(manager.getSchema(collection, ConnectorSmbInstance.SCHEMA_CODE)
				.withNewFormMetadata(ConnectorSmbInstance.SCHEMA_CODE + "_" + FORCE_SYNC_TREE));
		transaction.add(manager.getMetadata(collection, ConnectorSmbInstance.SCHEMA_CODE, FORCE_SYNC_TREE)
				.withMetadataGroup(advancedTab));

		manager.execute(transaction.build());
	}

	static class SchemaAlterationFor6_5_56 extends MetadataSchemasAlterationHelper {
		MetadataSchemaTypes types;

		protected SchemaAlterationFor6_5_56(String collection, MigrationResourcesProvider migrationResourcesProvider,
				AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
			types = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collection);
		}

		public String getVersion() {
			return "6.5.56";
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder types) {
			MetadataSchemaBuilder smbConnectorSchemaType = types.getSchema(ConnectorSmbInstance.SCHEMA_CODE);
			smbConnectorSchemaType.create(FORCE_SYNC_TREE)
					.setType(MetadataValueType.BOOLEAN).setDefaultValue(false);
		}
	}
}
