package com.constellio.model.entities.calculators.dependencies;

import java.util.List;

import com.constellio.model.entities.security.Authorization;
import com.constellio.model.utils.Lazy;

/**
 * Created by francisbaril on 2017-02-19.
 */
public class RecordAuthsDependencyValue {

	boolean concept;

	Lazy<List<Authorization>> authsOnRecord;

	public RecordAuthsDependencyValue(boolean concept, Lazy<List<Authorization>> authsOnRecord) {
		this.concept = concept;
		this.authsOnRecord = authsOnRecord;
	}

	public boolean isConcept() {
		return concept;
	}

	public List<Authorization> getAuthsOnRecord() {
		return authsOnRecord.get();
	}

}
