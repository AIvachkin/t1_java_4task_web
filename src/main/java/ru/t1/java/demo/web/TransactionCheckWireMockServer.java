package ru.t1.java.demo.web;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.Slf4jNotifier;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

@Component
@Slf4j
public class TransactionCheckWireMockServer {

    @Value("${integration.url}")
    private String url;

    @Value("${integration.host}")
    private String host;

    @Value("${integration.port}")
    private int port;

    @Value("${integration.resource}")
    private String resource;

    @PostConstruct
    public void setup() {
        WireMockServer wireMockServer = new WireMockServer(WireMockConfiguration.options()
                .port(port)
                .notifier(new Slf4jNotifier(true)));

        wireMockServer.start();
        WireMock.configureFor(host, port);

        stubCheckClientResponse(wireMockServer);

        log.info("WireMock server started at {}", url);
    }

    private void stubCheckClientResponse(WireMockServer wireMockServer) {
        wireMockServer.stubFor(post(urlEqualTo(resource))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("{\"blocked\":false}")));
    }
}