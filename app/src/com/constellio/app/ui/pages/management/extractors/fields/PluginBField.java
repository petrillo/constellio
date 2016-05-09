package com.constellio.app.ui.pages.management.extractors.fields;

import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextArea;

/**
 * Created by Majid on 2016-05-05.
 */
public class PluginBField extends MetadataPopulatorField {
	
    TextArea script = new TextArea();

    @Override
    public void clean() {
    	super.clean();
        script.setValue("");
    }

    @Override
    public Class<? extends MetadataPopulatorVO> getType() {
        return MetadataPopulatorVO.class;
    }

	@Override
	protected MetadataPopulatorVO newBean() {
		return new PluginB();
	}

	@Override
	protected void bindFields(FieldGroup fieldGroup) {
        fieldGroup.bindMemberFields(this);
	}

	@Override
	protected Component buildContent() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.addComponent(script);
        return layout;
	}
}
