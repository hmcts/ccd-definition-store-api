package uk.gov.hmcts.ccd.definition.store.excel.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.ccd.definition.store.domain.service.metadata.MetadataField;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MetadataCaseFieldParser {
    private static final Logger logger = LoggerFactory.getLogger(MetadataCaseFieldParser.class);

    private final ParseContext parseContext;
    private final Map<MetadataField, MetadataCaseFieldEntityFactory> metadataCaseFieldEntityFactoryRegistry;

    MetadataCaseFieldParser(ParseContext parseContext,
                            Map<MetadataField, MetadataCaseFieldEntityFactory> metadataCaseFieldEntityFactoryRegistry) {
        this.parseContext = parseContext;
        this.metadataCaseFieldEntityFactoryRegistry = metadataCaseFieldEntityFactoryRegistry;
    }

    public Collection<CaseFieldEntity> parseAll(CaseTypeEntity caseType) {
        logger.debug("Parsing metadata fields for case type {}...", caseType.getReference());

        List<CaseFieldEntity> caseFields = MetadataField.getDynamicFields().stream()
            .map(metadataCaseFieldEntityFactoryRegistry::get)
            .map(factory -> factory.createCaseFieldEntity(parseContext, caseType)).collect(Collectors.toList());

        parseContext.registerMetadataFields(caseFields);

        logger.info("Parsing metadata fields for case type {}: OK: {} metadata fields parsed",
            caseType.getReference(), caseFields.size());

        return caseFields;
    }

}
