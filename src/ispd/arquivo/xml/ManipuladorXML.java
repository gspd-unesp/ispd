/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd.arquivo.xml;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author denison
 */
public class ManipuladorXML {

    public static Document ler(File xmlFile, String dtd) throws ParserConfigurationException, IOException, SAXException {
        Document documento = null;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        final String dtdT = dtd;
        //Indicar local do arquivo .dtd
        builder.setEntityResolver(new EntityResolver() {
            InputSource substitute = new InputSource(ManipuladorXML.class.getResourceAsStream("dtd/"+dtdT));

            @Override
            public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
                return substitute;
            }
        });
        documento = builder.parse(xmlFile);
        return documento;
    }

    /**
     * Este método sobrescreve ou cria arquivo xml
     */
    public static boolean escrever(Document documento, File arquivo, String doc_system) {
        try {
            TransformerFactory tf = TransformerFactory.newInstance();
            //tf.setAttribute("indent-number", new Integer(4));
            Transformer transformer = tf.newTransformer();
            DOMSource source = new DOMSource(documento);
            StreamResult result = new StreamResult(arquivo);
            transformer.setOutputProperty(OutputKeys.ENCODING, "ISO-8859-1");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, doc_system);
            transformer.transform(source, result);
        } catch (TransformerConfigurationException ex) {
            Logger.getLogger(ManipuladorXML.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } catch (TransformerException ex) {
            Logger.getLogger(ManipuladorXML.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }

    /**
     * Cria novo documento
     *
     * @return novo documento xml iconico
     */
    public static Document novoDocumento() {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.newDocument();
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(ManipuladorXML.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
}