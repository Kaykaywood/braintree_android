package com.braintreepayments.api.exceptions;

import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 *  Error container returned when the Braintree server receives a 422 Unprocessible Entity.
 *  A 422 occurs when a request is properly formed, but the server was unable to take the requested
 *  action due to bad user data.
 *
 *  ErrorWithResponse parses the server's error response and exposes the errors.
 */
public class ErrorWithResponse extends Exception {

    private static final String ERROR_KEY = "error";
    private static final String MESSAGE_KEY = "message";
    private static final String FIELD_ERRORS_KEY = "fieldErrors";

    private int mStatusCode;
    private String mMessage;
    private String mOriginalResponse;
    private List<BraintreeError> mFieldErrors;

    public ErrorWithResponse(int statusCode, String jsonString) {
        mStatusCode = statusCode;
        mOriginalResponse = jsonString;

        try {
            JSONObject json = new JSONObject(jsonString);
            mMessage = json.getJSONObject(ERROR_KEY).getString(MESSAGE_KEY);
            mFieldErrors = BraintreeError.fromJsonArray(json.optJSONArray(FIELD_ERRORS_KEY));
        } catch (JSONException e) {
            mMessage = "Parsing error response failed";
            mFieldErrors = new ArrayList<>();
        }
    }

    /**
     * @return HTTP status code from the Braintree gateway.
     */
    public int getStatusCode() {
        return mStatusCode;
    }

    /**
     * @return Human readable top level summary of the error.
     */
    public String getMessage() {
        return mMessage;
    }

    /**
     * @return The full error response as a {@link String}.
     */
    public String getErrorResponse() {
        return mOriginalResponse;
    }

    /**
     * @return All the field errors.
     */
    public List<BraintreeError> getFieldErrors() {
        return mFieldErrors;
    }

    /**
     * Method to extract an error for an individual field, e.g. creditCard, customer, etc.
     *
     * @param field Name of the field desired, expected to be in camelCase.
     * @return {@link BraintreeError} for the field searched, or {@code null} if not found.
    */
    @Nullable
    public BraintreeError errorFor(String field) {
        BraintreeError returnError;
        if(mFieldErrors != null) {
            for (BraintreeError error : mFieldErrors) {
                if (error.getField().equals(field)) {
                    return error;
                } else if (error.getFieldErrors() != null) {
                    returnError = error.errorFor(field);
                    if (returnError != null) {
                        return returnError;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "ErrorWithResponse (" + mStatusCode + "): " + mMessage + "\n" +
                mFieldErrors.toString();
    }
}
