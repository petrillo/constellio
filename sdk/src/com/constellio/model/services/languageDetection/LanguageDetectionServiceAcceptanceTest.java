package com.constellio.model.services.languageDetection;

import com.constellio.data.dao.services.records.RecordDao;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.SearchServiceAcceptanceTestSchemas;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.entities.SearchBoost;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsSearchable;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;

/**
 * These tests check documents are indexed with a correct language id and when according the user preference the
 * search results are sorted. The user preference works as a boosting factor, not as a filter. Therefore, documents
 * will not be filtered according to the user preference and only their order may be affected.
 */
public class LanguageDetectionServiceAcceptanceTest extends ConstellioTest {
	RecordServices recordServices;
	SearchServices searchServices;
	RecordDao recordDao;

	SearchServiceAcceptanceTestSchemas schema = new SearchServiceAcceptanceTestSchemas(zeCollection);
	SearchServiceAcceptanceTestSchemas.ZeSchemaMetadatas zeSchema = schema.new ZeSchemaMetadatas();

	Transaction transaction;

	@Before
	public void setup() throws Exception {
		prepareSystem();
		givenSpecialCollection(zeCollection, Arrays.asList(Language.French.getCode(), Language.English.getCode()));

		recordServices = getModelLayerFactory().newRecordServices();
		recordDao = spy(getDataLayerFactory().newRecordDao());
		searchServices = new SearchServices(recordDao, getModelLayerFactory());

		transaction = new Transaction();
		defineSchemasManager().using(schema.withAStringMetadata(whichIsSearchable));
	}

	private List<Record> indexADocument(String... contents) throws RecordServicesException {
		List<Record> documents = new ArrayList<>();
		for (String content: contents) {
			Record document = recordServices.newRecordWithSchema(zeSchema.instance());
			transaction.addUpdate(document.set(zeSchema.stringMetadata(), content));
			documents.add(document);
		}
		recordServices.execute(transaction);
		return documents;
	}

	private List<Record> search(String strQuery, String... langsPreferences){
		LogicalSearchCondition condition = from(zeSchema.instance()).returnAll();
		LogicalSearchQuery query = new LogicalSearchQuery(condition);

		query.setFreeTextQuery(strQuery);
		//TODO: add a method to the LogicalSearchCondition class to support languages.
		List<SearchBoost> boosts = new ArrayList<>();
		double score = 1;
		for (int i = langsPreferences.length - 1; i >= 0; i--) {
			boosts.add(new SearchBoost("type", "search_txt_" + langsPreferences[i], "label", score));
			score *= 10;	//I am not sure if 10 is a proper number here. Maybe lower or higher number is better,
			//For now, I have no clue how we can set this number.
		}
		boosts.addAll(query.getFieldBoosts());
		query.setFieldBoosts(boosts);
		return searchServices.search(query);
	}

	@Test
	public void givenABiLingualCollectionWhenIndexingADocumentTheLanguageOfDocumentIsDetected(){
		//TODO
	}

	@Test
	public void givenATermInFrenchWordWhenSearchingTheTermThenOnlyTheStemmedVersionOfTermIsMatchedWithFrenchDocuments() throws RecordServicesException {
		String query = "allemandr";	//french stemmer will remove the final 'r'
		String enContent = "Gia Allemand is a model";
		String frContent = "Parlez-vous allemand";
		// user preference = fr, en
		//expected output = doc2.

		List<Record> docs = indexADocument(enContent, frContent);
		Record frDoc = docs.get(1);
		List<Record> results = search(query, "fr", "en");
		assertThat(results).containsOnly(frDoc);

		// user preference = en, fr
		results = search(query, "en", "fr");
		assertThat(results).containsOnly(frDoc);
		//expected output = doc2

	}

	@Test
	public void givenATermCommonInBothEnglishAndFrenchWhenTheUserPreferenceIsEnglishThenEnglishDocumentsAreBoosted() throws RecordServicesException {
		String query = "allemand";
		String enContent = "Gia Allemand is a model";
		String frContent = "Parlez-vous allemand";
		// user preference = fr, en
		//expected output = doc2, doc1

		List<Record> docs = indexADocument(frContent, enContent);
		List<Record> results = search(query, "fr", "en");
		assertThat(results).isEqualTo(docs);
	}

	@Test
	public void givenATermCommonInBothEnglishAndFrenchWhenTheUserPreferenceIsFrenchThenFrenchDocumentsAreBoosted() throws RecordServicesException {
		String query = "allemand";
		String enContent = "Gia Allemand is a model";
		String frContent = "Parlez-vous allemand";
		// user preference = fr, en
		//expected output = doc2, doc1

		List<Record> docs = indexADocument(frContent, enContent);
		List<Record> results = search(query, "en", "fr");
		Collections.reverse(docs);
		assertThat(results).isEqualTo(docs);
	}


	//Elaborate this test
	public void givenABiLingualDocumentWhenSearchingATermThenDetectThenThereWillBeAMatch(){

	}

	@Test
	public void givenAPhraseQueryWhenSearchingThenTheExactMatchedDocumentsAreReturned() throws RecordServicesException {
		String query = "\"the big Apple\"";
		String expectedDocContent = "The Big Apple was first popularized as a reference to New York City by John J. Fitz Gerald";
		String doc1 = "I like big company like the apple company.";
		String doc2 = "Please give me only big apples :)";

		//expected output = doc2;
		List<Record> docs = indexADocument(expectedDocContent, doc1, doc2);
		Record expectedDoc = docs.get(0);
		List<Record> results = search(query);
		assertThat(results).containsOnly(expectedDoc);
	}

}
