package io.arusland.money.service;

import com.mashape.unirest.http.exceptions.UnirestException;
import io.arusland.money.api.CreateAccountResponse;
import io.arusland.money.api.GetAccountsResponse;
import io.arusland.money.api.Status;
import io.arusland.money.api.TransferMoneyResponse;
import io.arusland.money.controller.BaseTest;
import io.arusland.money.model.Account;
import io.arusland.money.api.AccountRequest;
import io.arusland.money.util.AccountIdUtil;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;

public class AccountServiceTest extends BaseTest {
    private final static MathContext context = new MathContext(2);

    @Test
    public void testCreateNewAccount() throws UnirestException {
        AccountRequest obj = new AccountRequest("Alex Fine", BigDecimal.valueOf(12.34));
        String json = toJson(obj);

        CreateAccountResponse resp = apiPostAsObject("/api/account/create", json, CreateAccountResponse.class);
        Account accountResp = resp.getAccount();

        assertThat(resp.getStatus(), is(Status.OK));
        assertThat(accountResp.getName(), is(obj.getName()));
        assertThat(accountResp.getAmount(), is(obj.getAmount()));
        assertTrue(AccountIdUtil.isValid(accountResp.getId()));
    }

    @Test
    public void testListAllAccounts() throws UnirestException {
        List<Account> accounts0 = apiGetAsObject("/api/account/list",
                GetAccountsResponse.class).getAccounts();

        assertThat(accounts0.size(), is(0));

        AccountRequest acc1 = new AccountRequest("Alan Turing", BigDecimal.valueOf(12.34D));
        Account account1 = apiPostAsObject("/api/account/create", toJson(acc1),
                CreateAccountResponse.class).getAccount();

        GetAccountsResponse resp = apiGetAsObject("/api/account/list", GetAccountsResponse.class);
        List<Account> accounts = resp.getAccounts();

        assertThat(resp.getStatus(), is(Status.OK));
        assertThat(accounts.size(), is(1));
        assertEquals(accounts.get(0), account1);

        AccountRequest acc2 = new AccountRequest("Franz Kafka", BigDecimal.valueOf(3.14D));
        CreateAccountResponse accountRes2 = apiPostAsObject("/api/account/create", toJson(acc2),
                CreateAccountResponse.class);
        Account account2 = accountRes2.getAccount();

        assertThat(accountRes2.getStatus(), is(Status.OK));
        assertNotEquals(account1, account2);
        assertThat(account2.getName(), is(acc2.getName()));
        assertThat(account2.getAmount(), is(acc2.getAmount()));
        assertTrue(AccountIdUtil.isValid(account2.getId()));

        GetAccountsResponse resp2 = apiGetAsObject("/api/account/list", GetAccountsResponse.class);
        List<Account> accounts2 = resp2.getAccounts();

        assertThat(accounts2.size(), is(2));
        assertTrue(accounts2.contains(account1));
        assertTrue(accounts2.contains(account2));
    }

    @Test
    public void testFail_WhenCreateAccountWithTheSameName() throws UnirestException {
        AccountRequest acc1 = new AccountRequest("Bob Marley", BigDecimal.valueOf(99.99D));
        AccountRequest acc2 = new AccountRequest("Bob Marley", BigDecimal.valueOf(23.01D));

        CreateAccountResponse resp1 = apiPostAsObject("/api/account/create", toJson(acc1),
                CreateAccountResponse.class);

        assertThat(resp1.getStatus(), is(Status.OK));

        CreateAccountResponse resp2 = apiPostAsObject("/api/account/create", toJson(acc2),
                CreateAccountResponse.class);
        assertThat(resp2.getStatus(), is(Status.ERROR));
        assertThat(resp2.getMessage(), is("Account with the same name already exists: Bob Marley"));
    }

    @Test
    public void testTransferMoney() throws UnirestException {
        BigDecimal amount = BigDecimal.valueOf(42.13D);
        AccountRequest acc1 = new AccountRequest("Sergey Brin", BigDecimal.valueOf(142.73D));
        AccountRequest acc2 = new AccountRequest("Linus Torvalds", BigDecimal.valueOf(3D));

        Account resp1 = apiPostAsObject("/api/account/create", toJson(acc1),
                CreateAccountResponse.class).getAccount();
        Account resp2 = apiPostAsObject("/api/account/create", toJson(acc2),
                CreateAccountResponse.class).getAccount();

        TransferMoneyResponse transferResp = apiPost(String.format("/api/money/transfer/%s/%s/%s",
                resp1.getId(), resp2.getId(), amount), TransferMoneyResponse.class);

        assertThat(transferResp.getStatus(), is(Status.OK));

        List<Account> accounts = apiGetAsObject("/api/account/list",
                GetAccountsResponse.class).getAccounts();

        Account brinAccount = findAccountById(accounts, resp1.getId());
        Account torvaldsAccount = findAccountById(accounts, resp2.getId());

        assertEquals(brinAccount.getAmount(), acc1.getAmount().subtract(amount, context));
        assertEquals(torvaldsAccount.getAmount(), acc2.getAmount().add(amount, context));
    }

