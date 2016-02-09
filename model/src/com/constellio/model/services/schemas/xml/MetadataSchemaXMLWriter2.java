package com.constellio.model.services.schemas.xml;

import java.util.ArrayList;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;

import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.schemas.AllowedReferences;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataAccessRestriction;
import com.constellio.model.entities.schemas.MetadataPopulateConfigs;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.RegexConfig;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.schemas.entries.CalculatedDataEntry;
import com.constellio.model.entities.schemas.entries.CopiedDataEntry;
import com.constellio.model.entities.schemas.entries.DataEntry;
import com.constellio.model.entities.schemas.entries.DataEntryType;
import com.constellio.model.entities.schemas.validation.RecordMetadataValidator;
import com.constellio.model.entities.schemas.validation.RecordValidator;
import com.constellio.model.services.schemas.builders.ClassListBuilder;
import com.constellio.model.utils.ParametrizedInstanceUtils;

public class MetadataSchemaXMLWriter2 {

	public static final String FORMAT_ATTRIBUTE = "format";
	public static final String FORMAT_VERSION = MetadataSchemaXMLReader2.FORMAT_VERSION;

	public void writeEmptyDocument(String collection, Document document) {
		writeSchemaTypes(new MetadataSchemaTypes(collection, 0, new ArrayList<MetadataSchemaType>(), new ArrayList<String>(),
				new ArrayList<String>()), document);
	}

	public Document write(MetadataSchemaTypes schemaTypes) {
		Document document = new Document();
		writeSchemaTypes(schemaTypes, document);
		document.getRootElement().setAttribute(FORMAT_ATTRIBUTE, FORMAT_VERSION);
		return document;
	}

	private void writeCustomSchemas(MetadataSchemaType schemaType, Element schemaTypeElement, MetadataSchema collectionSchema) {
		Element customSchemasElement = new Element("customSchemas");
		for (MetadataSchema schema : schemaType.getSchemas()) {
			Element schemaElement = toXMLElement(schema, collectionSchema);
			customSchemasElement.addContent(schemaElement);
		}
		schemaTypeElement.addContent(customSchemasElement);
	}

	private void writeDefaultSchema(MetadataSchemaType schemaType, Element schemaTypeElement, MetadataSchema collectionSchema) {
		MetadataSchema defaultSchema = schemaType.getDefaultSchema();
		Element defaultSchemaElement = new Element("defaultSchema");
		defaultSchemaElement.setAttribute("code", "" + defaultSchema.getLocalCode());
		defaultSchemaElement.setAttribute("label", "" + defaultSchema.getLabel());
		for (Metadata metadata : defaultSchema.getMetadatas()) {
			Metadata metadataInCollectionSchema = null;
			if (collectionSchema != null && Schemas.isGlobalMetadata(metadata.getLocalCode())
					&& collectionSchema.hasMetadataWithCode(metadata.getLocalCode())) {
				metadataInCollectionSchema = collectionSchema.getMetadata(metadata.getLocalCode());
			}

			addMetadataToSchema(defaultSchemaElement, metadata, metadataInCollectionSchema);
		}
		if (!defaultSchema.getValidators().isEmpty()) {
			defaultSchemaElement.addContent(writeSchemaValidators(defaultSchema));
		}
		schemaTypeElement.addContent(defaultSchemaElement);
	}

	private Element writeSchemaValidators(MetadataSchema defaultSchema) {
		Element validatorsElement = new Element("validators");
		List<String> validatorsClassNames = new ArrayList<>();
		for (RecordValidator validator : defaultSchema.getValidators()) {
			validatorsClassNames.add(validator.getClass().getName());
		}
		validatorsElement.setAttribute("classNames", toCommaSeparatedString(validatorsClassNames));
		return validatorsElement;
	}

