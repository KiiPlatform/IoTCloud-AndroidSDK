package com.kii.iotcloud.testmodel;

import com.kii.iotcloud.command.ActionResult;

public class SetColorResult extends ActionResult {
    public String getActionName() {
        return "setColor";
    }

    public SetColorResult() {
    }

    public SetColorResult(boolean succeeded) {
        this.succeeded = succeeded;
    }
}