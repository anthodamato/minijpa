package org.minijpa.jpa.metamodel.generator;

import java.io.File;

public class PathUtils {
    public static String[] buildPaths(String classNamePath) {
        String[] sv = classNamePath.split("\\.");
        if (sv.length == 1) {
            String path = sv[0] + "_.java";
            String[] paths = { path, sv[0] + "_", "", sv[0] };
            return paths;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < sv.length - 1; ++i) {
            sb.append(sv[i]);
            sb.append(File.separator);
        }

        sb.append(sv[sv.length - 1]);
        sb.append("_.java");
        String path = sb.toString();
        String className = sv[sv.length - 1] + "_";
        String packagePath = classNamePath.substring(0, classNamePath.lastIndexOf('.'));
        String[] paths = { path, className, packagePath, sv[sv.length - 1] };
        return paths;
    }

}
