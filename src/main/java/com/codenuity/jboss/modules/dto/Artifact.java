package com.codenuity.jboss.modules.dto;

import lombok.Data;

import java.util.Map;

@Data
public class Artifact {
    private String artifactId;
    private String groupId;
    private String version;
    private String packaging;
    private String classifier;
    private String destFileName;
    private Map<String, String> attributes;
}