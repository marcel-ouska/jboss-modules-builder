package com.jboss.builder.dto;

import java.util.ArrayList;
import java.util.List;

public class Layer {
    public String name;
    public List<Module> modules = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Module> getModules() {
        return modules;
    }

    public void setModules(List<Module> modules) {
        this.modules = modules;
    }

    public String toString() {
        return "Layer{" +
                "name='" + name + '\'' +
                ", modules=" + modules +
                '}';
    }
}