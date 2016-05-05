package com.constellio.app.ui.pages.management.extractors.fields;

import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextArea;

/**
 * Created by Majid on 2016-05-05.
 */
public class PluginBField extends MetadataPopulatorField {
    FieldGroup fieldGroup;
    TextArea script = new TextArea();
    PluginB data = new PluginB();

    @Override
    public void clean() {
        fieldGroup.setItemDataSource(new BeanItem<PluginB>(new PluginB()));
        script.setValue("");
    }

    @Override
    protected Component initContent() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.addComponent(script);

        BeanItem<PluginB> item = new BeanItem<PluginB>(data);
        fieldGroup = new FieldGroup(item);
        fieldGroup.bindMemberFields(this);
        setInternalValue(data);

        return layout;
    }

    @Override
    public Class<? extends MetadataPopulatorVO> getType() {
        return null;
    }
}
