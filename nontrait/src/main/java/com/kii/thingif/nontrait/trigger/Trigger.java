package com.kii.thingif.trigger;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.kii.thingif.core.TypedID;
import com.kii.thingif.command.Command;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents a trigger that is fired when status of thing changed or it became at the designated time.
 */
public class Trigger implements Parcelable {

    private String triggerID;
    private TypedID targetID;
    private final Predicate predicate;
    private final Command command;
    private final ServerCode serverCode;
    private boolean disabled;
    private String disabledReason;
    private String title;
    private String description;
    private JSONObject metadata;

    public Trigger(@NonNull Predicate predicate, @NonNull Command command) {
        if (predicate == null) {
            throw new IllegalArgumentException("predicate is null");
        }
        if (command == null) {
            throw new IllegalArgumentException("command is null");
        }
        this.predicate = predicate;
        this.command = command;
        this.serverCode = null;
    }
    public Trigger(@NonNull Predicate predicate, @NonNull ServerCode serverCode) {
        if (predicate == null) {
            throw new IllegalArgumentException("predicate is null");
        }
        if (serverCode == null) {
            throw new IllegalArgumentException("serverCode is null");
        }
        this.predicate = predicate;
        this.command = null;
        this.serverCode = serverCode;
    }

    /**
     * Get ID of the Trigger.
     * @return ID of the Trigger
     */
    public String getTriggerID() {
        return this.triggerID;
    }

    /**
     * Get Target ID of the Trigger.
     * When Trigger is created with ThingIFAPI#postNewTrigger() API families,
     * Target ID is determined by target bound to ThingIFAPI.
     * @return Target ID of Trigger.
     */
    public TypedID getTargetID() {
        return this.targetID;
    }

    /**
     * Indicate whether the Trigger is disabled.
     * @return true if disabled, otherwise false.
     */
    public boolean disabled() {
        return this.disabled;
    }

    /**
     * Get Predicate of the Trigger.
     * @return Predicate of the Trigger
     */
    public Predicate getPredicate() {
        return this.predicate;
    }

    /**
     * Get Command bounds to the Trigger.
     * @return Command  bounds to the Trigger.
     */
    public Command getCommand() {
        return this.command;
    }

    /**
     * Get Server Code bounds to the Trigger.
     * @return Server Code bounds to the Trigger.
     */
    public ServerCode getServerCode() {
        return this.serverCode;
    }

    /**
     * Get enum indicates whether the Command or Server Code is triggered.
     * @return TriggersWhat enum.
     */
    public TriggersWhat getTriggersWhat() {
        if (this.command != null) {
            return TriggersWhat.COMMAND;
        }
        return TriggersWhat.SERVER_CODE;
    }


    /**
     * Get the reason of the Trigger has been disabled.
     * If #disabled is false, It returns null.
     * @return Reason of the Trigger has been disabled.
     */
    public String getDisabledReason() {
        return this.disabledReason;
    }

    /**
     * Get title.
     * @return title of this trigger.
     */
    public String getTitle() {
        return this.title;
    }
    /**
     * Get description.
     * @return description of this trigger.
     */
    public String getDescription() {
        return this.description;
    }
    /**
     * Get meta data
     * @return meta data of this trigger.
     */
    public JSONObject getMetadata() {
        return this.metadata;
    }

    // Implementation of Parcelable
    protected Trigger(Parcel in) {
        this.triggerID = in.readString();
        this.targetID = in.readParcelable(TypedID.class.getClassLoader());
        this.predicate = in.readParcelable(Predicate.class.getClassLoader());
        this.command = in.readParcelable(Command.class.getClassLoader());
        this.serverCode = in.readParcelable(Command.class.getClassLoader());
        this.disabled = (in.readByte() != 0);
        this.disabledReason = in.readString();
        this.title = in.readString();
        this.description = in.readString();
        String metadata = in.readString();
        if (!TextUtils.isEmpty(metadata)) {
            try {
                this.metadata = new JSONObject(metadata);
            } catch (JSONException ignore) {
                // Won’t happen
            }
        }
    }

    public static final Creator<Trigger> CREATOR = new Creator<Trigger>() {
        @Override
        public Trigger createFromParcel(Parcel in) {
            return new Trigger(in);
        }

        @Override
        public Trigger[] newArray(int size) {
            return new Trigger[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.triggerID);
        dest.writeParcelable(this.targetID, flags);
        dest.writeParcelable(this.predicate, flags);
        dest.writeParcelable(this.command, flags);
        dest.writeParcelable(this.serverCode, flags);
        dest.writeByte((byte) (this.disabled ? 1 : 0));
        dest.writeString(this.disabledReason);
        dest.writeString(this.title);
        dest.writeString(this.description);
        dest.writeString(this.metadata == null ? null : this.metadata.toString());
    }
}