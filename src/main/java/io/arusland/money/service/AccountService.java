package io.arusland.money.service;

import io.arusland.money.model.Account;
import io.arusland.money.api.AccountRequest;
import io.arusland.money.util.AccountIdUtil;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * All operations related with account.
 */
public class AccountService {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final MathContext context = new MathContext(2);
    private final List<Account> accounts = new ArrayList<>();

    /**
     * Creates new account.
     *
     * @param accountNew account data
     * @return newly created account
     */
    public Account create(AccountRequest accountNew) {
        Validate.notNull(accountNew, "accountNew");

        synchronized (accounts) {
            if (accounts.stream().anyMatch(p -> p.getName().equals(accountNew.getName()))) {
                throw new RuntimeException("Account with the same name already exists: " + accountNew.getName());
            }

            Account account = new Account(AccountIdUtil.generate(), accountNew.getName(), accountNew.getAmount());
            accounts.add(account);

            log.info("New account created: {}", account);

            return account;
        }
    }

    /**
     * Returns all accounts.
     */
    public List<Account> getAccounts() {
        synchronized (accounts) {
            return Collections.unmodifiableList(new ArrayList<>(accounts));
        }
    }

    /**
     * Transfers money from one account to another.
     *
     * @param fromAccountId account from which need to move money
     * @param toAccountId   account to which need to move money
     * @param amount        amount of money to move
     */
    public void transfer(String fromAccountId, String toAccountId, BigDecimal amount) {
        log.info("Try to transfer money from '{}' to '{}', amount: {}", fromAccountId, toAccountId, amount);

        synchronized (accounts) {
            Optional<Account> fromAccount = accounts.stream()
                    .filter(p -> p.getId().equals(fromAccountId))
                    .findFirst();

            if (!fromAccount.isPresent()) {
                throw new RuntimeException("Source Account Id not found: " + fromAccountId);
            }

            Optional<Account> toAccount = accounts.stream()
                    .filter(p -> p.getId().equals(toAccountId))
                    .findFirst();

            if (!toAccount.isPresent()) {
                throw new RuntimeException("Target Account Id not found: " + toAccountId);
            }

            transferInternal(fromAccount.get(), toAccount.get(), amount);
        }
    }

    private void transferInternal(Account fromAccount, Account toAccount, BigDecimal amount) {
        BigDecimal fromResultAmount = fromAccount.getAmount().subtract(amount, context);

        if (fromResultAmount.signum() < 0) {
            throw new RuntimeException("Insufficient funds in the account: " + fromAccount.getId());
        }

        fromAccount.setAmount(fromResultAmount);
        toAccount.setAmount(toAccount.getAmount().add(amount, context));

        log.info("Money transferred, amount: {}. Eventual state - source: {}, target: {}",
                amount, fromAccount, toAccount);
    }
}
