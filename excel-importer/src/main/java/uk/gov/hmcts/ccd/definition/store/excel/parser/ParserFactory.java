package uk.gov.hmcts.ccd.definition.store.excel.parser;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.ApplicationParams;
import uk.gov.hmcts.ccd.definition.store.domain.service.metadata.MetadataField;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.ShowConditionParser;
import uk.gov.hmcts.ccd.definition.store.excel.validation.RoleToAccessProfilesValidator;
import uk.gov.hmcts.ccd.definition.store.excel.validation.HiddenFieldsValidator;
import uk.gov.hmcts.ccd.definition.store.excel.validation.SearchCriteriaValidator;
import uk.gov.hmcts.ccd.definition.store.excel.validation.SearchPartyValidator;
import uk.gov.hmcts.ccd.definition.store.excel.validation.SpreadsheetValidator;
import java.util.concurrent.Executor;

@Component
public class ParserFactory {

    private final ShowConditionParser showConditionParser;
    private final EntityToDefinitionDataItemRegistry entityToDefinitionDataItemRegistry;
    private final Map<MetadataField, MetadataCaseFieldEntityFactory> metadataCaseFieldEntityFactoryRegistry;
    private final SpreadsheetValidator spreadsheetValidator;
    private final HiddenFieldsValidator hiddenFieldsValidator;
    private final ChallengeQuestionParser challengeQuestionParser;
    private final CategoryParser categoryParser;
    private final SearchPartyValidator searchPartyValidator;
    private final SearchCriteriaValidator searchCriteriaValidator;
    private final ApplicationParams applicationParams;
    private final Executor executor;

    @Autowired
    public ParserFactory(ShowConditionParser showConditionParser,
                         EntityToDefinitionDataItemRegistry entityToDefinitionDataItemRegistry,
                         Map<MetadataField, MetadataCaseFieldEntityFactory> metadataCaseFieldEntityFactoryRegistry,
                         SpreadsheetValidator spreadsheetValidator,
                         HiddenFieldsValidator hiddenFieldsValidator,
                         ChallengeQuestionParser challengeQuestionParser,
                         CategoryParser categoryParser, SearchPartyValidator searchPartyValidator,
                         SearchCriteriaValidator searchCriteriaValidator,
                         ApplicationParams applicationParams, @Qualifier("validateExecutor") Executor executor) {
        this.showConditionParser = showConditionParser;
        this.entityToDefinitionDataItemRegistry = entityToDefinitionDataItemRegistry;
        this.metadataCaseFieldEntityFactoryRegistry = metadataCaseFieldEntityFactoryRegistry;
        this.spreadsheetValidator = spreadsheetValidator;
        this.hiddenFieldsValidator = hiddenFieldsValidator;
        this.challengeQuestionParser = challengeQuestionParser;
        this.categoryParser = categoryParser;
        this.searchPartyValidator = searchPartyValidator;
        this.searchCriteriaValidator = searchCriteriaValidator;
        this.applicationParams = applicationParams;
        this.executor = executor;
    }

    public JurisdictionParser createJurisdictionParser() {
        return new JurisdictionParser();
    }

    public FieldsTypeParser createFieldsTypeParser(ParseContext context) {
        final FieldTypeParser fieldTypeParser = new FieldTypeParser(context);

        return new FieldsTypeParser(
            new ListFieldTypeParser(context, spreadsheetValidator),
            new ComplexFieldTypeParser(
                context,
                fieldTypeParser,
                showConditionParser,
                entityToDefinitionDataItemRegistry,
                hiddenFieldsValidator, executor),
            new CaseFieldTypeParser(context, fieldTypeParser)
        );
    }

    public CaseTypeParser createCaseTypeParser(ParseContext context) {
        return new CaseTypeParser(
            context,
            new CaseFieldParser(context, entityToDefinitionDataItemRegistry),
            new StateParser(context),
            new EventParser(
                context,
                new EventCaseFieldParser(
                    context,
                    showConditionParser,
                    entityToDefinitionDataItemRegistry,
                    hiddenFieldsValidator
                ),
                new EventCaseFieldComplexTypeParser(showConditionParser, hiddenFieldsValidator),
                entityToDefinitionDataItemRegistry,
                showConditionParser,
                applicationParams.isDefaultPublish()),
            new AuthorisationCaseTypeParser(context, entityToDefinitionDataItemRegistry),
            new AuthorisationCaseFieldParser(context, entityToDefinitionDataItemRegistry),
            new AuthorisationComplexTypeParser(context, entityToDefinitionDataItemRegistry),
            new AuthorisationCaseEventParser(context, entityToDefinitionDataItemRegistry),
            new AuthorisationCaseStateParser(context, entityToDefinitionDataItemRegistry),
            new MetadataCaseFieldParser(context, metadataCaseFieldEntityFactoryRegistry),
            new CaseRoleParser(context),
            new SearchAliasFieldParser(context));
    }

    public LayoutParser createLayoutParser(ParseContext parseContext) {
        return new LayoutParser(
            new WorkbasketInputLayoutParser(parseContext, entityToDefinitionDataItemRegistry, showConditionParser),
            new WorkbasketLayoutParser(parseContext, entityToDefinitionDataItemRegistry, showConditionParser),
            new SearchInputLayoutParser(parseContext, entityToDefinitionDataItemRegistry, showConditionParser),
            new SearchResultLayoutParser(parseContext, entityToDefinitionDataItemRegistry, showConditionParser),
            new CaseTypeTabParser(parseContext, showConditionParser, entityToDefinitionDataItemRegistry),
            new WizardPageParser(parseContext, showConditionParser, entityToDefinitionDataItemRegistry),
            new SearchCasesResultLayoutParser(parseContext, entityToDefinitionDataItemRegistry, showConditionParser));
    }

    public UserProfilesParser createUserProfileParser() {
        return new UserProfilesParser();
    }

    public BannerParser createBannerParser(ParseContext context) {
        return new BannerParser(context);
    }

    public JurisdictionUiConfigParser createJurisdictionUiConfigParser(ParseContext context) {
        return new JurisdictionUiConfigParser(context);
    }

    public ChallengeQuestionParser createNewChallengeQuestionParser() {
        return challengeQuestionParser;
    }

    public RoleToAccessProfilesParser createRoleToAccessProfilesParser() {
        return new RoleToAccessProfilesParser();
    }

    public RoleToAccessProfilesValidator createAccessProfileValidator() {
        return new RoleToAccessProfilesValidator();
    }

    public SearchPartyParser createNewSearchPartyParser() {
        return new SearchPartyParser();
    }

    public SearchPartyValidator createNewSearchPartyValidator() {
        return searchPartyValidator;
    }

    public SearchCriteriaParser createSearchCriteriaParser() {
        return new SearchCriteriaParser();
    }

    public SearchCriteriaValidator createSearchCriteriaValidator() {
        return searchCriteriaValidator;
    }

    public CategoryParser createCategoriesParser() {
        return categoryParser;
    }
}
