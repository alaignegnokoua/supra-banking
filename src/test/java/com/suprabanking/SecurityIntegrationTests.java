package com.suprabanking;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.suprabanking.models.Beneficiaire;
import com.suprabanking.models.Compte;
import com.suprabanking.models.Transaction;
import com.suprabanking.models.User;
import com.suprabanking.repositories.BeneficiaireRepository;
import com.suprabanking.repositories.ClientRepository;
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
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
    private ClientRepository clientRepository;

    @Autowired
    private BeneficiaireRepository beneficiaireRepository;

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
    void adminShouldUpdateClientRiskProfile() throws Exception {
        registerAndGetToken("clientRiskProfileAdmin", "clientRiskProfileAdmin@test.local", "Secret123!");
        User clientUser = userRepository.findByUsername("clientRiskProfileAdmin").orElseThrow();

        String adminToken = loginAndGetToken("admin", "Admin123!");

        String payload = """
            {
              "riskProfile": "VIP"
            }
            """;

        mockMvc.perform(patch("/api/clients/" + clientUser.getClient().getId() + "/risk-profile")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(clientUser.getClient().getId()))
            .andExpect(jsonPath("$.riskProfile").value("VIP"));
    }

    @Test
    void clientShouldNotUpdateClientRiskProfile() throws Exception {
        String clientToken = registerAndGetToken("clientRiskForbiddenA", "clientRiskForbiddenA@test.local", "Secret123!");
        registerAndGetToken("clientRiskForbiddenB", "clientRiskForbiddenB@test.local", "Secret123!");
        User otherUser = userRepository.findByUsername("clientRiskForbiddenB").orElseThrow();

        String payload = """
            {
              "riskProfile": "SENSIBLE"
            }
            """;

        mockMvc.perform(patch("/api/clients/" + otherUser.getClient().getId() + "/risk-profile")
                .header("Authorization", "Bearer " + clientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
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
        void clientShouldNotProcessExternalTransferWhenSingleLimitIsExceeded() throws Exception {
        String token = registerAndGetToken("clientLimitA", "clientLimitA@test.local", "Secret123!");
        User user = userRepository.findByUsername("clientLimitA").orElseThrow();

        Compte source = new Compte();
        source.setNumeroCompte("LIM-SRC-1");
        source.setType("courant");
        source.setSolde(50000.0);
        source.setDateCreation(LocalDateTime.now());
        source.setClient(user.getClient());
        source = compteRepository.save(source);

        String payloadBeneficiaire = """
            {
              "nom": "Prestataire Limit",
              "iban": "FR7630001007941234567890187",
              "banque": "Banque Externe",
              "email": "limit@test.local"
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
              "montant": 20000,
              "description": "Test plafond unitaire"
            }
            """.formatted(source.getId(), beneficiaireId);

        mockMvc.perform(post("/api/transactions/me/virement-externe")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadTransfer))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("plafond unitaire")));
        }

        @Test
        void clientShouldReadOwnTransferLimits() throws Exception {
        String token = registerAndGetToken("clientLimitStatus", "clientLimitStatus@test.local", "Secret123!");

        mockMvc.perform(get("/api/transactions/me/limits")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.maxSingleAmount").value(10000.0))
            .andExpect(jsonPath("$.maxDailyTotal").value(15000.0))
            .andExpect(jsonPath("$.maxDailyCount").value(10))
            .andExpect(jsonPath("$.minIntervalSeconds").value(120))
            .andExpect(jsonPath("$.todayOutgoingTotal").value(0.0))
            .andExpect(jsonPath("$.remainingDailyAmount").value(15000.0))
            .andExpect(jsonPath("$.todayOutgoingCount").value(0))
            .andExpect(jsonPath("$.remainingDailyCount").value(10))
            .andExpect(jsonPath("$.remainingCooldownSeconds").value(0));
        }

        @Test
        void clientShouldPreviewHighTransferRisk() throws Exception {
        String token = registerAndGetToken("clientRiskA", "clientRiskA@test.local", "Secret123!");
        User user = userRepository.findByUsername("clientRiskA").orElseThrow();

        Compte source = new Compte();
        source.setNumeroCompte("RISK-SRC-1");
        source.setType("courant");
        source.setSolde(100000.0);
        source.setDateCreation(LocalDateTime.now());
        source.setClient(user.getClient());
        source = compteRepository.save(source);

        for (int i = 0; i < 9; i++) {
            Transaction existingOutgoing = new Transaction();
            existingOutgoing.setType("virement_externe");
            existingOutgoing.setMontant(1600.0);
            existingOutgoing.setDateTransaction(LocalDateTime.now().minusMinutes(20 + i));
            existingOutgoing.setDescription("Virement de risque " + i);
            existingOutgoing.setClient(user.getClient());
            existingOutgoing.setCompte(source);
            transactionRepository.save(existingOutgoing);
        }

        mockMvc.perform(get("/api/transactions/me/risk-preview")
                .queryParam("montant", "10000")
                .queryParam("type", "EXTERNE")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.score").isNumber())
            .andExpect(jsonPath("$.level").value("ELEVE"))
            .andExpect(jsonPath("$.blocked").value(true))
            .andExpect(jsonPath("$.operationType").value("EXTERNE"))
            .andExpect(jsonPath("$.blockThreshold").value(90))
            .andExpect(jsonPath("$.amountScore").isNumber())
            .andExpect(jsonPath("$.dailyAmountScore").isNumber())
            .andExpect(jsonPath("$.dailyCountScore").isNumber())
            .andExpect(jsonPath("$.amountRatio").isNumber())
            .andExpect(jsonPath("$.dailyAmountRatio").isNumber())
            .andExpect(jsonPath("$.dailyCountRatio").isNumber());
        }

        @Test
        void riskPreviewShouldDifferentiateExternalAndInternalThresholds() throws Exception {
        String token = registerAndGetToken("clientRiskTypeA", "clientRiskTypeA@test.local", "Secret123!");
        User user = userRepository.findByUsername("clientRiskTypeA").orElseThrow();

        Compte source = new Compte();
        source.setNumeroCompte("RISK-TYPE-SRC-1");
        source.setType("courant");
        source.setSolde(100000.0);
        source.setDateCreation(LocalDateTime.now());
        source.setClient(user.getClient());
        source = compteRepository.save(source);

        for (int i = 0; i < 7; i++) {
            Transaction existingOutgoing = new Transaction();
            existingOutgoing.setType("virement_externe");
            existingOutgoing.setMontant(285.0);
            existingOutgoing.setDateTransaction(LocalDateTime.now().minusMinutes(15 + i));
            existingOutgoing.setDescription("Virement typé " + i);
            existingOutgoing.setClient(user.getClient());
            existingOutgoing.setCompte(source);
            transactionRepository.save(existingOutgoing);
        }

        mockMvc.perform(get("/api/transactions/me/risk-preview")
                .queryParam("montant", "10000")
                .queryParam("type", "EXTERNE")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.blocked").value(true));

        mockMvc.perform(get("/api/transactions/me/risk-preview")
                .queryParam("montant", "10000")
                .queryParam("type", "INTERNE")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.blocked").value(false))
            .andExpect(jsonPath("$.operationType").value("INTERNE"))
            .andExpect(jsonPath("$.blockThreshold").value(95));
        }

        @Test
        void riskPreviewShouldIncreaseScoreForNewBeneficiary() throws Exception {
        String token = registerAndGetToken("clientRiskBenA", "clientRiskBenA@test.local", "Secret123!");
        User user = userRepository.findByUsername("clientRiskBenA").orElseThrow();

        Beneficiaire oldBeneficiary = new Beneficiaire();
        oldBeneficiary.setNom("Ancien Benef");
        oldBeneficiary.setIban("FR7630001007941234567890101");
        oldBeneficiary.setBanque("Banque Test");
        oldBeneficiary.setEmail("ancien.benef@test.local");
        oldBeneficiary.setClient(user.getClient());
        oldBeneficiary.setCreatedAt(LocalDateTime.now().minusDays(3));
        oldBeneficiary = beneficiaireRepository.save(oldBeneficiary);

        Beneficiaire newBeneficiary = new Beneficiaire();
        newBeneficiary.setNom("Nouveau Benef");
        newBeneficiary.setIban("FR7630001007941234567890102");
        newBeneficiary.setBanque("Banque Test");
        newBeneficiary.setEmail("nouveau.benef@test.local");
        newBeneficiary.setClient(user.getClient());
        newBeneficiary.setCreatedAt(LocalDateTime.now());
        newBeneficiary = beneficiaireRepository.save(newBeneficiary);

        JsonNode oldRisk = objectMapper.readTree(
            mockMvc.perform(get("/api/transactions/me/risk-preview")
                    .queryParam("montant", "1000")
                    .queryParam("type", "EXTERNE")
                    .queryParam("beneficiaireId", oldBeneficiary.getId().toString())
                    .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.newBeneficiary").value(false))
                .andExpect(jsonPath("$.newBeneficiaryScore").value(0))
                .andReturn()
                .getResponse()
                .getContentAsString()
        );

        JsonNode newRisk = objectMapper.readTree(
            mockMvc.perform(get("/api/transactions/me/risk-preview")
                    .queryParam("montant", "1000")
                    .queryParam("type", "EXTERNE")
                    .queryParam("beneficiaireId", newBeneficiary.getId().toString())
                    .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.newBeneficiary").value(true))
                .andExpect(jsonPath("$.newBeneficiaryScore").value(15))
                .andReturn()
                .getResponse()
                .getContentAsString()
        );

        assertThat(newRisk.get("score").asInt()).isGreaterThan(oldRisk.get("score").asInt());
        }

        @Test
        void riskPreviewShouldExposeUnusualHourSignal() throws Exception {
        String token = registerAndGetToken("clientRiskHourA", "clientRiskHourA@test.local", "Secret123!");

        mockMvc.perform(get("/api/transactions/me/risk-preview")
                .queryParam("montant", "1000")
                .queryParam("type", "INTERNE")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.unusualHour").value(true))
            .andExpect(jsonPath("$.unusualHourScore").value(0));
        }

        @Test
        void riskPreviewShouldExposeMultiBeneficiaryVelocitySignal() throws Exception {
        String token = registerAndGetToken("clientRiskVelocityA", "clientRiskVelocityA@test.local", "Secret123!");
        User user = userRepository.findByUsername("clientRiskVelocityA").orElseThrow();

        Compte source = new Compte();
        source.setNumeroCompte("RISK-VELOCITY-SRC-1");
        source.setType("courant");
        source.setSolde(100000.0);
        source.setDateCreation(LocalDateTime.now());
        source.setClient(user.getClient());
        source = compteRepository.save(source);

        Beneficiaire b1 = new Beneficiaire();
        b1.setNom("Benef 1");
        b1.setIban("FR7630001007941234567890201");
        b1.setBanque("Banque Test");
        b1.setEmail("b1@test.local");
        b1.setClient(user.getClient());
        b1.setCreatedAt(LocalDateTime.now().minusDays(2));
        b1 = beneficiaireRepository.save(b1);

        Beneficiaire b2 = new Beneficiaire();
        b2.setNom("Benef 2");
        b2.setIban("FR7630001007941234567890202");
        b2.setBanque("Banque Test");
        b2.setEmail("b2@test.local");
        b2.setClient(user.getClient());
        b2.setCreatedAt(LocalDateTime.now().minusDays(2));
        b2 = beneficiaireRepository.save(b2);

        Beneficiaire b3 = new Beneficiaire();
        b3.setNom("Benef 3");
        b3.setIban("FR7630001007941234567890203");
        b3.setBanque("Banque Test");
        b3.setEmail("b3@test.local");
        b3.setClient(user.getClient());
        b3.setCreatedAt(LocalDateTime.now().minusDays(2));
        b3 = beneficiaireRepository.save(b3);

        Beneficiaire b4 = new Beneficiaire();
        b4.setNom("Benef 4");
        b4.setIban("FR7630001007941234567890204");
        b4.setBanque("Banque Test");
        b4.setEmail("b4@test.local");
        b4.setClient(user.getClient());
        b4.setCreatedAt(LocalDateTime.now().minusDays(2));
        b4 = beneficiaireRepository.save(b4);

        Transaction tx1 = new Transaction();
        tx1.setType("virement_externe");
        tx1.setMontant(200.0);
        tx1.setDateTransaction(LocalDateTime.now().minusMinutes(30));
        tx1.setDescription("Velocity 1");
        tx1.setClient(user.getClient());
        tx1.setCompte(source);
        tx1.setBeneficiaireId(b1.getId());
        transactionRepository.save(tx1);

        Transaction tx2 = new Transaction();
        tx2.setType("virement_externe");
        tx2.setMontant(220.0);
        tx2.setDateTransaction(LocalDateTime.now().minusMinutes(25));
        tx2.setDescription("Velocity 2");
        tx2.setClient(user.getClient());
        tx2.setCompte(source);
        tx2.setBeneficiaireId(b2.getId());
        transactionRepository.save(tx2);

        Transaction tx3 = new Transaction();
        tx3.setType("virement_externe");
        tx3.setMontant(240.0);
        tx3.setDateTransaction(LocalDateTime.now().minusMinutes(20));
        tx3.setDescription("Velocity 3");
        tx3.setClient(user.getClient());
        tx3.setCompte(source);
        tx3.setBeneficiaireId(b3.getId());
        transactionRepository.save(tx3);

        mockMvc.perform(get("/api/transactions/me/risk-preview")
                .queryParam("montant", "300")
                .queryParam("type", "EXTERNE")
                .queryParam("beneficiaireId", b4.getId().toString())
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.multiBeneficiaryVelocity").value(true))
            .andExpect(jsonPath("$.multiBeneficiaryVelocityScore").value(12))
            .andExpect(jsonPath("$.distinctBeneficiariesWindow").value(4))
            .andExpect(jsonPath("$.externalTransfersWindow").value(4));
        }

        @Test
        void riskPreviewShouldExposeUnusualAmountSignal() throws Exception {
        String token = registerAndGetToken("clientRiskAmountA", "clientRiskAmountA@test.local", "Secret123!");
        User user = userRepository.findByUsername("clientRiskAmountA").orElseThrow();

        Compte source = new Compte();
        source.setNumeroCompte("RISK-AMOUNT-SRC-1");
        source.setType("courant");
        source.setSolde(100000.0);
        source.setDateCreation(LocalDateTime.now());
        source.setClient(user.getClient());
        source = compteRepository.save(source);

        Beneficiaire beneficiary = new Beneficiaire();
        beneficiary.setNom("Benef Amount");
        beneficiary.setIban("FR7630001007941234567890301");
        beneficiary.setBanque("Banque Test");
        beneficiary.setEmail("benef.amount@test.local");
        beneficiary.setClient(user.getClient());
        beneficiary.setCreatedAt(LocalDateTime.now().minusDays(2));
        beneficiary = beneficiaireRepository.save(beneficiary);

        for (int i = 0; i < 5; i++) {
            Transaction tx = new Transaction();
            tx.setType("virement_externe");
            tx.setMontant(100.0);
            tx.setDateTransaction(LocalDateTime.now().minusDays(1).minusMinutes(i));
            tx.setDescription("Amount history " + i);
            tx.setClient(user.getClient());
            tx.setCompte(source);
            tx.setBeneficiaireId(beneficiary.getId());
            transactionRepository.save(tx);
        }

        mockMvc.perform(get("/api/transactions/me/risk-preview")
                .queryParam("montant", "400")
                .queryParam("type", "EXTERNE")
                .queryParam("beneficiaireId", beneficiary.getId().toString())
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.unusualAmount").value(true))
            .andExpect(jsonPath("$.unusualAmountScore").value(0))
            .andExpect(jsonPath("$.historicalAverageAmount").value(100.0));
        }

        @Test
        void riskPreviewShouldExposeRepeatedSmallTransfersSignal() throws Exception {
        String token = registerAndGetToken("clientRiskSmallA", "clientRiskSmallA@test.local", "Secret123!");
        User user = userRepository.findByUsername("clientRiskSmallA").orElseThrow();

        Compte source = new Compte();
        source.setNumeroCompte("RISK-SMALL-SRC-1");
        source.setType("courant");
        source.setSolde(100000.0);
        source.setDateCreation(LocalDateTime.now());
        source.setClient(user.getClient());
        source = compteRepository.save(source);

        Beneficiaire beneficiary = new Beneficiaire();
        beneficiary.setNom("Benef Small");
        beneficiary.setIban("FR7630001007941234567890401");
        beneficiary.setBanque("Banque Test");
        beneficiary.setEmail("benef.small@test.local");
        beneficiary.setClient(user.getClient());
        beneficiary.setCreatedAt(LocalDateTime.now().minusDays(2));
        beneficiary = beneficiaireRepository.save(beneficiary);

        for (int i = 0; i < 3; i++) {
            Transaction tx = new Transaction();
            tx.setType("virement_externe");
            tx.setMontant(120.0);
            tx.setDateTransaction(LocalDateTime.now().minusMinutes(10 + i));
            tx.setDescription("Small transfer " + i);
            tx.setClient(user.getClient());
            tx.setCompte(source);
            tx.setBeneficiaireId(beneficiary.getId());
            transactionRepository.save(tx);
        }

        mockMvc.perform(get("/api/transactions/me/risk-preview")
                .queryParam("montant", "100")
                .queryParam("type", "EXTERNE")
                .queryParam("beneficiaireId", beneficiary.getId().toString())
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.repeatedSmallTransfers").value(true))
            .andExpect(jsonPath("$.repeatedSmallTransfersScore").value(0))
            .andExpect(jsonPath("$.smallTransfersWindowCount").value(4));
        }

        @Test
        void riskPreviewShouldAdaptThresholdToClientRiskProfile() throws Exception {
        String token = registerAndGetToken("clientRiskProfileA", "clientRiskProfileA@test.local", "Secret123!");
        User user = userRepository.findByUsername("clientRiskProfileA").orElseThrow();

        Compte source = new Compte();
        source.setNumeroCompte("RISK-PROFILE-SRC-1");
        source.setType("courant");
        source.setSolde(100000.0);
        source.setDateCreation(LocalDateTime.now());
        source.setClient(user.getClient());
        source = compteRepository.save(source);

        for (int i = 0; i < 7; i++) {
            Transaction existingOutgoing = new Transaction();
            existingOutgoing.setType("virement_externe");
            existingOutgoing.setMontant(285.0);
            existingOutgoing.setDateTransaction(LocalDateTime.now().minusMinutes(25 + i));
            existingOutgoing.setDescription("Virement profil dynamique " + i);
            existingOutgoing.setClient(user.getClient());
            existingOutgoing.setCompte(source);
            transactionRepository.save(existingOutgoing);
        }

        mockMvc.perform(get("/api/transactions/me/risk-preview")
                .queryParam("montant", "10000")
                .queryParam("type", "EXTERNE")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.blocked").value(true))
            .andExpect(jsonPath("$.riskProfile").value("STANDARD"))
            .andExpect(jsonPath("$.blockThreshold").value(90));

        user.getClient().setRiskProfile("VIP");
        clientRepository.save(user.getClient());

        mockMvc.perform(get("/api/transactions/me/risk-preview")
                .queryParam("montant", "10000")
                .queryParam("type", "EXTERNE")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.blocked").value(false))
            .andExpect(jsonPath("$.riskProfile").value("VIP"))
            .andExpect(jsonPath("$.blockThreshold").value(95));

        user.getClient().setRiskProfile("SENSIBLE");
        clientRepository.save(user.getClient());

        mockMvc.perform(get("/api/transactions/me/risk-preview")
                .queryParam("montant", "10000")
                .queryParam("type", "EXTERNE")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.blocked").value(true))
            .andExpect(jsonPath("$.riskProfile").value("SENSIBLE"))
            .andExpect(jsonPath("$.blockThreshold").value(85));
        }

        @Test
        void clientShouldNotProcessExternalTransferWhenDailyCountLimitIsExceeded() throws Exception {
        String token = registerAndGetToken("clientLimitCount", "clientLimitCount@test.local", "Secret123!");
        User user = userRepository.findByUsername("clientLimitCount").orElseThrow();

        Compte source = new Compte();
        source.setNumeroCompte("LIM-COUNT-SRC");
        source.setType("courant");
        source.setSolde(50000.0);
        source.setDateCreation(LocalDateTime.now());
        source.setClient(user.getClient());
        source = compteRepository.save(source);

        for (int i = 0; i < 10; i++) {
            Transaction existingOutgoing = new Transaction();
            existingOutgoing.setType("virement_externe");
            existingOutgoing.setMontant(100.0 + i);
            existingOutgoing.setDateTransaction(LocalDateTime.now().minusMinutes(30 + i));
            existingOutgoing.setDescription("Virement externe déjà effectué " + i);
            existingOutgoing.setClient(user.getClient());
            existingOutgoing.setCompte(source);
            transactionRepository.save(existingOutgoing);
        }

        String payloadBeneficiaire = """
            {
              "nom": "Prestataire Count",
              "iban": "FR7630001007941234567890199",
              "banque": "Banque Externe",
              "email": "count@test.local"
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
              "montant": 50,
              "description": "Test plafond nombre journalier"
            }
            """.formatted(source.getId(), beneficiaireId);

        mockMvc.perform(post("/api/transactions/me/virement-externe")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadTransfer))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Nombre maximal de virements journaliers atteint")));
        }

        @Test
        void clientShouldNotProcessExternalTransferWhenCooldownIsActive() throws Exception {
        String token = registerAndGetToken("clientLimitCooldown", "clientLimitCooldown@test.local", "Secret123!");
        User user = userRepository.findByUsername("clientLimitCooldown").orElseThrow();

        Compte source = new Compte();
        source.setNumeroCompte("LIM-COOLDOWN-SRC");
        source.setType("courant");
        source.setSolde(50000.0);
        source.setDateCreation(LocalDateTime.now());
        source.setClient(user.getClient());
        source = compteRepository.save(source);

        Transaction existingOutgoing = new Transaction();
        existingOutgoing.setType("virement_externe");
        existingOutgoing.setMontant(100.0);
        existingOutgoing.setDateTransaction(LocalDateTime.now());
        existingOutgoing.setDescription("Virement externe très récent");
        existingOutgoing.setClient(user.getClient());
        existingOutgoing.setCompte(source);
        transactionRepository.save(existingOutgoing);

        String payloadBeneficiaire = """
            {
              "nom": "Prestataire Cooldown",
              "iban": "FR7630001007941234567890198",
              "banque": "Banque Externe",
              "email": "cooldown@test.local"
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
              "montant": 50,
              "description": "Test délai minimal"
            }
            """.formatted(source.getId(), beneficiaireId);

        mockMvc.perform(post("/api/transactions/me/virement-externe")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadTransfer))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Veuillez attendre")));
        }

        @Test
        void blockedRiskTransferShouldCreateFraudAlertNotification() throws Exception {
        String token = registerAndGetToken("clientFraudAlert", "clientFraudAlert@test.local", "Secret123!");
        User user = userRepository.findByUsername("clientFraudAlert").orElseThrow();

        Compte source = new Compte();
        source.setNumeroCompte("FRAUD-ALERT-SRC");
        source.setType("courant");
        source.setSolde(120000.0);
        source.setDateCreation(LocalDateTime.now());
        source.setClient(user.getClient());
        source = compteRepository.save(source);

        for (int i = 0; i < 9; i++) {
            Transaction existingOutgoing = new Transaction();
            existingOutgoing.setType("virement_externe");
            existingOutgoing.setMontant(1000.0);
            existingOutgoing.setDateTransaction(LocalDateTime.now().minusMinutes(180 + i));
            existingOutgoing.setDescription("Historique risque fraude " + i);
            existingOutgoing.setClient(user.getClient());
            existingOutgoing.setCompte(source);
            transactionRepository.save(existingOutgoing);
        }

        String payloadBeneficiaire = """
            {
              "nom": "Fournisseur Risque",
              "iban": "FR7630001007941234567890177",
              "banque": "Banque Externe",
              "email": "fournisseur.risque@test.local"
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
              "montant": 10000,
              "description": "Tentative risque élevé"
            }
            """.formatted(source.getId(), beneficiaireId);

        mockMvc.perform(post("/api/transactions/me/virement-externe")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadTransfer))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Risque de fraude élevé")));

        mockMvc.perform(get("/api/notifications/me")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("ALERTE FRAUDE")))
            .andExpect(content().string(containsString("bloqué")));

        mockMvc.perform(get("/api/notifications/me/unread-count")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.unreadCount").value(1));
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

        mockMvc.perform(get("/api/notifications/me/unread-count")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.unreadCount").value(1));

        JsonNode notificationsBeforeRead = objectMapper.readTree(
                mockMvc.perform(get("/api/notifications/me")
                                .header("Authorization", "Bearer " + token))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString()
        );
        Long notificationId = notificationsBeforeRead.get(0).get("id").asLong();

        mockMvc.perform(patch("/api/notifications/me/" + notificationId + "/read")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.statut").value("LU"));

        mockMvc.perform(delete("/api/notifications/me/read")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/notifications/me")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(content().string(not(containsString("Virement externe effectué"))));

        mockMvc.perform(patch("/api/notifications/me/read-all")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/notifications/me/unread-count")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.unreadCount").value(0));

        mockMvc.perform(get("/api/audits/me")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("VIREMENT_EXTERNE")))
            .andExpect(content().string(containsString("SUCCES")));

        mockMvc.perform(get("/api/audits/me/page")
                .queryParam("page", "0")
                .queryParam("size", "1")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray());
        }

        @Test
        void clientShouldNotReceiveInAppNotificationWhenPreferenceIsDisabled() throws Exception {
        String token = registerAndGetToken("clientNoNotif", "clientNoNotif@test.local", "Secret123!");
        User user = userRepository.findByUsername("clientNoNotif").orElseThrow();

        mockMvc.perform(put("/api/auth/me/preferences")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "notificationsInAppEnabled": false,
                      "notificationsEmailEnabled": false
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.notificationsInAppEnabled").value(false));

        Compte source = new Compte();
        source.setNumeroCompte("EXT-SRC-NONOTIF");
        source.setType("courant");
        source.setSolde(1000.0);
        source.setDateCreation(LocalDateTime.now());
        source.setClient(user.getClient());
        source = compteRepository.save(source);

        String payloadBeneficiaire = """
            {
              "nom": "Prestataire Sans Notif",
              "iban": "FR7630001007941234567890186",
              "banque": "Banque Externe",
              "email": "prestataire@test.local"
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
              "montant": 200,
              "description": "Paiement sans notif"
            }
            """.formatted(source.getId(), beneficiaireId);

        mockMvc.perform(post("/api/transactions/me/virement-externe")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadTransfer))
            .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/notifications/me/unread-count")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.unreadCount").value(0));
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

    private String loginAndGetToken(String username, String password) throws Exception {
        String payload = """
                {
                  "username": "%s",
                  "password": "%s"
                }
                """.formatted(username, password);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());
        return jsonNode.get("token").asText();
    }
}
