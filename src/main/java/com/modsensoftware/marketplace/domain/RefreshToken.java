package com.modsensoftware.marketplace.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

/**
 * @author andrey.demyanchik on 12/2/2022
 */
@Getter
@Setter
@RedisHash("RefreshToken")
public class RefreshToken {

    @Id
    private String id;
    @Indexed
    private String userEmail;
    private String refreshToken;

    public RefreshToken(String userEmail, String refreshToken) {
        this.userEmail = userEmail;
        this.refreshToken = refreshToken;
    }
}
