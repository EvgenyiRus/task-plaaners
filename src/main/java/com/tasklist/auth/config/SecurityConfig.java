package com.tasklist.auth.config;

import com.tasklist.auth.filter.AuthTokenFilter;
import com.tasklist.auth.filter.ExceptionHandlerFilter;
import com.tasklist.auth.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.session.SessionManagementFilter;


/*
 prePostEnabled позволяет использовать аннотации pre/post в компонентах Spring
 (@PreAuthorize для доступа к методам или контроллеру только с нужными правами)
 */
@Configuration
@EnableWebSecurity(debug = true) // вывод подробностей фильтров безопасности в лог
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableAsync // позволяет ассинхронно выполнять методы
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    // для получение пользователя из БД
    private UserDetailsServiceImpl userDetailsService;

    // перехватывает все входящие запросы (jwt если необходимо)
    private AuthTokenFilter authTokenFilter;
    private ExceptionHandlerFilter exceptionHandlerFilter;

    @Autowired
    public void setUserDetailsService(UserDetailsServiceImpl userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Autowired
    public void setAuthTokenFilter(AuthTokenFilter authTokenFilter) {
        this.authTokenFilter = authTokenFilter;
    }

    @Autowired
    public void setExceptionHandlerFilter(ExceptionHandlerFilter exceptionHandlerFilter) {
        this.exceptionHandlerFilter = exceptionHandlerFilter;
    }

    // для хеширования паролей используется алгоритм Bcrypt
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // стандартный готовый authenticationManager из Spring контейнера (используется для проверки логина-пароля)
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    // указываем наш сервис userDetailsService для проверки пользователя в БД и кодировщик паролей
    // эти методы доступны в документации Spring Security
    @Override
    public void configure(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
        // настройки AuthenticationManager для правильной проверки логин-пароль
        authenticationManagerBuilder.
                userDetailsService(userDetailsService). // использовать наш сервис для загрузки User из БД
                passwordEncoder(passwordEncoder()); // указываем кодировщик пароля (для корректной проверки пароля)
    }

    /* исключение authTokenFilter из цепочки фильтров сервлетов для всех запросов
       (отключение вызова authTokenFilter в servlet контейнере т.к. добавили вручную в spring контейнер)
       баг? https://stackoverflow.com/questions/39314176/filter-invoke-twice-when-register-as-spring-bean
    */
    @Bean
    public FilterRegistrationBean registrationBean(AuthTokenFilter authTokenFilter) {
        // FilterRegistrationBean - регистратор фильтров в сервлет контейнере
        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean(authTokenFilter);
        filterRegistrationBean.setEnabled(false); // отключение использования фильтра
        return filterRegistrationBean;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        /* если используется другая клиентская технология (не SpringMVC, а например Angular, React и пр.),
            то выключаем встроенную Spring-защиту от CSRF атак,
            иначе запросы от клиента не будут обрабатываться, т.к. Spring Security будет пытаться в каждом входящем запроcе искать спец. токен для защиты от CSRF
        */

        /* способ хранения сессии на сервере
           always - сохраняется JSESSIONID в куки браузера
           if_required(по умолчанию) - JSESSIONID создастся только после авторизации
           never - сессия создается, если есть JSESSIONID
           statеless - сессия никода не сохраняется на сервере(куки не будет)
         */
        /*
            Отключение хранение сессии на сервере, в этом нет необходимости.
            Клиент вызывает RESTful API сервера и передает токен с информацией о пользователе.
         */
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        /* отключаем на время разработки(для методов post put и др.
           которые изменяют данные, будут без ошибок)
         */

        http.csrf().disable();
        http.httpBasic().disable(); //отключаем стандартную авторизацию Spring
        http.formLogin().disable(); //отключаем стандартную форму логирования
        http.requiresChannel().anyRequest().requiresSecure(); //Обязательное исп. HTTPS

        /*
         внедрение фильтров в securityFilterChain
         1. exceptionHandlerFilter
         2. authTokenFilter
            ...
         */

        // перехват ошибок авторизации до вызова оснонвых фильтров в SessionManagementFilter
        http.addFilterBefore(authTokenFilter, SessionManagementFilter.class);

        // перехват ошибок на уровне фильтров.Всех последующих фильтров и отправляет их клиенту в формате JSON
        // этот фильтр должен обязательно находиться перед всеми остальными фильтрами
        http.addFilterBefore(exceptionHandlerFilter, AuthTokenFilter.class);
    }
}
