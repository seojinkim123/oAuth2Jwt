package com.example.oauth2jwt.service;

import com.example.oauth2jwt.dto.UserDto;
import com.example.oauth2jwt.entity.User;
import com.example.oauth2jwt.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public Optional<UserDto> findByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(UserDto::from);
    }

    public Optional<UserDto> findById(Long id) {
        return userRepository.findById(id)
                .map(UserDto::from);
    }

    @Transactional
    public UserDto saveUser(User user) {
        User savedUser = userRepository.save(user);
        return UserDto.from(savedUser);
    }
}