package com.github.switcherapi.fixture;

import com.github.switcherapi.client.model.Switcher;
import com.github.switcherapi.client.model.criteria.Criteria;
import com.github.switcherapi.client.model.criteria.Snapshot;
import com.github.switcherapi.client.model.criteria.SwitchersCheck;
import com.github.switcherapi.client.remote.ClientWSImpl;
import com.github.switcherapi.client.utils.SnapshotLoader;
import com.github.switcherapi.client.utils.SwitcherUtils;
import com.google.gson.Gson;
import mockwebserver3.MockResponse;
import mockwebserver3.MockWebServer;
import mockwebserver3.QueueDispatcher;

import java.io.IOException;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class MockWebServerHelper {

    protected static MockWebServer mockBackEnd;

    protected static void setupMockServer() throws IOException {
        mockBackEnd = new MockWebServer();
        mockBackEnd.start();
        ((QueueDispatcher) mockBackEnd.getDispatcher()).setFailFast(true);
    }

    protected static void tearDownMockServer() throws IOException {
        mockBackEnd.shutdown();
    }

    protected void givenResponse(MockResponse response) {
        ((QueueDispatcher) mockBackEnd.getDispatcher()).enqueueResponse(response);
    }

    protected MockResponse generateTimeOut(int timeoutMs) {
        MockResponse.Builder builder = new MockResponse.Builder();
        builder.code(200);
        builder.headersDelay(timeoutMs, TimeUnit.MILLISECONDS);
        return builder.build();
    }

    /**
     * @see ClientWSImpl#auth()
     *
     * @param secondsAhead which the token will expire
     * @return Generated mock /auth response
     */
    protected MockResponse generateMockAuth(int secondsAhead) {
        MockResponse.Builder builder = new MockResponse.Builder();
        builder.body(String.format("{ \"token\": \"%s\", \"exp\": \"%s\" }",
                "mocked_token", SwitcherUtils.addTimeDuration(secondsAhead + "s", new Date()).getTime()/1000));
        builder.addHeader("Content-Type", "application/json");
        return builder.build();
    }

    /**
     * @see ClientWSImpl#resolveSnapshot(String)
     *
     * @return Generated mock /graphql response based on src/test/resources/default.json
     */
    protected MockResponse generateSnapshotResponse(String resourcesPath) {
        final Snapshot mockedSnapshot = new Snapshot();
        final Criteria criteria = new Criteria();
        criteria.setDomain(SnapshotLoader.loadSnapshot(resourcesPath + "/default.json"));
        mockedSnapshot.setData(criteria);

        Gson gson = new Gson();
        MockResponse.Builder builder = new MockResponse.Builder();
        builder.body(gson.toJson(mockedSnapshot));
        builder.addHeader("Content-Type", "application/json");
        return builder.build();
    }

    /**
     * @see ClientWSImpl#checkSnapshotVersion(long, String)
     *
     * @param status is true when snapshot version is updated
     * @return Generated mock /criteria/snapshot_check response
     */
    protected MockResponse generateCheckSnapshotVersionResponse(String status) {
        MockResponse.Builder builder = new MockResponse.Builder();
        builder.body(String.format("{ \"status\": \"%s\" }", status));
        builder.addHeader("Content-Type", "application/json");
        return builder.build();
    }

    /**
     * @see ClientWSImpl#isAlive()
     *
     * @param code HTTP status
     * @return Generated mock /check response
     */
    protected MockResponse generateStatusResponse(String code) {
        MockResponse.Builder builder = new MockResponse.Builder();
        builder.status(code);
        return builder.build();
    }

    /**
     * @see ClientWSImpl#executeCriteriaService(Switcher, String)
     *
     * @param result returned by the criteria execution
     * @param reason if you want to display along with the result
     * @return Generated mock /criteria response
     */
    protected MockResponse generateCriteriaResponse(String result, boolean reason) {
        if (reason) {
            return generateCriteriaResponse(result, "Success");
        }

        return generateCriteriaResponse(result);
    }

    /**
     * @see ClientWSImpl#executeCriteriaService(Switcher, String)
     *
     * @param result returned by the criteria execution
     * @return Generated mock /criteria response
     */
    protected MockResponse generateCriteriaResponse(String result) {
        return generateCriteriaResponse(result, null);
    }

    /**
     * @see ClientWSImpl#executeCriteriaService(Switcher, String)
     *
     * @param result returned by the criteria execution
     * @param reason returned by the criteria execution
     * @return Generated mock /criteria response
     */
    protected MockResponse generateCriteriaResponse(String result, String reason) {
        String response;
        if (reason == null) {
            response = String.format("{ \"result\": \"%s\" }", result);
        } else {
            response = String.format("{ \"result\": \"%s\", \"reason\": \"%s\" }", result, reason);
        }

        MockResponse.Builder builder = new MockResponse.Builder();
        builder.body(String.format(response, result));
        builder.addHeader("Content-Type", "application/json");
        return builder.build();
    }

    /**
     * @see ClientWSImpl#executeCriteriaService(Switcher, String)
     *
     * @param metadata returned by the criteria execution
     * @return Generated mock /criteria response
     */
    protected <T> MockResponse generateCriteriaResponse(T metadata) {
        String jsonMetadata = new Gson().toJson(metadata);
        String response = String.format("{ \"result\": \"%s\", \"reason\": \"%s\", \"metadata\": %s }",
                true, "Success", jsonMetadata);

        MockResponse.Builder builder = new MockResponse.Builder();
        builder.body(String.format(response, true));
        builder.addHeader("Content-Type", "application/json");
        return builder.build();
    }

    /**
     * @see ClientWSImpl#checkSwitchers(Set, String)
     *
     * @param switchersNotFound Switcher Keys forced to be not found
     * @return Generated mock /criteria/check_switchers
     */
    protected MockResponse generateCheckSwitchersResponse(Set<String> switchersNotFound) {
        SwitchersCheck switchersCheckNotFound = new SwitchersCheck();
        switchersCheckNotFound.setNotFound(
                switchersNotFound.toArray(new String[0]));

        Gson gson = new Gson();
        MockResponse.Builder builder = new MockResponse.Builder();
        builder.body(gson.toJson(switchersCheckNotFound));
        builder.addHeader("Content-Type", "application/json");
        return builder.build();
    }

}
