package com.kii.thingif.largetests;

import android.support.test.runner.AndroidJUnit4;
import android.text.TextUtils;
import android.util.Pair;

import com.google.gson.JsonObject;
import com.kii.cloud.rest.client.KiiRest;
import com.kii.cloud.rest.client.model.KiiCredentials;
import com.kii.cloud.rest.client.model.storage.KiiThing;
import com.kii.thingif.OnboardWithVendorThingIDOptions;
import com.kii.thingif.Target;
import com.kii.thingif.ThingIFAPI;
import com.kii.thingif.TypedID;
import com.kii.thingif.actions.AirConditionerActions;
import com.kii.thingif.actions.HumidityActions;
import com.kii.thingif.clause.trigger.EqualsClauseInTrigger;
import com.kii.thingif.command.Action;
import com.kii.thingif.command.AliasAction;
import com.kii.thingif.command.Command;
import com.kii.thingif.exception.BadRequestException;
import com.kii.thingif.states.AirConditionerState;
import com.kii.thingif.trigger.Condition;
import com.kii.thingif.trigger.EventSource;
import com.kii.thingif.trigger.ScheduleOncePredicate;
import com.kii.thingif.trigger.SchedulePredicate;
import com.kii.thingif.trigger.ServerCode;
import com.kii.thingif.trigger.StatePredicate;
import com.kii.thingif.trigger.Trigger;
import com.kii.thingif.trigger.TriggeredCommandForm;
import com.kii.thingif.trigger.TriggeredServerCodeResult;
import com.kii.thingif.trigger.TriggersWhen;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RunWith(AndroidJUnit4.class)
public class TriggerTest extends LargeTestCaseBase{
    @Test
    public void basicStatePredicateTriggerTest() throws Exception {
        ThingIFAPI api = this.createDefaultThingIFAPI();
        String vendorThingID = UUID.randomUUID().toString();
        String thingPassword = "password";

        // on-boarding thing
        OnboardWithVendorThingIDOptions options =
                new OnboardWithVendorThingIDOptions.Builder()
                        .setThingType(DEFAULT_THING_TYPE)
                        .setFirmwareVersion(DEFAULT_FIRMWARE_VERSION).build();
        Target target = api.onboardWithVendorThingID(vendorThingID, thingPassword, options);        Assert.assertEquals(TypedID.Types.THING, target.getTypedID().getType());
        Assert.assertNotNull(target.getAccessToken());

        // create new trigger: command and state predicate
        List<AliasAction<? extends Action>> aliasActions = new ArrayList<>();
        aliasActions.add(
                new AliasAction<>(
                        ALIAS1,
                        new AirConditionerActions(true, 25)));

        Condition condition1 = new Condition(new EqualsClauseInTrigger(ALIAS1, "power", true));
        StatePredicate predicate1 = new StatePredicate(condition1, TriggersWhen.CONDITION_TRUE);

        TriggeredCommandForm form = TriggeredCommandForm.Builder.newBuilder(aliasActions).build();

        Trigger trigger1 = api.postNewTrigger(form, predicate1, null);
        Assert.assertNotNull(trigger1.getTriggerID());
        Assert.assertFalse(trigger1.disabled());
        Assert.assertNull(trigger1.getDisabledReason());
        Assert.assertEquals(target.getTypedID(), trigger1.getTargetID());
        Assert.assertNull(trigger1.getServerCode());

        Command trigger1Command = trigger1.getCommand();
        Assert.assertNull(trigger1Command.getCommandID());
        Assert.assertEquals(target.getTypedID(), trigger1Command.getTargetID());
        Assert.assertEquals(api.getOwner().getTypedID(), trigger1Command.getIssuerID());
        Assert.assertNull(trigger1Command.getCommandState());
        Assert.assertNull(trigger1Command.getFiredByTriggerID());
        Assert.assertNull(trigger1Command.getCreated());
        Assert.assertNull(trigger1Command.getModified());

        Assert.assertEquals(1, trigger1Command.getAliasActions().size());

        Assert.assertEquals(ALIAS1, trigger1Command.getAliasActions().get(0).getAlias());
        Action action1 = trigger1Command.getAliasActions().get(0).getAction();
        Assert.assertTrue(action1 instanceof AirConditionerActions);
        Assert.assertTrue(((AirConditionerActions)action1).isPower());
        Assert.assertEquals(25, ((AirConditionerActions)action1).getPresetTemperature().intValue());

        StatePredicate trigger1Predicate = (StatePredicate)trigger1.getPredicate();
        Assert.assertEquals(EventSource.STATES, trigger1Predicate.getEventSource());
        Assert.assertEquals(TriggersWhen.CONDITION_TRUE, trigger1Predicate.getTriggersWhen());
        Assert.assertEquals("power", ((EqualsClauseInTrigger)trigger1Predicate.getCondition().getClause()).getField());
        Assert.assertEquals(Boolean.TRUE, ((EqualsClauseInTrigger)trigger1Predicate.getCondition().getClause()).getValue());

        // disable/enable trigger
        trigger1 = api.enableTrigger(trigger1.getTriggerID(), false);
        Assert.assertTrue(trigger1.disabled());
        trigger1 = api.enableTrigger(trigger1.getTriggerID(), true);
        Assert.assertFalse(trigger1.disabled());

        // get target state (empty)
//        AirConditionerState lightState = api.getTargetState(ALIAS1);

        // create new trigger: command, schedule predicate
//        List<AliasAction<? extends Action>> aliasActions2 = new ArrayList<>();
//        aliasActions2.add(
//                new AliasAction<Action>(
//                        ALIAS2,
//                        new HumidityActions(50)));
//        SchedulePredicate predicate2 = new SchedulePredicate("5 * * * *");
//        Trigger trigger2 = api.postNewTrigger(
//                TriggeredCommandForm.Builder.newBuilder(aliasActions2).build(),
//                predicate2,
//                null);
//        Assert.assertNotNull(trigger2.getTriggerID());
//        Assert.assertFalse(trigger2.disabled());
//        Assert.assertNull(trigger2.getDisabledReason());
//        Assert.assertEquals(target.getTypedID(), trigger2.getTargetID());
//        Assert.assertNull(trigger2.getServerCode());
//
//        Command trigger2Command = trigger2.getCommand();
//        Assert.assertNull(trigger2Command.getCommandID());
//        Assert.assertEquals(target.getTypedID(), trigger2Command.getTargetID());
//        Assert.assertEquals(api.getOwner().getTypedID(), trigger2Command.getIssuerID());
//        Assert.assertNull(trigger2Command.getCommandState());
//        Assert.assertNull(trigger2Command.getFiredByTriggerID());
//        Assert.assertNull(trigger2Command.getCreated());
//        Assert.assertNull(trigger2Command.getModified());
//        Assert.assertEquals(1, trigger2Command.getAliasActions().size());
//
//        Assert.assertEquals(ALIAS2, trigger2Command.getAliasActions().get(0).getAlias());
//        Action action2 = trigger2Command.getAliasActions().get(0).getAction();
//        Assert.assertTrue(action2 instanceof HumidityActions);
//        Assert.assertEquals(50, ((HumidityActions)action2).getPresetHumidity().intValue());
//        Assert.assertNull(trigger2Command.getAliasActionResults());
//        Assert.assertNull(trigger2Command.getAliasActionResults());
//
//        SchedulePredicate trigger2Predicate = (SchedulePredicate) trigger2.getPredicate();
//        Assert.assertEquals("5 * * * *", trigger2Predicate.getSchedule());
//
//        // disable/enable trigger
//        trigger2 = api.enableTrigger(trigger2.getTriggerID(), false);
//        Assert.assertTrue(trigger2.disabled());


//        // create new trigger: command, schedule once predicate
//        List<AliasAction<? extends Action>> aliasActions3 = new ArrayList<>();
//        aliasActions3.add(
//                new AliasAction<Action>(
//                        ALIAS2,
//                        new HumidityActions(50)));
//        ScheduleOncePredicate predicate3 = new ScheduleOncePredicate(System.currentTimeMillis()+ 1000*1000);
//
//
//        Trigger trigger3 = api.postNewTrigger(
//                TriggeredCommandForm.Builder.newBuilder(aliasActions3).build(),
//                predicate3,
//                null);
//        Assert.assertNotNull(trigger3.getTriggerID());
//        Assert.assertFalse(trigger3.disabled());
//        Assert.assertNull(trigger3.getDisabledReason());
//        Assert.assertEquals(target.getTypedID(), trigger3.getTargetID());
//        Assert.assertNull(trigger3.getServerCode());
//
//        Command trigger3Command = trigger3.getCommand();
//        Assert.assertNull(trigger3Command.getCommandID());
//        Assert.assertEquals(target.getTypedID(), trigger3Command.getTargetID());
//        Assert.assertEquals(api.getOwner().getTypedID(), trigger3Command.getIssuerID());
//        Assert.assertNull(trigger3Command.getCommandState());
//        Assert.assertNull(trigger3Command.getFiredByTriggerID());
//        Assert.assertNull(trigger3Command.getCreated());
//        Assert.assertNull(trigger3Command.getModified());
//        Assert.assertEquals(1, trigger3Command.getAliasActions().size());
//
//        Assert.assertEquals(ALIAS2, trigger3Command.getAliasActions().get(0).getAlias());
//        Action action3 = trigger3Command.getAliasActions().get(0).getAction();
//        Assert.assertTrue(action3 instanceof HumidityActions);
//        Assert.assertEquals(50, ((HumidityActions)action3).getPresetHumidity().intValue());
//        Assert.assertNull(trigger3Command.getAliasActionResults());
//        Assert.assertNull(trigger3Command.getAliasActionResults());
//
//        ScheduleOncePredicate trigger3Predicate = (ScheduleOncePredicate) trigger3.getPredicate();
//        Assert.assertEquals(predicate3.getScheduleAt(), trigger3Predicate.getScheduleAt());


        // create new trigger: server code, state predicate
        String endpoint1 = "my_function";
        String executorAccessToken1 = target.getAccessToken();
        String targetAppID1 = api.getAppID();
        JSONObject parameters1 = new JSONObject("{\"doAction\":true}");
        ServerCode serverCode1 = new ServerCode(endpoint1, executorAccessToken1, targetAppID1, parameters1);
        Condition condition4 = new Condition(new EqualsClauseInTrigger(ALIAS1, "power", true));
        StatePredicate predicate4 = new StatePredicate(condition4, TriggersWhen.CONDITION_TRUE);

        Trigger trigger4 = api.postNewTrigger(serverCode1, predicate4);
        Assert.assertNotNull(trigger4.getTriggerID());
        Assert.assertFalse(trigger4.disabled());
        Assert.assertNull(trigger4.getDisabledReason());
        Assert.assertNull(trigger4.getCommand());

        ServerCode trigger1ServerCode = trigger4.getServerCode();

        Assert.assertEquals(endpoint1, trigger1ServerCode.getEndpoint());
        Assert.assertEquals(executorAccessToken1, trigger1ServerCode.getExecutorAccessToken());
        Assert.assertEquals(targetAppID1, trigger1ServerCode.getTargetAppID());
        assertJSONObject(parameters1, trigger1ServerCode.getParameters());

        StatePredicate trigger4Predicate = (StatePredicate)trigger4.getPredicate();
        Assert.assertEquals(EventSource.STATES, trigger4Predicate.getEventSource());
        Assert.assertEquals(TriggersWhen.CONDITION_TRUE, trigger4Predicate.getTriggersWhen());
        Assert.assertEquals("power", ((EqualsClauseInTrigger)trigger4Predicate.getCondition().getClause()).getField());
        Assert.assertEquals(Boolean.TRUE, ((EqualsClauseInTrigger)trigger4Predicate.getCondition().getClause()).getValue());


//        // create new trigger: server code, schedule predicate
//        String endpoint2 = "my_function";
//        String executorAccessToken2 = target.getAccessToken();
//        String targetAppID2 = api.getAppID();
//        JSONObject parameters2 = new JSONObject("{\"doAction\":false}");
//        ServerCode serverCode2 = new ServerCode(endpoint2, executorAccessToken2, targetAppID2, parameters2);
//        SchedulePredicate predicate5 = new SchedulePredicate("4 * * * *");
//
//        Trigger trigger5 = api.postNewTrigger(serverCode2, predicate5);
//        Assert.assertNotNull(trigger5.getTriggerID());
//        Assert.assertFalse(trigger5.disabled());
//        Assert.assertNull(trigger5.getDisabledReason());
//        Assert.assertNull(trigger5.getCommand());
//
//        ServerCode trigger5ServerCode = trigger5.getServerCode();
//
//        Assert.assertEquals(endpoint2, trigger5ServerCode.getEndpoint());
//        Assert.assertEquals(executorAccessToken2, trigger5ServerCode.getExecutorAccessToken());
//        Assert.assertEquals(targetAppID2, trigger5ServerCode.getTargetAppID());
//        assertJSONObject(parameters2, trigger5ServerCode.getParameters());
//
//        SchedulePredicate trigger5Predicate = (SchedulePredicate)trigger5.getPredicate();
//        Assert.assertEquals("4 * * * *", trigger5Predicate.getSchedule());
//

        // create new trigger: server code, scheduleOnce predicate
//        String endpoint3 = "my_function";
//        String executorAccessToken3 = target.getAccessToken();
//        String targetAppID3 = api.getAppID();
//        JSONObject parameters3 = new JSONObject("{\"doAction3\":true}");
//        ServerCode serverCode3 = new ServerCode(endpoint3, executorAccessToken3, targetAppID3, parameters3);
//        ScheduleOncePredicate predicate6 = new ScheduleOncePredicate(System.currentTimeMillis()+2000*1000);
//
//        Trigger trigger6 = api.postNewTrigger(serverCode3, predicate6);
//        Assert.assertNotNull(trigger6.getTriggerID());
//        Assert.assertFalse(trigger6.disabled());
//        Assert.assertNull(trigger6.getDisabledReason());
//        Assert.assertNull(trigger6.getCommand());
//
//        ServerCode trigger6ServerCode = trigger6.getServerCode();
//
//        Assert.assertEquals(endpoint3, trigger6ServerCode.getEndpoint());
//        Assert.assertEquals(executorAccessToken3, trigger6ServerCode.getExecutorAccessToken());
//        Assert.assertEquals(targetAppID3, trigger6ServerCode.getTargetAppID());
//        assertJSONObject(parameters3, trigger6ServerCode.getParameters());
//
//        ScheduleOncePredicate trigger6Predicate = (ScheduleOncePredicate)trigger6.getPredicate();
//        Assert.assertEquals(predicate6.getScheduleAt(), trigger6Predicate.getScheduleAt());

        // list triggers for first 5
        Pair<List<Trigger>, String> results1 = api.listTriggers(5, null);
        Assert.assertNotNull(results1.second);
        List<Trigger> triggers = results1.first;
        Assert.assertEquals(5, triggers.size());

        // list triggers for rest 1
        Pair<List<Trigger>, String> results2 = api.listTriggers(5, results1.second);
        Assert.assertEquals(1, results2.first.size());

        // listing order is undefined
//        for (Trigger trigger : triggers) {
//            if (TextUtils.equals(trigger1.getTriggerID(), trigger.getTriggerID())) {
//                trigger1 = trigger;
//            } else if (TextUtils.equals(trigger2.getTriggerID(), trigger.getTriggerID())) {
//                trigger2 = trigger;
//            }
//        }
//        // assert trigger1
//        Assert.assertNotNull(trigger1.getTriggerID());
//        Assert.assertFalse(trigger1.disabled());
//        Assert.assertNull(trigger1.getDisabledReason());
//        Assert.assertEquals(target.getTypedID(), trigger1.getTargetID());
//
//        trigger1Command = trigger1.getCommand();
//        Assert.assertNull(trigger1Command.getCommandID());
//        Assert.assertEquals(DEMO_SCHEMA_NAME, trigger1Command.getSchemaName());
//        Assert.assertEquals(DEMO_SCHEMA_VERSION, trigger1Command.getSchemaVersion());
//        Assert.assertEquals(target.getTypedID(), trigger1Command.getTargetID());
//        Assert.assertEquals(api.getOwner().getTypedID(), trigger1Command.getIssuerID());
//        Assert.assertNull(trigger1Command.getCommandState());
//        Assert.assertNull(trigger1Command.getFiredByTriggerID());
//        Assert.assertNull(trigger1Command.getCreated());
//        Assert.assertNull(trigger1Command.getModified());
//        Assert.assertEquals(2, trigger1Command.getActions().size());
//        Assert.assertEquals(setColor.getActionName(), trigger1Command.getActions().get(0).getActionName());
//        Assert.assertArrayEquals(setColor.color, ((SetColor) trigger1Command.getActions().get(0)).color);
//        Assert.assertEquals(setColorTemperature.getActionName(), trigger1Command.getActions().get(1).getActionName());
//        Assert.assertEquals(setColorTemperature.colorTemperature, ((SetColorTemperature)trigger1Command.getActions().get(1)).colorTemperature);
//        Assert.assertNull(trigger1Command.getActionResults());
//
//        trigger1Predicate = (StatePredicate)trigger1.getPredicate();
//        Assert.assertEquals(EventSource.STATES, trigger1Predicate.getEventSource());
//        Assert.assertEquals(TriggersWhen.CONDITION_TRUE, trigger1Predicate.getTriggersWhen());
//        Assert.assertEquals("power", ((Equals)trigger1Predicate.getCondition().getClause()).getField());
//        Assert.assertEquals(Boolean.TRUE, ((Equals)trigger1Predicate.getCondition().getClause()).getValue());
//
//        // assert trigger2
//        Assert.assertNotNull(trigger2.getTriggerID());
//        Assert.assertFalse(trigger2.disabled());
//        Assert.assertNull(trigger2.getDisabledReason());
//        Assert.assertEquals(target.getTypedID(), trigger2.getTargetID());
//        Assert.assertNull(trigger2.getServerCode());
//
//        trigger2Command = trigger2.getCommand();
//        Assert.assertNull(trigger2Command.getCommandID());
//        Assert.assertEquals(DEMO_SCHEMA_NAME, trigger2Command.getSchemaName());
//        Assert.assertEquals(DEMO_SCHEMA_VERSION, trigger2Command.getSchemaVersion());
//        Assert.assertEquals(target.getTypedID(), trigger2Command.getTargetID());
//        Assert.assertEquals(api.getOwner().getTypedID(), trigger2Command.getIssuerID());
//        Assert.assertNull(trigger2Command.getCommandState());
//        Assert.assertNull(trigger2Command.getFiredByTriggerID());
//        Assert.assertNull(trigger2Command.getCreated());
//        Assert.assertNull(trigger2Command.getModified());
//        Assert.assertEquals(2, trigger2Command.getActions().size());
//        Assert.assertEquals(setBrightness.getActionName(), trigger2Command.getActions().get(0).getActionName());
//        Assert.assertEquals(setBrightness.brightness, ((SetBrightness) trigger2Command.getActions().get(0)).brightness);
//        Assert.assertEquals(turnPower.getActionName(), trigger2Command.getActions().get(1).getActionName());
//        Assert.assertEquals(turnPower.power, ((TurnPower)trigger2Command.getActions().get(1)).power);
//        Assert.assertNull(trigger2Command.getActionResults());
//
//        trigger2Predicate = (StatePredicate)trigger2.getPredicate();
//        Assert.assertEquals(EventSource.STATES, trigger2Predicate.getEventSource());
//        Assert.assertEquals(TriggersWhen.CONDITION_CHANGED, trigger2Predicate.getTriggersWhen());
//        Assert.assertEquals("power", ((Equals)trigger2Predicate.getCondition().getClause()).getField());
//        Assert.assertEquals(Boolean.FALSE, ((Equals)trigger2Predicate.getCondition().getClause()).getValue());

        // delete triiger
        api.deleteTrigger(trigger1.getTriggerID());

//        // update trigger
//        List<Action> actions3 = new ArrayList<Action>();
//        SetBrightness setBrightness3 = new SetBrightness(100);
//        TurnPower turnPower3 = new TurnPower(false);
//        actions3.add(setBrightness3);
//        actions3.add(turnPower3);
//        Condition condition3 = new Condition(Range.greaterThan("brightness", 100));
//        StatePredicate predicate3 = new StatePredicate(condition3, TriggersWhen.CONDITION_FALSE_TO_TRUE);
//        api.patchTrigger(trigger2.getTriggerID(), DEMO_SCHEMA_NAME, DEMO_SCHEMA_VERSION, actions3, predicate3);
//
//        // list triggers
//        results = api.listTriggers(100, null);
//        Assert.assertNull(results.second);
//        triggers = results.first;
//        Assert.assertEquals(1, triggers.size());
//        Trigger updatedTriger2 = triggers.get(0);

//        // assert updated trigger1
//        Assert.assertEquals(trigger2.getTriggerID(), updatedTriger2.getTriggerID());
//        Assert.assertFalse(updatedTriger2.disabled());
//        Assert.assertNull(updatedTriger2.getDisabledReason());
//        Assert.assertEquals(target.getTypedID(), updatedTriger2.getTargetID());
//        Assert.assertNull(trigger2.getServerCode());
//
//        Command updatedTrigger2Command = updatedTriger2.getCommand();
//        Assert.assertNull(updatedTrigger2Command.getCommandID());
//        Assert.assertEquals(DEMO_SCHEMA_NAME, updatedTrigger2Command.getSchemaName());
//        Assert.assertEquals(DEMO_SCHEMA_VERSION, updatedTrigger2Command.getSchemaVersion());
//        Assert.assertEquals(target.getTypedID(), updatedTrigger2Command.getTargetID());
//        Assert.assertEquals(api.getOwner().getTypedID(), updatedTrigger2Command.getIssuerID());
//        Assert.assertNull(updatedTrigger2Command.getCommandState());
//        Assert.assertNull(updatedTrigger2Command.getFiredByTriggerID());
//        Assert.assertNull(updatedTrigger2Command.getCreated());
//        Assert.assertNull(updatedTrigger2Command.getModified());
//        Assert.assertEquals(2, updatedTrigger2Command.getActions().size());
//        Assert.assertEquals(setBrightness3.getActionName(), updatedTrigger2Command.getActions().get(0).getActionName());
//        Assert.assertEquals(setBrightness3.brightness, ((SetBrightness) updatedTrigger2Command.getActions().get(0)).brightness);
//        Assert.assertEquals(turnPower3.getActionName(), updatedTrigger2Command.getActions().get(1).getActionName());
//        Assert.assertEquals(turnPower3.power, ((TurnPower)updatedTrigger2Command.getActions().get(1)).power);
//        Assert.assertNull(updatedTrigger2Command.getActionResults());
//
//        StatePredicate updatedTrigger2Predicate = (StatePredicate)updatedTriger2.getPredicate();
//        Assert.assertEquals(EventSource.STATES, updatedTrigger2Predicate.getEventSource());
//        Assert.assertEquals(TriggersWhen.CONDITION_FALSE_TO_TRUE, updatedTrigger2Predicate.getTriggersWhen());
//        Assert.assertEquals("brightness", ((Range)updatedTrigger2Predicate.getCondition().getClause()).getField());
//        Assert.assertEquals(100, (long)((Range)updatedTrigger2Predicate.getCondition().getClause()).getLowerLimit());
    }
    @Test
    public void basicServerCodeTriggerTest() throws Exception {
        ThingIFAPI api = this.createDefaultThingIFAPI();
        String vendorThingID = UUID.randomUUID().toString();
        String thingPassword = "password";

        // on-boarding thing
        OnboardWithVendorThingIDOptions options =
                new OnboardWithVendorThingIDOptions.Builder()
                        .setThingType(DEFAULT_THING_TYPE)
                        .setFirmwareVersion(DEFAULT_FIRMWARE_VERSION).build();
        Target target = api.onboardWithVendorThingID(vendorThingID, thingPassword, options);
        Assert.assertEquals(TypedID.Types.THING, target.getTypedID().getType());
        Assert.assertNotNull(target.getAccessToken());

        // create new trigger
        String endpoint1 = "my_function";
        String executorAccessToken1 = target.getAccessToken();
        String targetAppID1 = api.getAppID();
        JSONObject parameters1 = new JSONObject("{\"doAction\":true}");
        ServerCode serverCode1 = new ServerCode(endpoint1, executorAccessToken1, targetAppID1, parameters1);
        Condition condition1 = new Condition(new EqualsClauseInTrigger(ALIAS1, "power", true));
        StatePredicate predicate1 = new StatePredicate(condition1, TriggersWhen.CONDITION_TRUE);

        Trigger trigger1 = api.postNewTrigger(serverCode1, predicate1);
        Assert.assertNotNull(trigger1.getTriggerID());
        Assert.assertFalse(trigger1.disabled());
        Assert.assertNull(trigger1.getDisabledReason());
        Assert.assertNull(trigger1.getCommand());

        ServerCode trigger1ServerCode = trigger1.getServerCode();

        Assert.assertEquals(endpoint1, trigger1ServerCode.getEndpoint());
        Assert.assertEquals(executorAccessToken1, trigger1ServerCode.getExecutorAccessToken());
        Assert.assertEquals(targetAppID1, trigger1ServerCode.getTargetAppID());
        assertJSONObject(parameters1, trigger1ServerCode.getParameters());

        StatePredicate trigger1Predicate = (StatePredicate)trigger1.getPredicate();
        Assert.assertEquals(EventSource.STATES, trigger1Predicate.getEventSource());
        Assert.assertEquals(TriggersWhen.CONDITION_TRUE, trigger1Predicate.getTriggersWhen());
        Assert.assertEquals("power", ((EqualsClauseInTrigger)trigger1Predicate.getCondition().getClause()).getField());
        Assert.assertEquals(Boolean.TRUE, ((EqualsClauseInTrigger)trigger1Predicate.getCondition().getClause()).getValue());

        // create new trigger
        List<AliasAction<? extends Action>> aliasActions2 = new ArrayList<>();
        aliasActions2.add(
                new AliasAction<Action>(
                        ALIAS2,
                        new HumidityActions(50)));
        Condition condition2 = new Condition(new EqualsClauseInTrigger(ALIAS1, "power", false));
        StatePredicate predicate2 = new StatePredicate(condition2, TriggersWhen.CONDITION_CHANGED);


        Trigger trigger2 = api.postNewTrigger(
                TriggeredCommandForm.Builder.newBuilder(aliasActions2).build(),
                predicate2,
                null);
        Assert.assertNotNull(trigger2.getTriggerID());
        Assert.assertFalse(trigger2.disabled());
        Assert.assertNull(trigger2.getDisabledReason());
        Assert.assertEquals(target.getTypedID(), trigger2.getTargetID());
        Assert.assertNull(trigger2.getServerCode());

        Command trigger2Command = trigger2.getCommand();
        Assert.assertNull(trigger2Command.getCommandID());
        Assert.assertEquals(target.getTypedID(), trigger2Command.getTargetID());
        Assert.assertEquals(api.getOwner().getTypedID(), trigger2Command.getIssuerID());
        Assert.assertNull(trigger2Command.getCommandState());
        Assert.assertNull(trigger2Command.getFiredByTriggerID());
        Assert.assertNull(trigger2Command.getCreated());
        Assert.assertNull(trigger2Command.getModified());
        Assert.assertEquals(1, trigger2Command.getAliasActions().size());

        Assert.assertEquals(ALIAS2, trigger2Command.getAliasActions().get(0).getAlias());
        Action action2 = trigger2Command.getAliasActions().get(0).getAction();
        Assert.assertTrue(action2 instanceof HumidityActions);
        Assert.assertEquals(50, ((HumidityActions)action2).getPresetHumidity().intValue());
        Assert.assertNull(trigger2Command.getAliasActionResults());
        Assert.assertNull(trigger2Command.getAliasActionResults());

        StatePredicate trigger2Predicate = (StatePredicate)trigger2.getPredicate();
        Assert.assertEquals(EventSource.STATES, trigger2Predicate.getEventSource());
        Assert.assertEquals(TriggersWhen.CONDITION_CHANGED, trigger2Predicate.getTriggersWhen());
        Assert.assertEquals("power", ((EqualsClauseInTrigger)trigger2Predicate.getCondition().getClause()).getField());
        Assert.assertEquals(Boolean.FALSE, ((EqualsClauseInTrigger)trigger2Predicate.getCondition().getClause()).getValue());


        // list triggers
        Pair<List<Trigger>, String> results = api.listTriggers(100, null);
        Assert.assertNull(results.second);
        List<Trigger> triggers = results.first;
        Assert.assertEquals(2, triggers.size());

        // listing order is undefined
        for (Trigger trigger : triggers) {
            if (TextUtils.equals(trigger1.getTriggerID(), trigger.getTriggerID())) {
                trigger1 = trigger;
            } else if (TextUtils.equals(trigger2.getTriggerID(), trigger.getTriggerID())) {
                trigger2 = trigger;
            }
        }
        // assert trigger1
        Assert.assertNotNull(trigger1.getTriggerID());
        Assert.assertFalse(trigger1.disabled());
        Assert.assertNull(trigger1.getDisabledReason());
        Assert.assertNull(trigger1.getCommand());

        trigger1ServerCode = trigger1.getServerCode();

        Assert.assertEquals(endpoint1, trigger1ServerCode.getEndpoint());
        Assert.assertEquals(executorAccessToken1, trigger1ServerCode.getExecutorAccessToken());
        Assert.assertEquals(targetAppID1, trigger1ServerCode.getTargetAppID());
        assertJSONObject(parameters1, trigger1ServerCode.getParameters());

        trigger1Predicate = (StatePredicate)trigger1.getPredicate();
        Assert.assertEquals(EventSource.STATES, trigger1Predicate.getEventSource());
        Assert.assertEquals(TriggersWhen.CONDITION_TRUE, trigger1Predicate.getTriggersWhen());
        Assert.assertEquals("power", ((EqualsClauseInTrigger)trigger1Predicate.getCondition().getClause()).getField());
        Assert.assertEquals(Boolean.TRUE, ((EqualsClauseInTrigger) trigger1Predicate.getCondition().getClause()).getValue());

        // assert trigger2
//        Assert.assertNotNull(trigger2.getTriggerID());
//        Assert.assertFalse(trigger2.disabled());
//        Assert.assertNull(trigger2.getDisabledReason());
//        Assert.assertEquals(target.getTypedID(), trigger2.getTargetID());
//
//        trigger2Command = trigger2.getCommand();
//        Assert.assertNull(trigger2Command.getCommandID());
//        Assert.assertEquals(DEMO_SCHEMA_NAME, trigger2Command.getSchemaName());
//        Assert.assertEquals(DEMO_SCHEMA_VERSION, trigger2Command.getSchemaVersion());
//        Assert.assertEquals(target.getTypedID(), trigger2Command.getTargetID());
//        Assert.assertEquals(api.getOwner().getTypedID(), trigger2Command.getIssuerID());
//        Assert.assertNull(trigger2Command.getCommandState());
//        Assert.assertNull(trigger2Command.getFiredByTriggerID());
//        Assert.assertNull(trigger2Command.getCreated());
//        Assert.assertNull(trigger2Command.getModified());
//        Assert.assertEquals(2, trigger2Command.getActions().size());
//        Assert.assertEquals(setBrightness.getActionName(), trigger2Command.getActions().get(0).getActionName());
//        Assert.assertEquals(setBrightness.brightness, ((SetBrightness) trigger2Command.getActions().get(0)).brightness);
//        Assert.assertEquals(turnPower.getActionName(), trigger2Command.getActions().get(1).getActionName());
//        Assert.assertEquals(turnPower.power, ((TurnPower) trigger2Command.getActions().get(1)).power);
//        Assert.assertNull(trigger2Command.getActionResults());
//
//        trigger2Predicate = (StatePredicate)trigger2.getPredicate();
//        Assert.assertEquals(EventSource.STATES, trigger2Predicate.getEventSource());
//        Assert.assertEquals(TriggersWhen.CONDITION_CHANGED, trigger2Predicate.getTriggersWhen());
//        Assert.assertEquals("power", ((Equals)trigger2Predicate.getCondition().getClause()).getField());
//        Assert.assertEquals(Boolean.FALSE, ((Equals) trigger2Predicate.getCondition().getClause()).getValue());
//
//        // delete triiger
//        api.deleteTrigger(trigger2.getTriggerID());
//
//        // update trigger
//        String endpoint2 = "my_function2";
//        String executorAccessToken2 = target.getAccessToken() + "2";
//        String targetAppID2 = api.getAppID() + "2";
//        JSONObject parameters2 = new JSONObject("{\"doAction\":false}");
//        ServerCode serverCode2 = new ServerCode(endpoint2, executorAccessToken2, targetAppID2, parameters2);
//        Condition condition3 = new Condition(Range.greaterThan("brightness", 100));
//        StatePredicate predicate3 = new StatePredicate(condition3, TriggersWhen.CONDITION_FALSE_TO_TRUE);
//        api.patchTrigger(trigger1.getTriggerID(), serverCode2, predicate3);
//
//        // list triggers
//        results = api.listTriggers(100, null);
//        Assert.assertNull(results.second);
//        triggers = results.first;
//        Assert.assertEquals(1, triggers.size());
//        Trigger updatedTriger1 = triggers.get(0);
//
//        // assert updated trigger1
//        Assert.assertEquals(trigger1.getTriggerID(), updatedTriger1.getTriggerID());
//        Assert.assertFalse(updatedTriger1.disabled());
//        Assert.assertNull(updatedTriger1.getDisabledReason());
//        Assert.assertNull(updatedTriger1.getTargetID());
//        Assert.assertNull(updatedTriger1.getCommand());
//
//        ServerCode updatedTrigger1ServerCode = updatedTriger1.getServerCode();
//
//        Assert.assertEquals(endpoint2, updatedTrigger1ServerCode.getEndpoint());
//        Assert.assertEquals(executorAccessToken2, updatedTrigger1ServerCode.getExecutorAccessToken());
//        Assert.assertEquals(targetAppID2, updatedTrigger1ServerCode.getTargetAppID());
//        assertJSONObject(parameters2, updatedTrigger1ServerCode.getParameters());
//
//        StatePredicate updatedTrigger2Predicate = (StatePredicate)updatedTriger1.getPredicate();
//        Assert.assertEquals(EventSource.STATES, updatedTrigger2Predicate.getEventSource());
//        Assert.assertEquals(TriggersWhen.CONDITION_FALSE_TO_TRUE, updatedTrigger2Predicate.getTriggersWhen());
//        Assert.assertEquals("brightness", ((Range)updatedTrigger2Predicate.getCondition().getClause()).getField());
//        Assert.assertEquals(100, (long)((Range)updatedTrigger2Predicate.getCondition().getClause()).getLowerLimit());
    }
    @Test
    public void listTriggersEmptyResultTest() throws Exception {
        ThingIFAPI api = this.createDefaultThingIFAPI();
        String vendorThingID = UUID.randomUUID().toString();
        String thingPassword = "password";

        // on-boarding thing
        OnboardWithVendorThingIDOptions options =
                new OnboardWithVendorThingIDOptions.Builder()
                        .setThingType(DEFAULT_THING_TYPE)
                        .setFirmwareVersion(DEFAULT_FIRMWARE_VERSION).build();
        Target target = api.onboardWithVendorThingID(vendorThingID, thingPassword, options);

        Assert.assertEquals(TypedID.Types.THING, target.getTypedID().getType());
        Assert.assertNotNull(target.getAccessToken());

        Pair<List<Trigger>, String> results = api.listTriggers(100, null);
        Assert.assertNull(results.second);
        List<Trigger> triggers = results.first;
        Assert.assertEquals(0, triggers.size());
    }
    @Test
    public void listTriggerServerCodeResultsTest() throws Exception {
        if (!this.server.hasAdminCredential()) {
            return;
        }
        // Deploy server code
        KiiRest rest = new KiiRest(this.server.getAppID(), this.server.getAppKey(), this.server.getBaseUrl() + "/api", this.server.getBaseUrl() + "/thing-if", this.server.getBaseUrl() + ":443/logs");
        KiiCredentials admin = rest.api().oauth().getAdminAccessToken(this.server.getClientId(), this.server.getClientSecret());
        rest.setCredentials(admin);

        StringBuilder javascript = new StringBuilder();
        javascript.append("function server_code_for_trigger(params, context){" + "\n");
        javascript.append("    return 100;" + "\n");
        javascript.append("}" + "\n");
        String versionID = rest.api().servercode().deploy(javascript.toString());
        rest.api().servercode().setCurrentVersion(versionID);

        // initialize ThingIFAPI
        ThingIFAPI api = this.createDefaultThingIFAPI();
        String vendorThingID = UUID.randomUUID().toString();
        String thingPassword = "password";

        // on-boarding thing
        OnboardWithVendorThingIDOptions options =
                new OnboardWithVendorThingIDOptions.Builder()
                        .setThingType(DEFAULT_THING_TYPE)
                        .setFirmwareVersion(DEFAULT_FIRMWARE_VERSION).build();
        Target target = api.onboardWithVendorThingID(vendorThingID, thingPassword, options);
        Assert.assertEquals(TypedID.Types.THING, target.getTypedID().getType());
        Assert.assertNotNull(target.getAccessToken());

        // create new server code trigger
        String endpoint = "server_code_for_trigger";
        String executorAccessToken = target.getAccessToken();
        String targetAppID = api.getAppID();
        JSONObject parameters = new JSONObject("{\"arg1\":\"passed_parameter\"}");
        ServerCode serverCode = new ServerCode(endpoint, executorAccessToken, targetAppID, parameters);
        Condition condition = new Condition(new EqualsClauseInTrigger(ALIAS1, "power", true));
        StatePredicate predicate = new StatePredicate(condition, TriggersWhen.CONDITION_TRUE);

        Trigger trigger = api.postNewTrigger(serverCode, predicate);
        Assert.assertNotNull(trigger.getTriggerID());
        Assert.assertFalse(trigger.disabled());
        Assert.assertNull(trigger.getDisabledReason());
        Assert.assertNull(trigger.getTargetID());
        Assert.assertNull(trigger.getCommand());

        Thread.sleep(3000);

        rest.setCredentials(new KiiCredentials(target.getAccessToken()));
        // update thing state in order to trigger the server code
        KiiThing targetThing = new KiiThing();
        targetThing.setThingID(target.getTypedID().getID());
        JsonObject thingState = new JsonObject();
        thingState.addProperty("power", false);
        rest.thingif().targets(targetThing).states().save(thingState);

        Thread.sleep(3000);

        thingState = new JsonObject();
        thingState.addProperty("power", true);
        rest.thingif().targets(targetThing).states().save(thingState);

        Thread.sleep(3000);

        Pair<List<TriggeredServerCodeResult>, String> triggerServerCodeResults = api.listTriggeredServerCodeResults(trigger.getTriggerID(), 0, null);
        Assert.assertEquals(1, triggerServerCodeResults.first.size());
        Assert.assertNull(triggerServerCodeResults.second);
        TriggeredServerCodeResult triggeredServerCodeResult = triggerServerCodeResults.first.get(0);
        Assert.assertTrue(triggeredServerCodeResult.isSucceeded());
        Assert.assertEquals(100, (int) triggeredServerCodeResult.getReturnedValueAsInteger());
        Assert.assertTrue(triggeredServerCodeResult.getExecutedAt() > 0);
        Assert.assertEquals(endpoint, triggeredServerCodeResult.getEndpoint());
        Assert.assertNull(triggeredServerCodeResult.getError());
    }
    @Test
    public void listTriggerServerCodeResultsWithErrorTest() throws Exception {
        if (!this.server.hasAdminCredential()) {
            return;
        }
        // Deploy server code
        KiiRest rest = new KiiRest(this.server.getAppID(), this.server.getAppKey(), this.server.getBaseUrl() + "/api", this.server.getBaseUrl() + "/thing-if", this.server.getBaseUrl() + ":443/logs");
        KiiCredentials admin = rest.api().oauth().getAdminAccessToken(this.server.getClientId(), this.server.getClientSecret());
        rest.setCredentials(admin);

        StringBuilder javascript = new StringBuilder();
        javascript.append("function server_code_for_trigger(params, context){" + "\n");
        javascript.append("    reference.error = 100;" + "\n");
        javascript.append("    return 100;" + "\n");
        javascript.append("}" + "\n");
        String versionID = rest.api().servercode().deploy(javascript.toString());
        rest.api().servercode().setCurrentVersion(versionID);

        // initialize ThingIFAPI
        ThingIFAPI api = this.createDefaultThingIFAPI();
        String vendorThingID = UUID.randomUUID().toString();
        String thingPassword = "password";

        // on-boarding thing
        OnboardWithVendorThingIDOptions options =
                new OnboardWithVendorThingIDOptions.Builder()
                        .setThingType(DEFAULT_THING_TYPE)
                        .setFirmwareVersion(DEFAULT_FIRMWARE_VERSION).build();
        Target target = api.onboardWithVendorThingID(vendorThingID, thingPassword, options);

        Assert.assertEquals(TypedID.Types.THING, target.getTypedID().getType());
        Assert.assertNotNull(target.getAccessToken());

        // create new server code trigger
        String endpoint = "server_code_for_trigger";
        String executorAccessToken = target.getAccessToken();
        String targetAppID = api.getAppID();
        JSONObject parameters = new JSONObject("{\"arg1\":\"passed_parameter\"}");
        ServerCode serverCode = new ServerCode(endpoint, executorAccessToken, targetAppID, parameters);
        Condition condition = new Condition(new EqualsClauseInTrigger(ALIAS1, "power", true));
        StatePredicate predicate = new StatePredicate(condition, TriggersWhen.CONDITION_TRUE);

        Trigger trigger = api.postNewTrigger(serverCode, predicate);
        Assert.assertNotNull(trigger.getTriggerID());
        Assert.assertFalse(trigger.disabled());
        Assert.assertNull(trigger.getDisabledReason());
        Assert.assertNull(trigger.getTargetID());
        Assert.assertNull(trigger.getCommand());

        Thread.sleep(3000);

        rest.setCredentials(new KiiCredentials(target.getAccessToken()));
        // update thing state in order to trigger the server code
        KiiThing targetThing = new KiiThing();
        targetThing.setThingID(target.getTypedID().getID());
        JsonObject thingState = new JsonObject();
        thingState.addProperty("power", false);
        rest.thingif().targets(targetThing).states().save(thingState);

        Thread.sleep(3000);

        thingState = new JsonObject();
        thingState.addProperty("power", true);
        rest.thingif().targets(targetThing).states().save(thingState);

        Thread.sleep(3000);

        Pair<List<TriggeredServerCodeResult>, String> triggerServerCodeResults = api.listTriggeredServerCodeResults(trigger.getTriggerID(), 0, null);
        Assert.assertEquals(1, triggerServerCodeResults.first.size());
        Assert.assertNull(triggerServerCodeResults.second);
        TriggeredServerCodeResult triggeredServerCodeResult = triggerServerCodeResults.first.get(0);
        Assert.assertFalse(triggeredServerCodeResult.isSucceeded());
        Assert.assertNull(triggeredServerCodeResult.getReturnedValue());
        Assert.assertTrue(triggeredServerCodeResult.getExecutedAt() > 0);
        Assert.assertEquals(endpoint, triggeredServerCodeResult.getEndpoint());
        Assert.assertNotNull(triggeredServerCodeResult.getError());
        Assert.assertEquals("Error found while executing the developer-defined code", triggeredServerCodeResult.getError().getErrorMessage());
        Assert.assertEquals("RUNTIME_ERROR", triggeredServerCodeResult.getError().getErrorCode());
        Assert.assertEquals("reference is not defined", triggeredServerCodeResult.getError().getDetailMessage());
    }

