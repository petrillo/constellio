package com.constellio.app.ui.pages.management.extractors.fields;

import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.pages.management.extractors.plugin.MetadataPopulatorField;
import com.constellio.app.ui.pages.management.extractors.plugin.MetadataPopulatorPluginFactory;
import com.constellio.app.ui.pages.management.extractors.plugin.MetadataPopulatorVO;
import com.vaadin.data.Property;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.util.AbstractProperty;
import com.vaadin.ui.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class GenericMetadataPopulatorField extends CustomField<MetadataPopulatorVO> {

    private Layout pluginLayout = new HorizontalLayout();

    private ComboBox type = new ComboBox();
    private MetadataPopulatorField activePlugin;
    private Map<String, MetadataPopulatorField> pluginLayouts = new HashMap<>();
    private List<MetadataVO> metadataOptions;
    private Map<Class<? extends MetadataPopulatorVO>, String> metadataPopulatorVOTypeMap = new HashMap<>();
    private FieldGroup fieldGroup;
    private MetadataPopulatorPluginFactory factory;

    public GenericMetadataPopulatorField(MetadataPopulatorPluginFactory factory){
        for (String aPlugin: factory.getRegisteredPluginNames()){
            type.addItems(aPlugin);
            pluginLayouts.put(aPlugin, factory.createInstance(aPlugin).getMetadataPopulatorField());
        }
        this.factory = factory;
    }

    private MetadataPopulatorField getPluginLayout(Object pluginName){
        return pluginLayouts.get(pluginName);
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
                    activePlugin.setMetadataOptions(metadataOptions);
                    setInternalValue(activePlugin.getValue());
                }
            }
        });

        layout.addComponent(pluginLayout);
        layout.addComponent(pluginLayout);

        setPropertyDataSource(new AbstractProperty<MetadataPopulatorVO>() {
            @Override
            public MetadataPopulatorVO getValue() {
                if (activePlugin != null) {
                    activePlugin.commit();
                    if (activePlugin.isEmpty())

                    return activePlugin.getValue();
                }
                return null;
            }

            @Override
            public void setValue(MetadataPopulatorVO newValue) throws ReadOnlyException {
                if (newValue != null){
                    //update type
                    String newType = factory.getPluginName(newValue.getClass());
                    type.setValue(newType);
                }

                if (activePlugin != null){
                    //set the value for the active plugin
                    activePlugin.setValue(newValue);
                }

                //set internal value
                if (activePlugin != null)
                    setInternalValue(activePlugin.getValue());
                else
                    setInternalValue(null);
            }

            @Override
            public Class<MetadataPopulatorVO> getType() {
                return MetadataPopulatorVO.class;
            }
        });

        setValue(null);

        return layout;
    }

    @Override
    public Class<? extends MetadataPopulatorVO> getType() {
        return MetadataPopulatorVO.class;
    }

    public void setMetadataOptions(List<MetadataVO> metadataOptions) {
        this.metadataOptions = metadataOptions;
    }

}
