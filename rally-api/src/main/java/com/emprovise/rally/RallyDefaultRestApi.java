package com.emprovise.rally;


import com.emprovise.rally.exception.RallyPasswordExpiredException;
import com.rallydev.rest.RallyRestApi;
import com.rallydev.rest.request.QueryRequest;
import com.rallydev.rest.response.QueryResponse;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class RallyDefaultRestApi extends RallyRestApi {

    private String rallyUser = null;
    private String rallyPassword = null;

    public RallyDefaultRestApi(URI server, String userName, String password) {
        super(server, userName, password);
        this.rallyUser = userName;
        this.rallyPassword = password;
    }

    @Override
    public QueryResponse query(QueryRequest request) throws IOException {
        try {
            return super.query(request);
        }
        catch(com.google.gson.JsonSyntaxException jex) {
            if(isPasswordExpired()) {
                throw new RallyPasswordExpiredException();
            }
            else {
                throw jex;
            }
        }
        catch(javax.net.ssl.SSLPeerUnverifiedException sslex) {
            if(isPasswordExpired()) {
                throw new RallyPasswordExpiredException();
            }
            else {
                throw sslex;
            }
        }
    }

    /**
     * Returns true when the rally account password is expired or else returns false.
     * @return
     * @throws java.net.URISyntaxException
     * @throws org.apache.http.ParseException
     * @throws java.io.IOException
     */
    private boolean isPasswordExpired() throws ParseException, IOException {

        DefaultHttpClient httpClient = new DefaultHttpClient();

        try {
            URI serverUri = new URI(RallyClient.RALLY_HOST);
            httpClient.getCredentialsProvider().setCredentials(new AuthScope(serverUri.getHost(), serverUri.getPort()), new UsernamePasswordCredentials(rallyUser, rallyPassword));
            setHttpClientProxy(httpClient);
            String rallyUrl = RallyClient.RALLY_HOST + "/slm/webservice/v2.0/security/authorize";
            HttpGet httpGet = new HttpGet(rallyUrl);

            HttpResponse response = httpClient.execute(httpGet);
            if (response.getStatusLine().getStatusCode() == 200) {
                HttpEntity entity = response.getEntity();
                String responseString = EntityUtils.toString(entity);

                if(responseString != null && responseString.contains("Password Reset"))
                    return true;
                else
                    return false;
            }
            throw new IOException(response.getStatusLine().toString());
        }
        catch (URISyntaxException uex) {
            throw new IOException(uex);
        }
        finally {
            httpClient.getConnectionManager().shutdown();
        }
    }

    void setHttpClientProxy(DefaultHttpClient httpClient) throws URISyntaxException {
        return;
    }

    protected String getRallyUser() {
        return rallyUser;
    }

    protected String getRallyPassword() {
        return rallyPassword;
    }
}
