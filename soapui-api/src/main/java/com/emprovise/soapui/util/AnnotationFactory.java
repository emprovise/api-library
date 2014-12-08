package com.emprovise.soapui.util;

import com.sun.xml.xsom.parser.AnnotationParser;
import com.sun.xml.xsom.parser.AnnotationParserFactory;

public class AnnotationFactory implements AnnotationParserFactory{
    @Override
    public AnnotationParser create() {
        return new XsdAnnotationParser();
    }
}
