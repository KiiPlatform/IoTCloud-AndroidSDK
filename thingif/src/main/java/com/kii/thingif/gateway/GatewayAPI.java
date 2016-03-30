package com.kii.thingif.gateway;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.text.TextUtils;
import android.util.Base64;

import com.kii.thingif.KiiApp;
import com.kii.thingif.MediaTypes;
import com.kii.thingif.exception.StoredGatewayAPIInstanceNotFoundException;
import com.kii.thingif.exception.ThingIFException;
import com.kii.thingif.internal.GsonRepository;
import com.kii.thingif.internal.http.IoTRestClient;
import com.kii.thingif.internal.http.IoTRestRequest;
import com.kii.thingif.internal.utils.Path;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GatewayAPI implements Parcelable {

    private static final String SHARED_PREFERENCES_KEY_INSTANCE = "GatewayAPI_INSTANCE";
    private static Context context;
    private final String tag;
    private final KiiApp app;
    private final GatewayAddress gatewayAddress;
    private String accessToken;
    private final IoTRestClient restClient;

    GatewayAPI(@Nullable Context context,
               @NonNull KiiApp app,
               @NonNull GatewayAddress gatewayAddress) {
        this(context, null, app, gatewayAddress);
    }
    GatewayAPI(@Nullable Context context,
               @Nullable String tag,
               @NonNull KiiApp app,
               @NonNull GatewayAddress gatewayAddress) {
        if (context != null) {
            GatewayAPI.context = context.getApplicationContext();
        }
        this.app = app;
        this.tag =tag;
        this.gatewayAddress = gatewayAddress;
        this.restClient = new IoTRestClient();
    }

    protected Map<String, String> newHeader() {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Authorization", "Bearer " + this.accessToken);
        return headers;
    }

    /** Login to the Gateway.
     * Local authentication for the Gateway access.
     * Required prior to call other APIs access to the gateway.
     * @param username Username of the Gateway.
     * @param password Password of the Gateway.
     * @throws ThingIFException
     */
    @WorkerThread
    public void login(@NonNull String username, @NonNull String password) throws ThingIFException {
        if (TextUtils.isEmpty(username)) {
            throw new IllegalArgumentException("username is null or empty");
        }
        if (TextUtils.isEmpty(password)) {
            throw new IllegalArgumentException("password is null or empty");
        }
        String path = MessageFormat.format("/{0}/token", this.app.getSiteName());
        String url = Path.combine(this.gatewayAddress.getBaseUrl(), path);

        String credential = this.app.getAppID() + ":" + this.app.getAppKey();
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Authorization", "Basic " + Base64.encodeToString(credential.getBytes(), Base64.NO_WRAP));

        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("username", username);
            requestBody.put("password", password);
        } catch (JSONException e) {
            // Won’t happen
        }

        IoTRestRequest request = new IoTRestRequest(url, IoTRestRequest.Method.POST, headers, MediaTypes.MEDIA_TYPE_JSON, requestBody);
        JSONObject responseBody = new IoTRestClient().sendRequest(request);
        this.accessToken = responseBody.optString("accessToken", null);
        this.saveInstance(this);
    }

    /** Let the Gateway Onboard.
     * @return Thing ID assigned by Kii Cloud.
     * @throws ThingIFException Thrown when gateway returns error response.
     * @throws IllegalStateException Thrown when user is not logged in.
     * See {@link #login(String, String)}
     */
    @NonNull
    @WorkerThread
    public String onboardGateway() throws ThingIFException {
        if (!isLoggedIn()) {
            throw new IllegalStateException("Needs user login before execute this API");
        }
        String path = MessageFormat.format("/{0}/apps/{1}/gateway/onboarding", this.app.getSiteName(), this.app.getAppID());
        String url = Path.combine(this.gatewayAddress.getBaseUrl(), path);
        Map<String, String> headers = this.newHeader();

        IoTRestRequest request = new IoTRestRequest(url, IoTRestRequest.Method.POST, headers);
        JSONObject responseBody = this.restClient.sendRequest(request);
        return responseBody.optString("thingID", null);
    }

    /**
     * Get Gateway ID
     * @return Thing ID assigned by Kii Cloud.
     * @throws ThingIFException Thrown when gateway returns error response.
     * @throws IllegalStateException Thrown when user is not logged in.
     * See {@link #login(String, String)}
     */
    @WorkerThread
    @NonNull
    public String getGatewayID() throws ThingIFException {
        if (!isLoggedIn()) {
            throw new IllegalStateException("Needs user login before execute this API");
        }
        String path = MessageFormat.format("/{0}/apps/{1}/gateway/id", this.app.getSiteName(), this.app.getAppID());
        String url = Path.combine(this.gatewayAddress.getBaseUrl(), path);
        Map<String, String> headers = this.newHeader();

        IoTRestRequest request = new IoTRestRequest(url, IoTRestRequest.Method.GET, headers);
        JSONObject responseBody = this.restClient.sendRequest(request);
        return responseBody.optString("thingID", null);
    }

    /** List connected end nodes which has not been onboarded.
     * @return List of end nodes connected to the gateway but waiting for onboarding.
     * @throws ThingIFException Thrown when gateway returns error response.
     * @throws IllegalStateException Thrown when user is not logged in.
     * See {@link #login(String, String)}
     */
    @WorkerThread
    @NonNull
    public List<PendingEndNode> listPendingEndNodes() throws ThingIFException {
        if (!isLoggedIn()) {
            throw new IllegalStateException("Needs user login before execute this API");
        }
        String path = MessageFormat.format("/{0}/apps/{1}/gateway/end-nodes/pending", this.app.getSiteName(), this.app.getAppID());
        String url = Path.combine(this.gatewayAddress.getBaseUrl(), path);
        Map<String, String> headers = this.newHeader();

        IoTRestRequest request = new IoTRestRequest(url, IoTRestRequest.Method.GET, headers);
        JSONObject responseBody = this.restClient.sendRequest(request);
        List<PendingEndNode> nodes = new ArrayList<PendingEndNode>();
        JSONArray results = responseBody.optJSONArray("results");
        if (results != null) {
            for (int i = 0; i < results.length(); i++) {
                try {
                    nodes.add(new PendingEndNode(results.getJSONObject(i)));
                } catch (JSONException ignore) {
                }
            }
        }
        return nodes;
    }

    /** Notify Onboarding completion
     * Call this api when the End Node onboarding is done.
     * After the call succeeded, End Node will be fully connected to Kii Cloud through the Gateway.
     * @param endNodeThingID ID of the end-node assigned by Kii Cloud.
     * @param endNodeVenderThingID ID of the end-node assigned by End Node vendor.
     * @throws ThingIFException
     * @throws IllegalStateException Thrown when user is not logged in.
     * See {@link #login(String, String)}
     */
    @WorkerThread
    public void notifyOnboardingCompletion(@NonNull String endNodeThingID, @NonNull String endNodeVenderThingID) throws ThingIFException {
        if (!isLoggedIn()) {
            throw new IllegalStateException("Needs user login before execute this API");
        }
        if (TextUtils.isEmpty(endNodeThingID)) {
            throw new IllegalArgumentException("thingID is null or empty");
        }
        if (TextUtils.isEmpty(endNodeVenderThingID)) {
            throw new IllegalArgumentException("venderThingID is null or empty");
        }
        String path = MessageFormat.format("/{0}/apps/{1}/gateway/end-nodes/VENDOR_THING_ID:{2}", this.app.getSiteName(), this.app.getAppID(), endNodeVenderThingID);
        String url = Path.combine(this.gatewayAddress.getBaseUrl(), path);
        Map<String, String> headers = this.newHeader();

        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("thingID", endNodeThingID);
        } catch (JSONException e) {
            // Won’t happen
        }
        IoTRestRequest request = new IoTRestRequest(url, IoTRestRequest.Method.PUT, headers, MediaTypes.MEDIA_TYPE_JSON, requestBody);
        this.restClient.sendRequest(request);
    }

    /** Restore the Gateway
     * @throws ThingIFException
     * @throws IllegalStateException Thrown when user is not logged in.
     * See {@link #login(String, String)}
     */
    @WorkerThread
    public void restore() throws ThingIFException {
        if (!isLoggedIn()) {
            throw new IllegalStateException("Needs user login before execute this API");
        }
        String path = "/gateway-app/gateway/restore";
        String url = Path.combine(this.gatewayAddress.getBaseUrl(), path);
        Map<String, String> headers = this.newHeader();
        IoTRestRequest request = new IoTRestRequest(url, IoTRestRequest.Method.POST, headers);
        this.restClient.sendRequest(request);
    }

    /**
     * Replace end-node by new vendorThingID for end node thingID.
     *
     * @param endNodeThingID ID of the end-node assigned by Kii Cloud.
     * @param endNodeVenderThingID ID of the end-node assigned by End Node vendor.
     * @throws ThingIFException
     * @throws IllegalStateException Thrown when user is not logged in.
     * See {@link #login(String, String)}
     */
    @WorkerThread
    public void replaceEndNode(@NonNull String endNodeThingID, @NonNull String endNodeVenderThingID) throws ThingIFException {
        if (!isLoggedIn()) {
            throw new IllegalStateException("Needs user login before execute this API");
        }
        if (TextUtils.isEmpty(endNodeThingID)) {
            throw new IllegalArgumentException("thingID is null or empty");
        }
        if (TextUtils.isEmpty(endNodeVenderThingID)) {
            throw new IllegalArgumentException("venderThingID is null or empty");
        }
        String path = MessageFormat.format("/{0}/apps/{1}/gateway/end-nodes/THING_ID:{2}", this.app.getSiteName(), this.app.getAppID(), endNodeThingID);
        String url = Path.combine(this.gatewayAddress.getBaseUrl(), path);
        Map<String, String> headers = this.newHeader();

        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("vendorThingID", endNodeVenderThingID);
        } catch (JSONException e) {
            // Won’t happen
        }
        IoTRestRequest request = new IoTRestRequest(url, IoTRestRequest.Method.PUT, headers, MediaTypes.MEDIA_TYPE_JSON, requestBody);
        this.restClient.sendRequest(request);
    }

    /**
     * Get vendorThingID of the Gateway.
     * When the end user replaces the Gateway, Gateway App/End Node App need to obtain the new Gateway’s vendorThingID.
     *
     * @return vendorThingID of the Gateway.
     * @throws ThingIFException
     * @throws IllegalStateException Thrown when user is not logged in.
     */
    @WorkerThread
    @NonNull
    public String getGatewayInformation() throws ThingIFException {
        if (!isLoggedIn()) {
            throw new IllegalStateException("Needs user login before execute this API");
        }
        String path = "/gateway-info";
        String url = Path.combine(this.gatewayAddress.getBaseUrl(), path);
        Map<String, String> headers = this.newHeader();

        IoTRestRequest request = new IoTRestRequest(url, IoTRestRequest.Method.GET, headers);
        JSONObject responseBody = this.restClient.sendRequest(request);
        return responseBody.optString("vendorThingID", null);
    }

    /** Check If user is logged in to the Gateway.
     * @return true if user is logged in, false otherwise.
     */
    public boolean isLoggedIn() {
        return !TextUtils.isEmpty(this.accessToken);
    }

    /**
     * Get a tag.
     * @return
     */
    public String getTag() {
        return this.tag;
    }
    /** Get Kii App
     * @return Kii Cloud Application.
     */
    public KiiApp getApp() {
        return this.app;
    }
    /**
     * Get AppID
     * @return
     */
    public String getAppID() {
        return this.app.getAppID();
    }
    /**
     * Get AppKey
     * @return
     */
    public String getAppKey() {
        return this.app.getAppKey();
    }

    /** Get GatewayAddress
     * @return Gateway Address
     */
    public GatewayAddress getGatewayAddress() {
        return this.gatewayAddress;
    }

    /**
     * Get Access Token
     * @return
     */
    public String getAccessToken() {
        return this.accessToken;
    }
    void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
    // Implementation of Parcelable
    public static final Creator<GatewayAPI> CREATOR = new Creator<GatewayAPI>() {
        @Override
        public GatewayAPI createFromParcel(Parcel in) {
            return new GatewayAPI(in);
        }

        @Override
        public GatewayAPI[] newArray(int size) {
            return new GatewayAPI[size];
        }
    };

    protected GatewayAPI(Parcel in) {
        this.tag = in.readString();
        this.app = in.readParcelable(KiiApp.class.getClassLoader());
        this.gatewayAddress = in.readParcelable(GatewayAddress.class.getClassLoader());
        this.accessToken = in.readString();
        this.restClient = new IoTRestClient();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.tag);
        dest.writeParcelable(this.app, flags);
        dest.writeParcelable(this.gatewayAddress, flags);
        dest.writeString(this.accessToken);
    }

    /**
     * Try to load the instance of GatewayAPI using stored serialized instance.
     * <BR>
     * Instance is automatically saved when {@link #login(String, String)} is called.
     * <BR>
     *
     * If the GatewayAPI instance is build without the tag, all instance is saved in same place
     * and overwritten when the instance is saved.
     * <BR>
     * <BR>
     *
     * If the GatewayAPI instance is build with the tag(optional), tag is used as key to distinguish
     * the storage area to save the instance. This would be useful to saving multiple instance.
     * You need specify tag to load the instance by the
     * {@link #loadFromStoredInstance(Context, String) api}.
     *
     * @param context context
     * @return ThingIFAPI instance.
     * @throws StoredGatewayAPIInstanceNotFoundException when the instance has not stored yet.
     */
    public static GatewayAPI loadFromStoredInstance(@NonNull Context context) throws StoredGatewayAPIInstanceNotFoundException {
        return loadFromStoredInstance(context, null);
    }

    /**
     * Try to load the instance of GatewayAPI using stored serialized instance.
     * <BR>
     * For details please refer to the {@link #loadFromStoredInstance(Context)} document.
     *
     * @param context context
     * @param  tag specified when the ThingIFAPI has been built.
     * @return GatewayAPI instance.
     * @throws StoredGatewayAPIInstanceNotFoundException when the instance has not stored yet.
     */
    public static GatewayAPI loadFromStoredInstance(@NonNull Context context, String tag) throws StoredGatewayAPIInstanceNotFoundException {
        GatewayAPI.context = context.getApplicationContext();
        SharedPreferences preferences = getSharedPreferences();
        String serializedJson = preferences.getString(getSharedPreferencesKey(tag), null);
        if (serializedJson != null) {
            return  GsonRepository.gson().fromJson(serializedJson, GatewayAPI.class);
        }
        throw new StoredGatewayAPIInstanceNotFoundException(tag);
    }
    /**
     * Clear all saved instances in the SharedPreferences.
     */
    public static void removeAllStoredInstances() {
        SharedPreferences preferences = getSharedPreferences();
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
    }
    /**
     * Remove saved specified instance in the SharedPreferences.
     *
     * @param tag
     */
    public static void removeStoredInstance(String tag) {
        SharedPreferences preferences = getSharedPreferences();
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(getSharedPreferencesKey(tag));
        editor.apply();
    }
    private static void saveInstance(GatewayAPI instance) {
        SharedPreferences preferences = getSharedPreferences();
        if (preferences != null) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(getSharedPreferencesKey(instance.tag), GsonRepository.gson().toJson(instance));
            editor.apply();
        }
    }
    private static String getSharedPreferencesKey(String tag) {
        return SHARED_PREFERENCES_KEY_INSTANCE + (tag == null ? "" : "_"  +tag);
    }
    private static SharedPreferences getSharedPreferences() {
        if (context != null) {
            return context.getSharedPreferences("com.kii.thingif.preferences", Context.MODE_PRIVATE);
        }
        return null;
    }
}
