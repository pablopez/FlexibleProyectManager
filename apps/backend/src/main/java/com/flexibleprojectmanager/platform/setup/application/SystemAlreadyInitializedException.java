package com.flexibleprojectmanager.platform.setup.application;

public class SystemAlreadyInitializedException extends RuntimeException {
    public SystemAlreadyInitializedException() {
        super("The system has already been initialized.");
    }
}
