package com.jboss.builder.dto;

public class Artifact {
    public String artifactId;
    public String groupId;
    public String version;
    public String packaging;

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getPackaging() {
        return packaging;
    }

    public void setPackaging(String packaging) {
        this.packaging = packaging;
    }

    public String toString() {
        return "Artifact{" +
                "artifactId='" + artifactId + '\'' +
                ", groupId='" + groupId + '\'' +
                ", version='" + version + '\'' +
                ", packaging='" + packaging + '\'' +
                '}';
    }
}