package ispd;

import ispd.application.GuiApplication;
import ispd.application.terminal.TerminalApplication;

import java.util.Locale;

public class Main {
    private static final Locale EN_US_LOCALE = new Locale("en", "US");

    public static void main(final String[] args) {
        Main.setDefaultLocale();

        final var app = (args.length == 0)
                ? new GuiApplication()
                : new TerminalApplication(args);

        app.run();
    }

    private static void setDefaultLocale() {
        Locale.setDefault(Main.EN_US_LOCALE);
    }
}