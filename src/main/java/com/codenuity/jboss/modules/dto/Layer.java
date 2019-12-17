package com.codenuity.jboss.modules.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Layer {
    private String name;
    private List<Module> modules = new ArrayList<>();
}