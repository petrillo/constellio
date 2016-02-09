package com.constellio.model.extensions.events.recordsImport;

import com.constellio.app.services.schemas.bulkImport.ImportDataErrors;
import com.constellio.app.services.schemas.bulkImport.data.ImportData;

public class ValidationParams {

	ImportDataErrors errors;
	ImportData importRecord;

	public ValidationParams(ImportDataErrors errors, ImportData importRecord) {
		this.errors = errors;
		this.importRecord = importRecord;
	}

	public ImportDataErrors getErrors() {
		return errors;
	}

	public ImportData getImportRecord() {
		return importRecord;
	}

}