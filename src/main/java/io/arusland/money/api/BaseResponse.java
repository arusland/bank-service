package io.arusland.money.api;

/**
 * Base API result.
 */
public class BaseResponse {
    private final Status status;
    private final String message;

    protected BaseResponse(Status status, String message) {
        this.status = status;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public Status getStatus() {
        return status;
    }

    public static BaseResponse ERROR(String message) {
        return new BaseResponse(Status.ERROR, message);
    }

    public static BaseResponse OK() {
        return new BaseResponse(Status.OK, null);
    }


}
