package com.emprovise.api.rally;


import com.emprovise.api.rally.exception.RallyPasswordExpiredException;
import com.rallydev.rest.RallyRestApi;
import com.rallydev.rest.client.ApiKeyClient;
import com.rallydev.rest.request.QueryRequest;
import com.rallydev.rest.response.QueryResponse;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;

public class RallyDefaultRestApi extends RallyRestApi {

    private String rallyUser = null;
    private String rallyPassword = null;
    private URI proxyUri = null;
    private String proxyUser = null;
    private String proxyPassword = null;

    public RallyDefaultRestApi(URI server, String userName, String password) {
        super(server, userName, password);
        this.rallyUser = userName;
        this.rallyPassword = password;
    }

    public RallyDefaultRestApi(URI server, String apiKey) {
        super(new ApiKeyClient(server, apiKey));
    }

    public RallyDefaultRestApi(URI server, String userName, String password, URI proxyUri, String proxyUser, String proxyPassword) {
        super(server, userName, password);
        this.proxyUri = proxyUri;
        this.proxyUser = proxyUser;
        this.proxyPassword = proxyPassword;
        setValidProxyUri(proxyUri, proxyUser, proxyPassword);
    }

    public RallyDefaultRestApi(URI server, String apiKey, URI proxyUri, String proxyUser, String proxyPassword) {
        super(server, apiKey);
        this.proxyUri = proxyUri;
        this.proxyUser = proxyUser;
        this.proxyPassword = proxyPassword;
        setValidProxyUri(proxyUri, proxyUser, proxyPassword);
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

        if(rallyUser==null || rallyPassword==null) {
            return false;
        }

        DefaultHttpClient httpClient = new DefaultHttpClient();

        try {
            URI serverUri = new URI(RallyClient.RALLY_HOST);
            httpClient.getCredentialsProvider().setCredentials(new AuthScope(serverUri.getHost(), serverUri.getPort()),
                    new UsernamePasswordCredentials(rallyUser, rallyPassword));

            if(proxyUri != null && isHostAvailable(proxyUri.getHost())) {
                httpClient.getCredentialsProvider().setCredentials(new AuthScope(proxyUri.getHost(), proxyUri.getPort()),
                        new UsernamePasswordCredentials(proxyUser, proxyPassword));
                HttpHost httpProxy = new HttpHost(proxyUri.getHost(), 80, "http");
                httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, httpProxy);
            }

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

    private void setValidProxyUri(URI proxyUri, String user, String password) {
        if(proxyUri != null && isHostAvailable(proxyUri.getHost())) {
            this.setProxy(proxyUri, user, password);
        }
    }

    private boolean isHostAvailable(String hostname) {

        Socket socket = null;
        boolean reachable = false;
        try {
            socket = new Socket(hostname, 80);
            reachable = true;
        } catch (Exception ex) {
            reachable = false;
        } finally {
            if (socket != null) try { socket.close(); } catch(IOException e) {}
        }
        return reachable;
    }
}
