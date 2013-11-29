package org.universe.test.jaxb;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.universe.ConsoleTable;
import org.universe.System6;
import org.universe.jaxb.JAXBSerializer;
import org.universe.jcl.Stress;
import org.universe.test.TestEnv;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestJAXB_Perfomance {

    static JAXBSerializer<SimplestVo> SimplestConverter = new JAXBSerializer<SimplestVo>(SimplestVo.class);

    @Test
    public void MyVo1_MARSHALLER_To_Data() throws Exception
    {
        final MyVo1 vo = VoEnv.createVO1();

        Stress.LoopBody iteration = new Stress.LoopBody() {
            @Override
            public void run() throws JAXBException, XMLStreamException {
                VoEnv.Ver1Converter.ToData(vo);
            }
        };

        measure(iteration, "MyVo1 MARSHALLER to byte[]", 2000);
    }

    @Test
    public void MyVo1_UNMARSHALLER_From_Data() throws Exception
    {
        final MyVo1 vo = VoEnv.createVO1();
        final byte[] data = VoEnv.Ver1Converter.ToData(vo);

        Stress.LoopBody iteration = new Stress.LoopBody() {
            @Override
            public void run() throws JAXBException, XMLStreamException {
                VoEnv.Ver1Converter.Parse(data);
            }
        };

        measure(iteration, "MyVo1 UNMARSHALLER from byte[]", 2000);
    }



    @Test
    public void MyVo1_PARSE_xml_into_DOM() throws Exception {
        final MyVo1 vo = VoEnv.createVO1();
        final String xml = VoEnv.Ver1Converter.ToXml(vo);
        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        Stress.LoopBody iteration = new Stress.LoopBody() {
            @Override
            public void run() throws ParserConfigurationException, IOException, SAXException {
                DocumentBuilder db = dbf.newDocumentBuilder();
                InputSource is = new InputSource(new StringReader(xml));
                Document x = db.parse(is);
                Assert.assertEquals(x.getDocumentElement().getNodeName(), MyVo1.RootName);
            }
        };

        measure(iteration, "MyVo1 PARSE xml from byte[]", 2000);
    }

    @Test
    public void Simplest_UNMARSHAL_data() throws Exception {
        final SimplestVo vo = new SimplestVo();
        final byte[] data = SimplestConverter.ToData(vo);

        Stress.LoopBody iteration = new Stress.LoopBody() {
            @Override
            public void run() throws JAXBException, XMLStreamException {
                SimplestConverter.Parse(data);
            }
        };

        measure(iteration, "UNMARSHAL Simplest from byte[]", 2000);
    }

    @Test
    public void Simplest_MARSHAL_data() throws Exception {
        final SimplestVo vo = new SimplestVo();
        Stress.LoopBody iteration = new Stress.LoopBody() {
            @Override
            public void run() throws JAXBException, XMLStreamException {
                byte[] data = SimplestConverter.ToData(vo);
            }
        };

        measure(iteration, "MARSHAL Simplest to byte[]", 2000);
    }

    private void measure(Stress.LoopBody run, String caption, int duration) throws InterruptedException {
        System.out.print(caption + ": ");
        Stress.Heat(run);
        List<Integer> threads = Arrays.asList(1, 2, 6);
        ConsoleTable tbl = Stress.CreateReport();
        for(int t : threads)
        {
            System.out.print(t + " ");
            Stress res = Stress.Measure(TestEnv.scaleDuration(duration), 1, t, run);
            res.WriteToReport(tbl);
        }

        System.out.println(System6.lineSeparator() + System6.lineSeparator() + caption + System6.lineSeparator() + tbl);
    }



}
