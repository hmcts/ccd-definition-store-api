package uk.gov.hmcts.ccd.definition.store.excel.client.translation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;


@Getter
@AllArgsConstructor
@RequiredArgsConstructor
public class Translation {

    @NonNull
    private String translation;
    private boolean yesOrNo;

}
