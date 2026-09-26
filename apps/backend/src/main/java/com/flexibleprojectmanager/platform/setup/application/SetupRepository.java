package com.flexibleprojectmanager.platform.setup.application;

public interface SetupRepository {
    boolean isInitialized();

    void persist(SetupData data);
}
