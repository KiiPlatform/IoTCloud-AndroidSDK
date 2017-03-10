package com.kii.thingif.query;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.kii.thingif.TargetState;
import com.kii.thingif.internal.gson.HistoryStateAdapter;
import com.kii.thingif.internal.gson.TimeRangeAdapter;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class AggregatedResultAdapter<T extends Number, S extends TargetState>
        implements JsonDeserializer<AggregatedResult<T, S>> {
    private Class<S> stateClass;
    private Class<T> fieldClass;

    public AggregatedResultAdapter(Class<S> stateClass, Class<T> fieldClass) {
        this.stateClass = stateClass;
        this.fieldClass = fieldClass;
    }

    @Override
    public AggregatedResult<T, S> deserialize(
            JsonElement jsonElement,
            Type typeOfT,
            JsonDeserializationContext context)
        throws JsonParseException
    {
        if (jsonElement == null) return null;

        JsonObject json = jsonElement.getAsJsonObject();

        if (!json.has("range") || !json.has("aggregations")) {
            throw new JsonParseException(
                    "json doesn't contain both of 2 necessary fields: range and aggregations.");
        }

        Gson gson = new GsonBuilder()
                .registerTypeAdapter(TimeRange.class, new TimeRangeAdapter())
                .registerTypeAdapter(HistoryState.class, new HistoryStateAdapter(this.stateClass))
                .create();

        TimeRange range = gson.fromJson(json.getAsJsonObject("range"), TimeRange.class);
        JsonArray aggregations = json.getAsJsonArray("aggregations");
        if (aggregations.size() != 1) {
            throw new JsonParseException(
                    "aggregations doesn't contain one object.");
        }
        JsonObject aggregation = aggregations.get(0).getAsJsonObject();
        T value = null;
        List<HistoryState<S>> aggregateObjects = null;
        if (aggregation.has("value")) {
            value = gson.fromJson(aggregation.getAsJsonPrimitive("value"), this.fieldClass);
        }
        if (aggregation.has("objects")) {
            aggregateObjects = new ArrayList<>();
            JsonArray objects = aggregation.getAsJsonArray("objects");
            Type historyStateType = new TypeToken<HistoryState<S>>(){}.getType();
            for (int i = 0; i < objects.size(); ++i) {
                HistoryState<S> historyState = gson.fromJson(objects.get(i), historyStateType);
                aggregateObjects.add(historyState);
            }
        }
        return new AggregatedResult<T, S>(range, value, aggregateObjects);
    }
}