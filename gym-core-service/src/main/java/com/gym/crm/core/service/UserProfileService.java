package com.gym.crm.core.service;

import com.gym.crm.core.facade.dto.AuthResponseDTO;
import com.gym.crm.core.facade.dto.PasswordChangeRequest;
import com.gym.crm.core.facade.dto.ToggleActiveRequestDTO;
import com.gym.crm.core.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.gym.crm.rest.LoginRequest;

public interface UserProfileService {
    String generateUsername(String firstName, String lastName);

    String generatePassword();

    AuthResponseDTO authenticate(String username, String password);

    void changePassword(PasswordChangeRequest requestDTO);

    User login(LoginRequest request);

    void toggleActive(ToggleActiveRequestDTO request);

    boolean checkPassword(@NotBlank(message = "New password is required")
                          @Size(min = 10, max = 100, message = "Password must be between 10 and 100 characters long")
                          String password, String password1);

    void logout(HttpServletRequest request);
}
