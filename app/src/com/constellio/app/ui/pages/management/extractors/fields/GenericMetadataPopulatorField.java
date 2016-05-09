package com.constellio.app.ui.pages.management.extractors.fields;

import com.vaadin.data.Property;
import com.vaadin.data.util.AbstractProperty;
import com.vaadin.ui.*;

import java.util.*;


/**
 * Created by Majid on 2016-04-25.
 */
public class GenericMetadataPopulatorField extends CustomField<TypedMetadataPopulatorVO> {

    TypedMetadataPopulatorVO typedMetadataPopulatorVO = new TypedMetadataPopulatorVO();

    private Layout pluginLayout = new HorizontalLayout();

    private ComboBox type = new ComboBox();
    private MetadataPopulatorField activePlugin;
    private Map<String, MetadataPopulatorField> pluginLayouts = new HashMap<>();

    public GenericMetadataPopulatorField(){
        List<MetadataPopulatorVO> plugins = new ArrayList<>();
        plugins.add(new PluginA());
        plugins.add(new PluginB());

        List<MetadataPopulatorField> pluginFields = new ArrayList<>();
        pluginFields.add(new PluginAField());
        pluginFields.add(new PluginBField());

        for (int i = 0; i < plugins.size(); i++){
            type.addItem(plugins.get(i).getName());
            pluginLayouts.put(plugins.get(i).getName(), pluginFields.get(i));
        }
    }

    @Override
    protected Component initContent() {
        Layout layout = new VerticalLayout();
        layout.addComponent(type);

        type.addValueChangeListener(new ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                Object propertyValue = event.getProperty().getValue();
                pluginLayout.removeAllComponents();
                activePlugin = getPluginLayout(propertyValue);
                if (activePlugin != null) {
                    pluginLayout.addComponent(activePlugin);
                    typedMetadataPopulatorVO.setType((String) propertyValue);
                }
            }
        });

        layout.addComponent(pluginLayout);

        setPropertyDataSource(new AbstractProperty<TypedMetadataPopulatorVO>() {
            @Override
            public TypedMetadataPopulatorVO getValue() {
                if (activePlugin != null) {
                    activePlugin.commit();
                    typedMetadataPopulatorVO.setMetadataPopulatorVO(activePlugin.getValue());
                }
                return typedMetadataPopulatorVO;
            }

            @Override
            public void setValue(TypedMetadataPopulatorVO newValue) throws ReadOnlyException {
                setInternalValue(newValue);
                if (newValue == null){  //click add button
                    activePlugin.clean();
                    typedMetadataPopulatorVO = new TypedMetadataPopulatorVO();
                } else if (newValue.getType() != null) {    //click edit or add button
                    typedMetadataPopulatorVO = newValue;
                    type.select(newValue.getType());
                    activePlugin.setValue(typedMetadataPopulatorVO.getMetadataPopulatorVO());
                }

            }

            @Override
            public Class<TypedMetadataPopulatorVO> getType() {
                return TypedMetadataPopulatorVO.class;
            }
        });

        setValue(typedMetadataPopulatorVO);

        return layout;
    }

    private MetadataPopulatorField getPluginLayout(Object pluginName){
        return pluginLayouts.get(pluginName);
    }

    @Override
    public Class<? extends TypedMetadataPopulatorVO> getType() {
        return TypedMetadataPopulatorVO.class;
    }
}
