package uk.gov.hmcts.ccd.definition.store.domain.service;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.exception.DuplicateUserRoleException;
import uk.gov.hmcts.ccd.definition.store.domain.exception.NotFoundException;
import uk.gov.hmcts.ccd.definition.store.domain.service.response.ServiceResponse;
import uk.gov.hmcts.ccd.definition.store.repository.UserRoleRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.UserRoleEntity;
import uk.gov.hmcts.ccd.definition.store.repository.model.UserRole;
import uk.gov.hmcts.ccd.definition.store.repository.model.UserRoleModelMapper;

import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static uk.gov.hmcts.ccd.definition.store.domain.service.response.SaveOperationEnum.CREATE;
import static uk.gov.hmcts.ccd.definition.store.domain.service.response.SaveOperationEnum.UPDATE;
import static uk.gov.hmcts.ccd.definition.store.repository.model.UserRoleModelMapper.toEntity;
import static uk.gov.hmcts.ccd.definition.store.repository.model.UserRoleModelMapper.toModel;

@Component
public class UserRoleServiceImpl implements UserRoleService {

    private final UserRoleRepository repository;

    UserRoleServiceImpl(final UserRoleRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserRole getRole(final String role) {
        return toModel(repository.findTopByReference(role).orElseThrow(() ->
            new NotFoundException("Role '" + role + "' is not found")));
    }

    @Override
    public ServiceResponse<UserRole> saveRole(final UserRole userRole) {
        final UserRoleEntity entity;
        final boolean roleFound;
        final Optional<UserRoleEntity> searchResult = repository.findTopByReference(userRole.getRole());
        if (searchResult.isPresent()) {
            entity = searchResult.get();
            entity.setSecurityClassification(userRole.getSecurityClassification());
            roleFound = true;
        } else {
            entity = toEntity(userRole);
            roleFound = false;
        }
        return new ServiceResponse<>(toModel(repository.save(entity)), roleFound ? UPDATE : CREATE);
    }

    @Override
    public ServiceResponse<UserRole> createRole(final UserRole userRole) {
        final UserRoleEntity entity;
        final Optional<UserRoleEntity> searchResult = repository.findTopByReference(userRole.getRole().trim());

        if (!searchResult.isPresent()) {
            userRole.setRole(userRole.getRole().trim());
            entity = toEntity(userRole);
            return new ServiceResponse<>(toModel(repository.save(entity)), CREATE);
        } else {
            throw new DuplicateUserRoleException("User role already exists");
        }
    }

    @Override
    public List<UserRole> getRoles(List<String> roles) {
        final List<UserRoleEntity> userRoles = repository.findByReferenceIn(roles);
        return userRoles.stream()
            .map(UserRoleModelMapper::toModel)
            .collect(toList());
    }

    @Override
    public List<UserRole> getRoles() {
        final List<UserRoleEntity> userRoles = repository.findAll();
        return userRoles.stream()
            .map(UserRoleModelMapper::toModel)
            .collect(toList());
    }
}
