package com.kii.iotcloud;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Pair;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import com.google.gson.Gson;
import com.kii.iotcloud.command.Action;
import com.kii.iotcloud.command.ActionResult;
import com.kii.iotcloud.command.Command;
import com.kii.iotcloud.exception.IoTCloudException;
import com.kii.iotcloud.exception.UnsupportedActionException;
import com.kii.iotcloud.exception.UnsupportedSchemaException;
import com.kii.iotcloud.schema.Schema;
import com.kii.iotcloud.trigger.Predicate;
import com.kii.iotcloud.trigger.Trigger;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class operates an IoT device that is specified by <@link #onBoard(String, String, String, JSONObject)></@link> method.
 */
public class IoTCloudAPI implements Parcelable, Serializable {

    private final String appID;
    private final String appKey;
    private final Site site;
    private final Owner owner;
    private final Map<Pair<String, Integer>, Schema> schemas = new HashMap<Pair<String, Integer>, Schema>();

    IoTCloudAPI(
            @NonNull String appID,
            @NonNull String appKey,
            @NonNull Site site,
            @NonNull Owner owner,
            @NonNull List<Schema> schemas) {
        this.appID = appID;
        this.appKey = appKey;
        this.site = site;
        this.owner = owner;
        for (Schema schema : schemas) {
            this.schemas.put(new Pair<String, Integer>(schema.getSchemaName(), schema.getSchemaVersion()), schema);
        }
    }

    /** On board IoT Cloud with the specified vendor thing ID.
     * Specified thing will be owned by owner who is specified
     * <@link newWithAccessToken></@link>.
     * IoT Cloud prepares communication channel to the target.
     * @param vendorThingID Thing ID given by vendor. Must be specified.
     * @param thingPassword Thing Password given by vendor. Must be specified.
     * @param thingType Type of the thing given by vendor.
     *                  If the thing is already registered, this value would be
     *                  ignored by IoT Cloud.
     * @param thingProperties Properties of thing.
     *                        If the thing is already registered, this value
     *                        would be ignored by IoT Cloud.<br>
     *                        Refer to the <@link http://docs.kii.com/rest/#thing_management-register_a_thing>register_a_thing</@link><br>
     *                        About the format of this Document.
     * @return Target instance can be used to operate target, manage resources
     * of the target.
     * @throws IoTCloudException Thrown when failed to connect IoT Cloud Server
     * or IoT Cloud returns error response.
     */
    @NonNull
    @WorkerThread
    public Target onBoard(
            @NonNull String vendorThingID,
            @NonNull String thingPassword,
            @Nullable String thingType,
            @Nullable JSONObject thingProperties)
            throws IoTCloudException {
        // TODO: Implement it.
        return null;
    }

    /** On board IoT Cloud with the specified thing ID.
     * When you are sure that the on boarding process has been done,
     * this method is more convenient than
     * <@link #onBoard(String, String, String, JSONObject)></@link>.
     * @param thingID Thing ID given by IoT Cloud. Must be specified.
     * @param thingPassword Thing password given by vendor. Must be specified.
     * @return Target instance can be used to operate target, manage resources
     * of the target.
     * @throws IoTCloudException Thrown when failed to connect IoT Cloud Server
     * or IoT Cloud returns error response.
     */
    @NonNull
    @WorkerThread
    public Target onBoard(
            @NonNull String thingID,
            @NonNull String thingPassword) throws
            IoTCloudException {
        // TODO: Implement it.
        return null;
    }

    /** Checks whether on boarding is done.
     * @return true if done, otherwise false.
     */
    public boolean onBoarded()
    {
        // TODO: implement it.
        return false;
    }

