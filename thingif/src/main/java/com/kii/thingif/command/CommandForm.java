package com.kii.thingif.command;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONObject;

import java.util.List;

/**
 * Form of a command.
 *
 * This class contains data in order to create {@link Command} with {@link
 * com.kii.thingif.ThingIFAPI#postNewCommand(CommandForm)}.
 * <br><br>
 * Mandatory data are followings:
 * <ul>
 * <li>Name of a schema</li>
 * <li>Version of a schema</li>
 * <li>List of actions</li>
 * </ul>
 * Optional data are followings:
 * <ul>
 * <li>Title of a schema</li>
 * <li>Description of a schema</li>
 * <li>meta data of a schema</li>
 * </ul>
 */
public final class CommandForm {

    private final @NonNull String schemaName;
    private final int schemaVersion;
    private final @NonNull List<Action> actions;

    private @Nullable String title;
    private @Nullable String description;
    private @Nullable JSONObject metadata;

    /**
     * Constructs a CommandForm instance.
     *
     * @param schemaName name of schema. Must not be null or empty string.
     * @param schemaVersion version of schema.
     * @param actions List of actions. Must not be null or empty.
     * @throws IllegalArgumentException when schemaVersion is null or empty
     * string and/or actions is null or empty.
     */
    public CommandForm(
            @NonNull String schemaName,
            int schemaVersion,
            @NonNull List<Action> actions)
        throws IllegalArgumentException
    {
        // TODO: validate following fields if invalid, throw exception.
        this.schemaName = schemaName;
        this.schemaVersion = schemaVersion;
        this.actions = actions;
    }

    /**
     * Setter of title
     *
     * @param title Length of title must be equal or less than 50. title also
     * must not be empty string.
     * @return this instance
     * @throws IllegalArgumentException if title is invalid.
     */
    public CommandForm setTitle(
            @Nullable String title)
        throws IllegalArgumentException
    {
        // TODO: implement me.
        return null;
    }

    /**
     * Setter of description
     *
     * @param description
     * @return this instance.
     */
    public CommandForm setDescription(@Nullable String description) {
        // TODO: implement me.
        return null;
    }

    /**
     * Setter of meta data.
     *
     * @param metadata
     * @return this instance.
     */
    public CommandForm setMetadata(@Nullable JSONObject metadata) {
        // TODO: implement me.
        return null;
    }

}
