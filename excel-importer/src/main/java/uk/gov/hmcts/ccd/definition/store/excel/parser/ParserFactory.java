package uk.gov.hmcts.ccd.definition.store.excel.parser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.ShowConditionParser;

@Component
public class ParserFactory {

    private final ShowConditionParser showConditionParser;
    private final EntityToDefinitionDataItemRegistry entityToDefinitionDataItemRegistry;

    @Autowired
    public ParserFactory(ShowConditionParser showConditionParser,
                         EntityToDefinitionDataItemRegistry entityToDefinitionDataItemRegistry) {
        this.showConditionParser = showConditionParser;
        this.entityToDefinitionDataItemRegistry = entityToDefinitionDataItemRegistry;
    }

    public JurisdictionParser createJurisdictionParser() {
        return new JurisdictionParser();
    }

    public FieldsTypeParser createFieldsTypeParser(ParseContext context) {
        final FieldTypeParser fieldTypeParser = new FieldTypeParser(context);

        return new FieldsTypeParser(
            new ListFieldTypeParser(context),
            new ComplexFieldTypeParser(context, fieldTypeParser, showConditionParser, entityToDefinitionDataItemRegistry),
            new CaseFieldTypeParser(context, fieldTypeParser)
        );
    }

    public CaseTypeParser createCaseTypeParser(ParseContext context) {
        return new CaseTypeParser(
            context,
            new CaseFieldParser(context),
            new StateParser(context),
            new EventParser(
                context,
                new EventCaseFieldParser(
                    context,
                    showConditionParser,
                    entityToDefinitionDataItemRegistry
                ),
                entityToDefinitionDataItemRegistry
            ),
            new AuthorisationCaseTypeParser(context, entityToDefinitionDataItemRegistry),
            new AuthorisationCaseFieldParser(context, entityToDefinitionDataItemRegistry),
            new AuthorisationCaseEventParser(context, entityToDefinitionDataItemRegistry),
            new AuthorisationCaseStateParser(context, entityToDefinitionDataItemRegistry)
        );
    }

    public LayoutParser createLayoutParser(ParseContext parseContext) {
        return new LayoutParser(
            new WorkbasketInputLayoutParser(parseContext, entityToDefinitionDataItemRegistry),
            new WorkbasketLayoutParser(parseContext, entityToDefinitionDataItemRegistry),
            new SearchInputLayoutParser(parseContext, entityToDefinitionDataItemRegistry),
            new SearchResultLayoutParser(parseContext, entityToDefinitionDataItemRegistry),
            new CaseTypeTabParser(parseContext, showConditionParser, entityToDefinitionDataItemRegistry),
            new WizardPageParser(parseContext, showConditionParser, entityToDefinitionDataItemRegistry));
    }

    public UserProfilesParser createUserProfileParser() {
        return new UserProfilesParser();
    }
}
