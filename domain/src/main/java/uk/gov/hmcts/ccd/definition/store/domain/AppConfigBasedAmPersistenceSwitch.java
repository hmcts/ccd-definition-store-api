package uk.gov.hmcts.ccd.definition.store.domain;

import static uk.gov.hmcts.ccd.definition.store.domain.AmPersistenceReadSource.FROM_AM;
import static uk.gov.hmcts.ccd.definition.store.domain.AmPersistenceReadSource.FROM_CCD;
import static uk.gov.hmcts.ccd.definition.store.domain.AmPersistenceWriteDestination.TO_AM;
import static uk.gov.hmcts.ccd.definition.store.domain.AmPersistenceWriteDestination.TO_BOTH;
import static uk.gov.hmcts.ccd.definition.store.domain.AmPersistenceWriteDestination.TO_CCD;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Maps;

import javax.inject.Named;
import javax.inject.Singleton;

import java.util.List;
import java.util.Map;

@Named
@Singleton
public class AppConfigBasedAmPersistenceSwitch implements AmPersistenceSwitch {

    private final Map<String, AmPersistenceWriteDestination> caseTypesToWriteModes = Maps.newHashMap();

    private final Map<String, AmPersistenceReadSource> caseTypesToReadModes = Maps.newHashMap();

    public AppConfigBasedAmPersistenceSwitch(final ApplicationParams appParams) {

        mapCaseTypeVsSwitchValueWith(appParams.getCaseTypesWithAmWrittenOnlyToCcd(),
                caseTypesToWriteModes, TO_CCD);

        mapCaseTypeVsSwitchValueWith(appParams.getCaseTypesWithAmWrittenOnlyToAm(),
                caseTypesToWriteModes, TO_AM);

        mapCaseTypeVsSwitchValueWith(appParams.getCaseTypesWithAmWrittenToBoth(),
                caseTypesToWriteModes, TO_BOTH);

        mapCaseTypeVsSwitchValueWith(appParams.getCaseTypesWithAmReadFromCcd(), caseTypesToReadModes, FROM_CCD);

        mapCaseTypeVsSwitchValueWith(appParams.getCaseTypesWithAmReadFromAm(), caseTypesToReadModes, FROM_AM);

    }

    @Override
    public AmPersistenceWriteDestination getWriteDataSourceFor(String caseType) {
        return caseTypesToWriteModes.getOrDefault(caseType.toUpperCase(), AmPersistenceWriteDestination.TO_CCD);
    }

    @Override
    public AmPersistenceReadSource getReadDataSourceFor(String caseType) {
        return caseTypesToReadModes.getOrDefault(caseType.toUpperCase(), AmPersistenceReadSource.FROM_CCD);
    }

    private <T> void mapCaseTypeVsSwitchValueWith(List<String> caseTypesConfigured, Map<String, T> map, T value) {
        caseTypesConfigured.forEach(caseType -> {
            if (!StringUtils.isEmpty(caseType)) {
                map.put(caseType.toUpperCase(), value);
            }
        });
    }
}
