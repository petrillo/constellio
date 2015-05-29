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
package com.constellio.model.services.schemas;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsSearchable;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;

import org.junit.Before;

import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.dao.services.DataStoreTypesFactory;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.ConditionTemplate;
import com.constellio.model.services.search.query.logical.valueCondition.ConditionTemplateFactory;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestRecord;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.AnotherSchemaMetadatas;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ZeCustomSchemaMetadatas;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ZeSchemaMetadatas;

public class SchemasModificationImpactsAcceptanceTest extends ConstellioTest {

	TaxonomiesManager taxonomiesManager;

	MetadataSchemasManager schemasManager;

	ConfigManager configManager;

	TestsSchemasSetup schemas = new TestsSchemasSetup();
	ZeSchemaMetadatas zeSchema = schemas.new ZeSchemaMetadatas();
	ZeCustomSchemaMetadatas zeCustomSchema = schemas.new ZeCustomSchemaMetadatas();
	AnotherSchemaMetadatas anotherSchema = schemas.new AnotherSchemaMetadatas();

	SearchServices searchServices;
	DataStoreTypesFactory typesFactory;
	CollectionsListManager collectionsListManager;
	RecordServices recordServices;

	@Before
	public void setUp()
			throws Exception {

		recordServices = getModelLayerFactory().newRecordServices();
		configManager = getDataLayerFactory().getConfigManager();
		schemasManager = getModelLayerFactory().getMetadataSchemasManager();
		taxonomiesManager = getModelLayerFactory().getTaxonomiesManager();
		searchServices = getModelLayerFactory().newSearchServices();
		typesFactory = getDataLayerFactory().newTypesFactory();
		collectionsListManager = getModelLayerFactory().getCollectionsListManager();

		givenCollection("zeCollection");
	}

	//@Test
	//TODO Disabled, because there is a problem with impacts handling causing a change of status to destroy all search indexes.
	public void whenModifyingTheAutocompleteFlagOfMetadatasThenRecordsReindexed()
			throws Exception {
		defineSchemasManager().using(schemas.withALargeTextMetadata());
		String record1 = givenZeSchemaRecordWithLargeText("Je vais prendre mes pommes pour emporter");
		String record2 = givenZeSchemaRecordWithLargeText("Un shish par jour éloigne le docteur pour toujours");

		assertThatAutoCompleteSearch("pomme").isEmpty();
		assertThatSimpleSearch("pomme").isEmpty();

		schemasManager.modify(zeCollection, changeLargeTextAutocompletStatus(true));
		waitForBatchProcess();

		assertThatAutoCompleteSearch("pomme").containsOnly(record1);
		assertThatSimpleSearch("pomme").isEmpty();

		schemasManager.modify(zeCollection, changeLargeTextAutocompletStatus(false));
		waitForBatchProcess();

		assertThatAutoCompleteSearch("pomme").isEmpty();
		assertThatSimpleSearch("pomme").isEmpty();
	}

	//@Test
	//TODO Disabled, because there is a problem with impacts handling causing a change of status to destroy all search indexes.
	public void whenModifyingTheSearchableFlagOfMetadatasThenRecordsReindexed()
			throws Exception {
		defineSchemasManager().using(schemas.withALargeTextMetadata());
		String record1 = givenZeSchemaRecordWithLargeText("Je vais prendre mes pommes pour emporter");
		String record2 = givenZeSchemaRecordWithLargeText("Un shish par jour éloigne le docteur pour toujours");

		assertThatAutoCompleteSearch("pommes").isEmpty();
		assertThatSimpleSearch("pommes").isEmpty();

		schemasManager.modify(zeCollection, changeLargeTextSearchableStatus(true));
		waitForBatchProcess();

		assertThatAutoCompleteSearch("pommes").isEmpty();
		assertThatSimpleSearch("pommes").containsOnly(record1);

		schemasManager.modify(zeCollection, changeLargeTextSearchableStatus(false));
		waitForBatchProcess();

		assertThatAutoCompleteSearch("pommes").isEmpty();
		assertThatSimpleSearch("pommes").isEmpty();
	}

