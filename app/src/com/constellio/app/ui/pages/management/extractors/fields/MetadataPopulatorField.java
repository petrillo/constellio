package com.constellio.app.ui.pages.management.extractors.fields;

import com.constellio.app.ui.framework.components.fields.list.ListAddEditCustomField;

/**
 * Created by Majid on 2016-05-05.
 */
public abstract class MetadataPopulatorField extends ListAddEditCustomField<MetadataPopulatorVO>{
    
	public void clean() {
    	setValue(newBean());
    }
	
}
