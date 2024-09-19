package uk.gov.hmcts.ccd.definition.store.domain.service;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.ccd.definition.store.domain.exception.DuplicateUserRoleException;
import uk.gov.hmcts.ccd.definition.store.domain.exception.NotFoundException;
import uk.gov.hmcts.ccd.definition.store.domain.service.response.ServiceResponse;
import uk.gov.hmcts.ccd.definition.store.repository.AccessProfileRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.AccessProfileEntity;
import uk.gov.hmcts.ccd.definition.store.repository.model.UserRole;
import uk.gov.hmcts.ccd.definition.store.repository.model.UserRoleModelMapper;

import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static uk.gov.hmcts.ccd.definition.store.domain.service.response.SaveOperationEnum.CREATE;
import static uk.gov.hmcts.ccd.definition.store.domain.service.response.SaveOperationEnum.UPDATE;
import static uk.gov.hmcts.ccd.definition.store.repository.model.UserRoleModelMapper.toEntity;
import static uk.gov.hmcts.ccd.definition.store.repository.model.UserRoleModelMapper.toModel;

/**
 * RDM-10539 - The UserRoles will not be changed in this class as it is only used in the UserRole controller which will
 * soon possibly be out of commission in the future.
 */
@Component
public class AccessProfileServiceImpl implements AccessProfileService {

    private final AccessProfileRepository repository;

    AccessProfileServiceImpl(final AccessProfileRepository repository) {
        this.repository = repository;
    }

    @Transactional
    @Override
    public UserRole getRole(final String role) {
        return toModel(repository.findTopByReference(role).orElseThrow(() ->
            new NotFoundException("Role '" + role + "' is not found")));
    }

    @Transactional
    @Override
    public ServiceResponse<UserRole> saveRole(final UserRole userRole) {
        final AccessProfileEntity entity;
        final boolean roleFound;
        final Optional<AccessProfileEntity> searchResult = repository.findTopByReference(userRole.getRole());
        if (searchResult.isPresent()) {
            entity = searchResult.get();
            entity.setSecurityClassification(userRole.getSecurityClassification());
            roleFound = true;
        } else {
            entity = toEntity(userRole);
            roleFound = false;
        }
        return new ServiceResponse<>(toModel(repository.saveAndFlush(entity)), roleFound ? UPDATE : CREATE);
    }

    @Transactional
    @Override
    public ServiceResponse<UserRole> createRole(final UserRole userRole) {
        final AccessProfileEntity entity;
        final Optional<AccessProfileEntity> searchResult = repository.findTopByReference(userRole.getRole().trim());

        if (!searchResult.isPresent()) {
            userRole.setRole(userRole.getRole().trim());
            entity = toEntity(userRole);
            return new ServiceResponse<>(toModel(repository.saveAndFlush(entity)), CREATE);
        } else {
            throw new DuplicateUserRoleException("User role already exists");
        }
    }

    @Transactional
    @Override
    public List<UserRole> getRoles(List<String> roles) {
        final List<AccessProfileEntity> accessProfiles = repository.findByReferenceIn(roles);
        return accessProfiles.stream()
            .map(UserRoleModelMapper::toModel)
            .collect(toList());
    }

    @Transactional
    @Override
    public List<UserRole> getRoles() {
        final List<AccessProfileEntity> accessProfiles = repository.findAll();
        return accessProfiles.stream()
            .map(UserRoleModelMapper::toModel)
            .collect(toList());
    }
}
