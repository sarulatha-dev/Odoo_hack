package com.brightloop.subscription.auth;

public final class PasswordPolicy {

    public static final String REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[^A-Za-z0-9]).{9,100}$";

    public static final String MESSAGE =
            "Password must be more than 8 characters and include an uppercase letter, a lowercase letter and a special character";

    private PasswordPolicy() {
    }
}
