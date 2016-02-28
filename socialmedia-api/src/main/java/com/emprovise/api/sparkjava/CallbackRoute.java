package com.emprovise.api.sparkjava;

import spark.Request;
import spark.Response;
import spark.Route;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static spark.Spark.*;

public class CallbackRoute implements Route {

    private static final int MAX_REQUESTS = 1;
    private Map<String, Optional<String>> paramMap;
    private String callbackURL;
    private int noOfRequests = 0;

    public CallbackRoute(String callbackURL) {
        this.callbackURL = callbackURL;
    }

    public CallbackRoute(String callbackURL, String... params) {
        this.callbackURL = callbackURL;
        setRequestParams(params);
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {

        noOfRequests++;

        if(paramMap!=null && !paramMap.isEmpty()){
            this.paramMap.forEach((k, v) -> {
                String value = request.queryParams(k);

                if(value != null){
                    paramMap.put(k, Optional.of(value));
                    System.out.println(value);
                }
            });
        }

        if(noOfRequests >= MAX_REQUESTS) {
            Runnable stopTask = () -> stop();
            new Thread(stopTask).start();

            synchronized(this) {
                this.notify();
            }
        }

        return null;
    }

    public void setRequestParams(String... params) {
        this.paramMap = Arrays.asList(params)
                        .stream()
                        .collect(Collectors.toMap(e -> e, e -> Optional.empty()));
    }

    public String getRequestParameter(String param) {
        if(this.paramMap != null && paramMap.get(param).isPresent()) {
            return paramMap.get(param).get();
        }

        return null;
    }

    public void startGetRoute(String callbackRoute) {
        get(callbackRoute, this);
    }

    public void waitForResponse() {
        synchronized(this) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public String getCallbackURL() {
        return callbackURL;
    }

    public void setCallbackURL(String callbackURL) {
        this.callbackURL = callbackURL;
    }

    public int getNoOfRequests() {
        return noOfRequests;
    }

    public void setNoOfRequests(int noOfRequests) {
        this.noOfRequests = noOfRequests;
    }

    public static CallbackRoute createCallbackRoute(String callbackURL, String... params) {
        int port = 4567;
        port(port);
        CallbackRoute route = new CallbackRoute(callbackURL, params);

        if(callbackURL.startsWith("/")) {
            callbackURL = callbackURL.replaceFirst("/", "");
        }

        route.callbackURL = String.format("http://127.0.0.1:%d/%s", port, callbackURL);
        return route;
    }

    public static void stopAllRoutes() {
        stop();
    }
}
