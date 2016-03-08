package com.constellio.app.modules.rm.model;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.joda.time.LocalDate;

import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.model.enums.DisposalType;
import com.constellio.model.entities.schemas.ModifiableStructure;
import com.constellio.model.entities.schemas.StructureFactory;
import com.constellio.model.services.search.query.logical.criteria.IsContainingTextCriterion;
import com.constellio.model.utils.EnumWithSmallCodeUtils;

public class CopyRetentionRuleFactory implements StructureFactory {

	private static final String NULL = "~null~";
	public static final String VERSION_2 = "version2";
	public static final String VERSION_3 = "version3";

	@Override
	public ModifiableStructure build(String string) {
		StringTokenizer stringTokenizer = new StringTokenizer(string.replace("::", ":~null~:"), ":");
		String versionOrCode = readString(stringTokenizer);
		if (isVersion2(versionOrCode)) {
			return getModifiableStructureV2(stringTokenizer);

		} else if (isVersion3(versionOrCode)) {
			return getModifiableStructureV3(stringTokenizer);

		}
		stringTokenizer = new StringTokenizer(string.replace("::", ":~null~:"), ":");
		ModifiableStructure copyRetentionRuleFactory = getModifiableStructureV1(stringTokenizer);
		String newString = toString(copyRetentionRuleFactory);
		return build(newString);

	}

	private ModifiableStructure getModifiableStructureV2(StringTokenizer stringTokenizer) {
		CopyRetentionRule copyRetentionRule = new CopyRetentionRule();
		copyRetentionRule.setCode(readString(stringTokenizer));
		copyRetentionRule.setCopyType((CopyType) EnumWithSmallCodeUtils.toEnum(CopyType.class, readString(stringTokenizer)));
		copyRetentionRule.setContentTypesComment(readString(stringTokenizer));
		copyRetentionRule.setActiveRetentionPeriod(readRetentionPeriod(stringTokenizer));
		copyRetentionRule.setActiveRetentionComment(readString(stringTokenizer));
		copyRetentionRule.setSemiActiveRetentionPeriod(readRetentionPeriod(stringTokenizer));
		copyRetentionRule.setSemiActiveRetentionComment(readString(stringTokenizer));

		String disposalType = readString(stringTokenizer);
		if (disposalType != null && DisposalType.isValidCode(disposalType)) {
			copyRetentionRule.setInactiveDisposalType(readDisposalType(disposalType));
			copyRetentionRule.setInactiveDisposalComment(readString(stringTokenizer));
		} else {
			copyRetentionRule.setInactiveDisposalType(DisposalType.DESTRUCTION);
			copyRetentionRule.setInactiveDisposalComment(disposalType);
		}
		copyRetentionRule.setDocumentTypeId(readString(stringTokenizer));
		copyRetentionRule.setActiveDateMetadata(readString(stringTokenizer));
		copyRetentionRule.setSemiActiveDateMetadata(readString(stringTokenizer));

		List<String> contentTypesCodes = new ArrayList<>();
		while (stringTokenizer.hasMoreTokens()) {
			contentTypesCodes.add(readString(stringTokenizer));
		}
		copyRetentionRule.setMediumTypeIds(contentTypesCodes);
		copyRetentionRule.dirty = false;

		return copyRetentionRule;
	}

	private ModifiableStructure getModifiableStructureV3(StringTokenizer stringTokenizer) {
		CopyRetentionRule copyRetentionRule = new CopyRetentionRule();
		copyRetentionRule.setCode(readString(stringTokenizer));
		copyRetentionRule.setCopyType((CopyType) EnumWithSmallCodeUtils.toEnum(CopyType.class, readString(stringTokenizer)));
		copyRetentionRule.setContentTypesComment(readString(stringTokenizer));
		copyRetentionRule.setOpenActiveRetentionPeriod(readInteger(stringTokenizer));
		copyRetentionRule.setActiveRetentionPeriod(readRetentionPeriod(stringTokenizer));
		copyRetentionRule.setActiveRetentionComment(readString(stringTokenizer));
		copyRetentionRule.setSemiActiveRetentionPeriod(readRetentionPeriod(stringTokenizer));
		copyRetentionRule.setSemiActiveRetentionComment(readString(stringTokenizer));

		String disposalType = readString(stringTokenizer);
		if (disposalType != null && DisposalType.isValidCode(disposalType)) {
			copyRetentionRule.setInactiveDisposalType(readDisposalType(disposalType));
			copyRetentionRule.setInactiveDisposalComment(readString(stringTokenizer));
		} else {
			copyRetentionRule.setInactiveDisposalType(DisposalType.DESTRUCTION);
			copyRetentionRule.setInactiveDisposalComment(disposalType);
		}
		copyRetentionRule.setDocumentTypeId(readString(stringTokenizer));
		copyRetentionRule.setActiveDateMetadata(readString(stringTokenizer));
		copyRetentionRule.setSemiActiveDateMetadata(readString(stringTokenizer));

		List<String> contentTypesCodes = new ArrayList<>();
		while (stringTokenizer.hasMoreTokens()) {
			contentTypesCodes.add(readString(stringTokenizer));
		}
		copyRetentionRule.setMediumTypeIds(contentTypesCodes);
		copyRetentionRule.dirty = false;

		return copyRetentionRule;
	}

