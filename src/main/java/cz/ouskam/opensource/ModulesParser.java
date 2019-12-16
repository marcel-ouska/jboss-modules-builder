package cz.ouskam.opensource;

import cz.ouskam.opensource.dto.ModulesWrapper;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ModulesParser {

    public static ModulesWrapper parseModule(File modulesFile, Map<String, String> parameters) throws IOException, IllegalAccessException {
        Yaml yaml = new Yaml();
        ModulesWrapper wrapper;
        try (InputStream in = new FileInputStream(modulesFile)) {
            wrapper = yaml.loadAs(in, ModulesWrapper.class);
        }

        Queue<InstanceFieldPair> fieldsToResolve = new LinkedList<>();
        fieldsToResolve.addAll(
                Arrays.asList(wrapper.getClass().getFields())
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
                field.set(instance, resolveString((String) value, parameters));
            } else if (value instanceof Collection) {
                Collection collection = (Collection) value;
                for (Object item: collection) {
                    fieldsToResolve.addAll(
                            Arrays.asList(item.getClass().getFields())
                                    .stream()
                                    .map(obj -> new InstanceFieldPair(item, obj))
                                    .collect(Collectors.toList())
                    );
                }
            }
        }

        return wrapper;
    }

    public static String resolveString(String string, Map<String, String> parameters) {
        String resultString = string;
        Pattern p = Pattern.compile("\\$\\{([0-9a-zA-Z^.-]+)\\}");
        Matcher m = p.matcher(string);
        while (m.find()) {
            if (parameters.containsKey(m.group(1))) {
                resultString = resultString.replace(m.group(0) , parameters.get(m.group(1)));
            }
        }

        return resultString;
    }

}

class InstanceFieldPair {
    Object instance;
    Field field;

    public InstanceFieldPair(Object instance, Field field) {
        this.instance = instance;
        this.field = field;
    }
}