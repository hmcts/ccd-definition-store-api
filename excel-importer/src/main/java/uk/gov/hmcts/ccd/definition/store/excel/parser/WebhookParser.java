package uk.gov.hmcts.ccd.definition.store.excel.parser;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.repository.entity.WebhookEntity;

import java.util.List;

public class WebhookParser {

    public static final String WEBHOOK_RETRIES_SEPARATOR = ",";

    private WebhookParser() {
        // Hide Utility Class Constructor : Utility classes should not have a public or default constructor (squid:S1118)
    }

    public static WebhookEntity parseWebhook(DefinitionDataItem eventDefinition, ColumnName urlColumn,
                                    ColumnName retriesColumn) {
        WebhookEntity webhook = null;

        final String url = eventDefinition.getString(urlColumn);
        if (!StringUtils.isBlank(url)) {
            webhook = new WebhookEntity();
            webhook.setUrl(url);

            final String retriesRaw = eventDefinition.getString(retriesColumn);
            if (!StringUtils.isBlank(retriesRaw)) {
                List<Integer> timeouts = Lists.newArrayList();
                for (String retry : retriesRaw.split(WEBHOOK_RETRIES_SEPARATOR)) {
                    timeouts.add(Integer.valueOf(retry));
                }
                webhook.setTimeouts(timeouts);
            }
        }

        return webhook;
    }
}
