package cz.ouskam.opensource.dto;

import lombok.Data;

import java.util.Map;

@Data
public class Dependency {
    private String name;
    private Map<String, String> attributes;
}
