package com.jboss.builder.dto;

import java.util.ArrayList;
import java.util.List;

public class ModulesWrapper {
    public List<Layer> layers = new ArrayList<>();

    public List<Layer> getLayers() {
        return layers;
    }

    public void setLayers(List<Layer> layers) {
        this.layers = layers;
    }

    public String toString() {
        return "ModulesFile{" +
                "layers=" + layers +
                '}';
    }
}
