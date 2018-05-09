package uk.gov.hmcts.ccd.definitionstore.tests.helper;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;

public class S2SHelper {

    private final String s2sUrl;
    private final String secret;
    private final String microservice;
    private final GoogleAuthenticator googleAuthenticator;

    public S2SHelper(final String s2sUrl, final String secret, final String microservice) {
        this.s2sUrl = s2sUrl;
        this.secret = secret;
        this.microservice = microservice;
        this.googleAuthenticator = new GoogleAuthenticator();
    }

    // Weird dependency problems with feign in this project
    public String getToken() {
        final String oneTimePassword = format("%06d", googleAuthenticator.getTotpPassword(secret));

        Map<String, String> signInDetails = new HashMap<>();
        signInDetails.put("microservice", this.microservice);
        signInDetails.put("oneTimePassword", oneTimePassword);

        return new RestTemplate().postForEntity(s2sUrl + "/lease", signInDetails, String.class).getBody();
    }
}