	private Element writeSchemaTypes(MetadataSchemaTypes schemaTypes, Document document) {
		Element schemaTypesElement = new Element("types");
		document.setRootElement(schemaTypesElement);
		schemaTypesElement.setAttribute("version", "" + schemaTypes.getVersion());

		MetadataSchema collectionSchema = null;
		if (schemaTypes.hasType(Collection.SCHEMA_TYPE)) {
			MetadataSchemaType collectionSchemaType = schemaTypes.getSchemaType(Collection.SCHEMA_TYPE);
			schemaTypesElement.addContent(writeSchemaType(collectionSchemaType, null));
			collectionSchema = collectionSchemaType.getDefaultSchema();
		}

		for (MetadataSchemaType schemaType : schemaTypes.getSchemaTypes()) {
			if (!schemaType.getCode().equals(Collection.SCHEMA_TYPE)) {
				schemaTypesElement.addContent(writeSchemaType(schemaType, collectionSchema));
			}
		}

		return schemaTypesElement;
	}

	private Element writeSchemaType(MetadataSchemaType schemaType, MetadataSchema collectionSchema) {
		Element schemaTypeElement = new Element("type");
		schemaTypeElement.setAttribute("code", schemaType.getCode());
		schemaTypeElement.setAttribute("label", schemaType.getLabel());
		if (schemaType.hasSecurity()) {
			schemaTypeElement.setAttribute("security", writeBoolean(schemaType.hasSecurity()));
		}
		schemaTypeElement.setAttribute("inTransactionLog", writeBoolean(schemaType.isInTransactionLog()));
		writeDefaultSchema(schemaType, schemaTypeElement, collectionSchema);
		writeCustomSchemas(schemaType, schemaTypeElement, collectionSchema);
		return schemaTypeElement;
	}

	private Element toXMLElement(MetadataSchema schema, MetadataSchema collectionSchema) {
		Element schemaElement = new Element("schema");
		schemaElement.setAttribute("code", schema.getLocalCode());
		schemaElement.setAttribute("label", schema.getLabel());
		schemaElement.setAttribute("undeletable", writeBoolean(schema.isUndeletable()));
		for (Metadata metadata : schema.getMetadatas()) {
			Metadata globalMetadataInCollectionSchema = null;
			if (collectionSchema != null && Schemas.isGlobalMetadata(metadata.getLocalCode()) && collectionSchema
					.hasMetadataWithCode(metadata.getLocalCode())) {
				globalMetadataInCollectionSchema = collectionSchema.getMetadata(metadata.getLocalCode());
			}

			addMetadataToSchema(schemaElement, metadata, globalMetadataInCollectionSchema);
		}
		if (!schema.getValidators().isEmpty()) {
			schemaElement.addContent(writeSchemaValidators(schema));
		}
		return schemaElement;
	}

	private void addMetadataToSchema(Element schemaElement, Metadata metadata, Metadata globalMetadataInCollectionSchema) {
		ParametrizedInstanceUtils utils = new ParametrizedInstanceUtils();
		Element metadataElement = new Element("m");
		metadataElement.setAttribute("code", metadata.getLocalCode());

		boolean differentFromInheritance;

		String localCode = metadata.getLocalCode();
		boolean notUserDefinedMetadata = !localCode.startsWith("USR");

		if (metadata.inheritDefaultSchema() && !metadata.getSchemaCode().contains("default")) {
			differentFromInheritance = writeMetadataWithInheritance(metadata, metadataElement);
		} else {

			if (globalMetadataInCollectionSchema == null) {
				writeMetadataWithoutInheritance(metadata, utils, metadataElement, notUserDefinedMetadata);
				differentFromInheritance = true;
			} else {
				writeGlobalMetadataWithoutInheritance(metadata, utils, metadataElement,
						notUserDefinedMetadata, globalMetadataInCollectionSchema);
				differentFromInheritance = true;
			}
		}

		if (differentFromInheritance) {
			schemaElement.addContent(metadataElement);
		}
	}

