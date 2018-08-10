package io.arusland.money.api;

import io.arusland.money.model.Account;

import java.util.List;

public class GetAccountsResponse extends BaseResponse {
    private List<Account> accounts;

    public GetAccountsResponse(List<Account> accounts) {
        this();
        this.accounts = accounts;
    }

    public GetAccountsResponse() {
        super(Status.OK, null);
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<Account> accounts) {
        this.accounts = accounts;
    }
}
