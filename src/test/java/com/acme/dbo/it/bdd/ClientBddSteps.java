package com.acme.dbo.it.bdd;

import com.acme.dbo.account.dao.AccountRepository;
import com.acme.dbo.client.dao.ClientRepository;
import com.acme.dbo.client.domain.Client;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.DisabledIf;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static java.lang.Integer.parseInt;
import static lombok.AccessLevel.PRIVATE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/**
 * https://cucumber.io/docs/cucumber/cucumber-expressions/
 */
@DisabledIf(expression = "#{environment['features.client'] == 'false'}", loadContext = true)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("it")
@Slf4j
@FieldDefaults(level = PRIVATE)
public class ClientBddSteps {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper jsonMapper;
    @Autowired ClientRepository clients;
    @Autowired AccountRepository accounts;

    private List<Client> foundClients;

    @Given("^application is running$")  // задаем начальные условия
    public void applicationIsRunning() throws Exception {
        mockMvc.perform(get("/actuator/health"))    // делаем заглушку через исполнение гет запроса по адресу
            .andExpect(status().isOk()) // ожидаем отклик Ок
            .andExpect(jsonPath("$.status", is("UP"))); // и ожидаем получения json со значениями
    }

    @Given("^application has no stored clients$")
    public void applicationHasNoStoredClients() {
        accounts.deleteAll(); //Due to FK       удаляем все аккаунты
        clients.deleteAll();                 // удаляем всех клинтов
    }

    @When("^user requests all clients$")        // когда пользоватлеь делает следующиее
    public void userRequestsAllClients() throws Exception {
        MockHttpServletResponse response = mockMvc.perform( // запрос на получение данных о клиентах
                get("/api/client").header("X-API-VERSION", "1"))
                    .andExpect(status().isOk())
                    .andReturn().getResponse();

        foundClients = Arrays.asList(       // найденных клиентов складываем в массив
                jsonMapper.readValue(       // через маппер json
                    response.getContentAsString(),  // полученные даные о клиентах
                    Client[].class                  // как класс Client
        ));
    }

    @Then("^user got (.*)clients$")         // тогда
    public void userGetClients(String howMany) {    // разбираем строку ответа
        switch (howMany) {
            case "no " : assertThat(foundClients).isEmpty(); break; // при ответе нет  - проверяем то получили пустое значение
            case "" : assertThat(foundClients).isNotNull(); break;  // при пустом ответе - проверяем что получили не пустое значение клиентов
            default : assertThat(foundClients).hasSize(parseInt(howMany.trim())); break; // в остальных случаях проверяем полученное количество
        }
    }

    @Given("application has stored clients")        // Дано: в приложении есль клиенты
    public void applicationHasStoredClients() {
        assumeFalse(clients.findAll().isEmpty());
    } // принять что clients имеет не пустое значение

    @And("client stored with login {string}, secret {string}, salt {string}") //  и установить что
    public void storeClient(String login, String secret, String salt) throws Exception {
        Client client = Client.builder() // client задается через билдер с заданными параметрами
                .login(login)
                .secret(secret)
                .salt(salt)
            .build();

        mockMvc.perform(post("/api/client")     // через заглушку посылаем post запрос
                .contentType("application/json")           // с json значением
                .content(jsonMapper.writeValueAsString(client)) // client
                .header("X-API-VERSION", "1"))
            .andExpect(status().isCreated());   // ожидаем статус создано
    }

    @And("client got with login {string}, secret {string}, salt {string}, {word}") // и
    public void gotClient(String login, String secret, String salt, String enabled) {
        assertThat(foundClients).contains(  // проверям что в найденных клиентах  содержится
                Client.builder()            // клиент с начально  заданными значениями
                        .login(login)
                        .secret(secret)
                        .salt(salt)
                        .enabled("enabled".equals(enabled)) // и доступный
                .build()
        );
    }
}
