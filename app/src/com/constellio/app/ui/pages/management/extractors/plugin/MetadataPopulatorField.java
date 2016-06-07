package com.constellio.app.ui.pages.management.extractors.plugin;

import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.framework.components.fields.list.ListAddEditCustomField;

import java.util.List;

/**
 * Created by Majid on 2016-05-05.
 */
public abstract class MetadataPopulatorField extends ListAddEditCustomField<MetadataPopulatorVO>{

	public void clean() {
		setValue(newBean());
	}

	public void setMetadataOptions(List<MetadataVO> metadataOptions) {
	}

	public abstract boolean isEmpty();
}
