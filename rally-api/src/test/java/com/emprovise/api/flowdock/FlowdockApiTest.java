package com.emprovise.api.flowdock;

import com.emprovise.api.flowdock.model.Message;
import com.emprovise.api.flowdock.type.MessageType;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class FlowdockApiTest {

    @Test
    public void getFlows() throws Exception {
        FlowdockApi api = new FlowdockApi("FLOWDOCK_API_TOKEN");
        JsonArray flows = api.getFlows();
        System.out.println(flows);
    }

    @Test
    public void getAllFlows() throws Exception {
        FlowdockApi api = new FlowdockApi("FLOWDOCK_API_TOKEN");
        JsonArray allFlows = api.getAllFlows();
        System.out.println(allFlows);
    }

    @Test
    public void getFlow() throws Exception {
        FlowdockApi api = new FlowdockApi("FLOWDOCK_API_TOKEN");
        JsonObject flow = api.getFlow("test-org", "test-forum");
        System.out.println(flow);
    }

    @Test
    public void getMessages() throws Exception {
        FlowdockApi api = new FlowdockApi("FLOWDOCK_API_TOKEN");
        JsonArray jsonArray = api.getMessages("test-org", "test-forum", "THREAD_ID");
        System.out.println(jsonArray);
    }

    @Test
    public void postMessage() throws Exception {
        FlowdockApi api = new FlowdockApi("FLOWDOCK_API_TOKEN");
        Message message = new Message();
        message.setEvent(MessageType.MESSAGE);
        message.setContent("Hello Everyone !!");
        message.addTags("todo");
        api.postMessage("test-org", "test-forum", "THREAD_ID", message);
    }
}