package io.arusland.money;

import io.arusland.money.controller.ServiceController;
import io.arusland.money.service.AccountService;
import org.apache.commons.lang3.StringUtils;

/**
 * Application entry-point.
 */
public class BankServiceApp {
    public static void main(String[] args) {
        int port = Integer.parseInt(StringUtils.defaultString(
                System.getProperty("server.port"), "8080"));
        AccountService accountService = new AccountService();

        new ServiceController(accountService)
                .setPort(port)
                .run();
    }
}
