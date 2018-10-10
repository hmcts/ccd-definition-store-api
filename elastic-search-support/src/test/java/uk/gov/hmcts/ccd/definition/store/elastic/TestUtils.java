package uk.gov.hmcts.ccd.definition.store.elastic;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

public interface TestUtils {

    default Path readFileFromClasspath(final String fileName) {
        try {
            return Paths.get(getClass().getClassLoader().getResource(fileName).toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
