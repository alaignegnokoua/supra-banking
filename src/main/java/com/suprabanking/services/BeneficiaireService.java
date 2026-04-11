package com.suprabanking.services;

import com.suprabanking.services.dto.BeneficiaireDTO;

import java.util.List;
import java.util.Optional;

public interface BeneficiaireService {

    BeneficiaireDTO saveMyBeneficiaire(BeneficiaireDTO dto);

    BeneficiaireDTO updateMyBeneficiaire(Long id, BeneficiaireDTO dto);

    List<BeneficiaireDTO> findMyBeneficiaires();

    Optional<BeneficiaireDTO> findMyBeneficiaire(Long id);

    void deleteMyBeneficiaire(Long id);
}
