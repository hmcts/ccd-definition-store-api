package uk.gov.hmcts.ccd.definition.store.excel.client.translation;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@NoArgsConstructor
@Data
public class DictionaryRequest {

    private Map<String, String> translations;

}
