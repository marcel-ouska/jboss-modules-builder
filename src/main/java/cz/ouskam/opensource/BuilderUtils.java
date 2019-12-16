package cz.ouskam.opensource;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    public static void buildTemplate(String templatePath, VelocityContext context, String outFile) throws IOException {
        String template = buildTemplate(templatePath, context);
        writeToFile(template.getBytes("UTF-8"), outFile);
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

    public static boolean mkDirs(String path) throws IOException {
        File file = new File(path);
        if (file.getName().contains(".")) {
            return file.getParentFile().mkdirs();
        } else {
            return file.mkdirs();
        }
    }
}
