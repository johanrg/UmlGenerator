package com.github.johanrg.umlgenerator;

import com.github.johanrg.parser.CommandLineParser;
import com.github.johanrg.parser.CommandLineParserException;

import java.lang.reflect.*;
import java.util.*;


/**
 * @author Johan Gustafsson
 * @since 6/111222/2016.
 */
public class Main {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: ");
            System.out.println("\t-depth  The level of inheritance to include 0..n");
            System.out.println("\t-classes <class 1> <class 2> ... <class n>");
            System.exit(0);
        }

        CommandLineParser clp = new CommandLineParser(args);
        int classDepth = 0;
        List<String> classList = null;
        try {
            classDepth = clp.getInteger("-depth", 1);
            classList = clp.getList("-classes", -1);
        } catch (CommandLineParserException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }

        final StringBuilder plantUmlScript = new StringBuilder();
        plantUmlScript.append("@startuml\n");

        Set<Class<?>> classes = new LinkedHashSet<>();
        for (String className : classList) {
            try {
                classes.add(Class.forName(className));
                getSuperClasses(Class.forName(className), classDepth, classes);
                getInterfaces(Class.forName(className), classDepth, classes);
            } catch (ClassNotFoundException e) {
                System.out.printf("Can not find class %s\n", className);
                System.exit(1);
            }
        }
        Set<String> relationWith = new LinkedHashSet<>();
        for (Class<?> cls : classes) {
            if (cls.isInterface()) {
                plantUmlScript.append("interface ");
                plantUmlScript.append(cls.getName()).append(" {\n");
            } else if (cls.isEnum()) {
                plantUmlScript.append("enum ");
                plantUmlScript.append(cls.getName()).append(" {\n");
            } else {
                plantUmlScript.append("class ");
                plantUmlScript.append(cls.getName()).append(" {\n");
            }

            if (classDepth > 0) {
                Class superClass = cls.getSuperclass();
                if (superClass != null && classes.contains(superClass)) {
                    if (!superClass.getName().equals("java.lang.Object") && !superClass.getName().equals("java.lang.Enum")) {
                        relationWith.add(String.format("%s <|-- %s", superClass.getName(), cls.getName()));
                    }
                }

                for (Class<?> iface : cls.getInterfaces()) {
                    relationWith.add(String.format("%s <|.. %s", iface.getName(), cls.getName()));
                }
            }

            for (Field field : cls.getDeclaredFields()) {
                for (Class<?> c : classes) {
                    if (c.getName().equals(field.getType().getName())) {
                        relationWith.add(String.format("%s *-- %s", c.getName(), cls.getName()));
                    }
                }
                plantUmlScript.append("\t").append(modifiersToString(field.getModifiers()));
                plantUmlScript.append(field.getName()).append(" ").append(field.getType().getName().replace("[L", "")).append("\n");
            }
            for (Method method : cls.getDeclaredMethods()) {
                plantUmlScript.append("\t").append(modifiersToString(method.getModifiers()));
                plantUmlScript.append(method.getName()).append("(");
                List<String> types = new ArrayList<>();
                for (Parameter param : method.getParameters()) {

                    types.add(param.getType().getName().replace("[L", ""));
                }
                plantUmlScript.append(String.join(", ", types)).append(")\n");
            }
            plantUmlScript.append("}\n");

        }

        plantUmlScript.append(String.join("\n", relationWith)).append("\n");
        plantUmlScript.append("@enduml\n");

        System.out.println(plantUmlScript);
    }

    private static void getSuperClasses(Class<?> cls, int depth, Set<Class<?>> list) {
        if (depth == 0 || cls == null) return;

        if (cls.getSuperclass() != null) {
            String name = cls.getSuperclass().getName();
            if (!name.equals("java.lang.Object") && !name.equals("java.lang.Enum")) {
                list.add(cls.getSuperclass());
                getSuperClasses(cls.getSuperclass(), --depth, list);
            }
        }
    }

    private static void getInterfaces(Class<?> cls, int depth, Set<Class<?>> list) {
        if (depth == 0 || cls == null) return;

        for (Class<?> c : cls.getInterfaces()) {
            list.add(c);
            getInterfaces(c, --depth, list);
        }
    }

    private static String modifiersToString(int modifiers) {
        final List<String> modifierList = new ArrayList<>();

        if (Modifier.isPrivate(modifiers)) {
            modifierList.add("-");
        } else if (Modifier.isProtected(modifiers)) {
            modifierList.add("#");
        } else if (Modifier.isPublic(modifiers)) {
            modifierList.add("+");
        } else if (Modifier.isAbstract(modifiers)) {
            modifierList.add("{abstract}");
        } else if (Modifier.isStatic(modifiers)) {
            modifierList.add("{static}");
        }

        return String.join(" ", modifierList);
    }
}
