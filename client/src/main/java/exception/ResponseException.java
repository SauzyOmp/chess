package exception;

import com.google.gson.Gson;

import java.io.InputStream;

public class ResponseException extends Exception {
    private final int statusCode;

    public ResponseException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public static ResponseException fromJson(InputStream body) {
        try {
            // Read the entire content as string
            String content = new String(body.readAllBytes());
            
            // Try to parse as JSON first
            try {
                Gson gson = new Gson();
                ErrorResponse error = gson.fromJson(content, ErrorResponse.class);
                return new ResponseException(500, error.message());
            } catch (Exception jsonEx) {
                // If JSON parsing fails, return the raw string content
                return new ResponseException(500, content.trim());
            }
        } catch (Exception e) {
            return new ResponseException(500, "Unknown error: " + e.getMessage());
        }
    }
} 