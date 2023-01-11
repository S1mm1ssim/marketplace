package com.modsensoftware.marketplace.dto.mapper;

import com.modsensoftware.marketplace.domain.UserTransaction;
import com.modsensoftware.marketplace.dto.PlacedUserTransaction;
import com.modsensoftware.marketplace.dto.request.UserTransactionRequest;
import org.mapstruct.Mapper;

/**
 * @author andrey.demyanchik on 11/27/2022
 */
@Mapper(uses = {OrderMapper.class})
public interface UserTransactionMapper {

    UserTransaction toUserTransaction(UserTransactionRequest transactionDto);

    PlacedUserTransaction toPlacedUserTransaction(UserTransaction userTransaction);
}
