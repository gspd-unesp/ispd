package ispd.gui.utils;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for translation of UI text.
 * Starts with a bundle adequate to the system's default locale,
 * as acquired with {@link Locale}'s {@code getDefault} class method.
 */
class Translator {
    private static final ResourceBundle bundle =
            ResourceBundle.getBundle("ispd.idioma.Idioma", Locale.getDefault());

    /**
     * Translate the given text using the current instantiated {@link ResourceBundle}.
     * If translation is not found, logs a warning and returns original text.
     *
     * @param text Text to be translated.
     * @return Translated text, if available. Otherwise, original {@code text}.
     */
    public static String translate(final String text) {
        if (Translator.bundle.containsKey(text))
            return Translator.bundle.getString(text);
        Translator.logMissingTranslation(text);
        return text;
    }

    private static void logMissingTranslation(final String text) {
        Logger.getLogger(Translator.class.getName()).log(Level.WARNING,
                "Missing translation for text <%s>".formatted(text));
    }

    public static void setBundle(final ResourceBundle bundle) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}