    /** Install push notification to receive notification from IoT Cloud.
     * IoT Cloud will send notification when the Target replies to the Command.
     * Application can receive the notification and check the result of Command
     * fired by Application or registered Trigger.
     * After installation is done Installation ID is managed in this class.
     * @param deviceToken for GCM, specify token obtained by
     *                    InstanceID.getToken().
     *                    for JPUSH, specify id obtained by
     *                    JPushInterface.getUdid().
     * @param pushBackend Specify backend to use.
     * @return Installation ID used in IoT Cloud.
     * @throws IoTCloudException Thrown when failed to connect IoT Cloud Server
     * or IoT Cloud returns error response.
     */
    @NonNull
    @WorkerThread
    public String installPush(
            @Nullable String deviceToken,
            @NonNull PushBackend pushBackend
    ) throws IoTCloudException {
        // TODO: Implement it.
        return null;
    }

    /** Get installationID if the push is already installed.
     * null will be returned if the push installation has not been done.
     * @return Installation ID used in IoT Cloud.
     */
    @Nullable
    public String getInstallationID() {
        // TODO: Implement it.
        return null;
    }

    /** Uninstall push notification.
     * After done, notification from IoT Cloud won't be notified.
     * @param installationID installation ID returned from
     *                       <@link #installPush(String, PushBackend)></@link>
     *                       if null is specified, value obtained by
     *                       <@link #getInstallationID()></@link> is used.
     * @throws IoTCloudException Thrown when failed to connect IoT Cloud Server
     * or IoT Cloud returns error response.
     */
    @NonNull
    @WorkerThread
    public void uninstallPush(
            @Nullable String installationID
    ) throws IoTCloudException {
    }

    /** Post new command to IoT Cloud.
     * Command will be delivered to specified target and result will be notified
     * through push notification.
     * @param target Target of the command to be delivered.
     * @param actions Actions to be executed.
     * @param issuer Specify command issuer. If you execute command as group,
     *               you can use group:{gropuID} as issuer.
     *               If Null owner specified in <@link #newWithAccessToken></@link>
     *               Will be the issuer of the command.
     * @return Created Command instance. At this time, Command is delivered to
     * the target Asynchronously and may not finished. Actual Result will be
     * delivered through push notification or you can check the latest status
     * of the command by calling <@link #getCommand></@link>.
     * @throws IoTCloudException Thrown when failed to connect IoT Cloud Server
     * or IoT Cloud returns error response.
     */
    @NonNull
    @WorkerThread
    public Command postNewCommand(
            @NonNull Target target,
            @NonNull String schemaName,
            int schemaVersion,
            @NonNull List<Action> actions,
            @Nullable TypedID issuer) throws IoTCloudException {
        // TODO: Implement it.
        if (target == null) {
            throw new IllegalArgumentException("target is null");
        }
        Schema schema = this.getSchema(schemaName, schemaVersion);
        if (schema == null) {
            throw new UnsupportedOperationException(schemaName + " is not supported");
        }
        if (actions == null || actions.size() == 0) {
            throw new IllegalArgumentException("actions is null or empty");
        }
        return null;
    }

    /** Get specified command.
     * @param target Target of the command.
     * @param commandID ID of the command to obtain. ID is present in the
     *                  instance returned by <@link #postNewCommand></@link>
     *                  and can be obtained by <@link Command#getCommandID></@link>
     *
     * @return Command instance.
     * @throws IoTCloudException Thrown when failed to connect IoT Cloud Server
     * or IoT Cloud returns error response.
     */
    @NonNull
    @WorkerThread
    public Command getCommand(
            @NonNull Target target,
            @NonNull String commandID)
            throws IoTCloudException {
        // TODO: implement it.
        return null;
    }

    /** List Commands in the specified Target.
     * @param target Target to which the Commands belongs.
     * @param bestEffortLimit Maximum number of the Commands in the response.
     *                        if the value is <= 0, default limit internally
     *                        defined is applied.
     *                        Meaning of 'bestEffort' is if the specified limit
     *                        is greater than default limit, default limit is
     *                        applied.
     * @param paginationKey Used to get the next page of previously obtained.
     *                      If there is further page to obtain, this method
     *                      returns paginationKey as the 2nd element of pair.
     *                      Applying this key to the argument results continue
     *                      to get the result from the next page.
     * @return 1st Element is Commands belongs to the Target. 2nd element is
     * paginationKey if there is next page to be obtained.
     * @throws IoTCloudException Thrown when failed to connect IoT Cloud Server
     * or IoT Cloud returns error response.
     */
    public Pair<List<Command>, String> listCommands (
            @NonNull Target target,
            int bestEffortLimit,
            @Nullable String paginationKey)
            throws IoTCloudException {
        // TODO: implement it.
        return null;
    }

