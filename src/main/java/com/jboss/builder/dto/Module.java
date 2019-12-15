package com.jboss.builder.dto;

import java.util.ArrayList;
import java.util.List;

public class Module {
    public String name;
    public List<Artifact> artifacts = new ArrayList<>();
    public List<Dependency> dependencies = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Artifact> getArtifacts() {
        return artifacts;
    }

    public void setArtifacts(List<Artifact> artifacts) {
        this.artifacts = artifacts;
    }

    public List<Dependency> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<Dependency> dependencies) {
        this.dependencies = dependencies;
    }

    @Override
    public String toString() {
        return "Module{" +
                "name='" + name + '\'' +
                ", artifacts=" + artifacts +
                ", dependencies=" + dependencies +
                '}';
    }
}