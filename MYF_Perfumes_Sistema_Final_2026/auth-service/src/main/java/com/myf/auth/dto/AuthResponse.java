package com.myf.auth.dto;
public record AuthResponse(String token, Long userId, String email, String nombres) {}
