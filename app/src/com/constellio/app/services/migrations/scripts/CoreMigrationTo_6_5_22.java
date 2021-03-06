package com.constellio.app.services.migrations.scripts;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.entities.schemas.MetadataValueType.BOOLEAN;
import static com.constellio.model.entities.schemas.MetadataValueType.CONTENT;
import static com.constellio.model.entities.schemas.MetadataValueType.DATE_TIME;
import static com.constellio.model.entities.schemas.MetadataValueType.REFERENCE;
import static com.constellio.model.entities.schemas.MetadataValueType.STRING;

import com.constellio.app.entities.schemasDisplay.MetadataDisplayConfig;
import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.records.calculators.UserTitleCalculator;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.security.global.GlobalGroupStatus;
import com.constellio.model.entities.security.global.SolrGlobalGroup;
import com.constellio.model.entities.security.global.SolrUserCredential;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;

import org.apache.commons.lang3.StringUtils;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.wrappers.ApprovalTask;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.records.wrappers.UserDocument;
import com.constellio.model.entities.records.wrappers.WorkflowTask;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.security.global.UserCredentialStatus;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.builders.CommonMetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.schemas.calculators.AllUserAuthorizationsCalculator;
import com.constellio.model.services.schemas.calculators.RolesCalculator;
import com.constellio.model.services.schemas.calculators.UserTokensCalculator2;
import com.constellio.model.services.schemas.validators.DecisionValidator;
import com.constellio.model.services.schemas.validators.EmailValidator;

import java.util.HashMap;
import java.util.Map;

public class CoreMigrationTo_6_5_22 implements MigrationScript {

	@Override
	public String getVersion() {
		return "6.5.22";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
			AppLayerFactory appLayerFactory) {
		new CoreSchemaAlterationFor6_5_22(collection, migrationResourcesProvider, appLayerFactory).migrate();

		SchemasDisplayManager displayManager = appLayerFactory.getMetadataSchemasDisplayManager();

		MetadataDisplayConfig displayConfig;

		displayConfig = displayManager.getMetadata(collection, User.DEFAULT_SCHEMA + "_" + User.PERSONAL_EMAILS)
				.withInputType(MetadataInputType.TEXTAREA);

		displayManager.saveMetadata(displayConfig);
	}

	private class CoreSchemaAlterationFor6_5_22 extends MetadataSchemasAlterationHelper {
		public CoreSchemaAlterationFor6_5_22(String collection, MigrationResourcesProvider migrationResourcesProvider,
				AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder builder) {

			builder.getDefaultSchema(User.SCHEMA_TYPE)
					.create(User.PERSONAL_EMAILS).
					setType(MetadataValueType.STRING).
					setMultivalue(true).
					setEnabled(true).
					setEssential(false);
		}

	}
}