	private void writeMetadataWithoutInheritance(Metadata metadata, ParametrizedInstanceUtils utils, Element metadataElement,
			boolean notUserDefinedMetadata) {
		metadataElement.setAttribute("label", metadata.getLabel());

		if (!metadata.isEnabled()) {
			metadataElement.setAttribute("enabled", writeBoolean(metadata.isEnabled()));
		}
		if (!metadata.isUndeletable()) {
			metadataElement.setAttribute("undeletable", writeBoolean(metadata.isUndeletable()));
		}
		if (metadata.isMultivalue()) {
			metadataElement.setAttribute("multivalue", writeBoolean(metadata.isMultivalue()));
		}
		if (metadata.isSearchable()) {
			metadataElement.setAttribute("searchable", writeBoolean(metadata.isSearchable()));
		}
		if (metadata.isSortable()) {
			metadataElement.setAttribute("sortable", writeBoolean(metadata.isSortable()));
		}
		if (metadata.isSchemaAutocomplete()) {
			metadataElement.setAttribute("schemaAutocomplete", writeBoolean(metadata.isSchemaAutocomplete()));
		}
		if (notUserDefinedMetadata && !metadata.isSystemReserved()) {
			metadataElement.setAttribute("systemReserved", writeBoolean(metadata.isSystemReserved()));
		}
		if (metadata.isEssential()) {
			metadataElement.setAttribute("essential", writeBoolean(metadata.isEssential()));
		}
		if (metadata.isEssentialInSummary()) {
			metadataElement.setAttribute("essentialInSummary", writeBoolean(metadata.isEssentialInSummary()));
		}
		if (metadata.isEncrypted()) {
			metadataElement.setAttribute("encrypted", writeBoolean(metadata.isEncrypted()));
		}
		if (metadata.isChildOfRelationship()) {
			metadataElement.setAttribute("childOfRelationship", writeBoolean(metadata.isChildOfRelationship()));
		}
		if (metadata.isTaxonomyRelationship()) {
			metadataElement.setAttribute("taxonomyRelationship", writeBoolean(metadata.isTaxonomyRelationship()));
		}
		if (metadata.isUniqueValue()) {
			metadataElement.setAttribute("uniqueValue", writeBoolean(metadata.isUniqueValue()));
		}
		if (metadata.isUnmodifiable()) {
			metadataElement.setAttribute("unmodifiable", writeBoolean(metadata.isUnmodifiable()));
		}
		metadataElement.setAttribute("type", metadata.getType().name());
		if (metadata.isDefaultRequirement()) {
			metadataElement.setAttribute("defaultRequirement", writeBoolean(metadata.isDefaultRequirement()));
		}
		if (metadata.inheritDefaultSchema()) {
			metadataElement.setAttribute("inheriting", writeBoolean(metadata.inheritDefaultSchema()));
		}
		if (metadata.getStructureFactory() != null) {
			metadataElement.setAttribute("structureFactory", metadata.getStructureFactory().getClass().getName());
		}
		if (metadata.getEnumClass() != null) {
			metadataElement.setAttribute("enumClass", metadata.getEnumClass().getName());
		}
		if (!metadata.getAccessRestrictions().isEmpty()) {
			metadataElement.addContent(toAccessRestrictionsElement(metadata.getAccessRestrictions()));
		}
		if (metadata.getAllowedReferences() != null) {
			metadataElement.addContent(toRefencesElement(metadata.getAllowedReferences()));
		}
		if (metadata.getDefaultValue() != null) {
			utils.toElement(metadata.getDefaultValue(), metadataElement, "defaultValue");
		}
		if (!metadata.inheritDefaultSchema()) {
			Element dataEntryElement = toDataEntryElement(metadata.getDataEntry());
			if (dataEntryElement != null) {
				metadataElement.addContent(dataEntryElement);
			}
		}
		if (!metadata.getValidators().isEmpty()) {
			metadataElement.addContent(writeRecordMetadataValidators(metadata));
		}
		if (!metadata.getPopulateConfigs().isEmpty()) {
			metadataElement.addContent(toPopulateConfigsElement(metadata.getPopulateConfigs()));
		}
	}

