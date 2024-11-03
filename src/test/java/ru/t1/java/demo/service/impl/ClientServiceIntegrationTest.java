package ru.t1.java.demo.service.impl;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import ru.t1.java.demo.kafka.KafkaClientProducer;
import ru.t1.java.demo.model.Client;
import ru.t1.java.demo.repository.ClientRepository;
import ru.t1.java.demo.web.CheckWebClient;
import ru.t1.java.demo.web.TransactionCheckWireMockServer;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource("classpath:application-test.yml")
@Import(TransactionCheckWireMockServer.class)
@ActiveProfiles("test")
class ClientServiceIntegrationTest {

    @Mock
    KafkaClientProducer kafkaClientProducer;

    @MockBean
    ClientRepository clientRepository;

    //    @MockBean
    @Autowired
    CheckWebClient testCheckWebClient;

    //    @InjectMocks
    @Autowired
    ClientServiceImpl clientService;

    @Test
    void registerClientTest() {

        Client client = new Client();
        client.setFirstName("John");
        client.setLastName("Doe");

        Client client2 = new Client();
        client2.setId(422222L);
        client2.setFirstName("John");
        client2.setLastName("Doe");

        when(clientRepository.save(client)).thenReturn(client2);

        doNothing()
                .when(kafkaClientProducer)
                .send(anyLong());

        List<Client> clients = clientService.registerClients(List.of(client));

        assertThat(clients.get(0).getId()).isEqualTo(422222L);

    }

    @Test
    void registerClients_shouldSaveUnblockedClients() {
        List<Client> clients = Arrays.asList(
                new Client("Oleg", "Petrov", "Andreevich", false, "any"),
                new Client("Olga", "Petrova", "Andreevna", false, "any")
        );

        List<Client> savedClients = clientService.registerClients(clients);

        assertThat(savedClients).hasSize(2);
        assertThat(savedClients).extracting(Client::getId).containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    void registerClient_shouldSaveUnblockedClient() {
        Client client = new Client("Elena", "Ivanova", "Sergeevna", false, "any");
        Client savedClient = clientService.registerClient(client);

        assertThat(savedClient).isNotNull();
        assertThat(savedClient.getId()).isEqualTo(3L);
    }
}
