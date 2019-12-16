package cz.ouskam.opensource.dto;

import lombok.Data;

import java.util.Map;

@Data
public class Artifact {
    private String artifactId;
    private String groupId;
    private String version;
    private String packaging;
    private String classifier;
    private Map<String, String> attributes;
}