	private boolean writeGlobalMetadataWithoutInheritance(Metadata metadata, ParametrizedInstanceUtils utils,
			Element metadataElement, boolean notUserDefinedMetadata, Metadata globalMetadataInCollection) {

		boolean different = false;
		if (!globalMetadataInCollection.getLabel().equals(metadata.getLabel())) {
			metadataElement.setAttribute("label", metadata.getLabel());
			different = true;
		}

		if (globalMetadataInCollection.isEnabled() != metadata.isEnabled()) {
			metadataElement.setAttribute("enabled", writeBoolean(metadata.isEnabled()));
			different = true;
		}
		if (globalMetadataInCollection.isUndeletable() != metadata.isUndeletable()) {
			metadataElement.setAttribute("undeletable", writeBoolean(metadata.isUndeletable()));
			different = true;
		}
		if (globalMetadataInCollection.isMultivalue() != metadata.isMultivalue()) {
			metadataElement.setAttribute("multivalue", writeBoolean(metadata.isMultivalue()));
			different = true;
		}
		if (globalMetadataInCollection.isSearchable() != metadata.isSearchable()) {
			metadataElement.setAttribute("searchable", writeBoolean(metadata.isSearchable()));
			different = true;
		}
		if (globalMetadataInCollection.isSortable() != metadata.isSortable()) {
			metadataElement.setAttribute("sortable", writeBoolean(metadata.isSortable()));
			different = true;
		}
		if (globalMetadataInCollection.isSchemaAutocomplete() != metadata.isSchemaAutocomplete()) {
			metadataElement.setAttribute("schemaAutocomplete", writeBoolean(metadata.isSchemaAutocomplete()));
			different = true;
		}
		if (globalMetadataInCollection.isSystemReserved() != metadata.isSystemReserved()) {
			metadataElement.setAttribute("systemReserved", writeBoolean(metadata.isSystemReserved()));
			different = true;
		}
		if (globalMetadataInCollection.isEssential() != metadata.isEssential()) {
			metadataElement.setAttribute("essential", writeBoolean(metadata.isEssential()));
			different = true;
		}
		if (globalMetadataInCollection.isEssentialInSummary() != metadata.isEssentialInSummary()) {
			metadataElement.setAttribute("essentialInSummary", writeBoolean(metadata.isEssentialInSummary()));
			different = true;
		}
		if (globalMetadataInCollection.isEncrypted() != metadata.isEncrypted()) {
			metadataElement.setAttribute("encrypted", writeBoolean(metadata.isEncrypted()));
			different = true;
		}
		if (globalMetadataInCollection.isChildOfRelationship() != metadata.isChildOfRelationship()) {
			metadataElement.setAttribute("childOfRelationship", writeBoolean(metadata.isChildOfRelationship()));
			different = true;
		}
		if (globalMetadataInCollection.isTaxonomyRelationship() != metadata.isTaxonomyRelationship()) {
			metadataElement.setAttribute("taxonomyRelationship", writeBoolean(metadata.isTaxonomyRelationship()));
			different = true;
		}
		if (globalMetadataInCollection.isUniqueValue() != metadata.isUniqueValue()) {
			metadataElement.setAttribute("uniqueValue", writeBoolean(metadata.isUniqueValue()));
			different = true;
		}
		if (globalMetadataInCollection.isUnmodifiable() != metadata.isUnmodifiable()) {
			metadataElement.setAttribute("unmodifiable", writeBoolean(metadata.isUnmodifiable()));
			different = true;
		}
		if (metadata.getType() != globalMetadataInCollection.getType()) {
			metadataElement.setAttribute("type", metadata.getType().name());
			different = true;
		}

		if (globalMetadataInCollection.isDefaultRequirement() != metadata.isDefaultRequirement()) {
			metadataElement.setAttribute("defaultRequirement", writeBoolean(metadata.isDefaultRequirement()));
			different = true;
		}
		if (metadata.getStructureFactory() != null && !metadata.getStructureFactory().getClass()
				.equals(globalMetadataInCollection.getStructureFactory().getClass())) {
			metadataElement.setAttribute("structureFactory", metadata.getStructureFactory().getClass().getName());
			different = true;
		}
		if (metadata.getEnumClass() != null && !metadata.getEnumClass()
				.equals(globalMetadataInCollection.getEnumClass())) {
			metadataElement.setAttribute("enumClass", metadata.getEnumClass().getName());
			different = true;
		}
		if (metadata.getAccessRestrictions() != null
				&& !metadata.getAccessRestrictions().equals(globalMetadataInCollection.getAccessRestrictions())) {
			metadataElement.addContent(toAccessRestrictionsElement(metadata.getAccessRestrictions()));
			different = true;
		}
		if (metadata.getAllowedReferences() != null
				&& !metadata.getAllowedReferences().equals(globalMetadataInCollection.getAccessRestrictions())) {
			metadataElement.addContent(toRefencesElement(metadata.getAllowedReferences()));
			different = true;
		}
		if (metadata.getDefaultValue() != null
				&& !metadata.getDefaultValue().equals(globalMetadataInCollection.getDefaultValue())) {
			utils.toElement(metadata.getDefaultValue(), metadataElement, "defaultValue");
			different = true;
		}
		if (!metadata.getDataEntry().equals(globalMetadataInCollection.getDataEntry())) {
			Element dataEntryElement = toDataEntryElement(metadata.getDataEntry());
			if (dataEntryElement != null) {
				metadataElement.addContent(dataEntryElement);
				different = true;
			}

		}
		if (metadata.getValidators() != null
				&& !ClassListBuilder.isSameValues(metadata.getValidators(), globalMetadataInCollection.getValidators())) {
			metadataElement.addContent(writeRecordMetadataValidators(metadata));
			different = true;
		}
		if (!metadata.getPopulateConfigs().isEmpty()) {
			metadataElement.addContent(toPopulateConfigsElement(metadata.getPopulateConfigs()));
			different = true;
		}

		return different;
	}

