package com.constellio.app.modules.rm.ui.pages.folder2;

import static com.constellio.app.ui.i18n.i18n.$;

import com.constellio.app.modules.rm.ui.components.folder.fields.FolderRetentionRuleFieldImpl;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.OverridingMetadataFieldFactory;
import com.constellio.app.ui.framework.components.RecordForm;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.model.frameworks.validation.ValidationException;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;

public class AddEditFolderViewImpl2 extends BaseViewImpl implements AddEditFolderView2 {

	private final AddEditFolderPresenter2 presenter;

	private FolderForm form;

	private RecordVO folder;

	public AddEditFolderViewImpl2() {
		this.presenter = new AddEditFolderPresenter2(this);
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {
		folder = presenter.forParams(event.getParameters()).getFolder();
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		form = new FolderForm(folder);

		for (final Field<?> field : form.getFields()) {
			field.addValueChangeListener(new ValueChangeListener() {
				@Override
				public void valueChange(ValueChangeEvent event) {
					presenter.fieldValueChanged(field);
				}
			});
		}

		return form;
	}

	@Override
	protected void afterViewAssembled(ViewChangeEvent event) {
		presenter.viewAssembled();
	}

	@Override
	protected String getTitle() {
		return $(presenter.isAddMode() ? "AddEditFolderView.addViewTitle" : "AddEditFolderView.editViewTitle");
	}

	@Override
	public FolderForm getForm() {
		return form;
	}

	public class FolderForm extends RecordForm {
		public FolderForm(RecordVO record) {
			super(record, new FolderMetadataFieldFactory(presenter));
		}

		@Override
		protected void saveButtonClick(RecordVO viewObject)
				throws ValidationException {
			presenter.saveButtonClicked(viewObject);
		}

		@Override
		protected void cancelButtonClick(RecordVO viewObject) {
			presenter.backButtonClicked();
		}
	}

	public static class FolderMetadataFieldFactory extends OverridingMetadataFieldFactory<AddEditFolderPresenter2> {
		public FolderMetadataFieldFactory(AddEditFolderPresenter2 presenter) {
			super(presenter);
		}

		@Override
		protected Field<?> newSingleValueField(MetadataVO metadata) {

			if (Folder.RETENTION_RULE_ENTERED.equals(metadata.getLocalCode())) {
				return new FolderRetentionRuleFieldImpl(metadata.getCollection());
			}

			return super.newSingleValueField(metadata);
		}

		@Override
		protected Field<?> newMultipleValueField(MetadataVO metadata) {
			return super.newMultipleValueField(metadata);
		}

	}
}
