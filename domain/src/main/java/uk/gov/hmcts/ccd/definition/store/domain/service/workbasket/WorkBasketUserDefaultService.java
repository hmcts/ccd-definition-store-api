package uk.gov.hmcts.ccd.definition.store.domain.service.workbasket;

import org.hibernate.service.spi.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.ccd.definition.store.domain.ApplicationParams;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationException;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.domain.validation.userprofile.UserProfileValidator;
import uk.gov.hmcts.ccd.definition.store.repository.SecurityUtils;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionEntity;
import uk.gov.hmcts.ccd.definition.store.repository.model.WorkBasketUserDefault;

import javax.inject.Inject;
import java.util.List;

@Service
public class WorkBasketUserDefaultService {

    private static final Logger LOG = LoggerFactory.getLogger(WorkBasketUserDefaultService.class);
    private final ApplicationParams applicationParams;
    private final SecurityUtils securityUtils;
    private final UserProfileValidator userProfileValidator;
    private final RestTemplate restTemplate;

    @Inject
    public WorkBasketUserDefaultService(final ApplicationParams applicationParams,
                                        final SecurityUtils securityUtils,
                                        final UserProfileValidator userProfileValidator,
                                        final RestTemplate restTemplate) {
        this.applicationParams = applicationParams;
        this.securityUtils = securityUtils;
        this.userProfileValidator = userProfileValidator;
        this.restTemplate = restTemplate;
    }

    public void saveWorkBasketUserDefaults(final List<WorkBasketUserDefault> workBasketUserDefaults,
                                           final JurisdictionEntity jurisdiction,
                                           final List<CaseTypeEntity> caseTypes,
                                           final String actionedBy) {
        final ValidationResult userDefaultsValidationResults = userProfileValidator.validate(workBasketUserDefaults,
            jurisdiction,
            caseTypes);
        if (userDefaultsValidationResults.isValid()) {
            putUserProfiles(workBasketUserDefaults, actionedBy);
        } else {
            throw new ValidationException(userDefaultsValidationResults);
        }
    }

    private void putUserProfiles(final List<WorkBasketUserDefault> workBasketUserDefaults, final String actionedBy) {
        try {
            final HttpHeaders headers = securityUtils.authorizationHeaders();
            headers.add("actionedBy", actionedBy);
            final HttpEntity<List<WorkBasketUserDefault>> requestEntity = new HttpEntity<>(workBasketUserDefaults,
                headers);
            LOG.info("Updating user profile- URL {}", applicationParams.userProfilePutURL());
            restTemplate.exchange(applicationParams.userProfilePutURL(), HttpMethod.PUT, requestEntity, String.class);

        } catch (Exception e) {
            LOG.error("Problem updating user profile: {} because of ", workBasketUserDefaults, e);
            throw new ServiceException(String.format("Problem updating user profile: '%s' because of '%s'",
                workBasketUserDefaults,
                e.getMessage()), e);
        }
    }
}
