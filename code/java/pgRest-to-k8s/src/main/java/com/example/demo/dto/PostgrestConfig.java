package com.example.demo.dto;


public class PostgrestConfig {
    private String dbUri;
    private String dbSchema;
    private String dbRole;
    private int port = 3000; // default port if not provided

    // Getters and setters
    public String getDbUri() {
        return dbUri;
    }

    public void setDbUri(String dbUri) {
        this.dbUri = dbUri;
    }

    public String getDbSchema() {
        return dbSchema;
    }

    public void setDbSchema(String dbSchema) {
        this.dbSchema = dbSchema;
    }

    public String getDbRole() {
        return dbRole;
    }

    public void setDbRole(String dbRole) {
        this.dbRole = dbRole;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}

