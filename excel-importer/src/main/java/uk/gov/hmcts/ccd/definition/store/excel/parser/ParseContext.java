package uk.gov.hmcts.ccd.definition.store.excel.parser;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.collections4.map.MultiKeyMap;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.MapperException;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseRoleEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.StateEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.UserRoleEntity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import static java.util.stream.Collectors.toMap;

/**
 * Accumulate everything that has been parsed so far and which is required for a subsequent parse stage. This is not meant
 * to expose the result of the parse itself, but instead to facilitate the mapping of dependent elements parsed at different
 * stages. As such, it should focus on the ease of consumption by parser.
 *
 * To expose the results of a parse stage, use {@link ParseResult} instead.
 */
public class ParseContext {

    private JurisdictionEntity jurisdiction;
    private final Map<String, FieldTypeEntity> baseTypes = Maps.newHashMap();
    private final Map<String, FieldTypeEntity> allTypes = Maps.newHashMap();
    private final Set<CaseTypeEntity> caseTypes = Sets.newHashSet();
    private final Map<String, UserRoleEntity> userRoles = Maps.newHashMap();
    private final MultiKeyMap<String, CaseRoleEntity> caseRoles = new MultiKeyMap();

    /**
     * Accumulate Field types by case type and field IDs for subsequent linking to case fields.
     */
    private final Map<String, Map<String, FieldTypeEntity>> caseFieldTypes = Maps.newHashMap();

    /**
     * Accumulate States by case type and state ID for subsequent linking to event states.
     */
    private final Map<String, Map<String, StateEntity>> statesByCaseTypes = Maps.newHashMap();

    /**
     * Accumulate Case fields by case type and field ID for subsequent linking to event case fields.
     */
    private final Map<String, Map<String, CaseFieldEntity>> caseFieldByCaseTypes = Maps.newHashMap();

    /**
     * Accumulate Events by case type and event ID for subsequent linking to events.
     */
    private final Map<String, Map<String, EventEntity>> eventsByCaseTypes = Maps.newHashMap();

    /**
     * Accumulate CaseRoles by case type and case role ID for subsequent linking to event states.
     */
    private final Map<String, Map<String, CaseRoleEntity>> caseRolesByCaseTypes = Maps.newHashMap();

    /**
     * Store metadata fields for linking to layouts
     */
    private final Map<String, CaseFieldEntity> metadataFields = new HashMap<>();

    /**
     * Missing User Roles.
     */
    private final Set<String> missingUserRoles = new HashSet<>();

    public JurisdictionEntity getJurisdiction() {
        return jurisdiction;
    }

    public void setJurisdiction(JurisdictionEntity jurisdiction) {
        this.jurisdiction = jurisdiction;
    }

    public void addBaseTypes(List<FieldTypeEntity> baseTypes) {
        for (FieldTypeEntity baseType : baseTypes) {
            this.baseTypes.put(baseType.getReference(), baseType);
            addToAllTypes(baseType);
        }
    }

    public void addToAllTypes(FieldTypeEntity fieldType) {
        allTypes.put(fieldType.getReference(), fieldType);
    }

    public void addToAllTypes(List<FieldTypeEntity> fieldTypes) {
        for (FieldTypeEntity fieldType : fieldTypes) {
            addToAllTypes(fieldType);
        }
    }

    public Set<CaseTypeEntity> getCaseTypes() {
        return caseTypes;
    }

    public ParseContext registerCaseType(CaseTypeEntity caseType) {
        this.caseTypes.add(caseType);
        return this;
    }

    public ParseContext registerCaseFieldType(String caseTypeId, String fieldId, FieldTypeEntity fieldType) {
        if (!caseFieldTypes.containsKey(caseTypeId)) {
            caseFieldTypes.put(caseTypeId, Maps.newHashMap());
        }

        final Map<String, FieldTypeEntity> caseTypeFields = caseFieldTypes.get(caseTypeId);

        if (caseTypeFields.containsKey(fieldId)) {
            throw new SpreadsheetParsingException("Type already registered for field: " + fieldId);
        }

        caseTypeFields.put(fieldId, fieldType);

        return this;
    }

    public FieldTypeEntity getCaseFieldType(String caseTypeId, String caseFieldId) {
        final Map<String, FieldTypeEntity> caseType = caseFieldTypes.get(caseTypeId);

        if (null == caseType) {
            throw new SpreadsheetParsingException("No types registered for case type: " + caseTypeId);
        }

        final FieldTypeEntity fieldType = caseType.get(caseFieldId);

        if (null == fieldType) {
            throw new SpreadsheetParsingException(
                String.format("No types registered for case field ID: %s/%s", caseTypeId, caseFieldId));
        }

        return fieldType;
    }

    public ParseContext registerStateForCaseType(String caseTypeId, StateEntity state) {
        if (!statesByCaseTypes.containsKey(caseTypeId)) {
            statesByCaseTypes.put(caseTypeId, Maps.newHashMap());
        }

        final Map<String, StateEntity> caseTypeStates = statesByCaseTypes.get(caseTypeId);

        if (caseTypeStates.containsKey(state.getReference())) {
            throw new SpreadsheetParsingException("State already registered for ID: " + state.getReference());
        }

        caseTypeStates.put(state.getReference(), state);

        return this;
    }

