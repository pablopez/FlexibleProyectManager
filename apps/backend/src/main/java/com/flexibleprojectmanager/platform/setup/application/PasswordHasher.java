package com.flexibleprojectmanager.platform.setup.application;

public interface PasswordHasher {
    String hash(String password);
}
