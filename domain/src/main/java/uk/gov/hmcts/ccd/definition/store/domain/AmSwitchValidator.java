package uk.gov.hmcts.ccd.definition.store.domain;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.InvalidPropertyException;

import java.util.ArrayList;
import java.util.List;

public class AmSwitchValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(AmSwitchValidator.class);

    public void validateAmPersistenceSwitchesIn(ApplicationParams appParams) {
        List<String> duplicateForWriteDestinations = new ArrayList<>();
        List<String> allForWriteDestinations = new ArrayList<>();

        findAndStoreDuplicates(appParams.getCaseTypesWithAmWrittenOnlyToCcd(), allForWriteDestinations,
            duplicateForWriteDestinations);

        findAndStoreDuplicates(appParams.getCaseTypesWithAmWrittenOnlyToAm(), allForWriteDestinations,
            duplicateForWriteDestinations);

        findAndStoreDuplicates(appParams.getCaseTypesWithAmWrittenToBoth(), allForWriteDestinations,
            duplicateForWriteDestinations);

        List<String> duplicateForReadSources = new ArrayList<>();
        List<String> allForReadSources = new ArrayList<>();

        findAndStoreDuplicates(appParams.getCaseTypesWithAmReadFromCcd(), allForReadSources, duplicateForReadSources);

        findAndStoreDuplicates(appParams.getCaseTypesWithAmReadFromAm(), allForReadSources, duplicateForReadSources);

        if (!(duplicateForReadSources.isEmpty() && duplicateForWriteDestinations.isEmpty())) {
            LOGGER.error("Duplicate configuration(s) detected for case type(s)!");
            LOGGER.error("{} Case Types With Duplicate Read Source Configurations: {}", duplicateForReadSources.size(),
                duplicateForReadSources);
            LOGGER.error("{} Case Types With Duplicate Write Destination Configurations: {}",
                duplicateForWriteDestinations.size(), duplicateForWriteDestinations);
            throw new InvalidPropertyException(ApplicationParams.class,
                "properties mapped from ccd.am.read.* and ccd.am.write.*",
                "Duplicate case type configurations detected for Access Management persistence switches.");
        }
    }

    private <T> void findAndStoreDuplicates(List<String> caseTypes, List<String> allCaseTypesConfigured,
                                            List<String> duplicateCaseTypesConfigured) {
        caseTypes.forEach(caseType -> {
            if (!StringUtils.isEmpty(caseType)) {
                if (allCaseTypesConfigured.contains(caseType.toUpperCase())) {
                    duplicateCaseTypesConfigured.add(caseType.toUpperCase());
                } else {
                    allCaseTypesConfigured.add(caseType.toUpperCase());
                }
            }
        });
    }
}
