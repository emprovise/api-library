package com.emprovise.soap;

import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.io.IOUtils;
import org.apache.http.entity.StringEntity;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;

public class SoapUtil {

    private SoapUtil() {
        throw new UnsupportedOperationException();
    }

    public static StringRequestEntity toSoapEntity(String xmlIP, String contentType) throws SOAPException, SAXException, IOException, ParserConfigurationException {

        SOAPMessage soapMessage = MessageFactory.newInstance().createMessage();
        SOAPBody soapBody = soapMessage.getSOAPPart().getEnvelope().getBody();
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        builderFactory.setNamespaceAware(true);
        InputStream stream = new ByteArrayInputStream(xmlIP.getBytes());
        Document document = builderFactory.newDocumentBuilder().parse(stream);
        soapBody.addDocument(document);

        ByteArrayOutputStream ostream = new ByteArrayOutputStream();
        soapMessage.writeTo(ostream);
        StringRequestEntity entity = new StringRequestEntity(new String(ostream.toByteArray()), contentType, "UTF-8");
        return entity;
    }

    public static String toXmlString(Object inputObject, Class<Object> inputClass) throws JAXBException, IOException {

        JAXBContext jaxbContext = JAXBContext.newInstance(inputClass);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        StringWriter stringWriter = new StringWriter();
        marshaller.marshal(new JAXBElement<Object>(new QName("http://"+inputClass.getName(), inputClass.getName()),
                inputClass, inputObject), stringWriter);
        String xmlIP = stringWriter.toString();
        stringWriter.close();
        return xmlIP;
    }

    public static String documentToString(Document doc) throws TransformerException {
        StringWriter sw = new StringWriter();
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

        transformer.transform(new DOMSource(doc), new StreamResult(sw));
        return sw.toString();
    }

    public static SOAPMessage getSoapMessage(String xmlMessage) throws SOAPException, IOException {
        MessageFactory messageFactory = MessageFactory.newInstance();
        InputStream inputStream = new ByteArrayInputStream(xmlMessage.getBytes());
        SOAPMessage message = messageFactory.createMessage(null, inputStream);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        message.writeTo(out);
        return message;
    }

    public static org.jdom.Document toDocument(String xmlString) throws IOException {
        InputStream soapStream = new ByteArrayInputStream(xmlString.getBytes());
        SAXBuilder parser = new SAXBuilder();
        org.jdom.Document result = null;
        try {
            result = parser.build(soapStream);
        } catch (JDOMException e) {
            throw new IOException("Could not parse XML.", e);
        }
        return result;
    }

    public static String convertInputStreamToString(InputStream inputStream) throws IOException {
        StringWriter writer = new StringWriter();
        IOUtils.copy(inputStream, writer, "UTF-8");
        return writer.toString();
    }
}
