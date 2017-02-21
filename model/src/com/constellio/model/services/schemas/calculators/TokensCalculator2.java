package com.constellio.model.services.schemas.calculators;

import static com.constellio.model.entities.security.Role.DELETE;
import static com.constellio.model.entities.security.Role.READ;
import static com.constellio.model.entities.security.Role.WRITE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.HierarchyDependencyValue;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.calculators.dependencies.RecordAuthsDependencyValue;
import com.constellio.model.entities.calculators.dependencies.SpecialDependencies;
import com.constellio.model.entities.calculators.dependencies.SpecialDependency;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.security.Authorization;
import com.constellio.model.services.schemas.builders.CommonMetadataBuilder;

public class TokensCalculator2 implements MetadataValueCalculator<List<String>> {

	SpecialDependency<RecordAuthsDependencyValue> authsParam = SpecialDependencies.RECORD_AUTHS;
	SpecialDependency<HierarchyDependencyValue> hierarchyParam = SpecialDependencies.HIERARCHY;
	LocalDependency<List<String>> manualTokensParam = LocalDependency.toAStringList(CommonMetadataBuilder.MANUAL_TOKENS);

	@Override
	public List<String> calculate(CalculatorParameters parameters) {
		List<String> manualTokens = parameters.get(manualTokensParam);

		Set<String> tokens = new HashSet<>();
		tokens.addAll(manualTokens);

		RecordAuthsDependencyValue auths = parameters.get(authsParam);

		if (!auths.isConcept()) {

			List<Authorization> authorizations = auths.getAuthsOnRecord();
			for (Authorization auth : authorizations) {
				if (auth.getDetail().getRoles().contains(READ)) {
					for (String principal : auth.getGrantedToPrincipals()) {
						tokens.add("r_" + principal);
					}
				}
				if (auth.getDetail().getRoles().contains(WRITE)) {
					for (String principal : auth.getGrantedToPrincipals()) {
						tokens.add("w_" + principal);
					}
				}
				if (auth.getDetail().getRoles().contains(DELETE)) {
					for (String principal : auth.getGrantedToPrincipals()) {
						tokens.add("d_" + principal);
					}
				}

			}

		}

		List<String> sortedTokens = new ArrayList<>(tokens);
		Collections.sort(sortedTokens);

		return sortedTokens;
	}

	@Override
	public List<String> getDefaultValue() {
		return Collections.emptyList();
	}

	@Override
	public MetadataValueType getReturnType() {
		return MetadataValueType.STRING;
	}

	@Override
	public boolean isMultiValue() {
		return true;
	}

	@Override
	public List<? extends Dependency> getDependencies() {
		return Arrays.asList(manualTokensParam, authsParam, hierarchyParam);
	}
}
