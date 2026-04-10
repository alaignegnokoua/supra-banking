package com.suprabanking.services.mapper;

import com.suprabanking.models.Client;
import com.suprabanking.services.dto.ClientDTO;

public interface ClientMapper {

    Client toEntity(ClientDTO dto);

    ClientDTO toDto(Client entity);

    void partialUpdate(Client entity, ClientDTO dto);
}
