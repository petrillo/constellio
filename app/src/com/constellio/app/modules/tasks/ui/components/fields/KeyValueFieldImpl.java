package com.constellio.app.modules.tasks.ui.components.fields;

import static com.constellio.app.ui.i18n.i18n.$;

import org.apache.commons.lang3.StringUtils;

import com.constellio.app.modules.tasks.ui.entities.KeyValueVO;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.data.util.AbstractProperty;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

public class KeyValueFieldImpl extends CustomField<KeyValueVO> implements KeyValueField {
	private KeyValueVO keyValueVO;

	private BeanItem<KeyValueVO> keyValueItem;

	private FieldGroup fieldGroup;

	@PropertyId("key")
	private TextField keyField;
	@PropertyId("value")
	private TextField valueField;

	@Override
	protected Component initContent() {
		if (keyValueVO == null) {
			keyValueVO = new KeyValueVO();
		}
		keyValueItem = new BeanItem<>(keyValueVO);
		fieldGroup = new FieldGroup(keyValueItem);

		setPropertyDataSource(new AbstractProperty<KeyValueVO>() {
			@Override
			public KeyValueVO getValue() {
				boolean submittedValueValid = keyValueVO.getKey() != null;
				return submittedValueValid ? keyValueVO : null;
			}

			@Override
			public void setValue(KeyValueVO newValue)
					throws ReadOnlyException {
				setInternalValue(newValue);
				keyValueVO = newValue != null ? newValue : new KeyValueVO();
				if (fieldGroup != null) {
					keyValueItem = new BeanItem<>(keyValueVO);
					fieldGroup.setItemDataSource(keyValueItem);
				}
			}

			@Override
			public Class<? extends KeyValueVO> getType() {
				return KeyValueVO.class;
			}
		});

		VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setWidth("99%");
		mainLayout.setHeightUndefined();
		mainLayout.setSpacing(true);

		keyField = new TextField();
		keyField.setCaption($("KeyValueField.key"));
		keyField.setEnabled(true);

		valueField = new TextField();
		valueField.setCaption($("KeyValueField.value"));

		mainLayout.addComponent(keyField);
		mainLayout.addComponent(valueField);

		fieldGroup.bindMemberFields(this);

		return mainLayout;
	}

	@Override
	public Class<? extends KeyValueVO> getType() {
		return KeyValueVO.class;
	}

	private boolean isInvalidFieldValue() {
		boolean invalidFieldValue;
		String key = keyField.getValue();
		String value = valueField.getValue();
		if (StringUtils.isBlank(key) || StringUtils.isBlank(value)) {
			invalidFieldValue = true;
		} else {
			invalidFieldValue = false;
		}
		return invalidFieldValue;
	}

	@Override
	public void commit()
			throws SourceException, InvalidValueException {
		if (!isInvalidFieldValue()) {
			try {
				fieldGroup.commit();
			} catch (CommitException e) {
				throw new InvalidValueException(e.getMessage());
			}
			super.commit();
		}
	}
}
