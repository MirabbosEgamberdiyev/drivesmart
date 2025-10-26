package uz.drivesmart.service.impl;


import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import uz.drivesmart.constants.ApiConstants;
import uz.drivesmart.dto.request.AddAdminRequest;
import uz.drivesmart.dto.response.AuthResponse;
import uz.drivesmart.entity.User;
import uz.drivesmart.exception.ResourceNotFoundException;
import uz.drivesmart.repository.UserRepository;
import uz.drivesmart.service.UserService;
import uz.drivesmart.util.JwtUtil;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public AuthResponse addAdmin(AddAdminRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new ResourceNotFoundException("Bu email allaqachon mavjud");
        }
        var admin = new User();
        admin.setEmail(request.email());
        admin.setPasswordHash(passwordEncoder.encode(request.password()));
        admin.setRole(ApiConstants.ROLE_ADMIN);
        userRepository.save(admin);
        var token = jwtUtil.generateToken(admin);
        return new AuthResponse(token);
    }
}