    /** Post new Trigger to IoT Cloud.
     * @param target Target of which the trigger stored. It the trigger is based
     *               on state of target, Trigger is evaluated when the state of
     *               the target has been updated.
     * @param actions Specify actions included in the Command is fired by the
     *                trigger.
     * @param issuer Specify issuer of the Command includes specified actions
     *               when the Command is fired by trigger.
     * @param predicate Specify when the Trigger fires command.
     * @return Instance of the Trigger registered in IoT Cloud.
     * @throws IoTCloudException Thrown when failed to connect IoT Cloud Server
     * or IoT Cloud returns error response.
     */
    @NonNull
    @WorkerThread
    public Trigger postNewTrigger(
            @NonNull Target target,
            @NonNull String schemaName,
            int schemaVersion,
            @NonNull List<Action> actions,
            @Nullable TypedID issuer,
            @NonNull Predicate predicate)
            throws IoTCloudException {
        // TODO: implement it.
        return null;
    }

    /** Get specified Trigger.
     * @param target Target of which the trigger stored.
     * @param triggerID ID of the Trigger to get.
     * @return Trigger instance.
     * @throws IoTCloudException Thrown when failed to connect IoT Cloud Server
     * or IoT Cloud returns error response.
     */
    @NonNull
    @WorkerThread
    public Trigger getTrigger(
            @NonNull Target target,
            @NonNull String triggerID)
            throws IoTCloudException {
        // TODO: implement it.
        return null;
    }

    /** Apply Patch to registered Trigger
     * Modify registered Trigger with specified patch.
     * @param target Target of which the Trigger stored
     * @param triggerID ID ot the Trigger to apply patch
     * @param actions Modified actions.
     *                If null NonNull predicate must be specified.
     * @param predicate Modified predicate.
     *                  If null NonNull actions must be specified.
     * @return Updated Trigger instance.
     * @throws IoTCloudException Thrown when failed to connect IoT Cloud Server
     * or IoT Cloud returns error response.
     * @throws IllegalArgumentException when both actions and predicates are null
     */
    @NonNull
    @WorkerThread
    public Trigger patchTrigger(
            @NonNull Target target,
            @NonNull String triggerID,
            @Nullable List<Action> actions,
            @Nullable Predicate predicate) throws
            IoTCloudException {
        // TODO: implement it.
        return null;
    }

    /** Enable/Disable registered Trigger
     * If its already enabled(/disabled),
     * this method won't throw Exception and behave as succeeded.
     * @param target Target of which the Trigger stored.
     * @param triggerID ID of the Trigger to be enabled(/disabled).
     * @param enable specify whether enable of disable the Trigger.
     * @return Updated Trigger Instance.
     * @throws IoTCloudException Thrown when failed to connect IoT Cloud Server
     * or IoT Cloud returns error response.
     */
    @NonNull
    @WorkerThread
    public Trigger enableTrigger(
            @NonNull Target target,
            @NonNull String triggerID,
            boolean enable)
            throws IoTCloudException {
        // TODO: implement it.
        return null;
    }

    /** Delete the specified Trigger.
     * @param target Target of which the Trigger stored.
     * @param triggerID ID of the Trigger to be deleted.
     * @return Deleted Trigger Instance.
     * @throws IoTCloudException Thrown when failed to connect IoT Cloud Server
     * or IoT Cloud returns error response.
     */
    @NonNull
    @WorkerThread
    public Trigger deleteTrigger(Target target, String triggerID) throws
            IoTCloudException {
        // TODO: implement it.
        return null;
    }