	private ModifiableStructure getModifiableStructureV1(StringTokenizer stringTokenizer) {
		CopyRetentionRule copyRetentionRule = new CopyRetentionRule();
		copyRetentionRule.setCode(readString(stringTokenizer));
		copyRetentionRule.setCopyType((CopyType) EnumWithSmallCodeUtils.toEnum(CopyType.class, readString(stringTokenizer)));
		copyRetentionRule.setContentTypesComment(readString(stringTokenizer));
		copyRetentionRule.setActiveRetentionPeriod(readRetentionPeriod(stringTokenizer));
		copyRetentionRule.setActiveRetentionComment(readString(stringTokenizer));
		copyRetentionRule.setSemiActiveRetentionPeriod(readRetentionPeriod(stringTokenizer));
		copyRetentionRule.setSemiActiveRetentionComment(readString(stringTokenizer));

		String disposalType = readString(stringTokenizer);
		if (disposalType != null && DisposalType.isValidCode(disposalType)) {
			copyRetentionRule.setInactiveDisposalType(readDisposalType(disposalType));
			copyRetentionRule.setInactiveDisposalComment(readString(stringTokenizer));
		} else {
			copyRetentionRule.setInactiveDisposalType(DisposalType.DESTRUCTION);
			copyRetentionRule.setInactiveDisposalComment(disposalType);
		}

		List<String> contentTypesCodes = new ArrayList<>();
		while (stringTokenizer.hasMoreTokens()) {
			contentTypesCodes.add(readString(stringTokenizer));
		}
		copyRetentionRule.setMediumTypeIds(contentTypesCodes);
		copyRetentionRule.dirty = false;
		return copyRetentionRule;
	}

	private boolean isVersion2(String versionOrCode) {
		return VERSION_2.equals(versionOrCode);
	}

	private boolean isVersion3(String versionOrCode) {
		return VERSION_3.equals(versionOrCode);
	}

	private DisposalType readDisposalType(String value) {

		return value == null ? null : (DisposalType) EnumWithSmallCodeUtils.toEnum(DisposalType.class, value);
	}

	@Override
	public String toString(ModifiableStructure structure) {
		CopyRetentionRule rule = (CopyRetentionRule) structure;
		StringBuilder stringBuilder = new StringBuilder();

		writeString(stringBuilder, VERSION_3);
		writeString(stringBuilder, rule.getCode());
		writeString(stringBuilder, rule.getCopyType() == null ? "" : rule.getCopyType().getCode());
		writeString(stringBuilder, rule.getContentTypesComment());
		writeString(stringBuilder, write(rule.getOpenActiveRetentionPeriod()));
		writeString(stringBuilder, write(rule.getActiveRetentionPeriod()));
		writeString(stringBuilder, rule.getActiveRetentionComment());
		writeString(stringBuilder, write(rule.getSemiActiveRetentionPeriod()));
		writeString(stringBuilder, rule.getSemiActiveRetentionComment());
		writeString(stringBuilder, rule.getInactiveDisposalType() == null ? NULL : rule.getInactiveDisposalType().getCode());
		writeString(stringBuilder, rule.getInactiveDisposalComment());

		writeString(stringBuilder, rule.getDocumentTypeId());
		writeString(stringBuilder, rule.getActiveDateMetadata());
		writeString(stringBuilder, rule.getSemiActiveDateMetadata());

		for (String contentTypeCodes : rule.getMediumTypeIds()) {
			writeString(stringBuilder, contentTypeCodes);
		}

		return stringBuilder.toString();
	}

	private String write(Integer value) {
		if (value == null) {
			return NULL;
		} else {
			return "" + value;
		}
	}

	private String write(RetentionPeriod activeRetentionPeriod) {
		if (activeRetentionPeriod == null) {
			return NULL;
		} else {
			String type = activeRetentionPeriod.isVariablePeriod() ? "V" : "F";
			return type + activeRetentionPeriod.getValue();
		}
	}

	private Integer readInteger(StringTokenizer stringTokenizer) {
		String value = stringTokenizer.nextToken();

		if (NULL.equals(value)) {
			return null;
		} else {
			return Integer.valueOf(value);
		}
	}

	private String readString(StringTokenizer stringTokenizer) {
		String value = stringTokenizer.nextToken();

		if (NULL.equals(value)) {
			return null;
		} else {
			return value.replace("~~~", ":");
		}
	}

	private void writeString(StringBuilder stringBuilder, String value) {
		if (stringBuilder.length() != 0) {
			stringBuilder.append(":");
		}
		if (value == null) {
			stringBuilder.append(NULL);
		} else {
			stringBuilder.append(value.replace(":", "~~~"));
		}
	}

	private RetentionPeriod readRetentionPeriod(StringTokenizer stringTokenizer) {
		String value = readString(stringTokenizer);
		if (value == null) {
			return RetentionPeriod.ZERO;

		} else if (value.startsWith("F")) {
			return RetentionPeriod.fixed(Integer.valueOf(value.substring(1)));

		} else if (value.startsWith("V")) {
			return RetentionPeriod.variable(value.substring(1));

		} else {
			return value == null ? null : new RetentionPeriod(Integer.valueOf(value));
		}
	}

	public static IsContainingTextCriterion variablePeriodCode(String code) {
		return new IsContainingTextCriterion(":V" + code + ":");
	}

	private LocalDate readLocalDate(StringTokenizer stringTokenizer) {
		String localDate = readString(stringTokenizer);
		return localDate == null ? null : LocalDate.parse(localDate);
	}

}