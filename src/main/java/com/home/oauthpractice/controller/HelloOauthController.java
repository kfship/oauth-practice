package com.home.oauthpractice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.org.apache.xml.internal.security.utils.Base64;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.security.Principal;


@RestController
@Slf4j
public class HelloOauthController {

    @GetMapping("/test/call")
    public String testCall() {
        return "Hello Oauth2!!";
    }

    @GetMapping("/api/test")
    public String apiTest(Principal principal) {
        log.info("principal = " + principal);
        if (principal == null) {
            return "principal is null";
        }
        return principal.getName();
    }

    @GetMapping("/api/me")
    public String apiMe(Principal principal) {
        log.info("principal = " + principal);
        if(principal == null) {
            return "principal is null";
        }
        return principal.getName();
    }

    private RestTemplate restTemplate;
    private ObjectMapper objectMapper;

    @GetMapping("/oauth2/callback")
    public String callback(@RequestParam String code) throws Exception {
        System.out.println("code = " + code);

        OauthTokenDto token = getToken(code);
        System.out.println("getToken() = " + token);

        return token.toString();
    }

    /**
     * token을 호출하여 access_token 획득
     * @param code
     * @return
     * @throws JsonProcessingException
     */
    private OauthTokenDto getToken(String code) throws Exception {

        restTemplate = new RestTemplate();
        objectMapper = new ObjectMapper();

        String credentials = "testClientId:testSecret";
        String encodedCredentials = new String(Base64.encode(credentials.getBytes()));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.add("Authorization", "Basic " + encodedCredentials);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("grant_type", "authorization_code");
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<String> response = restTemplate.postForEntity("http://localhost:8080/oauth/token", request, String.class);
        if (response.getStatusCode() == HttpStatus.OK) {
            System.out.println("response.getBody() = " + response.getBody());
            OauthTokenDto oauthTokenDto = objectMapper.readValue(response.getBody(), OauthTokenDto.class);
            return oauthTokenDto;
        }
        return null;
    }

    @Getter
    @ToString
    static class OauthTokenDto {
        private String access_token;
        private String token_type;
        private String refresh_token;
        private long expires_in;
        private String scope;
    }
}