    @Test(expected = BadRequestException.class)
    public void basicInvalidSchedulePredicateTriggerTest() throws Exception {
        ThingIFAPI api = this.createDefaultThingIFAPI();
        String vendorThingID = UUID.randomUUID().toString();
        String thingPassword = "password";

        // on-boarding thing
        OnboardWithVendorThingIDOptions options =
                new OnboardWithVendorThingIDOptions.Builder()
                        .setThingType(DEFAULT_THING_TYPE)
                        .setFirmwareVersion(DEFAULT_FIRMWARE_VERSION).build();
        Target target = api.onboardWithVendorThingID(vendorThingID, thingPassword, options);        Assert.assertEquals(TypedID.Types.THING, target.getTypedID().getType());
        Assert.assertEquals(TypedID.Types.THING, target.getTypedID().getType());
        Assert.assertNotNull(target.getAccessToken());

        // create new trigger
        List<AliasAction<? extends Action>> aliasActions = new ArrayList<>();
        aliasActions.add(
                new AliasAction<>(
                        ALIAS1,
                        new AirConditionerActions(true, 25)));

        TriggeredCommandForm form = TriggeredCommandForm.Builder.newBuilder(aliasActions).build();
        SchedulePredicate predicate1 = new SchedulePredicate("wrong format");
        Trigger trigger1 = api.postNewTrigger(form, predicate1, null);

    }
}
