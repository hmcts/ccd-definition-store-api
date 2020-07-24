package uk.gov.hmcts.ccd.definition.store.elastic.mapping.support;

import org.jooq.lambda.Unchecked;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.definition.store.elastic.TestUtils;
import uk.gov.hmcts.ccd.definition.store.elastic.exception.ElasticSearchInitialisationException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

    @Test
    public void shouldThrowElasticSearchInitialisationExceptionOnErrors() {
        TestJsonGenerator jsonGenerator = new TestJsonGenerator();

        assertThrows(ElasticSearchInitialisationException.class, () -> {
            jsonGenerator.newJson(Unchecked.consumer(jsonWriter -> {
                    throw new ArrayIndexOutOfBoundsException("test");
                }
            ));
        });
    }

    private static class TestJsonGenerator implements JsonGenerator {
    }
}

