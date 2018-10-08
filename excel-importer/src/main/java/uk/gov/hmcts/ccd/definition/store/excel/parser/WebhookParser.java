package uk.gov.hmcts.ccd.definition.store.excel.parser;

import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.repository.entity.WebhookEntity;

public class WebhookParser {
    public static final String WEBHOOK_RETRIES_SEPARATOR = ",";

    public static WebhookEntity parseWebhook(DefinitionDataItem eventDefinition, ColumnName urlColumn,
                                    ColumnName retriesColumn) {
        WebhookEntity webhook = null;

        final String url = eventDefinition.getString(urlColumn);
        if (!StringUtils.isBlank(url)) {
            webhook = new WebhookEntity();
            webhook.setUrl(url);

            final String retriesRaw = eventDefinition.getString(retriesColumn);
            if (!StringUtils.isBlank(retriesRaw)) {
                for (String retry : retriesRaw.split(WEBHOOK_RETRIES_SEPARATOR)) {
                    webhook.addTimeout(Integer.valueOf(retry));
                }
            }
        }

        return webhook;
    }
}