    public StateEntity getStateForCaseType(String caseTypeId, String stateId) {
        final Map<String, StateEntity> caseTypeStates = statesByCaseTypes.get(caseTypeId);

        if (null == caseTypeStates) {
            throw new SpreadsheetParsingException("No states registered for case type: " + caseTypeId);
        }

        final StateEntity state = caseTypeStates.get(stateId);

        if (null == state) {
            throw new SpreadsheetParsingException(
                String.format("No state registered for state ID: %s/%s", caseTypeId, stateId));
        }

        return state;
    }

    public ParseContext registerCaseRoleForCaseType(String caseTypeId, CaseRoleEntity caseRole) {
        if (!caseRolesByCaseTypes.containsKey(caseTypeId)) {
            caseRolesByCaseTypes.put(caseTypeId, Maps.newHashMap());
        }
        final Map<String, CaseRoleEntity> caseTypeCaseRoles = caseRolesByCaseTypes.get(caseTypeId);
        caseTypeCaseRoles.put(caseRole.getReference(), caseRole);
        return this;
    }

    public CaseRoleEntity getCaseRoleForCaseType(String caseTypeId, String caseRoleId) {
        final Map<String, CaseRoleEntity> caseTypeCaseRoles = caseRolesByCaseTypes.get(caseTypeId);

        if (null == caseTypeCaseRoles) {
            throw new SpreadsheetParsingException("No case roles registered for case type: " + caseTypeId);
        }

        final CaseRoleEntity caseRole = caseTypeCaseRoles.get(caseRoleId);

        if (null == caseRole) {
            throw new SpreadsheetParsingException(
                String.format("No case role registered for case role ID: %s/%s", caseTypeId, caseRoleId));
        }

        return caseRole;
    }

    public ParseContext registerCaseFieldForCaseType(String caseTypeId, CaseFieldEntity caseField) {
        if (!caseFieldByCaseTypes.containsKey(caseTypeId)) {
            caseFieldByCaseTypes.put(caseTypeId, Maps.newHashMap());
        }

        final Map<String, CaseFieldEntity> caseTypeFields = caseFieldByCaseTypes.get(caseTypeId);

        if (caseTypeFields.containsKey(caseField.getReference())) {
            throw new SpreadsheetParsingException("Case field already registered for ID: " + caseField.getReference());
        }

        caseTypeFields.put(caseField.getReference(), caseField);

        return this;
    }

    public CaseFieldEntity getCaseFieldForCaseType(String caseTypeId, String caseFieldId) {
        final Map<String, CaseFieldEntity> caseTypeFields = caseFieldByCaseTypes.get(caseTypeId);

        if (null == caseTypeFields) {
            throw new SpreadsheetParsingException("No case fields registered for case type: " + caseTypeId);
        }

        final CaseFieldEntity caseField = caseTypeFields.getOrDefault(caseFieldId, metadataFields.get(caseFieldId));

        if (null == caseField) {
            throw new SpreadsheetParsingException(
                String.format("Unknown field %s for case type %s", caseFieldId, caseTypeId));
        }

        return caseField;
    }

    public Optional<FieldTypeEntity> getType(String reference) {
        return Optional.ofNullable(allTypes.get(reference));
    }

    public Optional<FieldTypeEntity> getBaseType(String reference) {
        return Optional.ofNullable(baseTypes.get(reference));
    }

    public Optional<UserRoleEntity> getIdamRole(final String role) {
        return Optional.ofNullable(userRoles.get(role));
    }

    public Optional<UserRoleEntity> getRole(String caseType, final String role) {
        Optional<UserRoleEntity> userRoleEntity = Optional.ofNullable(userRoles.get(role));
        if (userRoleEntity.isPresent()) {
            return userRoleEntity;
        } else {
            return Optional.ofNullable(caseRoles.get(caseType, role));
        }
    }

    public void registerCaseRoles(List<CaseRoleEntity> caseRoleEntityList) {
        caseRoleEntityList.stream().forEach(caseRoleEntity ->
            caseRoles.put(
                caseRoleEntity.getCaseType().getReference(),
                caseRoleEntity.getReference(),
                caseRoleEntity
            )
        );
    }

    public ParseContext registerUserRoles(final List<UserRoleEntity> userRoleList) {
        userRoles.clear();
        userRoles.putAll(userRoleList
                             .stream()
                             .collect(toMap(UserRoleEntity::getReference, u -> u)));
        return this;
    }

    public ParseContext registerEvent(String caseTypeId, EventEntity event) {
        if (!eventsByCaseTypes.containsKey(caseTypeId)) {
            eventsByCaseTypes.put(caseTypeId, Maps.newHashMap());
        }

        final Map<String, EventEntity> caseTypeEvents = eventsByCaseTypes.get(caseTypeId);

        if (caseTypeEvents.containsKey(event.getReference())) {
            throw new MapperException("Event already registered for ID: " + event.getReference());
        }

        caseTypeEvents.put(event.getReference(), event);

        return this;
    }

    public EventEntity getEventForCaseType(String caseTypeId, String eventId) {
        final Map<String, EventEntity> caseTypeEvents = eventsByCaseTypes.get(caseTypeId);
        EventEntity event = null;
        if (null != caseTypeEvents) {
            event = caseTypeEvents.get(eventId);
        }
        return event;
    }

    public void registerMetadataFields(List<CaseFieldEntity> fields) {
        metadataFields.putAll(fields.stream()
                                  .filter(Objects::nonNull)
                                  .collect(toMap(CaseFieldEntity::getReference, Function.identity())));
    }

    public Set<String> getMissingUserRoles() {
        return missingUserRoles;
    }

    public void addMissingUserRole(String missingUserRole) {
        this.missingUserRoles.add(missingUserRole);
    }
}
