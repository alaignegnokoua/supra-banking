package com.suprabanking.services;

import com.suprabanking.services.dto.ClientDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ClientService {

    ClientDTO saveClient(ClientDTO clientDTO);

    ClientDTO updateClient(ClientDTO clientDTO, Long id);

    ClientDTO partialUpdateClient(ClientDTO clientDTO, Long id);

    ClientDTO updateClientRiskProfile(Long id, String riskProfile);

    ClientDTO updateClientTransferLimits(Long id, Double maxSingleTransferAmount, Double maxDailyTransferTotal,
                                         Integer maxDailyTransferCount, Integer minTransferIntervalSeconds);

    Page<ClientDTO> findAllClients(Pageable pageable);

    Optional<ClientDTO> findOne(Long id);

    void delete(Long id);
}
