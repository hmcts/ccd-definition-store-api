package uk.gov.hmcts.ccd.definition.store.excel.service;

import com.google.common.annotations.VisibleForTesting;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.service.FieldTypeService;
import uk.gov.hmcts.ccd.definition.store.domain.service.JurisdictionService;
import uk.gov.hmcts.ccd.definition.store.domain.service.JurisdictionUiConfigService;
import uk.gov.hmcts.ccd.definition.store.domain.service.LayoutService;
import uk.gov.hmcts.ccd.definition.store.domain.service.banner.BannerService;
import uk.gov.hmcts.ccd.definition.store.domain.service.casetype.CaseTypeService;
import uk.gov.hmcts.ccd.definition.store.domain.service.nocconfig.NoCConfigService;
import uk.gov.hmcts.ccd.definition.store.domain.service.question.ChallengeQuestionTabService;
import uk.gov.hmcts.ccd.definition.store.domain.service.workbasket.WorkBasketUserDefaultService;
import uk.gov.hmcts.ccd.definition.store.event.DefinitionImportedEvent;
import uk.gov.hmcts.ccd.definition.store.excel.domain.definition.model.DefinitionFileUploadMetadata;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.InvalidImportException;
import uk.gov.hmcts.ccd.definition.store.excel.parser.SpreadsheetParser;
import uk.gov.hmcts.ccd.definition.store.excel.parser.ParserFactory;
import uk.gov.hmcts.ccd.definition.store.excel.parser.ParseContext;
import uk.gov.hmcts.ccd.definition.store.excel.parser.JurisdictionParser;
import uk.gov.hmcts.ccd.definition.store.excel.parser.BannerParser;
import uk.gov.hmcts.ccd.definition.store.excel.parser.JurisdictionUiConfigParser;
import uk.gov.hmcts.ccd.definition.store.excel.parser.FieldsTypeParser;
import uk.gov.hmcts.ccd.definition.store.excel.parser.ParseResult;
import uk.gov.hmcts.ccd.definition.store.excel.parser.CaseTypeParser;
import uk.gov.hmcts.ccd.definition.store.excel.parser.LayoutParser;
import uk.gov.hmcts.ccd.definition.store.excel.parser.UserProfilesParser;
import uk.gov.hmcts.ccd.definition.store.excel.parser.ChallengeQuestionParser;
import uk.gov.hmcts.ccd.definition.store.excel.parser.NoCConfigParser;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.excel.validation.SpreadsheetValidator;
import uk.gov.hmcts.ccd.definition.store.repository.CaseFieldRepository;
import uk.gov.hmcts.ccd.definition.store.repository.UserRoleRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.BannerEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionUiConfigEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DataFieldType;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.GenericLayoutEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DisplayGroupEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ChallengeQuestionTabEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.NoCConfigEntity;
import uk.gov.hmcts.ccd.definition.store.repository.model.WorkBasketUserDefault;
import uk.gov.hmcts.ccd.definition.store.rest.model.IdamProperties;
import uk.gov.hmcts.ccd.definition.store.rest.service.IdamProfileClient;

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
    private final BannerService bannerService;
    private final JurisdictionUiConfigService jurisdictionUiConfigService;
    private final NoCConfigService noCConfigService;
    private final ChallengeQuestionTabService challengeQuestionTabService;

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
                             IdamProfileClient idamProfileClient,
                             BannerService bannerService,
                             JurisdictionUiConfigService jurisdictionUiConfigService,
                             NoCConfigService noCConfigService,
                             ChallengeQuestionTabService challengeQuestionTabService) {

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
        this.bannerService = bannerService;
        this.jurisdictionUiConfigService = jurisdictionUiConfigService;
        this.noCConfigService = noCConfigService;
        this.challengeQuestionTabService = challengeQuestionTabService;
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

        bannerService.deleteJurisdictionBanners(jurisdiction.getReference());
        if (definitionSheets.get(SheetName.BANNER.getName()) != null) {
            logger.debug("Importing spreadsheet: Banner...");
            final BannerParser bannerParser = parserFactory.createBannerParser(parseContext);
            Optional<BannerEntity> bannerEntity = bannerParser.parse(definitionSheets);
            bannerEntity.ifPresent(this::importBanner);
            logger.debug("Importing spreadsheet: Banner...: OK");
        }
        final JurisdictionUiConfigParser jurisdictionUiConfigParser = parserFactory
            .createJurisdictionUiConfigParser(parseContext);
        JurisdictionUiConfigEntity jurisdictionUiConfigEntity = jurisdictionUiConfigParser.parse(definitionSheets);
        importJurisdictionUiConfig(jurisdictionUiConfigEntity);

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
        parseContext.registerMetadataFields(caseFieldRepository
            .findByDataFieldTypeAndCaseTypeNull(DataFieldType.METADATA));

        /*
            4 - Case Type
         */
        logger.debug("Importing spreadsheet: Case types...");

        final CaseTypeParser caseTypeParser = parserFactory.createCaseTypeParser(parseContext);
        final ParseResult<CaseTypeEntity> parsedCaseTypes = caseTypeParser.parseAll(definitionSheets);
        List<CaseTypeEntity> caseTypes = parsedCaseTypes.getNewResults();
        caseTypeService.createAll(jurisdiction, caseTypes, parseContext.getMissingUserRoles()); // runs validation

        logger.info("Case types parsing: OK: {} case types parsed", parsedCaseTypes.getAllResults().size());

        importNoCConfigDefinition(definitionSheets, parseContext);

        logger.info("Importing spreadsheet: Case types: OK: {} case types imported", caseTypes.size());

        /*
            5 - UI definition
         */
        logger.debug("Importing spreadsheet: UI definition...");

        final LayoutParser layoutParser = parserFactory.createLayoutParser(parseContext);

        final ParseResult<GenericLayoutEntity> searchInputResult
            = layoutParser.parseSearchInputLayout(definitionSheets);
        final ParseResult<GenericLayoutEntity> searchResultResult
            = layoutParser.parseSearchResultLayout(definitionSheets);
        final ParseResult<GenericLayoutEntity> workbasketInputResult
            = layoutParser.parseWorkbasketInputLayout(definitionSheets);
        final ParseResult<GenericLayoutEntity> workbasketLayoutResult
            = layoutParser.parseWorkbasketLayout(definitionSheets);

        layoutService.createGenerics(searchInputResult.getNewResults());
        layoutService.createGenerics(searchResultResult.getNewResults());
        layoutService.createGenerics(workbasketInputResult.getNewResults());
        layoutService.createGenerics(workbasketLayoutResult.getNewResults());

        if (definitionSheets.get(SheetName.SEARCH_CASES_RESULT_FIELDS.getName()) != null) {
            final ParseResult<GenericLayoutEntity> searchCasesResultLayoutResult
                = layoutParser.parseSearchCasesResultsLayout(definitionSheets);
            layoutService.createGenerics(searchCasesResultLayoutResult.getNewResults());
        }

        final ParseResult<DisplayGroupEntity> displayGroupsResult
            = layoutParser.parseAllDisplayGroups(definitionSheets);
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

        // ChallengeQuestion
        if (definitionSheets.get(SheetName.CHALLENGE_QUESTION_TAB.getName()) != null) {
            logger.debug("Importing spreadsheet: NewChallengeQuestion...");
            final ChallengeQuestionParser challengeQuestionParser =
                parserFactory.createNewChallengeQuestionParser();

            List<ChallengeQuestionTabEntity> newChallengeQuestionEntities = challengeQuestionParser
                .parse(definitionSheets,parseContext);
            challengeQuestionTabService.saveAll(newChallengeQuestionEntities);
            logger.debug("Importing spreadsheet: NewChallengeQuestion...: OK");
        }

        return metadata;
    }

    private void importNoCConfigDefinition(Map<String, DefinitionSheet> definitionSheets, ParseContext parseContext) {
        String nocConfigSheetName = SheetName.NOC_CONFIG.getName();
        if (definitionSheets.get(nocConfigSheetName) != null) {
            logger.debug("Importing tab: {} ...", nocConfigSheetName);
            final NoCConfigParser noCConfigParser = parserFactory.createNoCConfigParser(parseContext);
            Map<String, List<NoCConfigEntity>> caseTypeNoCConfigEntities = noCConfigParser.parse(definitionSheets);
            Set<String> caseTypesWithMultipleEntries = caseTypeNoCConfigEntities
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue().size() > 1)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

            if (!caseTypesWithMultipleEntries.isEmpty()) {
                throw new InvalidImportException("Only one NoC config is allowed per case type(s) "
                    + caseTypesWithMultipleEntries.stream().sorted().collect(Collectors.joining(",")));
            }
            caseTypeNoCConfigEntities
                .forEach((key, value) -> value.forEach(noCConfigService::save));
            logger.debug("Importing tab: {} ...: OK", nocConfigSheetName);
        }
    }

    @VisibleForTesting  // used by BaseTest
    public IdamProperties getUserDetails() {
        return idamProfileClient.getLoggedInUserDetails();
    }

    /**
     * Gets any warnings that occur during the parse stage of the Definition import process.
     *
     * @return A list of warning messages
     */
    @Override
    public List<String> getImportWarnings() {
        return spreadsheetParser.getImportWarnings();
    }

    private JurisdictionEntity importJurisdiction(JurisdictionEntity jurisdiction) {
        return jurisdictionService.get(jurisdiction.getReference()).orElseGet(() -> {
            jurisdictionService.create(jurisdiction);
            return jurisdiction;
        });
    }

    private void importBanner(BannerEntity bannerEntity) {
        bannerService.save(bannerEntity);
    }

    private void importJurisdictionUiConfig(JurisdictionUiConfigEntity jurisdictionUiConfigEntity) {
        jurisdictionUiConfigService.save(jurisdictionUiConfigEntity);
    }

}
