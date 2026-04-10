package com.suprabanking.services;

import com.suprabanking.services.dto.CompteDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface CompteService {

    CompteDTO saveCompte(CompteDTO compteDTO);

    CompteDTO updateCompte(CompteDTO compteDTO, Long id);

    CompteDTO partialUpdateCompte(CompteDTO compteDTO, Long id);

    Page<CompteDTO> findAllComptes(Pageable pageable);

    Optional<CompteDTO> findOne(Long id);

    void delete(Long id);
}
