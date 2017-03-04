import org.xml.sax.SAXException;
import schema.AddressType;
import schema.CarType;
import schema.ObjectFactory;
import schema.PersonType;

import javax.xml.XMLConstants;
import javax.xml.bind.*;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.StringWriter;

/**
 * Created by angela on 19.02.17.
 */
public class XMLChecker {
    private String error = "";
    class MyValidationEventHandler implements ValidationEventHandler {
        public boolean handleEvent(ValidationEvent event) {
            error="LINE NUMBER: " + event.getLocator().getLineNumber() + " :" + event.getMessage();
            return true;
        }
    }

    private Unmarshaller unmarshaller;
    public XMLChecker() throws JAXBException, SAXException {
        JAXBContext context = JAXBContext.newInstance("schema");
        unmarshaller = context.createUnmarshaller();

        SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = sf.newSchema(new File("schema.xsd"));

        unmarshaller.setSchema(schema);
        unmarshaller.setEventHandler(new MyValidationEventHandler());
        if(unmarshaller.getSchema()==null)
            throw new JAXBException("no validation!");
    }

    public String check(String filename) throws FileNotFoundException, JAXBException {
        error = "";
        JAXBElement<PersonType> p =  (JAXBElement<PersonType>)
                unmarshaller.unmarshal(new FileInputStream(filename));
        return error;
    }
}
