package uz.drivesmart.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import uz.drivesmart.dto.request.RegisterRequest;
import uz.drivesmart.dto.response.AuthResponse;
import uz.drivesmart.entity.User;
import uz.drivesmart.exception.ResourceNotFoundException;
import uz.drivesmart.repository.UserRepository;
import uz.drivesmart.service.AuthService;
import uz.drivesmart.util.JwtUtil;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Override
    public AuthResponse register(RegisterRequest request) {
        var user = new User();
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole("USER");
        userRepository.save(user);
        var token = jwtUtil.generateToken(user);
        return new AuthResponse(token);
    }

    @Override
    public AuthResponse login(RegisterRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        var user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("Foydalanuvchi topilmadi"));
        var token = jwtUtil.generateToken(user);
        return new AuthResponse(token);
    }
}