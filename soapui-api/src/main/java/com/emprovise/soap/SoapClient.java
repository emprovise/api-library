package com.emprovise.soap;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.auth.BasicScheme;
import org.apache.commons.httpclient.methods.FileRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.w3c.dom.Document;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;

public class SoapClient {

	private HttpClient httpClient;

	public SoapClient() {
		this(new HttpClient());
	}

	public SoapClient(HttpClient client) {

		if (client == null) {
			throw new IllegalArgumentException("Invalid http client");
		}

		this.httpClient = client;
		client.getHttpConnectionManager().getParams().setConnectionTimeout(15000);
		client.getHttpConnectionManager().getParams().setSoTimeout(35000);
	}

	public Object callSOAPService(String serviceUrl, String userId, String password, Object inputObject, Class<Object> inputClass, Class<?> outputClass) throws Exception {

		String responseString = callSOAPService(serviceUrl, userId, password, inputClass.getName(), SoapUtil.toXmlString(inputObject, inputClass), "application/xml");
		JAXBContext jaxbContext = JAXBContext.newInstance(outputClass);

		InputStream responseStream = new ByteArrayInputStream(responseString.getBytes(StandardCharsets.UTF_8));

		SOAPMessage soapMessage = MessageFactory.newInstance().createMessage(new MimeHeaders(), responseStream);
		Document document = soapMessage.getSOAPBody().extractContentAsDocument();
		String opXml = SoapUtil.documentToString(document);

		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		unmarshaller.setEventHandler(new ValidationEventHandler() {
			public boolean handleEvent(ValidationEvent event) {
				throw new RuntimeException(event.getMessage(), event.getLinkedException());
			}
		});

		Object outputObject = unmarshaller.unmarshal(
				new StreamSource(new ByteArrayInputStream(opXml.getBytes())), outputClass).getValue();

		return outputObject;
	}

	public String callSOAPService(String serviceUrl, String username, String password, String operation, File xmlRequestFile, String contentType) throws Exception {
		RequestEntity entity = new FileRequestEntity(xmlRequestFile, contentType + "; charset=ISO-8859-1");
		return callSOAPService(serviceUrl, username, password, operation, entity, contentType);
	}

	public String callSOAPService(String serviceUrl, String username, String password, String operation, String xmlRequestString, String contentType) throws Exception {
		return callSOAPService(serviceUrl, username, password, operation, SoapUtil.toSoapEntity(xmlRequestString, contentType), contentType);
	}

	public String callSOAPService(String serviceUrl, String username, String password, String operation, RequestEntity entity, String contentType) throws Exception {

		PostMethod post = new PostMethod(serviceUrl);
		post.setRequestEntity(entity);
		post.setRequestHeader(new Header("Content-type", contentType + "; charset=\"utf-8\""));
		post.setRequestHeader("SOAPAction", operation);

		UsernamePasswordCredentials authCred = new UsernamePasswordCredentials(username, password);
		String authHeaderVal = BasicScheme.authenticate(authCred, "utf-8");
		post.addRequestHeader("Authorization", authHeaderVal);

		int statusCode = httpClient.executeMethod(post);

		if (statusCode != HttpStatus.SC_OK) {
			throw new RuntimeException("Invalid Rresponse Status: " + post.getResponseBodyAsString());
		}

		return post.getResponseBodyAsString();
	}

	private void addHeaders(HttpMethod httpMethod, Map<String, String> headers) {
		Iterator<String> headerIt = headers.keySet().iterator();
		while (headerIt.hasNext()) {
			String headerName = headerIt.next();
			String headerValue = headers.get(headerName);
			if (headerName != null && headerValue != null) {
				httpMethod.addRequestHeader(headerName, headerValue);
			}
		}
	}
}