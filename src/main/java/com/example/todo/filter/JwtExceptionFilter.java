package com.example.todo.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Component
@Slf4j
public class JwtExceptionFilter extends OncePerRequestFilter {
    
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // 예외가 발생하지 않으면 Auth Filter로 통과~
            filterChain.doFilter(request, response); // 필터를 통과하는 메서드
        } catch (JwtException e) {
            // 토큰이 만료되었을 시 Auth Filter에서 예외를 강제 발생 -> 앞에 있는 Exception Filter로 전달
            log.info("만료 예외 발생! - {}", e.getMessage());
            setErrorResponse(response, e);
        }
    }

    private void setErrorResponse(HttpServletResponse response, JwtException e) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json; charset=UTF-8");

        Map<String,Object> responseMap = new HashMap<>();
        responseMap.put("message", e.getMessage());
        responseMap.put("code", 401); // 401이 오면 만료가 된거 군아

        // Map을 JSON문자열로 변환
        String jsonString = new ObjectMapper().writeValueAsString(responseMap);

        // json 데이터를 응답객체에 실어서 브라우저로 바로 응답.
        response.getWriter().write(jsonString);
    }
}
