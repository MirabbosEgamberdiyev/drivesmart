package uz.drivesmart.service;

import uz.drivesmart.dto.request.RegisterRequest;
import uz.drivesmart.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(RegisterRequest request);
}