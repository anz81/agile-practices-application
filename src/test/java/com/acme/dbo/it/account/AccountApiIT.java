package com.acme.dbo.it.account;

import com.acme.dbo.account.domain.Account;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.DisabledIf;
import org.springframework.test.web.servlet.MockMvc;

import static com.acme.dbo.account.domain.Account.builder;
import static lombok.AccessLevel.PRIVATE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisabledIf(expression = "#{environment['features.account'] == 'false'}", loadContext = true)
// отключно если условие
@SpringBootTest // использовать SpringBoot в тесте, запускаем все слои
@AutoConfigureMockMvc // будем использовать заглушки MockMVC
@ActiveProfiles("it") // активируем профиль it для теста
@Slf4j // создаем логгер
@FieldDefaults(level = PRIVATE) // добавляем полям модификатор private
public class AccountApiIT {
    @Autowired MockMvc mockMvc;     // Автоматически создаем объект MockMVC
    @Autowired ObjectMapper jsonMapper; // Автоматически создаем объект ObjectMapper

    @Test // указываем, что этот метод тест
    public void shouldGetAccountsWhenPrepopulatedDbHas() throws Exception {
        String accountsFoundJsonString = mockMvc.perform(   // создаем заглушку запрс
                get("/api/account").header("X-API-VERSION", "1") // сетод get по адресу с хедером
        ).andDo(print()).andExpect(status().is(200)) // выводим результат и ждем ответ 200 - ок
                .andReturn().getResponse().getContentAsString(); // возвращаемся и получаем ответ в качестве строки

        Account[] accountsFound = jsonMapper.readValue(accountsFoundJsonString, Account[].class); // расшифровываем какие Account мы получили

        assertThat(accountsFound).contains(    // проверяем наши утверждения
                builder().clientId(1L).amount(0.).build(),
                builder().clientId(1L).amount(100.).build(),
                builder().clientId(2L).amount(200.).build()
        );
    }
}
