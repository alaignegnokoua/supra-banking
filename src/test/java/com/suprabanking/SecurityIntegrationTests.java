package com.suprabanking;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.suprabanking.models.Compte;
import com.suprabanking.models.Transaction;
import com.suprabanking.models.User;
import com.suprabanking.repositories.CompteRepository;
import com.suprabanking.repositories.TransactionRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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

    @Autowired
    private TransactionRepository transactionRepository;

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

        @Test
        void currentUserEndpointShouldReturnClientProfileFields() throws Exception {
        String token = registerAndGetToken("clientProfile", "clientProfile@test.local", "Secret123!");

        mockMvc.perform(get("/api/auth/me")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value("clientProfile"))
            .andExpect(jsonPath("$.clientId").isNumber())
            .andExpect(jsonPath("$.clientEmail").value("clientProfile@test.local"));
        }

        @Test
        void currentUserShouldUpdateOwnProfile() throws Exception {
        String token = registerAndGetToken("clientEdit", "clientEdit@test.local", "Secret123!");

        String payload = """
            {
              "nom": "Diallo",
              "prenom": "Aminata",
              "email": "clientEdit.updated@test.local",
              "telephone": "+2250102030405"
            }
            """;

        mockMvc.perform(put("/api/auth/me/profile")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value("clientEdit"))
            .andExpect(jsonPath("$.email").value("clientEdit.updated@test.local"))
            .andExpect(jsonPath("$.clientNom").value("Diallo"))
            .andExpect(jsonPath("$.clientPrenom").value("Aminata"))
            .andExpect(jsonPath("$.clientTelephone").value("+2250102030405"));
        }

        @Test
        void currentUserShouldChangeOwnPassword() throws Exception {
        String token = registerAndGetToken("clientPwdA", "clientPwdA@test.local", "Secret123!");

        String payload = """
            {
              "currentPassword": "Secret123!",
              "newPassword": "Secret456!"
            }
            """;

        mockMvc.perform(put("/api/auth/me/password")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isNoContent());

        String oldLoginPayload = """
            {
              "username": "clientPwdA",
              "password": "Secret123!"
            }
            """;

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(oldLoginPayload))
            .andExpect(status().isUnauthorized());

        String newLoginPayload = """
            {
              "username": "clientPwdA",
              "password": "Secret456!"
            }
            """;

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(newLoginPayload))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").isString());
        }

        @Test
        void currentUserShouldUpdateNotificationPreferences() throws Exception {
        String token = registerAndGetToken("clientPrefA", "clientPrefA@test.local", "Secret123!");

        String payload = """
            {
              "notificationsInAppEnabled": true,
              "notificationsEmailEnabled": true
            }
            """;

        mockMvc.perform(put("/api/auth/me/preferences")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.notificationsInAppEnabled").value(true))
            .andExpect(jsonPath("$.notificationsEmailEnabled").value(true));
        }

        @Test
        void clientShouldBeForbiddenWhenReadingAnotherClientCompteById() throws Exception {
        String tokenUser1 = registerAndGetToken("clientOwn1", "clientOwn1@test.local", "Secret123!");
        registerAndGetToken("clientOwn2", "clientOwn2@test.local", "Secret123!");

        User user1 = userRepository.findByUsername("clientOwn1").orElseThrow();
        User user2 = userRepository.findByUsername("clientOwn2").orElseThrow();

        Compte compteUser1 = new Compte();
        compteUser1.setNumeroCompte("ACC-OWN-1");
        compteUser1.setType("courant");
        compteUser1.setSolde(1000.0);
        compteUser1.setDateCreation(LocalDateTime.now());
        compteUser1.setClient(user1.getClient());
        compteRepository.save(compteUser1);

        Compte compteUser2 = new Compte();
        compteUser2.setNumeroCompte("ACC-OWN-2");
        compteUser2.setType("epargne");
        compteUser2.setSolde(2500.0);
        compteUser2.setDateCreation(LocalDateTime.now());
        compteUser2.setClient(user2.getClient());
        compteUser2 = compteRepository.save(compteUser2);

        mockMvc.perform(get("/api/comptes/" + compteUser2.getId())
                .header("Authorization", "Bearer " + tokenUser1))
            .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/comptes/me/" + compteUser2.getId())
                .header("Authorization", "Bearer " + tokenUser1))
            .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/comptes/me/" + compteUser1.getId())
                .header("Authorization", "Bearer " + tokenUser1))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("ACC-OWN-1")));
        }

        @Test
        void clientShouldReadFilteredTransactionsOnlyOnOwnCompte() throws Exception {
        String tokenUser1 = registerAndGetToken("clientTx1", "clientTx1@test.local", "Secret123!");
        registerAndGetToken("clientTx2", "clientTx2@test.local", "Secret123!");

        User user1 = userRepository.findByUsername("clientTx1").orElseThrow();
        User user2 = userRepository.findByUsername("clientTx2").orElseThrow();

        Compte compteUser1 = new Compte();
        compteUser1.setNumeroCompte("TX-OWN-1");
        compteUser1.setType("courant");
        compteUser1.setSolde(5000.0);
        compteUser1.setDateCreation(LocalDateTime.now());
        compteUser1.setClient(user1.getClient());
        compteUser1 = compteRepository.save(compteUser1);

        Compte compteUser2 = new Compte();
        compteUser2.setNumeroCompte("TX-OWN-2");
        compteUser2.setType("epargne");
        compteUser2.setSolde(8000.0);
        compteUser2.setDateCreation(LocalDateTime.now());
        compteUser2.setClient(user2.getClient());
        compteUser2 = compteRepository.save(compteUser2);

        Transaction txDepot = new Transaction();
        txDepot.setType("depot");
        txDepot.setMontant(1500.0);
        txDepot.setDateTransaction(LocalDateTime.now().minusDays(1));
        txDepot.setDescription("Depot salaire");
        txDepot.setClient(user1.getClient());
        txDepot.setCompte(compteUser1);
        transactionRepository.save(txDepot);

        Transaction txRetrait = new Transaction();
        txRetrait.setType("retrait");
        txRetrait.setMontant(200.0);
        txRetrait.setDateTransaction(LocalDateTime.now().minusHours(8));
        txRetrait.setDescription("Retrait DAB");
        txRetrait.setClient(user1.getClient());
        txRetrait.setCompte(compteUser1);
        transactionRepository.save(txRetrait);

        Transaction txOtherUser = new Transaction();
        txOtherUser.setType("depot");
        txOtherUser.setMontant(9999.0);
        txOtherUser.setDateTransaction(LocalDateTime.now());
        txOtherUser.setDescription("Autre client");
        txOtherUser.setClient(user2.getClient());
        txOtherUser.setCompte(compteUser2);
        transactionRepository.save(txOtherUser);

        mockMvc.perform(get("/api/transactions/me/compte/" + compteUser1.getId())
                .queryParam("type", "depot")
                .queryParam("montantMin", "1000")
                .header("Authorization", "Bearer " + tokenUser1))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Depot salaire")))
            .andExpect(content().string(not(containsString("Retrait DAB"))))
            .andExpect(content().string(not(containsString("Autre client"))));

        mockMvc.perform(get("/api/transactions/me/compte/" + compteUser2.getId())
                .header("Authorization", "Bearer " + tokenUser1))
            .andExpect(status().isForbidden());
        }

        @Test
        void clientShouldProcessInternalTransferBetweenOwnAccounts() throws Exception {
        String token = registerAndGetToken("clientTransferOk", "clientTransferOk@test.local", "Secret123!");
        User user = userRepository.findByUsername("clientTransferOk").orElseThrow();

        Compte source = new Compte();
        source.setNumeroCompte("TR-SRC-1");
        source.setType("courant");
        source.setSolde(1200.0);
        source.setDateCreation(LocalDateTime.now());
        source.setClient(user.getClient());
        source = compteRepository.save(source);

        Compte destination = new Compte();
        destination.setNumeroCompte("TR-DST-1");
        destination.setType("epargne");
        destination.setSolde(300.0);
        destination.setDateCreation(LocalDateTime.now());
        destination.setClient(user.getClient());
        destination = compteRepository.save(destination);

        String payload = """
            {
              "compteSourceId": %d,
              "compteDestinationId": %d,
              "montant": 250,
              "description": "Transfert test"
            }
            """.formatted(source.getId(), destination.getId());

        mockMvc.perform(post("/api/transactions/me/virement-interne")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isNoContent());

        Compte sourceUpdated = compteRepository.findById(source.getId()).orElseThrow();
        Compte destinationUpdated = compteRepository.findById(destination.getId()).orElseThrow();

        org.junit.jupiter.api.Assertions.assertEquals(950.0, sourceUpdated.getSolde());
        org.junit.jupiter.api.Assertions.assertEquals(550.0, destinationUpdated.getSolde());

        mockMvc.perform(get("/api/transactions/me/compte/" + source.getId())
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("débit vers TR-DST-1")));

        mockMvc.perform(get("/api/transactions/me/compte/" + destination.getId())
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("crédit depuis TR-SRC-1")));
        }

        @Test
        void clientShouldNotProcessInternalTransferWhenBalanceIsInsufficient() throws Exception {
        String token = registerAndGetToken("clientTransferKo", "clientTransferKo@test.local", "Secret123!");
        User user = userRepository.findByUsername("clientTransferKo").orElseThrow();

        Compte source = new Compte();
        source.setNumeroCompte("TR-SRC-2");
        source.setType("courant");
        source.setSolde(100.0);
        source.setDateCreation(LocalDateTime.now());
        source.setClient(user.getClient());
        source = compteRepository.save(source);

        Compte destination = new Compte();
        destination.setNumeroCompte("TR-DST-2");
        destination.setType("epargne");
        destination.setSolde(50.0);
        destination.setDateCreation(LocalDateTime.now());
        destination.setClient(user.getClient());
        destination = compteRepository.save(destination);

        String payload = """
            {
              "compteSourceId": %d,
              "compteDestinationId": %d,
              "montant": 500,
              "description": "Transfert impossible"
            }
            """.formatted(source.getId(), destination.getId());

        mockMvc.perform(post("/api/transactions/me/virement-interne")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Solde insuffisant")));
        }

        @Test
        void clientShouldManageOwnBeneficiaires() throws Exception {
        String token = registerAndGetToken("clientBenA", "clientBenA@test.local", "Secret123!");

        String payload = """
            {
              "nom": "Alice Martin",
              "iban": "FR7612345678901234567890123",
              "banque": "Banque Test",
              "email": "alice.martin@test.local"
            }
            """;

        MvcResult created = mockMvc.perform(post("/api/beneficiaires/me")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isCreated())
            .andExpect(content().string(containsString("Alice Martin")))
            .andReturn();

        Long beneficiaireId = objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get("/api/beneficiaires/me")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Alice Martin")));

        mockMvc.perform(delete("/api/beneficiaires/me/" + beneficiaireId)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isNoContent());
        }

        @Test
        void clientShouldProcessExternalTransferWithOwnBeneficiaire() throws Exception {
        String token = registerAndGetToken("clientExtA", "clientExtA@test.local", "Secret123!");
        User user = userRepository.findByUsername("clientExtA").orElseThrow();

        Compte source = new Compte();
        source.setNumeroCompte("EXT-SRC-1");
        source.setType("courant");
        source.setSolde(2000.0);
        source.setDateCreation(LocalDateTime.now());
        source.setClient(user.getClient());
        source = compteRepository.save(source);

        String payloadBeneficiaire = """
            {
              "nom": "Fournisseur X",
              "iban": "FR7630001007941234567890185",
              "banque": "Banque Externe",
              "email": "fournisseur@test.local"
            }
            """;

        MvcResult benResult = mockMvc.perform(post("/api/beneficiaires/me")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadBeneficiaire))
            .andExpect(status().isCreated())
            .andReturn();

        Long beneficiaireId = objectMapper.readTree(benResult.getResponse().getContentAsString()).get("id").asLong();

        String payloadTransfer = """
            {
              "compteSourceId": %d,
              "beneficiaireId": %d,
              "montant": 500,
              "description": "Paiement fournisseur"
            }
            """.formatted(source.getId(), beneficiaireId);

        mockMvc.perform(post("/api/transactions/me/virement-externe")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadTransfer))
            .andExpect(status().isNoContent());

        Compte sourceUpdated = compteRepository.findById(source.getId()).orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals(1500.0, sourceUpdated.getSolde());

        mockMvc.perform(get("/api/transactions/me/compte/" + source.getId())
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("virement_externe")))
            .andExpect(content().string(containsString("Fournisseur X")));

        mockMvc.perform(get("/api/notifications/me")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Virement externe effectué")));

        mockMvc.perform(get("/api/audits/me")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("VIREMENT_EXTERNE")))
            .andExpect(content().string(containsString("SUCCES")));
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
