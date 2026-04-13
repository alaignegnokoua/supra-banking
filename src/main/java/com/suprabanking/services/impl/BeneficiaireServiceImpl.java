package com.suprabanking.services.impl;

import com.suprabanking.config.security.CurrentUserService;
import com.suprabanking.models.Beneficiaire;
import com.suprabanking.models.Client;
import com.suprabanking.repositories.BeneficiaireRepository;
import com.suprabanking.repositories.BeneficiaryUsageHistoryRepository;
import com.suprabanking.services.BeneficiaireService;
import com.suprabanking.services.dto.BeneficiaireDTO;
import com.suprabanking.web.errors.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BeneficiaireServiceImpl implements BeneficiaireService {

    private final BeneficiaireRepository beneficiaireRepository;
    private final BeneficiaryUsageHistoryRepository usageHistoryRepository;
    private final CurrentUserService currentUserService;

    @Override
    public BeneficiaireDTO saveMyBeneficiaire(BeneficiaireDTO dto) {
        log.debug("Request to save my beneficiaire: {}", dto);
        validateIbanRib(dto.getIban(), dto.getRib());

        Long clientId = currentUserService.requireCurrentClientId();

        if (dto.getIban() != null && !dto.getIban().isBlank()
                && beneficiaireRepository.existsByClient_IdAndIban(clientId, dto.getIban().trim())) {
            throw new IllegalArgumentException("Cet IBAN est déjà enregistré");
        }

        if (dto.getRib() != null && !dto.getRib().isBlank()
                && beneficiaireRepository.existsByClient_IdAndRib(clientId, dto.getRib().trim())) {
            throw new IllegalArgumentException("Ce RIB est déjà enregistré");
        }

        Client client = currentUserService.getCurrentUser().getClient();
        if (client == null) {
            throw new ResourceNotFoundException("Profil client introuvable");
        }

        Beneficiaire entity = new Beneficiaire();
        applyDto(entity, dto);
        entity.setClient(client);
        entity.setCreatedAt(LocalDateTime.now());

        return toDto(beneficiaireRepository.save(entity));
    }

    @Override
    public BeneficiaireDTO updateMyBeneficiaire(Long id, BeneficiaireDTO dto) {
        log.debug("Request to update my beneficiaire: {}", id);
        validateIbanRib(dto.getIban(), dto.getRib());

        Long clientId = currentUserService.requireCurrentClientId();
        Beneficiaire entity = beneficiaireRepository.findByIdAndClient_Id(id, clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Bénéficiaire introuvable"));

        applyDto(entity, dto);
        return toDto(beneficiaireRepository.save(entity));
    }

    @Override
    public List<BeneficiaireDTO> findMyBeneficiaires() {
        Long clientId = currentUserService.requireCurrentClientId();
        return beneficiaireRepository.findByClient_Id(clientId).stream().map(this::toDto).toList();
    }

    @Override
    public Optional<BeneficiaireDTO> findMyBeneficiaire(Long id) {
        Long clientId = currentUserService.requireCurrentClientId();
        return beneficiaireRepository.findByIdAndClient_Id(id, clientId).map(this::toDto);
    }

    @Override
    public void deleteMyBeneficiaire(Long id) {
        Long clientId = currentUserService.requireCurrentClientId();
        Beneficiaire entity = beneficiaireRepository.findByIdAndClient_Id(id, clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Bénéficiaire introuvable"));
        beneficiaireRepository.delete(entity);
    }

    private void validateIbanRib(String iban, String rib) {
        if ((iban == null || iban.isBlank()) && (rib == null || rib.isBlank())) {
            throw new IllegalArgumentException("Un IBAN ou un RIB est obligatoire");
        }

        if (iban != null && !iban.isBlank()) {
            String normalized = iban.replaceAll("\\s+", "").toUpperCase();
            if (normalized.length() < 10 || normalized.length() > 34 || !normalized.matches("[A-Z0-9]+")) {
                throw new IllegalArgumentException("IBAN invalide");
            }
        }

        if (rib != null && !rib.isBlank()) {
            String normalized = rib.replaceAll("\\s+", "");
            if (normalized.length() < 10 || normalized.length() > 30 || !normalized.matches("[0-9A-Za-z]+")) {
                throw new IllegalArgumentException("RIB invalide");
            }
        }
    }

    private void applyDto(Beneficiaire entity, BeneficiaireDTO dto) {
        entity.setNom(dto.getNom() != null ? dto.getNom().trim() : null);
        entity.setIban(dto.getIban() != null ? dto.getIban().replaceAll("\\s+", "").toUpperCase() : null);
        entity.setRib(dto.getRib() != null ? dto.getRib().replaceAll("\\s+", "") : null);
        entity.setBanque(dto.getBanque() != null ? dto.getBanque().trim() : null);
        entity.setEmail(dto.getEmail() != null ? dto.getEmail().trim() : null);
        
        // Only update status if provided in DTO
        if (dto.getStatus() != null) {
            entity.setStatus(dto.getStatus());
        }
    }

    private BeneficiaireDTO toDto(Beneficiaire entity) {
        long successfulUsages = usageHistoryRepository.countSuccessfulUsagesByBeneficiaire(entity.getId());
        long totalUsages = usageHistoryRepository.countTotalUsagesByBeneficiaire(entity.getId());
        int reputationScore = computeReputationScore(entity, successfulUsages, totalUsages);

        return BeneficiaireDTO.builder()
                .id(entity.getId())
                .nom(entity.getNom())
                .iban(entity.getIban())
                .rib(entity.getRib())
                .banque(entity.getBanque())
                .email(entity.getEmail())
                .createdAt(entity.getCreatedAt())
                .lastUsedAt(entity.getLastUsedAt())
                .status(entity.getStatus())
                .successfulTransfersCount(successfulUsages)
                .totalUsagesCount(totalUsages)
                .reputationScore(reputationScore)
                .reputationLevel(resolveReputationLevel(reputationScore))
                .reputationRecommendation(resolveReputationRecommendation(reputationScore))
                .build();
    }

    private int computeReputationScore(Beneficiaire entity, long successfulUsages, long totalUsages) {
        int score = 30;
        score += (int) Math.min(35, successfulUsages * 5);
        score += (int) Math.min(15, totalUsages * 2);

        LocalDateTime lastUsedAt = entity.getLastUsedAt();
        if (lastUsedAt != null) {
            long daysSinceLastUse = ChronoUnit.DAYS.between(lastUsedAt, LocalDateTime.now());
            if (daysSinceLastUse <= 7) {
                score += 12;
            } else if (daysSinceLastUse <= 30) {
                score += 8;
            } else if (daysSinceLastUse <= 90) {
                score += 4;
            }
        }

        String status = entity.getStatus() == null ? "ACTIVE" : entity.getStatus().toUpperCase();
        if ("BLOCKED".equals(status)) {
            score -= 50;
        } else if ("PENDING_VERIFICATION".equals(status)) {
            score -= 20;
        }

        return Math.max(0, Math.min(100, score));
    }

    private String resolveReputationLevel(int score) {
        if (score >= 80) {
            return "FIABLE";
        }
        if (score >= 60) {
            return "SURVEILLER";
        }
        if (score >= 40) {
            return "RISQUE_MOYEN";
        }
        return "RISQUE_ELEVE";
    }

    private String resolveReputationRecommendation(int score) {
        if (score >= 80) {
            return "Bénéficiaire de confiance, faible risque opérationnel";
        }
        if (score >= 60) {
            return "Bénéficiaire correct, surveillance standard recommandée";
        }
        if (score >= 40) {
            return "Vérifier les informations du bénéficiaire avant validation";
        }
        return "Risque élevé, validation manuelle recommandée";
    }
}
