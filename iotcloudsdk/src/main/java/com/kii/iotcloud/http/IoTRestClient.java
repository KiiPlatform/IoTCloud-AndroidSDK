package com.kii.iotcloud.http;

import android.text.TextUtils;

import com.kii.iotcloud.exception.BadRequestException;
import com.kii.iotcloud.exception.ConflictException;
import com.kii.iotcloud.exception.ForbiddenException;
import com.kii.iotcloud.exception.GatewayTimeoutException;
import com.kii.iotcloud.exception.InternalServerErrorException;
import com.kii.iotcloud.exception.IoTCloudException;
import com.kii.iotcloud.exception.IoTCloudRestException;
import com.kii.iotcloud.exception.NotFoundException;
import com.kii.iotcloud.exception.ServiceUnavailableException;
import com.kii.iotcloud.exception.UnauthorizedException;
import com.kii.iotcloud.utils.IOUtils;
import com.kii.iotcloud.utils.Log;
import com.kii.iotcloud.utils.StringUtils;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request.Builder;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okio.BufferedSink;

/**
 * Wrap the third party HTTP client library.
 * This class is for internal use only. Do not use it from your application.
 */
public class IoTRestClient {

    private static final String TAG = IoTRestClient.class.getSimpleName();
    protected static final OkHttpClient client = OkHttpClientFactory.newInstance();
    private static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json");

    public IoTRestClient() {
    }

    /**
     * Send specified HTTP request to KiiCloud
     *
     * @param request
     * @return
     * @throws IoTCloudException
     */
    public JSONObject sendRequest(IoTRestRequest request) throws IoTCloudException {
        Response response = this.execute(request);
        return this.parseResponseAsJsonObject(request, response);
    }
    private Response execute(IoTRestRequest request) throws IoTCloudException {
        Builder builder = new Builder();
        builder.url(request.getUrl());
        builder.headers(Headers.of(request.getHeaders()));

        OkHttpClient httpClient = client.clone();
        switch (request.getMethod()) {
            case HEAD:
                builder.head();
                break;
            case GET:
                builder.get();
                break;
            case POST:
                builder.post(this.createRequestBody(request.getContentType(), request.getEntity()));
                httpClient.setRetryOnConnectionFailure(false);
                break;
            case PUT:
                builder.put(this.createRequestBody(request.getContentType(), request.getEntity()));
                httpClient.setRetryOnConnectionFailure(false);
                break;
            case PATCH:
                builder.patch(this.createRequestBody(request.getContentType(), request.getEntity()));
                httpClient.setRetryOnConnectionFailure(false);
                break;
            case DELETE:
                builder.delete();
                break;
        }
        try {
            return httpClient.newCall(builder.build()).execute();
        } catch (IOException e) {
            throw new IoTCloudException(request.getCurl(), e);
        }
    }
    protected RequestBody createRequestBody(final MediaType contentType, final Object entity) {
        if (entity == null) {
            return RequestBody.create(contentType, "");
        }
        if (entity instanceof String) {
            return RequestBody.create(contentType, (String)entity);
        }
        if (entity instanceof byte[]) {
            return RequestBody.create(contentType, (byte[])entity);
        }
        if (entity instanceof File) {
            return RequestBody.create(contentType, (File)entity);
        }
        if (entity instanceof JSONObject) {
            return RequestBody.create(contentType, ((JSONObject)entity).toString());
        }
        if (entity instanceof JSONArray) {
            return RequestBody.create(contentType, ((JSONArray)entity).toString());
        }
        if (entity instanceof InputStream) {
            return new RequestBody() {
                @Override
                public long contentLength() throws IOException {
                    return ((InputStream)entity).available();
                }
                @Override
                public MediaType contentType() {
                    return contentType;
                }
                @Override
                public void writeTo(BufferedSink sink) throws IOException {
                    OutputStream os = sink.outputStream();
                    IOUtils.copy((InputStream) entity, os);
                }
            };
        }
        throw new RuntimeException("Unexpected entity type.");
    }
    private void parseResponse(IoTRestRequest request, Response response) throws IoTCloudException {
        try {
            String body = response.body().string();
            this.checkHttpStatus(request, response, body);
        } catch (IOException e) {
            throw new IoTCloudException(request.getCurl(), e);
        }
    }
    private String parseResponseAsString(IoTRestRequest request, Response response) throws IoTCloudException {
        try {
            String body = response.body().string();
            this.checkHttpStatus(request, response, body);
            if (TextUtils.isEmpty(body)) {
                return null;
            }
            return body;
        } catch (IOException e) {
            throw new IoTCloudException(request.getCurl(), e);
        }
    }
    private JSONObject parseResponseAsJsonObject(IoTRestRequest request, Response response) throws IoTCloudException {
        try {
            String body = response.body().string();
            this.checkHttpStatus(request, response, body);
            Log.d(TAG, request.getCurl() + StringUtils.LINE_SEPARATOR + body);
            if (TextUtils.isEmpty(body)) {
                return null;
            }
            return new JSONObject(body);
        } catch (JSONException e) {
            throw new IoTCloudException(request.getCurl(), e);
        } catch (IOException e) {
            throw new IoTCloudException(request.getCurl(), e);
        }
    }
    private JSONArray parseResponseAsJsonArray(IoTRestRequest request, Response response) throws IoTCloudException {
        try {
            String body = response.body().string();
            this.checkHttpStatus(request, response, body);
            if (TextUtils.isEmpty(body)) {
                return null;
            }
            return new JSONArray(body);
        } catch (JSONException e) {
            throw new IoTCloudException(request.getCurl(), e);
        } catch (IOException e) {
            throw new IoTCloudException(request.getCurl(), e);
        }
    }
    private InputStream parseResponseAsInputStream(IoTRestRequest request, Response response) throws IoTCloudException {
        try {
            if (!response.isSuccessful()) {
                String body = response.body().string();
                this.checkHttpStatus(request, response, body);
            }
            return response.body().byteStream();
        } catch (IOException e) {
            throw new IoTCloudException(request.getCurl(), e);
        }
    }
    private void checkHttpStatus(IoTRestRequest request, Response response, String responseBody) throws IoTCloudRestException {
        if (!response.isSuccessful()) {
            JSONObject errorDetail = null;
            try {
                errorDetail = new JSONObject(responseBody);
            } catch (Exception ignore) {
            }
            Log.w(TAG, request.getCurl() + "  --  " + response.code() + ":" + errorDetail);
            switch (response.code()) {
                case 400:
                    throw new BadRequestException(request.getCurl(), errorDetail);
                case 401:
                    throw new UnauthorizedException(request.getCurl(), errorDetail);
                case 403:
                    throw new ForbiddenException(request.getCurl(), errorDetail);
                case 404:
                    throw new NotFoundException(request.getCurl(), errorDetail);
                case 409:
                    throw new ConflictException(request.getCurl(), errorDetail);
                case 500:
                    throw new InternalServerErrorException(request.getCurl(), errorDetail);
                case 503:
                    throw new ServiceUnavailableException(request.getCurl(), errorDetail);
                case 504:
                    throw new GatewayTimeoutException(request.getCurl(), errorDetail);
                default:
                    throw new IoTCloudRestException(request.getCurl(), response.code(), errorDetail);
            }
        }
    }
}
