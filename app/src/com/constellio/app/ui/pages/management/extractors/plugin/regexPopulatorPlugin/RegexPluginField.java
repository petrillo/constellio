package com.constellio.app.ui.pages.management.extractors.plugin.regexPopulatorPlugin;

import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.pages.management.extractors.plugin.MetadataPopulatorField;
import com.constellio.app.ui.pages.management.extractors.plugin.MetadataPopulatorVO;
import com.constellio.model.entities.schemas.RegexConfig;
import com.vaadin.data.Property;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.ui.*;

import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

/**
 * Created by Majid on 2016-04-26.
 */
public class RegexPluginField extends MetadataPopulatorField {

	@PropertyId("metadataCode")
	private ComboBox inputMetadataComboboxField;

	@PropertyId("regexPattern")
	private TextField regexTextField;

	@PropertyId("value")
	private TextField valueTextField;

	@PropertyId("substitute")
	private Boolean substitute;
	private ComboBox regexConfigTypeComboboxField;

	TextField config = new TextField();

    @Override
    public Class<? extends MetadataPopulatorVO> getType() {
        return RegexPopulatorVO.class;
    }

    @Override
    protected Component buildContent() {
		VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setWidth("99%");
		mainLayout.setHeightUndefined();
		mainLayout.setSpacing(true);

		inputMetadataComboboxField = new ComboBox();
		inputMetadataComboboxField.setCaption($("RegexConfigField.inputMetadata"));
		inputMetadataComboboxField.setId("inputMetadata");
		inputMetadataComboboxField.addItem("test");
		inputMetadataComboboxField.setNullSelectionAllowed(false);

		regexTextField = new TextField();
		regexTextField.setCaption($("RegexConfigField.regex"));
		regexTextField.setId("regex");
		regexTextField.setNullRepresentation("");
//		regexTextField.addValidator(new NullValidator("The regex pattern is empty", false));

		valueTextField = new TextField();
		valueTextField.setCaption($("RegexConfigField.value"));
		valueTextField.setId("value");
		valueTextField.setNullRepresentation("");

		regexConfigTypeComboboxField = new ComboBox();
		regexConfigTypeComboboxField.setCaption($("RegexConfigField.regexConfigType"));
		regexConfigTypeComboboxField.setId("regexConfigType");
		regexConfigTypeComboboxField.addItem(RegexConfig.RegexConfigType.SUBSTITUTION);
		regexConfigTypeComboboxField
				.setItemCaption(RegexConfig.RegexConfigType.SUBSTITUTION, $("RegexConfigField.RegexConfigType.SUBSTITUTION"));

		regexConfigTypeComboboxField.addItem(RegexConfig.RegexConfigType.TRANSFORMATION);
		regexConfigTypeComboboxField
				.setItemCaption(RegexConfig.RegexConfigType.TRANSFORMATION, $("RegexConfigField.RegexConfigType.TRANSFORMATION"));
		regexConfigTypeComboboxField.setNullSelectionAllowed(true);


		regexConfigTypeComboboxField.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(Property.ValueChangeEvent event) {
				final Object value = event.getProperty().getValue();
				if (RegexConfig.RegexConfigType.SUBSTITUTION.equals(value))
					substitute = true;
				else if (RegexConfig.RegexConfigType.TRANSFORMATION.equals(value))
					substitute = false;
				else
					substitute = null;
			}
		});

		HorizontalLayout horizontalLayout = new HorizontalLayout();
		horizontalLayout.setSpacing(true);
		horizontalLayout.setWidth("100%");
		horizontalLayout.addComponents(inputMetadataComboboxField, regexTextField, regexConfigTypeComboboxField, valueTextField);

		mainLayout.addComponents(horizontalLayout);
		mainLayout.setExpandRatio(horizontalLayout, 1);

		return mainLayout;
	}

	@Override
	public void setMetadataOptions(List<MetadataVO> metadataVOs) {
		inputMetadataComboboxField.removeAllItems();
		for (MetadataVO metadataVO : metadataVOs) {
			inputMetadataComboboxField.addItem(metadataVO.getCode());
			inputMetadataComboboxField.setItemCaption(metadataVO.getCode(), metadataVO.getLabel());
		}
	}

	@Override
	public boolean isEmpty() {
		return regexTextField.getValue() == null;
	}

	@Override
	public MetadataPopulatorVO newBean() {
		return new RegexPopulatorVO();
	}

	@Override
	protected void bindFields(FieldGroup fieldGroup) {
		fieldGroup.bindMemberFields(this);
	}

}
