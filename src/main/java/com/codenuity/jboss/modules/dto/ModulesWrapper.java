package com.codenuity.jboss.modules.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ModulesWrapper {
    private List<Layer> layers = new ArrayList<>();
}
