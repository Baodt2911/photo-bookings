package vn.baodt2911.photobooking.photobooking.service.interfaces;

import vn.baodt2911.photobooking.photobooking.entity.User;

import java.util.UUID;

public interface UserService {
    User register(String name, String email, String password);
    User login(String email, String password);
    User findByEmail(String email);
    User findById(UUID id);
}