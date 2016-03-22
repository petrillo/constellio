package com.constellio.app.ui.pages.management.facet.fields;

import java.util.Map;

public interface ValuesListPresenter {
	String getI18NSuffix();

	Map<String,String> getValues();

	void removeValue(int index);

	void addValue(String key, String value, int index);

	Map<Integer,Map<String,String>> getOrderedValues();
}
