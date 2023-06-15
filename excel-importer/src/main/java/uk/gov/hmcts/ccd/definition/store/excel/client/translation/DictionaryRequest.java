package uk.gov.hmcts.ccd.definition.store.excel.client.translation;

import lombok.Getter;
import lombok.Setter;
import java.util.Map;

@Getter
@Setter
public class DictionaryRequest {

    private Map<String,Translation> translations;

}