	//@Test
	//TODO Disabled, because there is a problem with impacts handling causing a change of status to destroy all search indexes.
	public void whenModifyingTheMultivalueFlagOfMetadatasThenRecordsReindexed()
			throws Exception {
		defineSchemasManager().using(schemas.withAStringMetadata(whichIsSearchable).withANumberMetadata());
		String record1 = givenZeSchemaRecordWithStringAndNumber("Shish", 42.0);
		String record2 = givenZeSchemaRecordWithStringAndNumber("Pomme", null);
		assertThat(record(record1).get(zeSchemaStringMetadata())).isEqualTo("Shish");
		assertThat(record(record1).get(zeSchemaNumberMetadata())).isEqualTo(42.0);
		assertThat(record(record2).get(zeSchemaStringMetadata())).isEqualTo("Pomme");
		assertThat(record(record2).get(zeSchemaNumberMetadata())).isEqualTo(null);

		schemasManager.modify(zeCollection, changeStringAndNumberMultivalueStatus(true));
		waitForBatchProcess();
		assertThat(record(record1).get(zeSchemaStringMetadata())).isEqualTo(asList("Shish"));
		assertThat(record(record1).get(zeSchemaNumberMetadata())).isEqualTo(asList(42.0));
		assertThat(record(record2).get(zeSchemaStringMetadata())).isEqualTo(asList("Pomme"));
		assertThat(record(record2).get(zeSchemaNumberMetadata())).isEqualTo(new ArrayList<>());
		assertThatSimpleSearch("Shish").containsOnly(record1);

		recordServices.update(record(record1).set(zeSchemaStringMetadata(), asList("Shish", "inter"))
				.set(zeSchemaNumberMetadata(), asList(123.4, 42.0)));
		recordServices.update(record(record2).set(zeSchemaStringMetadata(), asList("Banane", "Shish"))
				.set(zeSchemaNumberMetadata(), new ArrayList<>()));

		assertThat(record(record1).get(zeSchemaStringMetadata())).isEqualTo(asList("Shish", "inter"));
		assertThat(record(record1).get(zeSchemaNumberMetadata())).isEqualTo(asList(123.4, 42.0));
		assertThat(record(record2).get(zeSchemaStringMetadata())).isEqualTo(asList("Banane", "Shish"));
		assertThat(record(record2).get(zeSchemaNumberMetadata())).isEqualTo(new ArrayList<>());
		assertThatSimpleSearch("Shish").containsOnly(record1, record2);

		schemasManager.modify(zeCollection, changeStringAndNumberMultivalueStatus(false));
		waitForBatchProcess();

		assertThat(record(record1).get(zeSchemaStringMetadata())).isEqualTo("Shish");
		assertThat(record(record1).getList(Schemas.dummyMultiValueMetadata(zeSchemaStringMetadata())))
				.isEqualTo(new ArrayList<>());
		assertThat(record(record1).get(zeSchemaNumberMetadata())).isEqualTo(123.4);
		assertThat(record(record2).get(zeSchemaStringMetadata())).isEqualTo("Banane");
		assertThat(record(record2).get(zeSchemaNumberMetadata())).isEqualTo(null);
		assertThatSimpleSearch("Shish").containsOnly(record1);

	}

	private Metadata zeSchemaStringMetadata() {
		return schemasManager.getSchemaTypes(zeCollection).getSchema(zeSchema.code())
				.getMetadata(zeSchema.stringMetadata().getLocalCode());
	}

	private Metadata zeSchemaNumberMetadata() {
		return schemasManager.getSchemaTypes(zeCollection).getSchema(zeSchema.code())
				.getMetadata(zeSchema.numberMetadata().getLocalCode());
	}

	private org.assertj.core.api.ListAssert<String> assertThatSimpleSearch(String text) {

		MetadataSchemaType type = schemasManager.getSchemaTypes(zeCollection).getSchemaType(zeSchema.typeCode());

		LogicalSearchQuery query = new LogicalSearchQuery()
				.setCondition(from(type).where(Schemas.FRENCH_SEARCH_FIELD).query(text));

		return assertThat(searchServices.searchRecordIds(query));
	}

	private org.assertj.core.api.ListAssert<String> assertThatAutoCompleteSearch(String text) {
		ConditionTemplate conditionTemplate = ConditionTemplateFactory.autocompleteFieldMatching(text);

		MetadataSchemaType type = schemasManager.getSchemaTypes(zeCollection).getSchemaType(zeSchema.typeCode());

		LogicalSearchQuery query = new LogicalSearchQuery()
				.setCondition(from(type).where(conditionTemplate));

		return assertThat(searchServices.searchRecordIds(query));
	}

	private MetadataSchemaTypesAlteration changeLargeTextAutocompletStatus(final boolean status) {
		return new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchema(zeSchema.code()).getMetadata(zeSchema.largeTextMetadata().getLocalCode())
						.setSchemaAutocomplete(status);
			}
		};
	}

	private MetadataSchemaTypesAlteration changeLargeTextSearchableStatus(final boolean status) {
		return new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchema(zeSchema.code()).getMetadata(zeSchema.largeTextMetadata().getLocalCode())
						.setSearchable(status);
			}
		};
	}

	private MetadataSchemaTypesAlteration changeStringAndNumberMultivalueStatus(final boolean status) {
		return new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchema(zeSchema.code()).getMetadata(zeSchema.stringMetadata().getLocalCode())
						.setMultivalue(status);

				types.getSchema(zeSchema.code()).getMetadata(zeSchema.numberMetadata().getLocalCode())
						.setMultivalue(status);
			}
		};
	}

	String givenZeSchemaRecordWithLargeText(String largeText) {
		Record record = new TestRecord(zeSchema);
		record.set(zeSchema.largeTextMetadata(), largeText);
		try {
			recordServices.add(record);
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}
		return record.getId();
	}

	String givenZeSchemaRecordWithStringAndNumber(Object text, Object number) {
		Record record = new TestRecord(zeSchema);
		record.set(zeSchema.stringMetadata(), text);
		record.set(zeSchema.numberMetadata(), number);
		try {
			recordServices.add(record);
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}
		return record.getId();
	}

}