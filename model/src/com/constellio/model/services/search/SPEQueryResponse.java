package com.constellio.model.services.search;

import com.constellio.data.dao.dto.records.FacetValue;
import com.constellio.model.entities.records.Record;

import java.util.*;

public class SPEQueryResponse {

	private final Map<String, List<FacetValue>> fieldFacetValues;
	private final Map<String, Map<String, Object>> statisticsValues;

	private final Map<String, Integer> queryFacetsValues;

	private final Map<String, Map<String, List<String>>> highlights;

	private final long qtime;

	private final long numFound;

	private final List<Record> records;
	private final List<Double> scores;

	private final Map<Record, Map<Record, Double>> recordsWithMoreLikeThis;

	private final boolean correctlySpelt;
	private final List<String> spellcheckerSuggestions;

	public SPEQueryResponse(List<Record> records, Map<Record, Map<Record, Double>> recordsWithMoreLikeThis) {
		this(new HashMap<String, List<FacetValue>>(), new HashMap<String, Map<String, Object>>(),
				new HashMap<String, Integer>(), -1, records.size(),
				records, new HashMap<String, Map<String, List<String>>>(), true, new ArrayList<String>(),
				recordsWithMoreLikeThis);
	}

	public SPEQueryResponse(
			Map<String, List<FacetValue>> fieldFacetValues, Map<String, Map<String, Object>> statisticsValues,
			Map<String, Integer> queryFacetsValues, long qtime,
			long numFound, List<Record> records, Map<String, Map<String, List<String>>> highlights, boolean correctlySpelt,
			List<String> spellcheckerSuggestions, Map<Record, Map<Record, Double>> recordsWithMoreLikeThis) {
		this.fieldFacetValues = fieldFacetValues;
		this.statisticsValues = statisticsValues;
		this.queryFacetsValues = queryFacetsValues;
		this.qtime = qtime;


		this.highlights = highlights;
		this.correctlySpelt = correctlySpelt;
		this.spellcheckerSuggestions = spellcheckerSuggestions;
		this.recordsWithMoreLikeThis = recordsWithMoreLikeThis;


		//FIXME this is temporary. what it does is that when the more like this is on, only the similar documents of
		//the first search results are stored in the records. This functionally is needed when similar documents of
		//a specific document is requested. By this, it seems the user searches for similar documents of a document
		if (records.size() == recordsWithMoreLikeThis.size() && recordsWithMoreLikeThis.size() != 0) {
			Map<Record, Double> firstItemSimilarDocuemtns =
					recordsWithMoreLikeThis.entrySet().iterator().next().getValue();
			this.records = new ArrayList<>();
			this.scores = new ArrayList<>(firstItemSimilarDocuemtns.size());
			for (Map.Entry<Record, Double> aResult: firstItemSimilarDocuemtns.entrySet()){
				this.records.add(aResult.getKey());
				this.scores.add(aResult.getValue());
			}
			this.numFound = this.records.size();
		}
		else {
			this.records = records;
			this.numFound = numFound;
			this.scores = new ArrayList<>(Collections.nCopies((int)this.numFound, 1.0)); //FIXME: update score (1.0) with the real one.

		}
	}

	public List<FacetValue> getFieldFacetValues(String metadata) {
		if (fieldFacetValues.containsKey(metadata)) {
			return fieldFacetValues.get(metadata);
		} else {
			return Collections.emptyList();
		}
	}

	public Map<String, Object> getStatValues(String metadata) {
		return this.statisticsValues.get(metadata);
	}

	public Integer getQueryFacetCount(String query) {
		return queryFacetsValues.get(query);
	}

	public List<String> getFieldFacetValuesWithResults(String field) {
		List<String> values = new ArrayList<>();

		for (FacetValue facetValue : getFieldFacetValues(field)) {
			if (facetValue.getQuantity() > 0) {
				values.add(facetValue.getValue());
			}
		}

		return values;
	}

	public List<Record> getRecords() {
		return records;
	}

	public long getQtime() {
		return qtime;
	}

	public long getNumFound() {
		return numFound;
	}

	public FacetValue getQueryFacetValue(String value) {
		int count = !queryFacetsValues.containsKey(value) ? 0 : queryFacetsValues.get(value);
		return new FacetValue(value, count);
	}

	public FacetValue getFieldFacetValue(String datastoreCode, String value) {
		if (fieldFacetValues.containsKey(datastoreCode)) {
			for (FacetValue facetValue : getFieldFacetValues(datastoreCode)) {
				if (facetValue.getValue().equals(value)) {
					return facetValue;
				}
			}
		}
		return null;
	}

	public Map<String, List<FacetValue>> getFieldFacetValues() {
		return fieldFacetValues;
	}

	public Map<String, Integer> getQueryFacetsValues() {
		return queryFacetsValues;
	}

	public SPEQueryResponse withModifiedRecordList(List<Record> records) {
		return new SPEQueryResponse(fieldFacetValues, statisticsValues, queryFacetsValues, qtime, numFound, records, null,
				correctlySpelt,
				spellcheckerSuggestions, recordsWithMoreLikeThis);
	}

	public SPEQueryResponse withNumFound(int numFound) {
		return new SPEQueryResponse(fieldFacetValues, statisticsValues, queryFacetsValues, qtime, numFound, records, null,
				correctlySpelt,
				spellcheckerSuggestions, recordsWithMoreLikeThis);
	}

	public Map<String, Map<String, List<String>>> getHighlights() {
		return highlights;
	}

	public boolean isCorrectlySpelt() {
		return correctlySpelt;
	}

	public List<String> getSpellCheckerSuggestions() {
		return spellcheckerSuggestions;
	}

	public Map<String, List<String>> getHighlighting(String recordId) {
		if (highlights == null) {
			return Collections.emptyMap();
		} else {
			return highlights.get(recordId);
		}
	}

	public Map<Record, Map<Record, Double>> getRecordsWithMoreLikeThis() {
		return recordsWithMoreLikeThis;
	}
}
