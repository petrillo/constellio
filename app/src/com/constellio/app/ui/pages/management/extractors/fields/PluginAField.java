package com.constellio.app.ui.pages.management.extractors.fields;

import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.util.AbstractProperty;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;

/**
 * Created by Majid on 2016-04-26.
 */
public class PluginAField extends MetadataPopulatorField {
    FieldGroup fieldGroup;
    TextField config = new TextField();

    private PluginA data = new PluginA();

    @Override
    protected Component initContent() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.addComponent(config);

        BeanItem<PluginA> item = new BeanItem<PluginA>(data);
        fieldGroup = new FieldGroup(item);
        fieldGroup.bind(config, "config");

        setPropertyDataSource(new AbstractProperty<PluginA>() {
            @Override
            public PluginA getValue() {
                return data;
            }

            @Override
            public void setValue(PluginA newValue) throws ReadOnlyException {
                setInternalValue(newValue);
                data = newValue;
            }

            @Override
            public Class<? extends PluginA> getType() {
                return PluginA.class;
            }
        });


        return layout;
    }

    @Override
    public Class<? extends MetadataPopulatorVO> getType() {
        return PluginA.class;
    }

    @Override
    public void clean() {
        data = new PluginA();
        fieldGroup.setItemDataSource(new BeanItem<PluginA>(data));
    }
}
