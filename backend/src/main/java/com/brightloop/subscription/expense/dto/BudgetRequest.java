package com.brightloop.subscription.expense.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;

public record BudgetRequest(@DecimalMin("0.00") BigDecimal monthlyBudget) {
}
