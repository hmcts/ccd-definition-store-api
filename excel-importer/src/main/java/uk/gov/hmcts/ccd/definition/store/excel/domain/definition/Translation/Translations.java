package uk.gov.hmcts.ccd.definition.store.excel.domain.definition.Translation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Translations {

    private Map<String, String> translations;

}
