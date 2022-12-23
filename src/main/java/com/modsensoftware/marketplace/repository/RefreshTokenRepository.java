package com.modsensoftware.marketplace.repository;

import com.modsensoftware.marketplace.domain.RefreshToken;
import org.springframework.data.keyvalue.repository.KeyValueRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author andrey.demyanchik on 12/2/2022
 */
@Repository
public interface RefreshTokenRepository extends KeyValueRepository<RefreshToken, String> {

    List<RefreshToken> findByUserEmail(String userEmail);
}
