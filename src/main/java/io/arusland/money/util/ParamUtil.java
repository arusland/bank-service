package io.arusland.money.util;

import io.javalin.Context;
import org.apache.commons.lang3.Validate;

import java.math.BigDecimal;

public class ParamUtil {
    public static BigDecimal parseDecimal(Context ctx, String name) {
        String value = ctx.param(name);

        Validate.notBlank(value, name);

        try {
            return new BigDecimal(value);
        } catch (NumberFormatException ex) {
            throw new RuntimeException(String.format("Param '%s' must be decimal", name));
        }
    }

    public static String validateAccountId(Context ctx, String name) {
        String value = ctx.param(name);

        if (!AccountIdUtil.isValid(value)) {
            throw new RuntimeException(String.format("Param '%s' is invalid Account Id: %s", name, value));
        }

        return value;
    }
}
