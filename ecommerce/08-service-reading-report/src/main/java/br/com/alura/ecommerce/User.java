package br.com.alura.ecommerce;

public class User {

    public String getUserId() {
        return userId;
    }

    private final String userId;

    public User(String userId) {
        this.userId = userId;
    }

    public String getReportPath() {
        return "target/" + userId + "-report.txt";
    }
}
