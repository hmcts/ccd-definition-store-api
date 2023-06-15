package uk.gov.hmcts.ccd.definition.store.excel.client.translation;

import lombok.Data;
import lombok.Setter;
import java.util.Map;

@Data
@Setter
public class DictionaryRequest {

    private Map<String,Translation> translations;

}
