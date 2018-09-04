package uk.gov.hmcts.ccd.definition.store.elastic.mapping.support;

import org.jooq.lambda.Unchecked;
import org.junit.Test;
import uk.gov.hmcts.ccd.definition.store.elastic.TestUtils;

import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.ccd.definition.store.elastic.hamcresutil.IsEqualJSON.equalToJSONInFile;

public class JsonGeneratorTest implements TestUtils {

    @Test
    public void shouldWriteContentInANewJsonObject() {
        TestJsonGenerator jsonGenerator = new TestJsonGenerator();

        String result = jsonGenerator.newJson(Unchecked.consumer(jsonWriter -> {
            jsonWriter.name("surname");
            jsonWriter.value("Doe");
        }
        ));

        assertThat(result, equalToJSONInFile(readFileFromClasspath("json/json_generator_test.json")));
    }

    private static class TestJsonGenerator implements JsonGenerator {}
}

