package uk.gov.hmcts.ccd.definition.store.excel.client.translation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;


@Data
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
public class Translation {

    @NonNull
    private String translation;
    private boolean yesOrNo;

}
