package io.arusland.money.api;

import java.math.BigDecimal;

public class AccountRequest {
    private String name;
    private BigDecimal amount;

    public AccountRequest() {
    }

    public AccountRequest(String name, BigDecimal amount) {
        this.name = name;
        this.amount = amount;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
