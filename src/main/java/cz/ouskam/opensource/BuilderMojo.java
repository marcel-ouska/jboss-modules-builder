package cz.ouskam.opensource;

import cz.ouskam.opensource.dto.Layer;
import cz.ouskam.opensource.dto.Module;
import cz.ouskam.opensource.dto.ModulesWrapper;
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

import static cz.ouskam.opensource.ModulesParser.parseModule;

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

    public void execute() {
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

        for (Layer layer: wrapper.layers) {
            for (Module module: layer.modules) {
                VelocityContext context = new VelocityContext();
                String moduleDirPath = layersDirectory + "/" + layer.name + "/" + module.name.replace(".", "/") + "/main/";

                context.put("modulePath", moduleDirPath);
                context.put("artifacts", module.artifacts);
                context.put("dependencies", module.dependencies);
                context.put("module", module);

                String pomFilePath = workDirectory.getAbsolutePath() + "/pom-" + module.name + ".xml";

                BuilderUtils.buildTemplate("templates/pom.template.vm", context, pomFilePath);
                BuilderUtils.buildTemplate("templates/module.template.vm", context,  moduleDirPath + "/module.xml");
                buildPom(pomFilePath);
            }
        }

        if (generateLayersConf) {
            generateLayersConf(wrapper.layers);
        }

        BuilderUtils.copyDir(modulesDirectory.toPath(), new File(outputDirectory.getAbsolutePath() + "/jboss/modules").toPath());
    }

    private void generateLayersConf(List<Layer> layers) throws IOException {
        VelocityContext context = new VelocityContext();
        context.put("layers", layers);
        BuilderUtils.buildTemplate("templates/layers.template.vm", context, outputDirectory.getAbsolutePath() + "/jboss/modules/layers.conf");
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
