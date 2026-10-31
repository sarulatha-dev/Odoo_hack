package com.brightloop.subscription.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.brightloop.subscription.common.ApiException;
import com.brightloop.subscription.user.User;
import com.brightloop.subscription.user.UserRepository;

@Service
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String email() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw ApiException.forbidden("Not authenticated");
        }
        return authentication.getName();
    }

    public User get() {
        return userRepository.findByEmailIgnoreCase(email())
                .orElseThrow(() -> ApiException.forbidden("Authenticated user no longer exists"));
    }
}
