package com.constellio.data.dao.services.solr;

import java.util.List;
import java.util.Map;

import org.apache.solr.common.SolrInputDocument;

import com.constellio.data.utils.ImpossibleRuntimeException;

public class ConstellioSolrInputDocument extends SolrInputDocument {

	@Override
	public void setField(String name, Object value) {
		value = convertEmptyListToNull(value);
		if (value == null) {
			super.remove(name);
		} else {
			validate(name, value);
			super.setField(name, value);
		}
	}

	@Override
	public void setField(String name, Object value, float boost) {
		value = convertEmptyListToNull(value);
		if (value == null) {
			super.remove(name);
		} else {
			validate(name, value);
			super.setField(name, value, boost);
		}
	}

	@Override
	public void addField(String name, Object value, float boost) {

		value = convertEmptyListToNull(value);

		if (value == null) {
			super.remove(name);
		} else {
			validate(name, value);
			super.addField(name, value, boost);
		}
	}

	private Object convertEmptyListToNull(Object value) {
		if (value instanceof List) {
			List list = (List) value;
			boolean hasNonNullValue = false;
			for (Object o : list) {
				if (o != null) {
					hasNonNullValue = true;
				}
			}

			return hasNonNullValue ? value : null;
		} else {
			return value;
		}
	}

	private void validate(String name, Object value) {
		if (name == null) {
			throw new ImpossibleRuntimeException("field name must not be null");
		}
		if (value == null) {
			throw new ImpossibleRuntimeException("value of field '" + name + "' must not be null");
		}
		if (name.equals("id") && !(value instanceof String)) {
			throw new ImpossibleRuntimeException("id field must be not null and a string");
		}
		if (value instanceof List) {
			for (Object item : (List) value) {
				if (item == null) {
					throw new ImpossibleRuntimeException("value of field '" + name + "' must not contain null values");
				}
			}
		}

		if (value instanceof Map) {
			for (Object item : ((Map) value).values()) {
				//				if (item == null) {
				//					throw new ImpossibleRuntimeException("value of field '" + name + "' must not contain null values");
				//				}
				if (item instanceof List) {
					for (Object mapValueItem : (List) item) {
						if (mapValueItem == null) {
							throw new ImpossibleRuntimeException("value of field '" + name + "' must not contain null values");
						}
					}
				}
			}
		}

		if (name.endsWith("_da") || name.endsWith("_dt")) {
			ensureValueIsOfClass(name, value, String.class);
		}

		if (name.endsWith("_das") || name.endsWith("_dts")) {
			ensureValueIsListOfClass(name, value, String.class);
		}
	}

	private void ensureValueIsOfClass(String fieldName, Object value, Class<?> expectedValueClass) {
		if (value instanceof Map) {
			Map<Object, Object> map = (Map) value;
			Object firstEntryValue = map.entrySet().iterator().next().getValue();
			ensureValueIsOfClass(fieldName, firstEntryValue, expectedValueClass);
		} else if (value != null && !expectedValueClass.isAssignableFrom(value.getClass())) {
			throw new ImpossibleRuntimeException(
					"value of field '" + fieldName + "' must be a " + expectedValueClass.getSimpleName() + " instead of a "
							+ value.getClass().getSimpleName());
		}

	}

	private void ensureValueIsListOfClass(String fieldName, Object value, Class<?> expectedValueClass) {
		if (value instanceof Map) {
			Map<Object, Object> map = (Map) value;
			Object firstEntryValue = map.entrySet().iterator().next().getValue();
			ensureValueIsListOfClass(fieldName, firstEntryValue, expectedValueClass);
		} else if (value != null) {
			if (!(value instanceof List)) {
				throw new ImpossibleRuntimeException(
						"value of field '" + fieldName + "' must be a List, but is of class " + value.getClass());
			}
			List<Object> list = (List) value;
			for (Object item : list) {
				ensureValueIsOfClass(fieldName, item, expectedValueClass);
			}
		}

	}
}
