package com.suprabanking;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.suprabanking.models.Compte;
import com.suprabanking.models.User;
import com.suprabanking.repositories.CompteRepository;
import com.suprabanking.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompteRepository compteRepository;

    @Test
    void clientShouldAuthenticateAndReadOnlyOwnComptes() throws Exception {
        String tokenUser1 = registerAndGetToken("clientA", "clientA@test.local", "Secret123!");
        registerAndGetToken("clientB", "clientB@test.local", "Secret123!");

        User user1 = userRepository.findByUsername("clientA").orElseThrow();
        User user2 = userRepository.findByUsername("clientB").orElseThrow();

        Compte compte1 = new Compte();
        compte1.setNumeroCompte("C-USER1");
        compte1.setType("courant");
        compte1.setSolde(1500.0);
        compte1.setDateCreation(LocalDateTime.now());
        compte1.setClient(user1.getClient());
        compteRepository.save(compte1);

        Compte compte2 = new Compte();
        compte2.setNumeroCompte("C-USER2");
        compte2.setType("epargne");
        compte2.setSolde(3000.0);
        compte2.setDateCreation(LocalDateTime.now());
        compte2.setClient(user2.getClient());
        compteRepository.save(compte2);

        mockMvc.perform(get("/api/comptes")
                        .header("Authorization", "Bearer " + tokenUser1))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("C-USER1")))
                .andExpect(content().string(not(containsString("C-USER2"))));
    }

    @Test
    void clientShouldNotAccessAdminAgentClientEndpoints() throws Exception {
        String token = registerAndGetToken("clientC", "clientC@test.local", "Secret123!");

        mockMvc.perform(get("/api/clients")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    private String registerAndGetToken(String username, String email, String password) throws Exception {
        String payload = """
                {
                  "username": "%s",
                  "email": "%s",
                  "password": "%s"
                }
                """.formatted(username, email, password);

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());
        return jsonNode.get("token").asText();
    }
}
