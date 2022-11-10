package ispd.utils;

import java.util.Collection;
import java.util.Set;

public class ValidaValores {
    private static final Collection<String> JAVA_RESERVED_KEYWORDS = Set.of(
            "abstract", "assert",
            "boolean", "break", "byte",
            "case", "catch", "char", "class", "const", "continue",
            "default", "do", "double",
            "else", "enum", "extends",
            "false", "final", "finally", "float", "for",
            "goto",
            "if", "implements", "import", "instanceof", "int", "interface",
            "long",
            "native", "new", "null",
            "package", "private", "protected", "public",
            "return",
            "short", "static", "strictfp", "super", "switch", "synchronized",
            "this", "throw", "throws", "transient", "true", "try",
            "void", "volatile",
            "while"
    ); 

    public static boolean validaNomeClasse(final String name) {
        return name.matches("[a-zA-Z$_][a-zA-Z\\d$_]*")
               && !ValidaValores.JAVA_RESERVED_KEYWORDS.contains(name);
    }
}