package com.constellio.app.ui.pages.management.extractors.fields;

import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;

/**
 * Created by Majid on 2016-04-26.
 */
public class PluginAField extends MetadataPopulatorField {
	
    TextField config = new TextField();

    @Override
    public Class<? extends MetadataPopulatorVO> getType() {
        return PluginA.class;
    }

    @Override
    protected Component buildContent() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.addComponent(config);
        return layout;
    }

	@Override
	protected MetadataPopulatorVO newBean() {
		return new PluginA();
	}

	@Override
	protected void bindFields(FieldGroup fieldGroup) {
		fieldGroup.bind(config, "config");
	}

	@Override
	public void clean() {
		super.clean();
	}

}
