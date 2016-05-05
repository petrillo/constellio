package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.pages.search.criteria.CriterionFactory;
import com.constellio.model.entities.records.wrappers.SavedSearch;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

import static com.constellio.model.entities.schemas.MetadataValueType.BOOLEAN;
import static com.constellio.model.entities.schemas.MetadataValueType.STRUCTURE;

/**
 * Created by Majid on 2016-05-05.
 */
public class CoreMigrationTo_6_4 implements MigrationScript {
    @Override
    public String getVersion() {
        return "6.4";
    }

    @Override
    public void migrate(String collection, MigrationResourcesProvider provider, AppLayerFactory appLayerFactory) throws Exception {
        new CoreSchemaAlterationFor6_4(collection, provider, appLayerFactory).migrate();
    }

    private class CoreSchemaAlterationFor6_4 extends MetadataSchemasAlterationHelper {

        protected CoreSchemaAlterationFor6_4(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory) {
            super(collection, migrationResourcesProvider, appLayerFactory);
        }

        @Override
        protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
            MetadataSchemaTypeBuilder type = typesBuilder.getSchemaType(SavedSearch.SCHEMA_TYPE);
            MetadataSchemaBuilder defaultSchema = type.getDefaultSchema();
            defaultSchema.createUndeletable(SavedSearch.RETURN_SIMILAR_DOCS).setType(BOOLEAN);
            defaultSchema.createUndeletable(SavedSearch.SIMILARITY_SEARCH).setType(STRUCTURE)
                    .defineStructureFactory(CriterionFactory.class).setMultivalue(true);

        }
    }
}
