package com.constellio.app.ui.framework.components.fields.list;

import com.constellio.app.ui.framework.components.fields.BaseCustomField;

public abstract class ListAddEditCustomField<T extends Object> extends BaseCustomField<T> {

	public ListAddEditCustomField() {
		setValue(null);
	}

	@Override
	public void setValue(T newValue)
			throws com.vaadin.data.Property.ReadOnlyException {
		if (newValue == null) {
			newValue = newBean();
		}
		super.setValue(newValue);
	}
	
	protected abstract T newBean();

}
