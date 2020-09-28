package uk.gov.hmcts.ccd.definitionstore.tests.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImportDefinitonDataSetup {

    private ImportDefinitonDataSetup() {
        // Hide Utility Class Constructor : Utility classes should not have a public or default constructor
        // (squid:S1118)
    }

    private static List<Map<String, String>> caseTypeFromDefinition = new ArrayList<>();

    public static List<Map<String, String>> populateDefinitionCaseTypeData() {
        caseTypeFromDefinition.add(addAATCaseTypeData());
        caseTypeFromDefinition.add(addMapperCaseTypeData());
        caseTypeFromDefinition.add(addAATPublicCaseTypeData());
        caseTypeFromDefinition.add(addAATPrivateCaseTypeData());
        caseTypeFromDefinition.add(addAATRestrictedCaseTypeData());
        return caseTypeFromDefinition;
    }

    private static Map<String, String> addAATCaseTypeData() {
        return new HashMap<String, String>() {{
                put("id", "AAT");
                put("name", "Demo case");
                put("description", "Demonstrate everything CCD can do!");
                put("security_classification", "PUBLIC");
            }};
    }

    private static Map<String, String> addMapperCaseTypeData() {
        return new HashMap<String, String>() {{
                put("id", "MAPPER");
                put("name", "Case type for Mapper");
                put("description", "Demonstrate everything CCD can do!");
                put("security_classification", "PUBLIC");
            }};
    }

    private static Map<String, String> addAATPublicCaseTypeData() {
        return new HashMap<String, String>() {{
                put("id", "AATPUBLIC");
                put("name", "AATPUBLIC");
                put("description", "Demonstrate everything CCD can do!");
                put("security_classification", "PUBLIC");
            }};
    }

    private static Map<String, String> addAATPrivateCaseTypeData() {
        return new HashMap<String, String>() {{
                put("id", "AATPRIVATE");
                put("name", "AATPRIVATE");
                put("description", "Demonstrate everything CCD can do!");
                put("security_classification", "PRIVATE");
            }};
    }

    private static Map<String, String> addAATRestrictedCaseTypeData() {
        return new HashMap<String, String>() {{
                put("id", "AATRESTRICTED");
                put("name", "AATRESTRICTED");
                put("description", "Demonstrate everything CCD can do!");
                put("security_classification", "RESTRICTED");
            }};
    }
}
