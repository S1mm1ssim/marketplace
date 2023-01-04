package com.modsensoftware.marketplace.dto.mapper;

import com.modsensoftware.marketplace.domain.UserTransaction;
import com.modsensoftware.marketplace.dto.UserTransactionDto;
import org.mapstruct.Mapper;

/**
 * @author andrey.demyanchik on 11/27/2022
 */
@Mapper(uses = {OrderMapper.class})
public interface UserTransactionMapper {

    UserTransaction toUserTransaction(UserTransactionDto transactionDto);
}
