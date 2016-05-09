package com.constellio.app.ui.framework.items;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

import com.vaadin.data.util.BeanUtil;
import com.vaadin.data.util.MethodPropertyDescriptor;
import com.vaadin.data.util.PropertysetItem;
import com.vaadin.data.util.VaadinPropertyDescriptor;

public class ReplaceableBeanItem<T> extends PropertysetItem {
	
	private T bean;
	
	public ReplaceableBeanItem() {
	}

	public ReplaceableBeanItem(T bean) {
		setBean(bean);
	}

	public final T getBean() {
		return bean;
	}

	@SuppressWarnings("unchecked")
	public final void setBean(T bean) {
		boolean newBean = bean != null && !bean.equals(this.bean);
		if (newBean) {
			Collection<?> propertyIds = new ArrayList<>(getItemPropertyIds());
			for (Object propertyId : propertyIds) {
				removeItemProperty(propertyId);
			}

			// Create bean information
	        LinkedHashMap<String, VaadinPropertyDescriptor<T>> propertyDescriptors = getPropertyDescriptors((Class<T>) bean.getClass());
	        for (VaadinPropertyDescriptor<T> pd : propertyDescriptors.values()) {
	            addItemProperty(pd.getName(), pd.createProperty(bean));
	        }
		}
		this.bean = bean;
	}

    /**
     * Copied from BeanItem
     * 
     * <p>
     * Perform introspection on a Java Bean class to find its properties.
     * </p>
     * 
     * <p>
     * Note : This version only supports introspectable bean properties and
     * their getter and setter methods. Stand-alone <code>is</code> and
     * <code>are</code> methods are not supported.
     * </p>
     * 
     * @param beanClass
     *            the Java Bean class to get properties for.
     * @return an ordered map from property names to property descriptors
     */
    static <BT> LinkedHashMap<String, VaadinPropertyDescriptor<BT>> getPropertyDescriptors(
            final Class<BT> beanClass) {
        final LinkedHashMap<String, VaadinPropertyDescriptor<BT>> pdMap = new LinkedHashMap<String, VaadinPropertyDescriptor<BT>>();

        // Try to introspect, if it fails, we just have an empty Item
        try {
            List<PropertyDescriptor> propertyDescriptors = BeanUtil
                    .getBeanPropertyDescriptor(beanClass);

            // Add all the bean properties as MethodProperties to this Item
            // later entries on the list overwrite earlier ones
            for (PropertyDescriptor pd : propertyDescriptors) {
                final Method getMethod = pd.getReadMethod();
                if ((getMethod != null)
                        && getMethod.getDeclaringClass() != Object.class) {
                    VaadinPropertyDescriptor<BT> vaadinPropertyDescriptor = new MethodPropertyDescriptor<BT>(
                            pd.getName(), pd.getPropertyType(),
                            pd.getReadMethod(), pd.getWriteMethod());
                    pdMap.put(pd.getName(), vaadinPropertyDescriptor);
                }
            }
        } catch (final java.beans.IntrospectionException ignored) {
        }

        return pdMap;
    }

}
