package uk.gov.hmcts.ccd.definition.store.elastic.mapping.support;

import com.google.gson.stream.JsonWriter;
import uk.gov.hmcts.ccd.definition.store.elastic.exception.ElasticSearchInitialisationException;

import java.io.StringWriter;
import java.util.function.Consumer;

public interface JsonGenerator {

    /**
     * starts a new json object and returns any content written by the consumer, enclosed in curly brackets.
     */
    default String newJson(Consumer<JsonWriter> jsonWriterConsumer) {
        try {
            StringWriter out = new StringWriter();
            JsonWriter jw = new JsonWriter(out);
            jw.beginObject();

            jsonWriterConsumer.accept(jw);

            jw.endObject();
            return out.toString();
        } catch (Exception e) {
            throw new ElasticSearchInitialisationException("mapping json generation exception", e);
        }
    }
}
