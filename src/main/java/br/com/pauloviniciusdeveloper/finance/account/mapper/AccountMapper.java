package br.com.pauloviniciusdeveloper.finance.account.mapper;

import br.com.pauloviniciusdeveloper.finance.account.dto.AccountRequest;
import br.com.pauloviniciusdeveloper.finance.account.dto.AccountResponse;
import br.com.pauloviniciusdeveloper.finance.account.entity.Account;

public class AccountMapper {

    public static Account toEntity(AccountRequest accountRequest) {
        return Account.builder()
            .name(accountRequest.name())
            .build();
    }

    public static Account toEntity(AccountResponse accountResponse) {
        return Account.builder()
            .id(accountResponse.id())
            .name(accountResponse.name())
            .build();
    }


    public static AccountResponse toResponse(Account account) {
        return AccountResponse.builder()
            .id(account.getId())
            .name(account.getName())
            .build();
    }

}