	private boolean writeMetadataWithInheritance(Metadata metadata, Element metadataElement) {
		boolean differentFromInheritance = false;
		if (metadata.getInheritance().isDefaultRequirement() != metadata.isDefaultRequirement()) {
			metadataElement.setAttribute("defaultRequirement", writeBoolean(metadata.isDefaultRequirement()));
			differentFromInheritance = true;
		}
		if (metadata.getInheritance().isEnabled() != metadata.isEnabled()) {
			metadataElement.setAttribute("enabled", writeBoolean(metadata.isEnabled()));
			differentFromInheritance = true;
		}
		if (!metadata.getValidators().equals(metadata.getInheritance().getValidators())) {
			metadataElement.addContent(writeRecordMetadataValidators(metadata));
			differentFromInheritance = true;
		}
		if (!metadata.getPopulateConfigs().equals(metadata.getInheritance().getPopulateConfigs())) {
			metadataElement.addContent(toPopulateConfigsElement(metadata.getPopulateConfigs()));
			differentFromInheritance = true;
		}
		if (metadata.getLabel() != null && !metadata.getLabel().equals(metadata.getInheritance().getLabel())) {
			metadataElement.setAttribute("label", metadata.getLabel());
			differentFromInheritance = true;
		}
		return differentFromInheritance;
	}

	private String writeBoolean(boolean enabled) {
		return enabled ? "t" : "f";
	}

	private Element toPopulateConfigsElement(MetadataPopulateConfigs populateConfigs) {
		Element populateConfigsElement = new Element("populateConfigs");

		if (!populateConfigs.getStyles().isEmpty()) {
			populateConfigsElement.setAttribute("styles", toCommaSeparatedString(populateConfigs.getStyles()));
		}
		if (!populateConfigs.getProperties().isEmpty()) {
			populateConfigsElement.setAttribute("properties", toCommaSeparatedString(populateConfigs.getProperties()));
		}

		Element regexConfigsElement = new Element("regexConfigs");
		for (RegexConfig regexConfig : populateConfigs.getRegexes()) {
			regexConfigsElement.addContent(toRegexElement(regexConfig));
		}
		if (!populateConfigs.getRegexes().isEmpty()) {
			populateConfigsElement.addContent(regexConfigsElement);
		}
		return populateConfigsElement;
	}

