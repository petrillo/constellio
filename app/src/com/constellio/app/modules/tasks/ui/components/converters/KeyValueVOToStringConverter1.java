package com.constellio.app.modules.tasks.ui.components.converters;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

import com.constellio.app.modules.tasks.ui.entities.KeyValueVO;
import com.vaadin.data.util.converter.Converter;

public class KeyValueVOToStringConverter1 implements Converter<String, KeyValueVO> {


	@Override
	public KeyValueVO convertToModel(String value, Class<? extends KeyValueVO> targetType, Locale locale)
			throws ConversionException {
		return null;
	}

	@Override
	public String convertToPresentation(KeyValueVO keyValueVO, Class<? extends String> targetType, Locale locale)
			throws ConversionException {
		String presentation;
		if (keyValueVO != null) {
			String key = keyValueVO.getKey();
			String value = keyValueVO.getValue();

			StringBuffer sb = new StringBuffer();
			if (StringUtils.isNotBlank(key) && StringUtils.isNotBlank(value)) {
				sb.append(key);
			}
			sb.append(", ");
			if (StringUtils.isNotBlank(value)) {
				sb.append(value);
			}
			presentation = sb.toString();
		} else {
			presentation = null;
		}
		return presentation;
	}

	@Override
	public Class<KeyValueVO> getModelType() {
		return KeyValueVO.class;
	}

	@Override
	public Class<String> getPresentationType() {
		return String.class;
	}
}

