package com.constellio.app.ui.framework.components.fields;

import java.util.ArrayList;
import java.util.List;

import com.constellio.app.ui.framework.items.ReplaceableBeanItem;
import com.constellio.app.ui.util.ComponentTreeUtils;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.server.ErrorMessage;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.Field;

public abstract class BaseCustomField<T extends Object> extends CustomField<T> {
	
	protected Component root;
	
	protected ReplaceableBeanItem<T> beanItem;
	
	protected FieldGroup fieldGroup;
	
	private boolean contentBuilt;
	
	public BaseCustomField() {
		this.fieldGroup = new FieldGroup();
	}
	
	public ReplaceableBeanItem<T> getBeanItem() {
		return beanItem;
	}

	public FieldGroup getFieldGroup() {
		return fieldGroup;
	}

	@SuppressWarnings("unchecked")
	protected List<AbstractField<?>> getFields() {
		List<AbstractField<?>> fields;
		if (root != null) {
			List<?> rawFields = ComponentTreeUtils.getChildren(root, AbstractField.class);
			fields = (List<AbstractField<?>>) rawFields;
		} else {
			fields = new ArrayList<>();
		}
		return fields;
	}
	
	protected AbstractField<?> getDefaultField() {
		AbstractField<?> defaultField;
		if (root != null) {
			defaultField = ComponentTreeUtils.getFirstChild(root, AbstractField.class);
		} else {
			defaultField = null;
		}
		return defaultField;
	}

	@Override
	public void commit()
			throws SourceException, InvalidValueException {
		try {
			fieldGroup.commit();
		} catch (CommitException e) {
			// TODO
			e.printStackTrace();
			throw new InvalidValueException(e.getMessage());
		}
	}

	@Override
	public void discard()
			throws SourceException {
		fieldGroup.discard();
	}

	@Override
	public boolean isValid() {
		// TODO
		return true;
	}

	@Override
	public void validate()
			throws InvalidValueException {
		// TODO
		super.validate();
	}

	@Override
	public void setValue(T newValue)
			throws com.vaadin.data.Property.ReadOnlyException {
		super.setValue(newValue);
		initFieldGroup(newValue);
	}

	public void focus() {
		AbstractField<?> defaultField = getDefaultField();
		if (defaultField != null) {
			defaultField.focus();
		}
	}

	public String getRequiredError() {
		String requiredMessage;
		AbstractField<?> defaultField = getDefaultField();
		if (defaultField != null) {
			requiredMessage = defaultField.getRequiredError();
		} else {
			requiredMessage = null;
		}
		return requiredMessage;
	}

	@Override
	public void setRequiredError(String requiredMessage) {
		AbstractField<?> defaultField = getDefaultField();
		if (defaultField != null) {
			defaultField.setRequiredError(requiredMessage);
		}
	}

	@Override
	public String getConversionError() {
		String conversionError;
		AbstractField<?> defaultField = getDefaultField();
		if (defaultField != null) {
			conversionError = defaultField.getConversionError();
		} else {
			conversionError = null;
		}
		return conversionError;
	}

	@Override
	public void setConversionError(String valueConversionError) {
		AbstractField<?> defaultField = getDefaultField();
		if (defaultField != null) {
			defaultField.setConversionError(valueConversionError);
		}
	}

	@Override
	public ErrorMessage getComponentError() {
		ErrorMessage componentError;
		AbstractField<?> defaultField = getDefaultField();
		if (defaultField != null) {
			componentError = defaultField.getComponentError();
		} else {
			componentError = null;
		}
		return componentError;
	}

	@Override
	public void setComponentError(ErrorMessage componentError) {
		AbstractField<?> defaultField = getDefaultField();
		if (defaultField != null) {
			defaultField.setComponentError(componentError);
		}
	}
	
	private void initFieldGroup(T value) {
		T beanItemValue = beanItem != null ? beanItem.getBean() : null;
		boolean newValue = value == null || !value.equals(beanItemValue);

		if (beanItem == null) {
			beanItem = new ReplaceableBeanItem<>(value);
			fieldGroup.setItemDataSource(beanItem);
		} 
		if (newValue) {
			if (contentBuilt) {
				unbindFields(fieldGroup);
			}
			beanItem.setBean(value);
			if (contentBuilt) {
				bindFields(fieldGroup);
			}
		}
	}

	@Override
	protected final Component initContent() {
		root = buildContent();
		contentBuilt = true;
		bindFields(fieldGroup);
		return root;
	}
	
	protected void unbindFields(FieldGroup fieldGroup) {
		for (Field<?> field : new ArrayList<>(fieldGroup.getFields())) {
			fieldGroup.unbind(field);
		}
	}
	
	protected abstract void bindFields(FieldGroup fieldGroup);

	protected abstract Component buildContent();
	
}
