package com.codenuity.jboss.modules.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class Module {
    private String name;
    private String namespace;
    private List<Artifact> artifacts;
    private List<Dependency> dependencies;
    private Map<String, String> properties;
}