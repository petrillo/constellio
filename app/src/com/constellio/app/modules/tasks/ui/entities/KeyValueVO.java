package com.constellio.app.modules.tasks.ui.entities;

import java.io.Serializable;

public class KeyValueVO implements Serializable {
	String key ="", value="";

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