    @Test
    public void testTransferMoney_WithEventualZeroAmount() throws UnirestException {
        BigDecimal amount = BigDecimal.valueOf(33.99D);
        AccountRequest acc1 = new AccountRequest("Sergey Brin", BigDecimal.valueOf(33.99D));
        AccountRequest acc2 = new AccountRequest("Linus Torvalds", BigDecimal.valueOf(100.01D));

        Account resp1 = apiPostAsObject("/api/account/create", toJson(acc1),
                CreateAccountResponse.class).getAccount();
        Account resp2 = apiPostAsObject("/api/account/create", toJson(acc2),
                CreateAccountResponse.class).getAccount();

        TransferMoneyResponse transferResp = apiPost(String.format("/api/money/transfer/%s/%s/%s",
                resp1.getId(), resp2.getId(), amount), TransferMoneyResponse.class);

        assertThat(transferResp.getStatus(), is(Status.OK));

        List<Account> accounts = apiGetAsObject("/api/account/list",
                GetAccountsResponse.class).getAccounts();

        Account brinAccount = findAccountById(accounts, resp1.getId());
        Account torvaldsAccount = findAccountById(accounts, resp2.getId());

        assertEquals(brinAccount.getAmount(), BigDecimal.valueOf(0, 2));
        assertEquals(torvaldsAccount.getAmount(), acc2.getAmount().add(amount, context));
    }

    @Test
    public void testFail_WhenTransferMoney_InsufficientFunds() throws UnirestException {
        BigDecimal amount = BigDecimal.valueOf(142.74D);
        AccountRequest acc1 = new AccountRequest("Sergey Brin", BigDecimal.valueOf(142.73D));
        AccountRequest acc2 = new AccountRequest("Linus Torvalds", BigDecimal.valueOf(3D));

        Account resp1 = apiPostAsObject("/api/account/create", toJson(acc1),
                CreateAccountResponse.class).getAccount();
        Account resp2 = apiPostAsObject("/api/account/create", toJson(acc2),
                CreateAccountResponse.class).getAccount();

        TransferMoneyResponse transferResp = apiPost(String.format("/api/money/transfer/%s/%s/%s",
                resp1.getId(), resp2.getId(), amount), TransferMoneyResponse.class);

        assertThat(transferResp.getStatus(), is(Status.ERROR));
        assertThat(transferResp.getMessage(), is("Insufficient funds in the account: " + resp1.getId()));

        List<Account> accounts = apiGetAsObject("/api/account/list",
                GetAccountsResponse.class).getAccounts();

        Account brinAccount = findAccountById(accounts, resp1.getId());
        Account torvaldsAccount = findAccountById(accounts, resp2.getId());

        assertEquals(brinAccount.getAmount(), acc1.getAmount());
        assertEquals(torvaldsAccount.getAmount(), acc2.getAmount());
    }

    @Test
    public void testFail_WhenTransferMoney_FromUnknownAccount() throws UnirestException {
        String unknownAccountId  = AccountIdUtil.generate();
        BigDecimal amount = BigDecimal.valueOf(142.74D);
        AccountRequest acc = new AccountRequest("Linus Torvalds", BigDecimal.valueOf(3D));

        Account resp = apiPostAsObject("/api/account/create", toJson(acc),
                CreateAccountResponse.class).getAccount();

        TransferMoneyResponse transferResp = apiPost(String.format("/api/money/transfer/%s/%s/%s",
                unknownAccountId, resp.getId(), amount), TransferMoneyResponse.class);

        assertThat(transferResp.getStatus(), is(Status.ERROR));
        assertThat(transferResp.getMessage(), is("Source Account Id not found: " + unknownAccountId));

        List<Account> accounts = apiGetAsObject("/api/account/list",
                GetAccountsResponse.class).getAccounts();

        assertThat(accounts.size(), is(1));
        Account torvaldsAccount = findAccountById(accounts, resp.getId());

        assertEquals(torvaldsAccount.getAmount(), acc.getAmount());
    }

    @Test
    public void testFail_WhenTransferMoney_ToUnknownAccount() throws UnirestException {
        String unknownAccountId  = AccountIdUtil.generate();
        BigDecimal amount = BigDecimal.valueOf(234.01D);
        AccountRequest acc = new AccountRequest("Linus Torvalds", BigDecimal.valueOf(13D));

        Account resp = apiPostAsObject("/api/account/create", toJson(acc),
                CreateAccountResponse.class).getAccount();

        TransferMoneyResponse transferResp = apiPost(String.format("/api/money/transfer/%s/%s/%s",
                resp.getId(), unknownAccountId, amount), TransferMoneyResponse.class);

        assertThat(transferResp.getStatus(), is(Status.ERROR));
        assertThat(transferResp.getMessage(), is("Target Account Id not found: " + unknownAccountId));

        List<Account> accounts = apiGetAsObject("/api/account/list",
                GetAccountsResponse.class).getAccounts();

        assertThat(accounts.size(), is(1));
        Account torvaldsAccount = findAccountById(accounts, resp.getId());

        assertEquals(torvaldsAccount.getAmount(), acc.getAmount());
    }

    @Test
    public void testFail_WhenTransferMoney_FromInvalidAccountId() throws UnirestException {
        String unknownAccountId = AccountIdUtil.generate();
        TransferMoneyResponse transferResp = apiPost(String.format("/api/money/transfer/foo/%s/12.3",
                unknownAccountId), TransferMoneyResponse.class);

        assertThat(transferResp.getStatus(), is(Status.ERROR));
        assertThat(transferResp.getMessage(), is("Param 'fromAccountId' is invalid Account Id: foo"));
    }

    @Test
    public void testFail_WhenTransferMoney_ToInvalidAccountId() throws UnirestException {
        String unknownAccountId = AccountIdUtil.generate();
        TransferMoneyResponse transferResp = apiPost(String.format("/api/money/transfer/%s/bar/99.99",
                unknownAccountId), TransferMoneyResponse.class);

        assertThat(transferResp.getStatus(), is(Status.ERROR));
        assertThat(transferResp.getMessage(), is("Param 'toAccountId' is invalid Account Id: bar"));
    }

    private static Account findAccountById(List<Account> accounts, String accountId) {
        return accounts.stream().
                filter(p -> p.getId().equals(accountId))
                .findFirst().get();
    }
}