	private Element toRegexElement(RegexConfig regexConfig) {
		Element regexConfigElement = new Element("regexConfig");

		regexConfigElement.setAttribute("metadataInput", regexConfig.getInputMetadata());
		regexConfigElement.setAttribute("regex", regexConfig.getRegex().pattern());
		regexConfigElement.setAttribute("value", regexConfig.getValue());
		regexConfigElement.setAttribute("regexConfigType", "" + regexConfig.getRegexConfigType());

		return regexConfigElement;
	}

	private Element toAccessRestrictionsElement(MetadataAccessRestriction accessRestrictions) {
		Element accessRestrictionsElement = new Element("accessRestrictions");

		if (!accessRestrictions.getRequiredReadRoles().isEmpty()) {
			accessRestrictionsElement
					.setAttribute("readAccessRestrictions", toCommaSeparatedString(accessRestrictions.getRequiredReadRoles()));
		}
		if (!accessRestrictions.getRequiredWriteRoles().isEmpty()) {
			accessRestrictionsElement
					.setAttribute("writeAccessRestrictions", toCommaSeparatedString(accessRestrictions.getRequiredWriteRoles()));
		}
		if (!accessRestrictions.getRequiredDeleteRoles().isEmpty()) {
			accessRestrictionsElement
					.setAttribute("deleteAccessRestrictions",
							toCommaSeparatedString(accessRestrictions.getRequiredDeleteRoles()));
		}
		if (!accessRestrictions.getRequiredModificationRoles().isEmpty()) {
			accessRestrictionsElement
					.setAttribute("modifyAccessRestrictions",
							toCommaSeparatedString(accessRestrictions.getRequiredModificationRoles()));
		}
		return accessRestrictionsElement;
	}

	private Element writeRecordMetadataValidators(Metadata metadata) {
		Element validatorsElement = new Element("validators");
		List<String> validatorsClassNames = new ArrayList<>();
		for (RecordMetadataValidator<?> validator : metadata.getValidators()) {
			validatorsClassNames.add(validator.getClass().getName());
		}
		validatorsElement.setAttribute("classNames", toCommaSeparatedString(validatorsClassNames));
		return validatorsElement;
	}

	private Element toRefencesElement(AllowedReferences allowedReferences) {
		Element references = new Element("references");
		if (allowedReferences.getAllowedSchemas().isEmpty()) {
			references.setAttribute("schemaType", allowedReferences.getAllowedSchemaType());
		} else {
			references.setAttribute("schemas", toCommaSeparatedString(allowedReferences.getAllowedSchemas()));
		}
		return references;
	}

	private String toCommaSeparatedString(java.util.Collection<String> values) {
		StringBuffer stringBuffer = new StringBuffer();
		for (String value : values) {
			stringBuffer.append(value).append(",");
		}
		if (!values.isEmpty()) {
			stringBuffer.deleteCharAt(stringBuffer.lastIndexOf(","));
		}
		return stringBuffer.toString();
	}

	private Element toDataEntryElement(DataEntry dataEntryValue) {
		if (dataEntryValue.getType() == DataEntryType.MANUAL) {
			return null;
		}
		Element dataEntry = new Element("dataEntry");
		if (dataEntryValue.getType() == DataEntryType.COPIED) {
			CopiedDataEntry copiedDataEntry = (CopiedDataEntry) dataEntryValue;
			dataEntry.setAttribute("copied", copiedDataEntry.getCopiedMetadata());
			dataEntry.setAttribute("reference", copiedDataEntry.getReferenceMetadata());

		} else if (dataEntryValue.getType() == DataEntryType.CALCULATED) {
			CalculatedDataEntry calculatedDataEntry = (CalculatedDataEntry) dataEntryValue;
			dataEntry.setAttribute("calculator", calculatedDataEntry.getCalculator().getClass().getName());
		}

		return dataEntry;
	}
}