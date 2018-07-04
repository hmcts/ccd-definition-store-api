package uk.gov.hmcts.ccd.definition.store.elastic.mapping.support;

import java.io.IOException;
import java.io.StringWriter;
import java.util.function.Consumer;

import com.google.gson.stream.JsonWriter;

public interface JsonGenerator {

    /**
     * returns any content written by the consumer within curly brackets
     */
    default String newJson(Consumer<JsonWriter> jsonWriterConsumer) throws IOException {
        StringWriter out = new StringWriter();
        JsonWriter jw = new JsonWriter(out);
        jw.beginObject();

        jsonWriterConsumer.accept(jw);

        jw.endObject();
        return out.toString();
    }
}
