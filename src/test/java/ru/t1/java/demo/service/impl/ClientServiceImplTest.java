package ru.t1.java.demo.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.t1.java.demo.kafka.KafkaClientProducer;
import ru.t1.java.demo.model.Client;
import ru.t1.java.demo.model.dto.CheckResponse;
import ru.t1.java.demo.model.dto.ClientDto;
import ru.t1.java.demo.repository.ClientRepository;
import ru.t1.java.demo.web.CheckWebClient;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientServiceImplTest {

//    @Spy
//    ObjectMapper mapper = new ObjectMapper();

    @InjectMocks
    @Spy
    ClientServiceImpl clientService;

    @Mock
    ClientRepository clientRepository;

    @Mock
    KafkaClientProducer kafkaClientProducer;

    @Mock
    CheckWebClient checkWebClient;

    @Mock
    ClientServiceImpl clientServiceMock;

    @Test
    void parseJsonSpy() {
        when(clientService.parseJson())
                .thenReturn(List.of(ClientDto.builder()
                        .build()));

        assertEquals(List.of(ClientDto.builder().build()), clientService.parseJson());
    }

    @Test
    void parseJsonMock() {

        List<ClientDto> clients = List.of(ClientDto.builder()
                .firstName("first_name_1")
                .build(), ClientDto.builder()
                .firstName("first_name_2")
                .build());

//        when(clientServiceMock.parseJson())
//                .thenReturn(List.of(ClientDto.builder().build()));

        assertEquals(clients,
                clientService.parseJson());
    }

    @Test
    void registerClient_WithValidClient_ShouldSaveAndSendToKafka() {
        Client client = getClient();

        when(checkWebClient.check(client.getId())).thenReturn(Optional.of(new CheckResponse(false)));
        when(clientRepository.save(any(Client.class))).thenReturn(client);

        Client savedClient = clientService.registerClient(client);

        assertNotNull(savedClient);
        verify(clientRepository, times(1)).save(any(Client.class));
        verify(kafkaClientProducer, times(1)).send(client.getId());
    }

    @Test
    void registerClient_WithBlockedClient_ShouldNotSaveClient() {
        Client client = getClient();

        when(checkWebClient.check(client.getId())).thenReturn(Optional.of(new CheckResponse(true)));

        Client savedClient = clientService.registerClient(client);

        assertNull(savedClient);
        verify(clientRepository, never()).save(any(Client.class));
        verify(kafkaClientProducer, never()).send(any());
    }

    @Test
    void registerClients_WithValidClients_ShouldSaveAndSendToKafka() {
        List<Client> clients = getClients();

        when(checkWebClient.check(any())).thenReturn(Optional.of(new CheckResponse(false)));
        when(clientRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        List<Client> savedClients = clientService.registerClients(clients);

        verify(clientRepository, times(2)).save(any());
        verify(kafkaClientProducer, times(2)).send(any());
        assertEquals(2, savedClients.size());
    }

    @Test
    void registerClients_WithBlockedClient_ShouldNotSaveClient() {
        List<Client> clients = getClients();

        when(checkWebClient.check(any())).thenReturn(
                Optional.of(new CheckResponse(true)),
                Optional.of(new CheckResponse(false)));
        when(clientRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        List<Client> savedClients = clientService.registerClients(clients);

        verify(clientRepository, times(1)).save(any());
        verify(kafkaClientProducer, times(1)).send(any());
        assertEquals(1, savedClients.size());
    }

    @Test
    void clearMiddleName() {
        List<ClientDto> dtos = Arrays.asList(
                new ClientDto("John", "Doe", "Middle"),
                new ClientDto("Jane", "Doe", "Doe")
        );

        clientService.clearMiddleName(dtos);

        dtos.forEach(dto -> assertNull(dto.getMiddleName()));
    }

    private List<Client> getClients() {
        return Arrays.asList(
                new Client("Ivan", "Ivanov", "Ivanovich", false, "test"),
                new Client("Peter", "Petrov", "Petrovich", false, "test")
        );
    }

    private Client getClient() {
        return new Client("Ivan", "Ivanov", "Ivanovich", false, "test");
    }
}