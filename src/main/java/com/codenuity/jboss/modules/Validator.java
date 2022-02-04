package com.codenuity.jboss.modules;

import com.codenuity.jboss.modules.dto.Artifact;
import com.codenuity.jboss.modules.dto.Dependency;
import com.codenuity.jboss.modules.dto.Layer;
import com.codenuity.jboss.modules.dto.Module;
import com.codenuity.jboss.modules.dto.ModulesWrapper;

public class Validator {

    public static void validateData(ModulesWrapper wrapper) {
        if (BuilderUtils.isEmpty(wrapper.getLayers())) {
            throw new RuntimeException("There no layers");
        }

        for (Layer layer: wrapper.getLayers()) {
            if (BuilderUtils.isEmpty(layer.getName())) {
                throw new RuntimeException("The layer has no name");
            }

            if (BuilderUtils.isEmpty(layer.getModules())) {
                for (Module module : layer.getModules()) {
                    if (BuilderUtils.isEmpty(module.getName())) {
                        throw new RuntimeException("The module in layer [" + layer.getName() + "] has no name");
                    }

                    if (BuilderUtils.isEmpty(module.getArtifacts())) {
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
                    }

                    if (BuilderUtils.isEmpty(module.getDependencies())) {
                        for (Dependency dependency: module.getDependencies()) {
                            if (BuilderUtils.isEmpty(dependency.getName())) {
                                throw new RuntimeException("The dependency in layer [" + layer.getName() + "/" + module.getName() + "] has no name");
                            }
                        }
                    }

                }
            }
        }
    }


}
