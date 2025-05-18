package com.loanapi.dto;

public class ErrorResponse {
    private final String title;
    private final String detail;

    public ErrorResponse(String title, String detail) {
        this.title = title;
        this.detail = detail;
    }

    // Getters for JSON serialization
    public String getTitle() {
        return title;
    }

    public String getDetail() {
        return detail;
    }
}
