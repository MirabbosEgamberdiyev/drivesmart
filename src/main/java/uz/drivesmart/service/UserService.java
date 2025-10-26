package uz.drivesmart.service;

import uz.drivesmart.dto.request.AddAdminRequest;
import uz.drivesmart.dto.response.AuthResponse;

public interface UserService {
    AuthResponse addAdmin(AddAdminRequest request);
}