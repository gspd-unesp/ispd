package ispd.policy.managers.util;

import ispd.policy.managers.FilePolicyManager;

import javax.tools.Tool;
import javax.tools.ToolProvider;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.stream.Collectors;

public class CompilationHelper {
    private final Optional<Tool> compiler = Optional.ofNullable(
            ToolProvider.getSystemJavaCompiler());
    private final File target;

    /* package-private */
    public CompilationHelper(final File target) {
        this.target = target;
    }

    /* package-private */
    public String compile() {
        return this.compiler
                .map(this::compileWithSystemTool)
                .orElseGet(this::tryCompileWithJavac);
    }

    private String compileWithSystemTool(final Tool tool) {
        final var err = new ByteArrayOutputStream();
        final var arg = this.target.getPath();
        tool.run(null, null, err, arg);
        return err.toString();
    }

    private String tryCompileWithJavac() {
        try {
            return this.compileWithJavac();
        } catch (final IOException ex) {
            FilePolicyManager.severeLog(ex);
            return "Não foi possível compilar";
        }
    }

    private String compileWithJavac() throws IOException {
        final var command = "javac %s".formatted(this.target.getPath());
        final var process = Runtime.getRuntime().exec(command);

        try (final var err = new BufferedReader(new InputStreamReader(
                process.getErrorStream(), StandardCharsets.UTF_8
        ))) {
            return err.lines().collect(Collectors.joining("\n"));
        }
    }
}
