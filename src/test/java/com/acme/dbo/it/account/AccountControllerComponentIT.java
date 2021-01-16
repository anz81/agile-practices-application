package com.acme.dbo.it.account;

import com.acme.dbo.account.controller.AccountController;
import com.acme.dbo.account.domain.Account;
import com.acme.dbo.account.service.AccountService;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.DisabledIf;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static lombok.AccessLevel.PRIVATE;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@DisabledIf(expression = "#{environment['features.account'] == 'false'}", loadContext = true)
@ExtendWith(MockitoExtension.class)    // используем расширение Mockito
@WebMvcTest(AccountController.class)  // указываем, что тестируем только MVC часть контроллера AccountController
@ActiveProfiles("it")
@Slf4j
@FieldDefaults(level = PRIVATE)
public class AccountControllerComponentIT {
    @Autowired AccountController sut;
    @MockBean AccountService accountServiceMock;  // создаем заглушку AccountService с помощью SpringBoot
    @Mock Account accountStub;  // создаем заглушку Account  с помощью Mockito

    @Test
    public void shouldNotGetAccountsWhenMockedDbIsEmpty() {
        given(accountServiceMock.getAccounts()).willReturn(emptyList()); // устанавливаем что в AccountService список аккаунтов пустой
        assertThat(sut.getAccounts()).isEmpty(); // проверяем что контроллер AccountController возвращает пустое значение аккаунтов
    }

    @Test
    public void shouldGetAccountWhenMockedDbHasOne() {
        given(accountServiceMock.getAccounts()).willReturn(singletonList(accountStub)); // устанавливаем что в AccountService в списке аккаунтов одна запись accountStub

        assertThat(sut.getAccounts()).containsOnly(accountStub); // проверяем что контроллер AccountController возвращает только значение accountStub
        verify(accountServiceMock, times(1)).getAccounts(); // проверяем количество обращений к Mock объекту
    }
}
