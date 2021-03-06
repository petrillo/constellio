package com.constellio.model.entities.schemas;

import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

public class ModificationImpact {

	final MetadataSchemaType impactedSchemaType;
	final List<Metadata> metadataToReindex;
	final LogicalSearchCondition logicalSearchCondition;
	final int potentialImpactsCount;

	public ModificationImpact(MetadataSchemaType impactedSchemaType, List<Metadata> metadataToReindex,
			LogicalSearchCondition logicalSearchCondition, int potentialImpactsCount) {
		this.impactedSchemaType = impactedSchemaType;
		this.metadataToReindex = metadataToReindex;
		this.logicalSearchCondition = logicalSearchCondition;
		this.potentialImpactsCount = potentialImpactsCount;

		if (logicalSearchCondition == null) {
			throw new RuntimeException("logicalSearchCondition required");
		}
	}

	public List<Metadata> getMetadataToReindex() {
		return metadataToReindex;
	}

	public LogicalSearchCondition getLogicalSearchCondition() {
		return logicalSearchCondition;
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	public MetadataSchemaType getImpactedSchemaType() {
		return impactedSchemaType;
	}

	public int getPotentialImpactsCount() {
		return potentialImpactsCount;
	}

	@Override
	public String toString() {
		return "ModificationImpact{" +
				"impactedSchemaType=" + impactedSchemaType +
				", metadataToReindex=" + metadataToReindex +
				", logicalSearchCondition=" + logicalSearchCondition +
				", potentialImpactsCount=" + potentialImpactsCount +
				'}';
	}
}
