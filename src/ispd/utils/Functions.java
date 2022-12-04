package ispd.utils;

import java.util.function.Consumer;

// TODO: Write documentation
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
