package ispd.utils;

import java.util.function.Consumer;

public class Functions {
    public static Runnable doNothing() {
        return () -> {
        };
    }

    public static <T> Consumer<T> ignoreParameter() {
        return (t) -> {
        };
    }
}
