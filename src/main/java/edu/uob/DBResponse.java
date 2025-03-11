package edu.uob;

public class DBResponse {
    private final boolean success;
    private final String message;
    private final String data;

    private DBResponse(boolean success, String message, String data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public static DBResponse success(String message) {
        return new DBResponse(true, message, null);
    }

    public static DBResponse success(String message, String data) {
        return new DBResponse(true, message, data);
    }

    public static DBResponse error(String message) {
        return new DBResponse(false, message, null);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getData() {
        return data;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(success ? "[OK]" : "[ERROR]");

        if (message != null && !message.isEmpty()) {
            result.append(" ").append(message);
        }

        if (data != null && !data.isEmpty()) {
            result.append("\n").append(data);
        }

        return result.toString();
    }
}