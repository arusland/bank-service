package io.arusland.money.api;

import io.arusland.money.model.Account;

public class CreateAccountResponse extends BaseResponse {
    private Account account;

    public CreateAccountResponse(Account account) {
        this();
        this.account = account;
    }

    protected CreateAccountResponse() {
        super(Status.OK, null);
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }
}
