package uk.gov.hmcts.ccd.definition.store.excel.service;

import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.service.FieldTypeService;
import uk.gov.hmcts.ccd.definition.store.domain.service.JurisdictionService;
import uk.gov.hmcts.ccd.definition.store.domain.service.LayoutService;
import uk.gov.hmcts.ccd.definition.store.domain.service.casetype.CaseTypeService;
import uk.gov.hmcts.ccd.definition.store.domain.service.workbasket.WorkBasketUserDefaultService;
import uk.gov.hmcts.ccd.definition.store.event.DefinitionImportedEvent;
import uk.gov.hmcts.ccd.definition.store.excel.domain.definition.model.DefinitionFileUploadMetadata;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.InvalidImportException;
import uk.gov.hmcts.ccd.definition.store.excel.parser.CaseTypeParser;
import uk.gov.hmcts.ccd.definition.store.excel.parser.FieldsTypeParser;
import uk.gov.hmcts.ccd.definition.store.excel.parser.JurisdictionParser;
import uk.gov.hmcts.ccd.definition.store.excel.parser.LayoutParser;
import uk.gov.hmcts.ccd.definition.store.excel.parser.ParseContext;
import uk.gov.hmcts.ccd.definition.store.excel.parser.ParseResult;
import uk.gov.hmcts.ccd.definition.store.excel.parser.ParserFactory;
import uk.gov.hmcts.ccd.definition.store.excel.parser.SpreadsheetParser;
import uk.gov.hmcts.ccd.definition.store.excel.parser.UserProfilesParser;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.excel.validation.SpreadsheetValidator;
import uk.gov.hmcts.ccd.definition.store.repository.CaseFieldRepository;
import uk.gov.hmcts.ccd.definition.store.repository.UserRoleRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DataFieldType;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DisplayGroupEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.GenericLayoutEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionEntity;
import uk.gov.hmcts.ccd.definition.store.repository.model.WorkBasketUserDefault;
import uk.gov.hmcts.ccd.definition.store.rest.model.IdamProperties;
import uk.gov.hmcts.ccd.definition.store.rest.service.IdamProfileClient;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

@Component
public class ImportServiceImpl implements ImportService {

    private static final Logger logger = LoggerFactory.getLogger(ImportServiceImpl.class);

    private final SpreadsheetValidator spreadsheetValidator;
    private final SpreadsheetParser spreadsheetParser;
    private final ParserFactory parserFactory;
    private final FieldTypeService fieldTypeService;
    private final JurisdictionService jurisdictionService;
    private final CaseTypeService caseTypeService;
    private final LayoutService layoutService;
    private final UserRoleRepository userRoleRepository;
    private final WorkBasketUserDefaultService workBasketUserDefaultService;
    private final CaseFieldRepository caseFieldRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final IdamProfileClient idamProfileClient;

