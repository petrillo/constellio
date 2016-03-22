package com.constellio.app.modules.tasks.ui.components.fields.list;

import com.constellio.app.modules.tasks.ui.components.converters.KeyValueVOToStringConverter1;
import com.constellio.app.modules.tasks.ui.components.fields.KeyValueFieldImpl;
import com.constellio.app.modules.tasks.ui.entities.KeyValueVO;
import com.constellio.app.ui.framework.components.fields.list.ListAddRemoveField;

public class ListAddRemoveKeyValueField extends ListAddRemoveField<KeyValueVO, KeyValueFieldImpl> {

	private KeyValueVOToStringConverter1 converter = new KeyValueVOToStringConverter1();

	@Override
	protected KeyValueFieldImpl newAddEditField() {
		return new KeyValueFieldImpl();
	}

	//FIXME should be always Vo or not
	@Override
	protected String getItemCaption(Object itemId) {
		return converter.convertToPresentation((KeyValueVO) itemId, String.class, getLocale());
	}


}

