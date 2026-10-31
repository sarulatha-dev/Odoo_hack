package com.brightloop.subscription.user;

public enum IndianState {
    KARNATAKA("Karnataka"),
    TAMIL_NADU("Tamil Nadu"),
    KERALA("Kerala"),
    NORTH("North");

    private final String label;

    IndianState(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
