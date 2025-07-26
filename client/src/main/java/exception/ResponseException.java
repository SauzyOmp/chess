package exception;

import com.google.gson.Gson;
import java.io.InputStream;
import java.io.InputStreamReader;

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
            Gson gson = new Gson();
            ErrorResponse error = gson.fromJson(new InputStreamReader(body), ErrorResponse.class);
            return new ResponseException(500, error.message());
        } catch (Exception e) {
            return new ResponseException(500, "Unknown error");
        }
    }
} 