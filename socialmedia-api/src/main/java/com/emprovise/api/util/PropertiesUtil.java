package com.emprovise.api.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;

public class PropertiesUtil {

    public static Properties loadProperties(String propsName) throws IOException {
        Properties props = new Properties();
        URL url = ClassLoader.getSystemResource(propsName);
        props.load(url.openStream());
        return props;
    }

    public static void writeProperties(String propsName, Properties properties) throws IOException, URISyntaxException {
        URL url = ClassLoader.getSystemResource(propsName);
        File file = new File(url.toURI());
        FileOutputStream fileOut = new FileOutputStream(file);
        properties.store(fileOut, "Properties");
        fileOut.close();
    }
}
