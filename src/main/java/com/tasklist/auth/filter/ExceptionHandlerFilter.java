package com.tasklist.auth.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tasklist.auth.object.JsonException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
  Обработчик ошибок на уровне фильтров. Срабатвает до вызова контроллеров.
 */
@Component
public class ExceptionHandlerFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws IOException, ServletException {
        try {
            filterChain.doFilter(request, response);
        } catch (RuntimeException ex) {
            JsonException jsonObject = new JsonException(ex.getClass().getSimpleName(), ex.getMessage());
            response.setStatus(HttpStatus.UNAUTHORIZED.value()); // статус - неавторизован
            response.getWriter().write(convertObjectToJson(jsonObject));
        }
    }

    // формирование JSON (с ипсользованием Jackson)
    private String convertObjectToJson(Object object) throws JsonProcessingException {
        if (object == null) {
            return null;
        }
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(object);
    }
}
