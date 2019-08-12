package uk.gov.hmcts.ccd.definition.store.domain;

import static uk.gov.hmcts.ccd.definition.store.domain.AmPersistenceReadSource.FROM_AM;
import static uk.gov.hmcts.ccd.definition.store.domain.AmPersistenceReadSource.FROM_CCD;
import static uk.gov.hmcts.ccd.definition.store.domain.AmPersistenceWriteDestination.TO_AM;
import static uk.gov.hmcts.ccd.definition.store.domain.AmPersistenceWriteDestination.TO_BOTH;
import static uk.gov.hmcts.ccd.definition.store.domain.AmPersistenceWriteDestination.TO_CCD;

import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.InvalidPropertyException;

import javax.inject.Named;
import javax.inject.Singleton;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Named
@Singleton
public class AppConfigBasedAmPersistenceSwitch implements AmPersistenceSwitch {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppConfigBasedAmPersistenceSwitch.class);

    private final Map<String, AmPersistenceWriteDestination> caseTypesToWriteModes = Maps.newHashMap();

    private final Map<String, AmPersistenceReadSource> caseTypesToReadModes = Maps.newHashMap();

    public AppConfigBasedAmPersistenceSwitch(final ApplicationParams appParams) {

        List<String> allForWriteDestinations = new ArrayList<>();
        List<String> duplicateForWriteDestinations = new ArrayList<>();

        mapCaseTypeVsSwitchValueWith(appParams.getCaseTypesWithAmWrittenOnlyToCcd(),
                caseTypesToWriteModes, TO_CCD,
                allForWriteDestinations, duplicateForWriteDestinations);

        mapCaseTypeVsSwitchValueWith(appParams.getCaseTypesWithAmWrittenOnlyToAm(),
                caseTypesToWriteModes, TO_AM,
                allForWriteDestinations, duplicateForWriteDestinations);

        mapCaseTypeVsSwitchValueWith(appParams.getCaseTypesWithAmWrittenToBoth(),
                caseTypesToWriteModes, TO_BOTH,
                allForWriteDestinations, duplicateForWriteDestinations);

        List<String> allForReadSources = new ArrayList<>();
        List<String> duplicateForReadSources = new ArrayList<>();

        mapCaseTypeVsSwitchValueWith(appParams.getCaseTypesWithAmReadFromCcd(), caseTypesToReadModes, FROM_CCD,
                allForReadSources, duplicateForReadSources);

        mapCaseTypeVsSwitchValueWith(appParams.getCaseTypesWithAmReadFromAm(), caseTypesToReadModes, FROM_AM,
                allForReadSources, duplicateForReadSources);
        
        if(!(duplicateForReadSources.isEmpty() && duplicateForWriteDestinations.isEmpty())) {
            LOGGER.error("Duplicate configuration(s) detected for case type(s)!");
            LOGGER.error("Case Types With Duplicate Read Source Configurations:", duplicateForReadSources);
            LOGGER.error("Case Types With Duplicate Write Destination Configurations:", duplicateForWriteDestinations);
            throw new InvalidPropertyException(AppConfigBasedAmPersistenceSwitch.class, "",
                    "Duplicate case type configurations detected for Access Management persistence switches.");
        }

    }

    @Override
    public AmPersistenceWriteDestination getWriteDataSourceFor(String caseType) {
        return caseTypesToWriteModes.getOrDefault(caseType, AmPersistenceWriteDestination.TO_BOTH);
    }

    @Override
    public AmPersistenceReadSource getReadDataSourceFor(String caseType) {
        return caseTypesToReadModes.getOrDefault(caseType, AmPersistenceReadSource.FROM_CCD);
    }


    private <T> void mapCaseTypeVsSwitchValueWith(List<String> caseTypesConfigured, Map<String, T> map, T value,
            List<String> allCaseTypesConfigured, List<String> duplicateCaseTypesConfigured) {
        caseTypesConfigured.forEach(caseType -> {
            if (allCaseTypesConfigured.contains(caseType)) {
                duplicateCaseTypesConfigured.add(caseType);
            }
            else {
                allCaseTypesConfigured.add(caseType);
                map.put(caseType, value);
            }
        });
    }
}