    /** List Triggers belongs to the specified Target.
     * @param target Target of which the Trigger stored.
     * @param paginationKey If specified obtain rest of the items.
     * @param bestEffortLimit limit the maximum number of the Triggers in the
     *                        Response. It ensures numbers in
     *                        response is equals to or less than specified number.
     *                        But doesn't ensures number of the Triggers
     *                        in the response is equal to specified value.<br>
     *                        If the specified value <= 0, Default size of the limit
     *                        is applied by IoT Cloud.
     * @return first is list of the Triggers and second is paginationKey returned
     * by IoT Cloud. paginationKey is null when there is next page to be obtained.
     * Obtained paginationKey can be used to get the rest of the items stored
     * in the target.
     * @throws IoTCloudException Thrown when failed to connect IoT Cloud Server
     * or IoT Cloud returns error response.
     */
    @NonNull
    @WorkerThread
    public Pair<List<Trigger>, String> listTriggers(
            @NonNull Target target,
            @NonNull String paginationKey,
            int bestEffortLimit) throws
            IoTCloudException {
        // TODO: implement it.
        return null;
    }

    /** Get the State of specified Target.
     * State will be serialized with Gson library.
     * @param classOfS Specify class of the State.
     * @param <S> State class.
     * @return Instance of Target State.
     */
    @NonNull
    @WorkerThread
    public <S extends TargetState> S getTargetState(
            @NonNull Target target,
            @NonNull Class<S> classOfS) {
        // TODO: implement it.
        Gson gson = new Gson();
        S ret = gson.fromJson("{\"power\" : true}", classOfS);
        return ret;
    }
    private Schema getSchema(String schemaName, int schemaVersion) {
        return this.schemas.get(new Pair<String, Integer>(schemaName, schemaVersion));
    }
    private Action generateAction(String schemaName, int schemaVersion, String actionName, JSONObject actionParameters) throws IoTCloudException {
        Schema schema = this.getSchema(schemaName, schemaVersion);
        if (schema == null) {
            throw new UnsupportedSchemaException(schemaName, schemaVersion);
        }
        Class<? extends Action> actionClass = schema.getActionClass(actionName);
        if (actionClass == null) {
            throw new UnsupportedActionException(schemaName, schemaVersion, actionName);
        }
        Gson gson = new Gson();
        String json = actionParameters == null ? "{}" : actionParameters.toString();
        return gson.fromJson(json, actionClass);
    }
    private ActionResult generateActionResult(String schemaName, int schemaVersion, String actionName, JSONObject actionResult) throws IoTCloudException {
        Schema schema = this.getSchema(schemaName, schemaVersion);
        if (schema == null) {
            throw new UnsupportedSchemaException(schemaName, schemaVersion);
        }
        Class<? extends ActionResult> actionResultClass = schema.getActionResultClass(actionName);
        if (actionResultClass == null) {
            throw new UnsupportedActionException(schemaName, schemaVersion, actionName);
        }
        Gson gson = new Gson();
        String json = actionResult == null ? "{}" : actionResult.toString();
        return gson.fromJson(json, actionResultClass);
    }

    protected IoTCloudAPI(Parcel in) {
        this.appID = in.readString();
        this.appKey = in.readString();
        this.site = (Site)in.readSerializable();
        this.owner = in.readParcelable(Owner.class.getClassLoader());
        ArrayList<Schema> schemas = in.createTypedArrayList(Schema.CREATOR);
        for (Schema schema : schemas) {
            this.schemas.put(new Pair<String, Integer>(schema.getSchemaName(), schema.getSchemaVersion()), schema);
        }
    }

    public static final Creator<IoTCloudAPI> CREATOR = new Creator<IoTCloudAPI>() {
        @Override
        public IoTCloudAPI createFromParcel(Parcel in) {
            return new IoTCloudAPI(in);
        }

        @Override
        public IoTCloudAPI[] newArray(int size) {
            return new IoTCloudAPI[size];
        }
    };
    @Override
    public int describeContents() {
        return 0;
    }
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.appID);
        dest.writeString(this.appKey);
        dest.writeSerializable(this.site);
        dest.writeParcelable(this.owner, flags);
        dest.writeTypedList(new ArrayList<Schema>(this.schemas.values()));
    }
}
