package uk.gov.hmcts.ccd.definition.store.rest.service;


import lombok.val;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultHttpResponseParserFactory;
import org.apache.http.impl.conn.ManagedHttpClientConnectionFactory;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.conn.SystemDefaultDnsResolver;
import org.apache.http.impl.io.DefaultHttpRequestWriterFactory;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

@Service
public class ProxyService {

    private static final Logger LOG = LoggerFactory.getLogger(ProxyService.class);

    @Transactional
    public String proxyRequest(String url) throws IOException {

        val request = new HttpGet(url);
        val httpClient = HttpClients.custom()
            .setConnectionManager(createHttpClientConnectionManager())
            .build();

        try {
            return httpClient.execute(request, httpResponse -> EntityUtils.toString(httpResponse.getEntity()));
        } finally {
            httpClient.close();
        }
    }

    @SuppressWarnings("squid:S1313")
    private HttpClientConnectionManager createHttpClientConnectionManager() {

        Map<String, String> hostToIp = new HashMap<>();
        hostToIp.put("ccd-user-profile-api-nonprod.service.core-compute-nonprod.internal", "51.140.33.54");

        return new PoolingHttpClientConnectionManager(
            RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.INSTANCE)
                .register("https", new SSLConnectionSocketFactory(SSLContexts.createSystemDefault()))
                .build(),
            new ManagedHttpClientConnectionFactory(
                new DefaultHttpRequestWriterFactory(), new DefaultHttpResponseParserFactory()),
            new SystemDefaultDnsResolver() {
                @Override
                public InetAddress[] resolve(String host) throws UnknownHostException {
                    if (hostToIp.containsKey(host)) {
                        String ip = hostToIp.get(host);
                        LOG.info(String.format("Resolved %s to %s internally", host, ip));
                        return new InetAddress[]{InetAddress.getByName(ip)};
                    } else {
                        try {
                            return super.resolve(host);
                        } catch (Exception e) {
                            LOG.error("Error looking up host " + host, e);
                            throw e;
                        }
                    }
                }
            }
        );
    }
}
