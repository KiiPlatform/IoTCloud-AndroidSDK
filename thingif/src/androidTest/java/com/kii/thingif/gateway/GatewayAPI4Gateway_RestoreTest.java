package com.kii.thingif.gateway;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.kii.thingif.KiiApp;
import com.kii.thingif.exception.BadRequestException;
import com.kii.thingif.exception.ConflictException;
import com.kii.thingif.exception.UnauthorizedException;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class GatewayAPI4Gateway_RestoreTest extends GatewayAPITestBase {

    @Test
    public void restoreTest() throws Exception {
        GatewayAPI4Gateway api = this.craeteGatewayAPI4GatewayWithLoggedIn();
        this.addEmptyMockResponse(204);
        api.restore();

        RecordedRequest request = this.server.takeRequest(1, TimeUnit.SECONDS);
        Assert.assertEquals("/gateway-app/gateway/restore", request.getPath());
        Assert.assertEquals("POST", request.getMethod());

        Map<String, String> expectedRequestHeaders = new HashMap<String, String>();
        expectedRequestHeaders.put("Authorization", "Bearer " + ACCESS_TOKEN);
        this.assertRequestHeader(expectedRequestHeaders, request);

        Assert.assertEquals(0, request.getBodySize());
    }
    @Test(expected = IllegalStateException.class)
    public void restoreNoLoggedInTest() throws Exception {
        KiiApp app = getApp(APP_ID, APP_KEY);
        GatewayAPI4Gateway api = new GatewayAPI4Gateway(InstrumentationRegistry.getTargetContext(), app);
        api.restore();
    }
    @Test
    public void restore400ErrorTest() throws Exception {
        GatewayAPI4Gateway api = this.craeteGatewayAPI4GatewayWithLoggedIn();
        this.addEmptyMockResponse(400);
        try {
            api.restore();
            Assert.fail("BadRequestException should be thrown");
        } catch (BadRequestException e) {
        }

        RecordedRequest request = this.server.takeRequest(1, TimeUnit.SECONDS);
        Assert.assertEquals("/gateway-app/gateway/restore", request.getPath());
        Assert.assertEquals("POST", request.getMethod());

        Map<String, String> expectedRequestHeaders = new HashMap<String, String>();
        expectedRequestHeaders.put("Authorization", "Bearer " + ACCESS_TOKEN);
        this.assertRequestHeader(expectedRequestHeaders, request);

        Assert.assertEquals(0, request.getBodySize());
    }
    @Test
    public void restore401ErrorTest() throws Exception {
        GatewayAPI4Gateway api = this.craeteGatewayAPI4GatewayWithLoggedIn();
        this.addEmptyMockResponse(401);
        try {
            api.restore();
            Assert.fail("UnauthorizedException should be thrown");
        } catch (UnauthorizedException e) {
        }

        RecordedRequest request = this.server.takeRequest(1, TimeUnit.SECONDS);
        Assert.assertEquals("/gateway-app/gateway/restore", request.getPath());
        Assert.assertEquals("POST", request.getMethod());

        Map<String, String> expectedRequestHeaders = new HashMap<String, String>();
        expectedRequestHeaders.put("Authorization", "Bearer " + ACCESS_TOKEN);
        this.assertRequestHeader(expectedRequestHeaders, request);

        Assert.assertEquals(0, request.getBodySize());
    }
    @Test
    public void restore409ErrorTest() throws Exception {
        GatewayAPI4Gateway api = this.craeteGatewayAPI4GatewayWithLoggedIn();
        this.addEmptyMockResponse(409);
        try {
            api.restore();
            Assert.fail("ConflictException should be thrown");
        } catch (ConflictException e) {
        }

        RecordedRequest request = this.server.takeRequest(1, TimeUnit.SECONDS);
        Assert.assertEquals("/gateway-app/gateway/restore", request.getPath());
        Assert.assertEquals("POST", request.getMethod());

        Map<String, String> expectedRequestHeaders = new HashMap<String, String>();
        expectedRequestHeaders.put("Authorization", "Bearer " + ACCESS_TOKEN);
        this.assertRequestHeader(expectedRequestHeaders, request);

        Assert.assertEquals(0, request.getBodySize());
    }
}
