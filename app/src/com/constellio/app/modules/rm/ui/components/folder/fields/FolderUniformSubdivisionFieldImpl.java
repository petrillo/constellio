package com.constellio.app.modules.rm.ui.components.folder.fields;

import com.constellio.app.modules.rm.wrappers.UniformSubdivision;
import com.constellio.app.ui.framework.components.converters.RecordIdToCaptionConverter;
import com.constellio.app.ui.framework.components.fields.record.RecordComboBox;

public class FolderUniformSubdivisionFieldImpl extends RecordComboBox implements FolderUniformSubdivisionField {

	private RecordIdToCaptionConverter captionConverter = new RecordIdToCaptionConverter();

	public FolderUniformSubdivisionFieldImpl() {
		super(UniformSubdivision.DEFAULT_SCHEMA);
	}

	@Override
	public String getItemCaption(Object itemId) {
		return captionConverter.convertToPresentation((String) itemId, String.class, getLocale());
	}

	@Override
	public String getFieldValue() {
		return (String) getConvertedValue();
	}

	@Override
	public void setFieldValue(Object value) {
		setInternalValue(value);
	}

}
