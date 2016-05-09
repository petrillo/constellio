package com.constellio.app.modules.rm.ui.components.retentionRule;

import com.constellio.app.modules.rm.model.enums.DisposalType;
import com.constellio.app.modules.rm.wrappers.structures.RetentionRuleDocumentType;
import com.constellio.app.modules.rm.wrappers.type.DocumentType;
import com.constellio.app.ui.framework.components.fields.enumWithSmallCode.EnumWithSmallCodeComboBox;
import com.constellio.app.ui.framework.components.fields.list.ListAddEditCustomField;
import com.constellio.app.ui.framework.components.fields.record.RecordComboBox;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;

public class RetentionRuleDocumentTypeField extends ListAddEditCustomField<RetentionRuleDocumentType> {
	
	private HorizontalLayout layout;
	
	private ComboBox documentTypeField;
	
	private EnumWithSmallCodeComboBox<DisposalType> disposalTypeField;
	
	protected void bindFields(FieldGroup fieldGroup) {
		fieldGroup.bind(documentTypeField, "documentTypeId");
		fieldGroup.bind(disposalTypeField, "disposalType");
	}

	@Override
	protected Component buildContent() {
		setSizeFull();
		
		layout = new HorizontalLayout();
		layout.setWidth("100%");
		layout.setSpacing(true);
		
		documentTypeField = new RecordComboBox(DocumentType.DEFAULT_SCHEMA);
		disposalTypeField = new EnumWithSmallCodeComboBox<DisposalType>(DisposalType.class) {
			@Override
			protected boolean isIgnored(String enumCode) {
				return DisposalType.SORT.getCode().equals(enumCode);
			}
		};
		
		layout.addComponents(documentTypeField, disposalTypeField);
		return layout;
	}
	
	public void setDisposalTypeFieldVisible(boolean visible) {
		this.disposalTypeField.setVisible(visible);
	}

	@Override
	public Class<? extends RetentionRuleDocumentType> getType() {
		return RetentionRuleDocumentType.class;
	}

	@Override
	protected RetentionRuleDocumentType newBean() {
		return new RetentionRuleDocumentType();
	}

	@Override
	protected AbstractField<?> getDefaultField() {
		return documentTypeField;
	}
	
}