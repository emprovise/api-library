package com.emprovise.soapui;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.*;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockOperation;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResponse;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockService;
import com.eviware.soapui.impl.wsdl.submit.transports.http.BaseHttpResponse;
import com.eviware.soapui.impl.wsdl.support.http.SoapUIMultiThreadedHttpConnectionManager;
import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlImporter;
import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.model.iface.Request;
import com.eviware.soapui.model.iface.Response;
import com.eviware.soapui.model.iface.Submit;
import com.eviware.soapui.model.mock.MockRunner;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class SoapUIApi {

    private WsdlDocument wsdlDoc;
    private Map<String, WsdlOperation> operations;
    private WsdlMockService mockService;

    public static final int DEFAULT_TIMEOUT = 20000;

    public SoapUIApi(URL url) throws IOException {
        wsdlDoc = new WsdlDocument(url);
        operations = generateOperations();
    }

    public SoapUIApi(String url) throws IOException {
        this(new URL(url));
    }

    public SoapUIApi(File file) throws IOException {
        this(file.toURI().toURL());
    }

    public void shutdown() {
        SoapUI.getThreadPool().shutdown();
        try {
            SoapUI.getThreadPool().awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Now to shutdown the monitor thread setup by SoapUI
        Thread[] tarray = new Thread[Thread.activeCount()];
        Thread.enumerate(tarray);
        for (Thread t : tarray) {
            if (t instanceof SoapUIMultiThreadedHttpConnectionManager.IdleConnectionMonitorThread) {
                ((SoapUIMultiThreadedHttpConnectionManager.IdleConnectionMonitorThread) t)
                        .shutdown();
            }
        }

        // shutdown soapUI.
        SoapUI.shutdown();
    }

    /**
     * Returns all operations defined in this service
     *
     * @return operations
     */
    public List<WsdlOperation> getOperations() {
        return new ArrayList<WsdlOperation>(operations.values());
    }

    private Map<String, WsdlOperation> generateOperations() throws IOException {

        HashMap<String, WsdlOperation> soapOperations = new HashMap<String, WsdlOperation>();
        WsdlInterface wsdlInterface = wsdlDoc.getWsdlInterface();
        List<Operation> operations = wsdlInterface.getOperationList();

        for (Operation operation : operations) {
            WsdlOperation op = (WsdlOperation) operation;
            soapOperations.put(op.getName(), op);
        }

        return soapOperations;
    }

    public WsdlOperation getOperation(String operation) {
        return operations.get(operation);
    }

    public String getDefaultRequest(String operation) throws IOException {
        return wsdlDoc.generateRequest(operation);
    }

    public String getDocumentation(String operation) throws IOException {
        String stringDocument = wsdlDoc.getOperationDocumentation(operation);
        return stringDocument.replaceAll("\\s+", " ").trim();
    }

    public String submitOperation(String operation, File requestFile, String username, String password, Integer timeout) throws IOException {
        return this.submitOperation(operation, readFile(requestFile), username, password, timeout);
    }

    public String submitOperation(String operation, String requestContent, String username, String password, Integer timeout) {
        WsdlOperation wsdlOperation = getOperation(operation);

        if(timeout == null) {
            timeout = DEFAULT_TIMEOUT;
        }

        return submitOperation(wsdlOperation, requestContent, username, password, timeout);
    }

    public void addMockOperation(String operation, String mockResponseContent, String groovyScript) throws Exception {

        if (mockService == null) {
            mockService = wsdlDoc.createMockService("Mock Service");
        }

        WsdlOperation wsdlOperation = getOperation(operation);
        WsdlMockOperation mockOperation = mockService.addNewMockOperation(wsdlOperation);
        WsdlMockResponse mockResponse = mockOperation.addNewMockResponse(operation, true);

        if(groovyScript != null) {
            mockResponse.setScript(groovyScript);
        }

        if(mockResponseContent != null) {
            mockResponse.setResponseContent(mockResponseContent);
        }
    }

    public void addMockOperation(String operation, File mockResponseFile, File groovyScriptFile) throws Exception {

        String mockResponseContent = null;
        String groovyScript = null;

        if(mockResponseFile != null) {
            mockResponseContent = readFile(mockResponseFile);
        }

        if(groovyScriptFile != null) {
            groovyScript = readFile(groovyScriptFile);
        }

        addMockOperation(operation, mockResponseContent, groovyScript);
    }

    public MockRunner startMockService() throws Exception {
        if (mockService != null) {
            return mockService.start();
        }

        return null;
    }

    public void stopMockService() throws Exception {
        if (mockService != null) {
            mockService.getMockRunner().stop();
        }
    }

    public String getMockServicePath() throws Exception {
        if (mockService != null) {
            return new StringBuilder(mockService.getHost())
                    .append(":")
                    .append(mockService.getPort())
                    .append(mockService.getPath())
                    .toString();
        }
        return null;
    }

    private String readFile(File file) throws IOException {
        InputStream inputStream = new FileInputStream(file);
        try {
            return IOUtils.toString(inputStream);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    public static String getDefaultRequest(WsdlOperation operation) throws IOException {
        return operation.createRequest(true);
    }

    public static List<Operation> loadOperations(URL httpUrl) {
        try {
            WsdlProject project = new WsdlProject();
            WsdlInterface[] wsdls = WsdlImporter.importWsdl(project, httpUrl.toString());
            WsdlInterface wsdl = wsdls[0];
            return wsdl.getOperationList();
        } catch(Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static String submitOperation(WsdlOperation wsdlOperation, String requestContent,
                                         String username, String password, Integer timeout) {
        // create a new empty request for that operation
        WsdlRequest request = wsdlOperation.addNewRequest("New Request");

        if(timeout != null) {
            request.setTimeout(timeout.toString());
        }

        if(requestContent == null) {
            // generateRequest
            requestContent = wsdlOperation.createRequest(true);
        }

        request.setRequestContent(requestContent);

        if (username!=null && password!=null && !username.isEmpty() && !password.isEmpty()) {
            // Add WSSecurity parameters
            request.setWssPasswordType("PasswordText");
            request.setAuthType("Preemptive");
            request.setUsername(username);
            request.setPassword(password);
            request.setWssTimeToLive("10000");
        }

        // submit the request
        try {
            WsdlSubmit<WsdlRequest> submit = request.submit(new WsdlSubmitContext(request), false);
            Submit.Status status = submit.getStatus(); //FINISHED OR ERROR
            Response response = submit.getResponse();
            String responseString = response.getContentAsString();

            int statusCode = HttpStatus.SC_OK;
            if(response instanceof BaseHttpResponse) {
                statusCode = ((BaseHttpResponse)response).getStatusCode();
            }

            if(statusCode != HttpStatus.SC_OK || !status.equals(Submit.Status.FINISHED)) {
                throw new RuntimeException(responseString);
            }

            return responseString;

        } catch (Request.SubmitException ex) {
            throw new RuntimeException(ex);
        }
    }
}
