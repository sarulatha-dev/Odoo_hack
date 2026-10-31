package com.brightloop.subscription.expense;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.brightloop.subscription.auth.dto.UserSummary;
import com.brightloop.subscription.expense.dto.BudgetRequest;
import com.brightloop.subscription.expense.dto.ExpenseSummaryResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @GetMapping
    public ExpenseSummaryResponse summary() {
        return expenseService.summary();
    }

    @PutMapping("/budget")
    public UserSummary setBudget(@Valid @RequestBody BudgetRequest request) {
        return expenseService.setBudget(request);
    }
}
