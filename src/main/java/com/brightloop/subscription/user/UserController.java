package com.brightloop.subscription.user;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.brightloop.subscription.auth.AuthService;
import com.brightloop.subscription.auth.dto.StaffCreateRequest;
import com.brightloop.subscription.auth.dto.UserSummary;
import com.brightloop.subscription.common.ApiException;
import com.brightloop.subscription.currency.CurrencyService;
import com.brightloop.subscription.security.CurrentUserService;
import com.brightloop.subscription.user.dto.AutopayRequest;
import com.brightloop.subscription.user.dto.CurrencyPreferenceRequest;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final CurrentUserService currentUserService;
    private final AuthService authService;
    private final UserRepository userRepository;
    private final CurrencyService currencyService;

    public UserController(CurrentUserService currentUserService, AuthService authService,
            UserRepository userRepository, CurrencyService currencyService) {
        this.currentUserService = currentUserService;
        this.authService = authService;
        this.userRepository = userRepository;
        this.currencyService = currencyService;
    }

    @GetMapping("/me")
    public UserSummary me() {
        return UserSummary.from(currentUserService.get());
    }

    @PutMapping("/me/currency")
    public UserSummary updateCurrency(@Valid @RequestBody CurrencyPreferenceRequest request) {
        String code = CurrencyService.normalize(request.currency());
        currencyService.rateToInr(code);
        User user = currentUserService.get();
        user.setPreferredCurrency(code);
        return UserSummary.from(userRepository.save(user));
    }

    @PutMapping("/me/autopay")
    public UserSummary updateAutopay(@Valid @RequestBody AutopayRequest request) {
        User user = currentUserService.get();
        if (request.enabled()) {
            if (request.method() == null) {
                throw ApiException.badRequest("Choose a saved payment method to enable autopay");
            }
            user.setAutopayMethod(request.method());
            if (user.getGatewayCustomerId() == null) {
                user.setGatewayCustomerId("cus_sim_" + user.getId());
            }
            if (user.getAutopayToken() == null) {
                user.setAutopayToken("tok_sim_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12));
            }
        }
        user.setAutopayEnabled(request.enabled());
        return UserSummary.from(userRepository.save(user));
    }

    @PostMapping("/staff")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserSummary> createStaff(@Valid @RequestBody StaffCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.createStaff(request).user());
    }
}
