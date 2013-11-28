package org.universe.jaxb;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.io.StringWriter;

// @ThreadSafe
// Three classes should be cached:
// JAXBContext, XMLInputFactory, XMLOutputFactory
public class JAXBUtils<T> {

    public final Class Class;
    private JAXBContext Context;
    private static final String Encoding = "UTF-8";

    // Google says XMLInputFactory is thread safe, but ...
    private static ThreadLocal<XMLInputFactory> XmlInputFactory = new ThreadLocal<XMLInputFactory>() {
        @Override
        protected XMLInputFactory initialValue() {
            return XMLInputFactory.newInstance();
        }
    };

    // documentation doesnt
    private static ThreadLocal<XMLOutputFactory> XmlOutputFactory = new ThreadLocal<XMLOutputFactory>() {
        @Override
        protected XMLOutputFactory initialValue() {
            return XMLOutputFactory.newInstance();
        }
    };

    public JAXBUtils(java.lang.Class aClass) {
        Class = aClass;
    }

    synchronized JAXBContext getContext() throws JAXBException {
        if (Context == null)
            Context = JAXBContext.newInstance(Class);

        return Context;
    }

    public T Parse(String xml) throws JAXBException, XMLStreamException {
        if (xml == null || xml.length() == 0)
            return null;

        JAXBContext jc = getContext();
        Unmarshaller um = jc.createUnmarshaller();
        StringReader rdr = new StringReader(xml);
        XMLStreamReader streamReader = XmlInputFactory.get().createXMLStreamReader(rdr);
        T copy = (T) um.unmarshal(streamReader);
        return copy;
    }


    public T Parse(byte[] data) throws JAXBException, XMLStreamException {
        if (data == null || data.length == 0)
            return null;

        JAXBContext jc = getContext();
        Unmarshaller um = jc.createUnmarshaller();
        ByteArrayInputStream mem = new ByteArrayInputStream(data);
/*
        // Slow way is under comment, its better to cache XmlInputFactory
        T copy = (T) um.unmarshal(mem);
        return copy;
*/
        XMLStreamReader streamReader = XmlInputFactory.get().createXMLStreamReader(mem, Encoding);
        return (T) um.unmarshal(streamReader);
    }

    public String ToXml(T instance) throws JAXBException {
        return ToString(instance, false);
    }

    public String ToFormattedXml(T instance) throws JAXBException {
        return ToString(instance, true);
    }

    private String ToString(T instance, boolean formatted) throws JAXBException {
        if (instance == null)
            return null;

        JAXBContext jc = getContext();
        Marshaller marsh = jc.createMarshaller();
        marsh.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, formatted ? Boolean.TRUE : Boolean.FALSE);
        StringWriter wr = new StringWriter();
        marsh.marshal(instance, wr);
        return wr.toString();
    }

    public byte[] ToData(T instance) throws JAXBException, XMLStreamException {
        JAXBContext jc = getContext();
        Marshaller marsh = jc.createMarshaller();
        marsh.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);
        ByteArrayOutputStream mem = new ByteArrayOutputStream();
/*
        marsh.marshal(instance, mem);
        return mem.toByteArray();
*/

        XMLStreamWriter wr = XmlOutputFactory.get().createXMLStreamWriter(mem, Encoding);
        marsh.marshal(instance, wr);
        wr.flush();
        wr.close();
        return mem.toByteArray();
    }

}


