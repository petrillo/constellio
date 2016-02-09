package com.constellio.app.extensions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.constellio.app.api.extensions.DownloadContentVersionLinkExtension;
import com.constellio.app.api.extensions.GenericRecordPageExtension;
import com.constellio.app.api.extensions.PageExtension;
import com.constellio.app.api.extensions.SearchPageExtension;
import com.constellio.app.api.extensions.TaxonomyPageExtension;
import com.constellio.app.api.extensions.taxonomies.GetCustomResultDisplayParam;
import com.constellio.app.api.extensions.taxonomies.GetTaxonomyExtraFieldsParam;
import com.constellio.app.api.extensions.taxonomies.GetTaxonomyManagementClassifiedTypesParams;
import com.constellio.app.api.extensions.taxonomies.TaxonomyExtraField;
import com.constellio.app.api.extensions.taxonomies.TaxonomyManagementClassifiedType;
import com.constellio.app.extensions.api.cmis.CmisExtension;
import com.constellio.app.extensions.api.cmis.params.BuildCmisObjectFromConstellioRecordParams;
import com.constellio.app.extensions.api.cmis.params.BuildConstellioRecordFromCmisObjectParams;
import com.constellio.app.extensions.records.RecordAppExtension;
import com.constellio.app.extensions.records.RecordNavigationExtension;
import com.constellio.app.extensions.records.params.BuildRecordVOParams;
import com.constellio.app.extensions.records.params.GetIconPathParams;
import com.constellio.app.ui.framework.components.SearchResultDisplay;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.data.frameworks.extensions.ExtensionBooleanResult;
import com.constellio.data.frameworks.extensions.ExtensionUtils.BooleanCaller;
import com.constellio.data.frameworks.extensions.VaultBehaviorsList;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaType;

public class AppLayerCollectionExtensions {

	//------------ Extension points -----------

	public Map<String, ModuleExtensions> moduleExtensionsMap = new HashMap<>();

	public VaultBehaviorsList<PageExtension> pageAccessExtensions = new VaultBehaviorsList<>();

	public VaultBehaviorsList<TaxonomyPageExtension> taxonomyAccessExtensions = new VaultBehaviorsList<>();

	public VaultBehaviorsList<GenericRecordPageExtension> schemaTypeAccessExtensions = new VaultBehaviorsList<>();

	public VaultBehaviorsList<SearchPageExtension> searchPageExtensions = new VaultBehaviorsList<>();

	public VaultBehaviorsList<RecordAppExtension> recordAppExtensions = new VaultBehaviorsList<>();

	public VaultBehaviorsList<RecordNavigationExtension> recordNavigationExtensions = new VaultBehaviorsList<>();

	public VaultBehaviorsList<CmisExtension> cmisExtensions = new VaultBehaviorsList<>();

	public List<DownloadContentVersionLinkExtension> downloadContentVersionLinkExtensions = new ArrayList<>();

	public <T extends ModuleExtensions> T forModule(String moduleId) {
		return (T) moduleExtensionsMap.get(moduleId);
	}

	//----------------- Callers ---------------

	public void buildRecordVO(BuildRecordVOParams params) {
		for (RecordAppExtension recordAppExtension : recordAppExtensions) {
			recordAppExtension.buildRecordVO(params);
		}
	}

	public String getIconForRecord(GetIconPathParams params) {
		for (RecordAppExtension recordAppExtension : recordAppExtensions) {
			String icon = recordAppExtension.getIconPathForRecord(params);
			if (icon != null) {
				return icon;
			}
		}
		return null;
	}

	public void buildCMISObjectFromConstellioRecord(BuildCmisObjectFromConstellioRecordParams params) {
		for (CmisExtension extension : cmisExtensions) {
			extension.buildCMISObjectFromConstellioRecord(params);
		}
	}

	public void buildConstellioRecordFromCmisObject(BuildConstellioRecordFromCmisObjectParams params) {
		for (CmisExtension extension : cmisExtensions) {
			extension.buildConstellioRecordFromCmisObject(params);
		}
	}

	public SearchResultDisplay getCustomResultDisplayFor(GetCustomResultDisplayParam params) {
		List<TaxonomyManagementClassifiedType> types = new ArrayList<>();
		for (SearchPageExtension extension : searchPageExtensions) {
			SearchResultDisplay result = extension.getCustomResultDisplayFor(params);
			if (result != null) {
				return result;
			}
		}
		return null;
	}

	public List<TaxonomyManagementClassifiedType> getClassifiedTypes(GetTaxonomyManagementClassifiedTypesParams params) {
		List<TaxonomyManagementClassifiedType> types = new ArrayList<>();
		for (TaxonomyPageExtension extension : taxonomyAccessExtensions) {
			types.addAll(extension.getClassifiedTypesFor(params));
		}
		return types;
	}