    @Autowired
    public ImportServiceImpl(SpreadsheetValidator spreadsheetValidator,
                             SpreadsheetParser spreadsheetParser,
                             ParserFactory parserFactory,
                             FieldTypeService fieldTypeService,
                             JurisdictionService jurisdictionService,
                             CaseTypeService caseTypeService,
                             LayoutService layoutService,
                             UserRoleRepository userRoleRepository,
                             WorkBasketUserDefaultService workBasketUserDefaultService,
                             CaseFieldRepository caseFieldRepository,
                             ApplicationEventPublisher applicationEventPublisher,
                             IdamProfileClient idamProfileClient) {
        this.spreadsheetValidator = spreadsheetValidator;
        this.spreadsheetParser = spreadsheetParser;
        this.parserFactory = parserFactory;
        this.fieldTypeService = fieldTypeService;
        this.jurisdictionService = jurisdictionService;
        this.caseTypeService = caseTypeService;
        this.layoutService = layoutService;
        this.userRoleRepository = userRoleRepository;
        this.workBasketUserDefaultService = workBasketUserDefaultService;
        this.caseFieldRepository = caseFieldRepository;
        this.idamProfileClient = idamProfileClient;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    /**
     * Imports the Case Definition data and inserts it into the database.
     *
     * @param inputStream the Case Definition data as an <code>InputStream</code>
     * @throws IOException            in the event that there is a problem reading in the data
     * @throws InvalidImportException if any of the Case Definition sheets fails checks for a definition name and a row
     *                                of attribute headers
     * @return A {@link DefinitionFileUploadMetadata} instance containing the Jurisdiction and Case Types from the
     *         Definition data, and the user ID of the account used for importing the Definition
     */
    @Override
    public DefinitionFileUploadMetadata importFormDefinitions(InputStream inputStream) throws IOException {
        logger.debug("Importing spreadsheet...");

        final Map<String, DefinitionSheet> definitionSheets = spreadsheetParser.parse(inputStream);

        spreadsheetValidator.validate(definitionSheets);

        final ParseContext parseContext = new ParseContext();
        parseContext.registerUserRoles(userRoleRepository.findAll());

        /*
            1 - Jurisdiction
         */
        logger.debug("Importing spreadsheet: Jurisdiction...");

        final JurisdictionParser jurisdictionParser = parserFactory.createJurisdictionParser();
        JurisdictionEntity parsedJurisdiction = jurisdictionParser.parse(definitionSheets);
        JurisdictionEntity jurisdiction = importJurisdiction(parsedJurisdiction);
        parseContext.setJurisdiction(jurisdiction);

        logger.info("Importing spreadsheet: Jurisdiction {} : OK ", jurisdiction.getReference());

        /*
            2 - Field types
         */
        logger.debug("Importing spreadsheet: Field types...");

        // Initialise parse context with existing types
        parseContext.addBaseTypes(fieldTypeService.getBaseTypes());
        parseContext.addToAllTypes(fieldTypeService.getPredefinedComplexTypes());
        parseContext.addToAllTypes(fieldTypeService.getTypesByJurisdiction(jurisdiction.getReference()));

        final FieldsTypeParser fieldsTypeParser = parserFactory.createFieldsTypeParser(parseContext);

        // Parse new types
        final ParseResult<FieldTypeEntity> parsedFieldTypes = fieldsTypeParser.parseAll(definitionSheets);
        fieldTypeService.saveTypes(jurisdiction, parsedFieldTypes.getNewResults());

        logger.info("Importing spreadsheet: Field types: OK: {} field types imported",
                    parsedFieldTypes.getNewResults().size());

        /*
            3 - metadata fields
         */
        parseContext.registerMetadataFields(caseFieldRepository.findByDataFieldTypeAndCaseTypeNull(DataFieldType.METADATA));

        /*
            4 - Case Type
         */
        logger.debug("Importing spreadsheet: Case types...");

        final CaseTypeParser caseTypeParser = parserFactory.createCaseTypeParser(parseContext);
        final ParseResult<CaseTypeEntity> parsedCaseTypes = caseTypeParser.parseAll(definitionSheets);
        List<CaseTypeEntity> caseTypes = parsedCaseTypes.getNewResults();
        caseTypeService.createAll(jurisdiction, caseTypes);

        logger.info("Importing spreadsheet: Case types: OK: {} case types imported",
                    caseTypes.size());

        /*
            5 - UI definition
         */
        logger.debug("Importing spreadsheet: UI definition...");

        final LayoutParser layoutParser = parserFactory.createLayoutParser(parseContext);

        final ParseResult<GenericLayoutEntity> genericsResult = layoutParser.parseAllGenerics(definitionSheets);
        layoutService.createGenerics(genericsResult.getNewResults());

        final ParseResult<DisplayGroupEntity> displayGroupsResult = layoutParser.parseAllDisplayGroups(definitionSheets);
        layoutService.createDisplayGroups(displayGroupsResult.getNewResults());

        logger.info("Importing spreadsheet: UI definition: OK");

        /*
            6 - User profiles
         */
        logger.debug("Importing spreadsheet: User profiles...");

        final UserProfilesParser userProfilesParser = parserFactory.createUserProfileParser();
        final List<WorkBasketUserDefault> workBasketUserDefaults = userProfilesParser.parse(definitionSheets);
        final IdamProperties userDetails = getUserDetails();
        workBasketUserDefaultService.saveWorkBasketUserDefaults(workBasketUserDefaults,
                                                                jurisdiction,
                                                                parsedCaseTypes.getAllResults(),
                                                                userDetails.getEmail());
        logger.info("Importing spreadsheet: User profiles: OK");

        applicationEventPublisher.publishEvent(new DefinitionImportedEvent(caseTypes));

        logger.info("Importing spreadsheet: OK: For jurisdiction {}", jurisdiction.getReference());

        // Populate the metadata to be returned for use when uploading the Definition File to Azure Storage
        DefinitionFileUploadMetadata metadata = new DefinitionFileUploadMetadata();
        metadata.setJurisdiction(jurisdiction.getReference());

        for (CaseTypeEntity entity : parsedCaseTypes.getNewResults()) {
            metadata.addCaseType(entity.getReference());
        }

        metadata.setUserId(userDetails.getEmail());

        return metadata;
    }

    @VisibleForTesting  // used by BaseTest
    public IdamProperties getUserDetails() {
        return idamProfileClient.getLoggedInUserDetails();
    }

    private JurisdictionEntity importJurisdiction(JurisdictionEntity jurisdiction) {
        return jurisdictionService.get(jurisdiction.getReference()).orElseGet(() -> {
            jurisdictionService.create(jurisdiction);
            return jurisdiction;
        });
    }
}
