package com.constellio.app.ui.pages.management.facet.fields;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.vaadin.dialogs.ConfirmDialog;

import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.model.entities.structures.MapStringStringStructure;
import com.vaadin.data.Item;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

public class AddEditMappingTableFieldImpl<ModifiableStructure> extends ValuesLabelField<ModifiableStructure> {

	private VerticalLayout mainLayout;
	private Table valuesList;
	private ValuesListPresenter presenter;

	public AddEditMappingTableFieldImpl(ValuesListPresenter presenter) {
		this.presenter = presenter;
	}

	@Override
	protected Component initContent() {

		String i18nSuffix = presenter.getI18NSuffix();
		mainLayout = new VerticalLayout();
		mainLayout.setSizeFull();
		mainLayout.setSpacing(true);

		Button addValueButton = new Button($("AddEditMappingTableFieldImpl.values.new." + i18nSuffix));
		addValueButton.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				addEmptyValue();
			}
		});

		HorizontalLayout horizontalLayout = new HorizontalLayout();
		horizontalLayout.addStyleName("buttons-sltyle");
		horizontalLayout.setSpacing(true);
		horizontalLayout.addComponents(addValueButton);

		final Table table = new Table($("AddEditMappingTableFieldImpl.tableTitle." + i18nSuffix));
		table.addContainerProperty("value", String.class, null);
		table.addContainerProperty("label", String.class, null);
		table.addContainerProperty("delete", DeleteButton.class, null);
		table.setColumnHeader("value", $("AddEditMappingTableFieldImpl.values.value." + i18nSuffix));
		table.setColumnHeader("label", $("AddEditMappingTableFieldImpl"
				+ ".values.label." + i18nSuffix));
		table.setColumnHeader("delete", "");
		table.setColumnWidth("delete", 50);
		table.setWidth("100%");
		table.setEditable(true);
		table.setPageLength(table.getItemIds().size());
		valuesList = table;

		Map<String, String> values = presenter.getValues();
		for (Entry<String, String> entry : values.entrySet()) {
			addItem(entry.getValue(), entry.getKey());
		}

		mainLayout.addComponents(horizontalLayout, table);

		return mainLayout;
	}

	private List<String> getValuesInTable() {
		List<String> valuesInTable = new ArrayList<>();
		if (valuesList != null) {
			List<Integer> ids = (List<Integer>) valuesList.getItemIds();
			for (Integer id : ids) {
				Item item = valuesList.getItem(id);
				String value = (String) item.getItemProperty("value").getValue();
				valuesInTable.add(value);
			}
		}
		return valuesInTable;
	}

	private void addEmptyValue() {
		addItem("", "");
	}

	private void addItem(String label, String value) {
		int id = (int) valuesList.addItem();
		addItem(label, value, id);
	}

	private void addItem(String label, final String value, final int id) {
		Item row1 = valuesList.getItem(id);
		DeleteButton delete = new DeleteButton() {
			@Override
			protected void confirmButtonClick(ConfirmDialog dialog) {
				saveValues();
				presenter.removeValue(id);
				refreshTable();
			}
		};

		row1.getItemProperty("value").setValue(value);
		row1.getItemProperty("label").setValue(label);
		row1.getItemProperty("delete").setValue(delete);

		presenter.addValue(label, value, id);
	}

	@Override
	public void saveValues() {
		if (valuesList != null) {
			List<Integer> ids = (List<Integer>) valuesList.getItemIds();
			for (Integer id : ids) {
				Item item = valuesList.getItem(id);
				String label = (String) item.getItemProperty("label").getValue();
				String value = (String) item.getItemProperty("value").getValue();
				presenter.addValue(label, value, id);
			}
		}

	}

	@Override
	public Table getValueListTable() {
		return valuesList;
	}

	private void refreshTable() {
		valuesList.removeAllItems();
		Map<Integer, Map<String, String>> values = presenter.getOrderedValues();
		for (Integer id : values.keySet()) {
			Map<String, String> value = values.get(id);
			for (final String label : value.keySet()) {
				valuesList.addItem(id);
				addItem(label, value.get(label), id);
			}
		}
	}

	@Override
	public Class getType() {
		return MapStringStringStructure.class;
	}

}