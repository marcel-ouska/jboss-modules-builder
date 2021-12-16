package com.codenuity.jboss.modules;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BuilderUtils {

    public static String buildTemplate(String templatePath, VelocityContext context) {
        VelocityEngine ve = new VelocityEngine();
        Properties properties = new Properties();
        properties.setProperty("resource.loader", "file");
        properties.setProperty("file.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        ve.init(properties);

        Template template = ve.getTemplate(templatePath);
        StringWriter resultString = new StringWriter();

        template.merge( context, resultString );

        return resultString.toString();
    }

    public static void buildTemplate(String templatePath, VelocityContext context, String outFile, boolean formatXml) throws Exception {
        String template = buildTemplate(templatePath, context);

        if (formatXml) {
            template = getPrettyXml(template, 2);
        }

        writeToFile(template.getBytes(StandardCharsets.UTF_8), outFile);
    }

    public static String getPrettyXml(String xmlData, int indent) throws Exception {
        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = db.parse(new InputSource(new StringReader(xmlData)));

        doc.getDocumentElement().normalize();
        XPathExpression xpath = XPathFactory.newInstance().newXPath().compile("//text()[normalize-space(.) = '']");
        NodeList blankTextNodes = (NodeList) xpath.evaluate(doc, XPathConstants.NODESET);

        for (int i = 0; i < blankTextNodes.getLength(); i++) {
            blankTextNodes.item(i).getParentNode().removeChild(blankTextNodes.item(i));
        }

        DOMSource domSource = new DOMSource(doc);
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        StringWriter sw = new StringWriter();
        StreamResult sr = new StreamResult(sw);
        transformer.transform(domSource, sr);
        return sw.toString();
    }


    public static void writeToFile(byte[] bytes, String path) throws IOException {
        mkDirs(path);
        Path file = Paths.get(path);
        Files.write(file, bytes);
    }

    public static void copyDir(Path src, Path dest) throws IOException {
        mkDirs(new File(dest.toAbsolutePath().toString()).getParentFile().getAbsolutePath());
        Files.walk(src)
                .forEach(source -> {
                    try {
                        Path destination = dest.resolve(src.relativize(source));
                        if (!destination.toFile().exists()) {
                            Files.copy(source, dest.resolve(src.relativize(source)));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }

    public static boolean mkDirs(String path) {
        File file = new File(path);
        if (file.getName().contains(".")) {
            return file.getParentFile().mkdirs();
        } else {
            return file.mkdirs();
        }
    }

    public static String resolveString(String string, Map<String, String> parameters) {
        String resultString = string;
        Pattern p = Pattern.compile("\\$\\{([0-9a-zA-Z^.-]+)\\}");
        Matcher m = p.matcher(string);
        while (m.find()) {
            if (parameters.containsKey(m.group(1))) {
                resultString = resultString.replace(m.group(0) , parameters.get(m.group(1)));
            }
        }

        return resultString;
    }

    public static boolean isEmpty(String string) {
        return string == null || string.isEmpty();
    }

    public static boolean isEmpty(Collection collection) {
        return collection == null || collection.isEmpty();
    }
}
