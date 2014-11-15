package com.emprovise.rally;

import com.rallydev.rest.request.CreateRequest;
import com.rallydev.rest.request.UpdateRequest;
import com.rallydev.rest.response.CreateResponse;
import com.rallydev.rest.response.UpdateResponse;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;

/*
 * Due the issue with the rally-rest-api version 2.0.4 onwards, were setting up the
 * proxy credentials causes the api to use proxy credentials instead of rally
 * credentials to authenticate for all write operations such as create/update/delete
 * rally items. The temporary fix here is to update the proxy with Rally credentials
 * just before calling the rally write methods thus overriding the proxy credentials
 * set in the http client.
 *
 */
public class RallyProxyRestApi extends RallyDefaultRestApi {

    private String proxyUrl = null;
    private String proxyUser = null;
    private String proxyPassword = null;

    public RallyProxyRestApi(URI server, String userName, String password, String proxyUrl, String proxyUser, String proxyPassword) {
        super(server, userName, password);
        this.proxyUrl = proxyUrl;
        this.proxyUser = proxyUser;
        this.proxyPassword = proxyPassword;
        setProxy(proxyUrl, proxyUser, proxyPassword);
    }

    @Override
    public UpdateResponse update(UpdateRequest request)
            throws IOException {
        setProxy(proxyUrl, getRallyUser(), getRallyPassword());
        UpdateResponse updateResponse = super.update(request);
        return updateResponse;
    }

    @Override
    public CreateResponse create(CreateRequest request)
            throws IOException {
        setProxy(proxyUrl, getRallyUser(), getRallyPassword());
        return super.create(request);
    }

    private void setProxy(String proxyUrl, String user, String password) {
        if(user != null && password !=null && isHostAvailable(proxyUrl)) {
            URI proxy;
            try {
                proxy = new URI("http", proxyUrl, "", null);
            } catch (URISyntaxException e) {
                throw new RuntimeException(proxyUrl, e);
            }
            this.setProxy(proxy, user, password);
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

    @Override
    void setHttpClientProxy(DefaultHttpClient httpClient) throws URISyntaxException {
        if(isHostAvailable(proxyUrl)) {
            URI proxy = new URI("http", proxyUrl, "", null);
            httpClient.getCredentialsProvider().setCredentials(new AuthScope(proxy.getHost(), proxy.getPort()), new UsernamePasswordCredentials(proxyUser, proxyPassword));
            HttpHost httpproxy = new HttpHost(proxyUrl, 80, "http");
            httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, httpproxy);
        }
    }
}
