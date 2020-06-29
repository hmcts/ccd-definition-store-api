package uk.gov.hmcts.ccd.definition.store.excel.parser;

import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.MapperException;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.repository.model.WorkBasketUserDefault;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName.*;

public class UserProfilesParser {

    public List<WorkBasketUserDefault> parse(Map<String, DefinitionSheet> sheets) {

        DefinitionSheet userProfileSheet = Optional.ofNullable(sheets.get(SheetName.USER_PROFILE.getName()))
            .orElseThrow(() -> new MapperException("A definition must contain a UserProfile worksheet"));

        Stream<Optional<WorkBasketUserDefault>> optionalProfiles = userProfileSheet.getDataItems()
            .stream()
            .map(this::parseUserProfile);

        return optionalProfiles.filter(Optional::isPresent).map(Optional::get).collect(toList());
    }

    private Optional<WorkBasketUserDefault> parseUserProfile(DefinitionDataItem item) {

        String id = item.getString(USER_IDAM_ID);
        String workBasketDefaultJurisdiction = item.getString(WORK_BASKET_DEFAULT_JURISDICTION);
        String workBasketDefaultCaseType = item.getString(WORK_BASKET_DEFAULT_CASETYPE);
        String workBasketDefaultState = item.getString(WORK_BASKET_DEFAULT_STATE);

        if (Arrays.asList(id, workBasketDefaultCaseType, workBasketDefaultJurisdiction, workBasketDefaultState)
            .stream()
            .anyMatch(StringUtils::isBlank)) {
            return Optional.empty();
        }

        WorkBasketUserDefault workBasketUserDefault = new WorkBasketUserDefault();
        workBasketUserDefault.setUserIdamId(id);
        workBasketUserDefault.setWorkBasketDefaultCaseType(workBasketDefaultCaseType);
        workBasketUserDefault.setWorkBasketDefaultJurisdiction(workBasketDefaultJurisdiction);
        workBasketUserDefault.setWorkBasketDefaultState(workBasketDefaultState);

        return Optional.of(workBasketUserDefault);
    }
}

