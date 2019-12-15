package cz.ouskam.opensource;

import cz.ouskam.opensource.dto.Layer;
import cz.ouskam.opensource.dto.Module;
import cz.ouskam.opensource.dto.ModulesWrapper;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.codehaus.plexus.util.FileUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Properties;

import static cz.ouskam.opensource.ModulesParser.parseModule;

@Mojo( name = "build", defaultPhase = LifecyclePhase.GENERATE_RESOURCES )
public class BuilderMojo extends AbstractMojo {
    /**
     * Location of the file.
     */
    @Parameter( defaultValue = "${project.build.directory}/modules-build", property = "outputDirectory" )
    private File outputDirectory;

    @Parameter( defaultValue = "${project.build.directory}/work", property = "workDirectory" )
    private File workDirectory;

    @Parameter(property = "layersConfFile", required = true )
    private File layersConfFile;

    @Parameter(property = "modulesYamlFile", required = true )
    private File modulesYamlFile;

    @Parameter(property = "parameters" )
    private Map<String, String> parameters;

    public void execute() throws MojoExecutionException {
        try {
            runtimeExecute();
        } catch (IOException | IllegalAccessException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public void runtimeExecute() throws IOException, IllegalAccessException {
        ModulesWrapper wrapper = parseModule(modulesYamlFile, parameters);

        File modulesDirectory = new File(workDirectory.getAbsolutePath() + "/modules/");
        File layersDirectory = new File(modulesDirectory.getAbsolutePath() + "/system/layers/");

        VelocityEngine ve = new VelocityEngine();
        Properties properties = new Properties();
        properties.setProperty("resource.loader", "file");
        properties.setProperty("file.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        ve.init(properties);

        Template pomTemplate = ve.getTemplate("templates/pom.template.vm");
        Template moduleXmlTemplate = ve.getTemplate("templates/module.template.vm");

        for (Layer layer: wrapper.layers) {
            for (Module module: layer.modules) {
                VelocityContext context = new VelocityContext();
                String moduleDirPath = layersDirectory + "/" + layer.name + "/" + module.name.replace(".", "/") + "/main/";
                File moduleDir = new File(moduleDirPath);
                moduleDir.mkdirs();

                context.put("modulePath", moduleDirPath);
                context.put("artifacts", module.artifacts);
                context.put("dependencies", module.dependencies);
                context.put("module", module);

                StringWriter pomFileString = new StringWriter();
                StringWriter moduleFileString = new StringWriter();

                pomTemplate.merge( context, pomFileString );
                moduleXmlTemplate.merge( context, moduleFileString );

                String pomFilePath = workDirectory.getAbsolutePath() + "/pom-" + module.name + ".xml";

                BuilderMojo.writeToFile(pomFileString.toString().getBytes("UTF-8"), pomFilePath);
                BuilderMojo.writeToFile(moduleFileString.toString().getBytes("UTF-8"), moduleDirPath + "/module.xml");

                buildPom(pomFilePath);
            }
        }

        outputDirectory.mkdirs();
        new File(outputDirectory.getAbsolutePath() + "/jboss").mkdirs();
        copyDir(modulesDirectory.toPath(), new File(outputDirectory.getAbsolutePath() + "/jboss/modules").toPath());
        FileUtils.copyFile(layersConfFile, new File(outputDirectory.getAbsolutePath() + "/jboss/modules/layers.conf"));
    }

    public static void writeToFile(byte[] bytes, String path) throws IOException {
        Path file = Paths.get(path);
        Files.write(file, bytes);
    }

    public static void copyDir(Path src, Path dest) throws IOException {
        Files.walk(src)
                .forEach(source -> {
                    try {
                        Files.copy(source, dest.resolve(src.relativize(source)),
                                StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }

    public static void buildPom(String pomFilePath) throws IOException {
        ProcessBuilder builder = new ProcessBuilder(
                "mvn", "clean", "install", "-f", pomFilePath);
        builder.redirectErrorStream(true);
        Process p = builder.start();
        BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        while (true) {
            line = r.readLine();
            if (line == null) { break; }
            System.out.println(line);
        }
    }
}
