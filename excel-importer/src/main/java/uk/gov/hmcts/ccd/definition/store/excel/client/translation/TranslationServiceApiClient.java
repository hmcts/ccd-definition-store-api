package uk.gov.hmcts.ccd.definition.store.excel.client.translation;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;


import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@FeignClient(
    name = "translation-api",
    url = "${ts.translation.service.host}",
    configuration = TranslationServiceApiClientConfig.class
)

public interface TranslationServiceApiClient {

    String DICTIONARY_URL = "/dictionary";

    @PutMapping(value = DICTIONARY_URL, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    ResponseEntity uploadToDictionary(@RequestBody DictionaryRequest dictionaryRequest);

}
