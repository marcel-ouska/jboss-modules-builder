package com.codenuity.jboss.modules;

import com.codenuity.jboss.modules.dto.ModulesWrapper;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public class ModulesParser {

    public ModulesWrapper parseModule(File modulesFile, Map<String, String> parameters) throws IOException, IllegalAccessException {
        Yaml yaml = new Yaml();
        ModulesWrapper wrapper;
        try (InputStream in = new FileInputStream(modulesFile)) {
            wrapper = yaml.loadAs(in, ModulesWrapper.class);
        }

        Queue<InstanceFieldPair> fieldsToResolve = new LinkedList<>();
        fieldsToResolve.addAll(
                Arrays.asList(wrapper.getClass().getDeclaredFields())
                        .stream()
                        .map(obj -> new InstanceFieldPair(wrapper, obj))
                        .collect(Collectors.toList())

        );

        while (!fieldsToResolve.isEmpty()) {
            InstanceFieldPair pair = fieldsToResolve.poll();
            Object instance = pair.instance;
            Field field = pair.field;

            field.setAccessible(true);
            Object value = field.get(instance);

            if (value instanceof String) {
                field.set(instance, BuilderUtils.resolveString((String) value, parameters));
            } else if (value instanceof Collection) {
                Collection collection = (Collection) value;
                for (Object item: collection) {
                    fieldsToResolve.addAll(
                            Arrays.asList(item.getClass().getDeclaredFields())
                                    .stream()
                                    .map(obj -> new InstanceFieldPair(item, obj))
                                    .collect(Collectors.toList())
                    );
                }
            }
        }

        return wrapper;
    }


    class InstanceFieldPair {
        Object instance;
        Field field;

        public InstanceFieldPair(Object instance, Field field) {
            this.instance = instance;
            this.field = field;
        }
    }

}