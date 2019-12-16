package cz.ouskam.opensource;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Properties;

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

        writeToFile(template.getBytes("UTF-8"), outFile);
    }

    public static String getPrettyXml(String xmlData, int indent) throws Exception {
        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = db.parse(new InputSource(new StringReader(xmlData)));

        OutputFormat format = new OutputFormat(doc);
        format.setIndenting(true);
        format.setIndent(indent);
        format.setOmitXMLDeclaration(false);
        format.setLineWidth(Integer.MAX_VALUE);
        Writer outxml = new StringWriter();
        XMLSerializer serializer = new XMLSerializer(outxml, format);
        serializer.serialize(doc);

        return outxml.toString();
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

    public static boolean isEmpty(String string) {
        return string == null || string.isEmpty();
    }

    public static boolean isEmpty(Collection collection) {
        return collection == null || collection.isEmpty();
    }
}