	public List<TaxonomyExtraField> getTaxonomyExtraFields(GetTaxonomyExtraFieldsParam params) {
		List<TaxonomyExtraField> fields = new ArrayList<>();
		for (TaxonomyPageExtension extension : taxonomyAccessExtensions) {
			fields.addAll(extension.getTaxonomyExtraFieldsFor(params));
		}
		return fields;
	}

	public boolean hasPageAccess(boolean defaultValue, final Class<? extends BasePresenter> presenterClass, final String params,
			final User user) {
		return pageAccessExtensions.getBooleanValue(defaultValue, new BooleanCaller<PageExtension>() {
			@Override
			public ExtensionBooleanResult call(PageExtension behavior) {
				return behavior.hasPageAccess(presenterClass, params, user);
			}
		});
	}

	public boolean hasRestrictedRecordAccess(boolean defaultValue, final Class<? extends BasePresenter> presenterClass,
			final String params, final User user, final Record restrictedRecord) {
		return pageAccessExtensions.getBooleanValue(defaultValue, new BooleanCaller<PageExtension>() {
			@Override
			public ExtensionBooleanResult call(PageExtension behavior) {
				return behavior.hasRestrictedRecordAccess(presenterClass, params, user, restrictedRecord);
			}
		});
	}

	public boolean canManageSchema(boolean defaultValue, final User user, final MetadataSchemaType schemaType) {
		return schemaTypeAccessExtensions.getBooleanValue(defaultValue, new BooleanCaller<GenericRecordPageExtension>() {
			@Override
			public ExtensionBooleanResult call(GenericRecordPageExtension behavior) {
				return behavior.canManageSchema(user, schemaType);
			}
		});
	}

	public boolean canViewSchemaRecord(boolean defaultValue, final User user, final MetadataSchemaType schemaType,
			final Record restrictedRecord) {
		return schemaTypeAccessExtensions.getBooleanValue(defaultValue, new BooleanCaller<GenericRecordPageExtension>() {
			@Override
			public ExtensionBooleanResult call(GenericRecordPageExtension behavior) {
				return behavior.canViewSchemaRecord(user, schemaType, restrictedRecord);
			}
		});
	}

	public boolean canModifySchemaRecord(boolean defaultValue, final User user, final MetadataSchemaType schemaType,
			final Record restrictedRecord) {
		return schemaTypeAccessExtensions.getBooleanValue(defaultValue, new BooleanCaller<GenericRecordPageExtension>() {
			@Override
			public ExtensionBooleanResult call(GenericRecordPageExtension behavior) {
				return behavior.canModifySchemaRecord(user, schemaType, restrictedRecord);
			}
		});
	}

	public boolean canLogicallyDeleteSchemaRecord(boolean defaultValue, final User user, final MetadataSchemaType schemaType,
			final Record restrictedRecord) {
		return schemaTypeAccessExtensions.getBooleanValue(defaultValue, new BooleanCaller<GenericRecordPageExtension>() {
			@Override
			public ExtensionBooleanResult call(GenericRecordPageExtension behavior) {
				return behavior.canLogicallyDeleteSchemaRecord(user, schemaType, restrictedRecord);
			}
		});
	}

	public boolean isSchemaTypeConfigurable(boolean defaultValue, final MetadataSchemaType schemaType) {
		return schemaTypeAccessExtensions.getBooleanValue(defaultValue, new BooleanCaller<GenericRecordPageExtension>() {
			@Override
			public ExtensionBooleanResult call(GenericRecordPageExtension behavior) {
				return behavior.isSchemaTypeConfigurable(schemaType);
			}
		});
	}

	public boolean canManageTaxonomy(boolean defaultValue, final User user, final Taxonomy taxonomy) {
		return taxonomyAccessExtensions.getBooleanValue(defaultValue, new BooleanCaller<TaxonomyPageExtension>() {
			@Override
			public ExtensionBooleanResult call(TaxonomyPageExtension behavior) {
				return behavior.canManageTaxonomy(user, taxonomy);
			}
		});
	}

	public boolean displayTaxonomy(boolean defaultValue, final User user, final Taxonomy taxonomy) {
		return taxonomyAccessExtensions.getBooleanValue(defaultValue, new BooleanCaller<TaxonomyPageExtension>() {
			@Override
			public ExtensionBooleanResult call(TaxonomyPageExtension behavior) {
				return behavior.displayTaxonomy(user, taxonomy);
			}
		});
	}

}