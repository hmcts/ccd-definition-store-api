package uk.gov.hmcts.ccd.definition.store.excel.service;

import java.util.Set;

public interface RoleToAccessProfileMappingService {

    String createAccessProfileMapping(Set<String> caseTypeIds);
}
