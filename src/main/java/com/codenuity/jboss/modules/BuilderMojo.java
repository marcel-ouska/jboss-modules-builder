package com.codenuity.jboss.modules;

import com.codenuity.jboss.modules.dto.Layer;
import com.codenuity.jboss.modules.dto.Module;
import com.codenuity.jboss.modules.dto.ModulesWrapper;
import org.apache.commons.lang.SystemUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.velocity.VelocityContext;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

@Mojo( name = "build", defaultPhase = LifecyclePhase.GENERATE_RESOURCES )
public class BuilderMojo extends AbstractMojo {

    @Parameter( defaultValue = "${project.build.directory}/modules-build", property = "outputDirectory" )
    private File outputDirectory;

    @Parameter( defaultValue = "${project.build.directory}/work", property = "workDirectory" )
    private File workDirectory;

    @Parameter( property = "mvnExecutable" )
    private File mvnExecutable;

    @Parameter( defaultValue = "true", property = "generateLayersConf" )
    private boolean generateLayersConf;

    @Parameter(property = "modulesYamlFile", required = true )
    private File modulesYamlFile;

    @Parameter(property = "parameters" )
    private Map<String, String> parameters;

    @Parameter( defaultValue = "urn:jboss:module:1.5", property = "moduleDefaultNamespace" )
    private String moduleDefaultNamespace;

    private ModulesParser parser;

    @Override
    public void execute() {
        try {
            init();
            runtimeExecute();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public void init() {
        parser = new ModulesParser();
    }

    public void runtimeExecute() throws Exception {
        System.out.println("moduleDefaultNamespace" + moduleDefaultNamespace);
        ModulesWrapper wrapper = parser.parseModule(modulesYamlFile, parameters);
        Validator.validateData(wrapper);
        File modulesDirectory = new File(workDirectory.getAbsolutePath() + "/modules/");
        File layersDirectory = new File(modulesDirectory.getAbsolutePath() + "/system/layers/");

        for (Layer layer: wrapper.getLayers()) {
            for (Module module: layer.getModules()) {
                VelocityContext context = new VelocityContext();
                String moduleDirPath = layersDirectory + "/" + layer.getName() + "/" + module.getName().replace(".", "/") + "/main/";

                context.put("modulePath", moduleDirPath);
                context.put("module", module);
                context.put("moduleDefaultNamespace", moduleDefaultNamespace);

                String pomFilePath = workDirectory.getAbsolutePath() + "/pom-" + module.getName() + ".xml";

                BuilderUtils.buildTemplate("templates/pom.template.vm", context, pomFilePath, true);
                BuilderUtils.buildTemplate("templates/module.template.vm", context,  moduleDirPath + "/module.xml", true);
                buildPom(pomFilePath);
            }
        }

        if (generateLayersConf) {
            generateLayersConf(wrapper.getLayers());
        }

        BuilderUtils.copyDir(modulesDirectory.toPath(), new File(outputDirectory.getAbsolutePath() + "/jboss/modules").toPath());
    }

    private void generateLayersConf(List<Layer> layers) throws Exception {
        VelocityContext context = new VelocityContext();
        context.put("layers", layers);
        BuilderUtils.buildTemplate("templates/layers.template.vm", context, outputDirectory.getAbsolutePath() + "/jboss/modules/layers.conf", false);
    }

    private void buildPom(String pomFilePath) throws IOException {
        String mvnCommand = "mvn";

        if (mvnExecutable != null && mvnExecutable.exists()) {
            mvnCommand = mvnExecutable.getAbsolutePath();
        } else if (SystemUtils.IS_OS_WINDOWS) {
            mvnCommand = "mvn.cmd";
        }

        ProcessBuilder builder = new ProcessBuilder(
                mvnCommand, "clean", "install", "-f", pomFilePath);
        Process p = builder.start();
        BufferedReader infoStreamReader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        boolean throwError = false;

        while (true) {
            String line = infoStreamReader.readLine();
            if (line == null) { break; }
            if (line.contains("BUILD FAILURE")) {
                System.err.println(line);
                throwError = true;
            } else {
                System.out.println(line);
            }
        }


        BufferedReader errorStreamReader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
        while (true) {
            String line = errorStreamReader.readLine();
            if (line == null) { break; }
            System.err.println(line);
            throwError = true;
        }

        if (throwError) {
            throw new RuntimeException("Pom [" + pomFilePath + "] could not be built");
        }
    }
}
