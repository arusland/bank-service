package io.arusland.money.controller;

import io.arusland.money.api.CreateAccountResponse;
import io.arusland.money.api.TransferMoneyResponse;
import io.arusland.money.model.Account;
import io.arusland.money.api.AccountRequest;
import io.arusland.money.api.BaseResponse;
import io.arusland.money.api.GetAccountsResponse;
import io.arusland.money.util.ParamUtil;
import io.arusland.money.service.AccountService;
import io.javalin.Context;
import io.javalin.Handler;
import io.javalin.Javalin;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;

/**
 * Routing, validation and error handling.
 */
public class ServiceController {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final AccountService accountService;
    private final Javalin server;
    private int port = 8080;

    public ServiceController(AccountService accountService) {
        this.accountService = Validate.notNull(accountService, "accountService");
        this.server = Javalin.create();
    }

    public void run() {
        server.port(port).start();

        apiPost("/api/account/create", this::handleCreateNewAccount);

        apiGet("/api/account/list", this::handleListAllAccounts);

        apiPost("/api/money/transfer/:fromAccountId/:toAccountId/:amount",
                this::handleTransferMoney);
    }

    private void handleCreateNewAccount(Context ctx) {
        AccountRequest accountNew = ctx.bodyAsClass(AccountRequest.class);
        Account account = accountService.create(accountNew);

        ctx.json(new CreateAccountResponse(account));
    }

    private void handleListAllAccounts(Context ctx) {
        ctx.json(new GetAccountsResponse(accountService.getAccounts()));
    }

    private void handleTransferMoney(Context ctx) {
        String fromAccountId = ParamUtil.validateAccountId(ctx, "fromAccountId");
        String toAccountId = ParamUtil.validateAccountId(ctx, "toAccountId");
        BigDecimal amount = ParamUtil.parseDecimal(ctx, "amount");

        accountService.transfer(fromAccountId, toAccountId, amount);
        ctx.json(new TransferMoneyResponse());
    }

    private void apiGet(String path, Handler handler) {
        server.get(path, ctx -> handleError(ctx, handler));
    }

    private void apiPost(String path, Handler handler) {
        server.post(path, ctx -> handleError(ctx, handler));
    }

    private void handleError(Context ctx, Handler handler) {
        try {
            handler.handle(ctx);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);

            ctx.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            ctx.json(BaseResponse.ERROR(StringUtils.defaultString(ex.getMessage(),
                    ex.getClass().getSimpleName())));
        }
    }

    protected Javalin getServer() {
        return server;
    }

    public int getPort() {
        return port;
    }

    public ServiceController setPort(int port) {
        this.port = port;
        return this;
    }
}
