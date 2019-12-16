package cz.ouskam.opensource;

import cz.ouskam.opensource.dto.*;

import java.util.Map;

public class Validator {

    public static void validateData(ModulesWrapper wrapper) {
        if (BuilderUtils.isEmpty(wrapper.getLayers())) {
            throw new RuntimeException("There no layers");
        }

        for (Layer layer: wrapper.getLayers()) {
            if (BuilderUtils.isEmpty(layer.getName())) {
                throw new RuntimeException("The layer has no name");
            }

            for (Module module : layer.getModules()) {
                if (BuilderUtils.isEmpty(module.getName())) {
                    throw new RuntimeException("The module in layer [" + layer.getName() + "] has no name");
                }

                for (Artifact artifact: module.getArtifacts()) {
                    if (BuilderUtils.isEmpty(artifact.getArtifactId())) {
                        throw new RuntimeException("The artifact in layer [" + layer.getName() + "/" + module.getName() + "] has no artifactId");
                    }
                    if (BuilderUtils.isEmpty(artifact.getGroupId())) {
                        throw new RuntimeException("The artifact in layer [" + layer.getName() + "/" + module.getName() + "] has no groupId");
                    }
                    if (BuilderUtils.isEmpty(artifact.getVersion())) {
                        throw new RuntimeException("The artifact in layer [" + layer.getName() + "/" + module.getName() + "] has no version");
                    }
                }

                for (Dependency dependency: module.getDependencies()) {
                    if (BuilderUtils.isEmpty(dependency.getName())) {
                        throw new RuntimeException("The dependency in layer [" + layer.getName() + "/" + module.getName() + "] has no name");
                    }
                }
            }
        }
    }


}
