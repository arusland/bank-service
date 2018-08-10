package io.arusland.money.util;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Random account id generator.
 */
public final class AccountIdUtil {
    public final static int ID_LENGTH = 7;

    public static String generate() {
        return RandomStringUtils.randomNumeric(ID_LENGTH);
    }

    public static boolean isValid(String id) {
        return StringUtils.isNotBlank(id) && id.length() == ID_LENGTH;
    }
}
