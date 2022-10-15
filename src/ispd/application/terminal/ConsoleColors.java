package ispd.application.terminal;

public enum ConsoleColors {
    RESET("0"),

    BLACK("0;30"),
    RED("0;31"),
    GREEN("0;32"),
    YELLOW("0;33"),
    BLUE("0;34"),
    PURPLE("0;35"),
    CYAN("0;36"),
    WHITE("0;37"),


    BLACK_BOLD("1;30"),
    RED_BOLD("1;31"),
    GREEN_BOLD("1;32"),
    YELLOW_BOLD("1;33"),
    BLUE_BOLD("1;34"),
    PURPLE_BOLD("1;35"),
    CYAN_BOLD("1;36"),
    WHITE_BOLD("1;37"),


    BLACK_UNDERLINED("4;30"),
    RED_UNDERLINED("4;31"),
    GREEN_UNDERLINED("4;32"),
    YELLOW_UNDERLINED("4;33"),
    BLUE_UNDERLINED("4;34"),
    PURPLE_UNDERLINED("4;35"),
    CYAN_UNDERLINED("4;36"),
    WHITE_UNDERLINED("4;37"),


    BLACK_BACKGROUND("40"),
    RED_BACKGROUND("41"),
    GREEN_BACKGROUND("42"),
    YELLOW_BACKGROUND("43"),
    BLUE_BACKGROUND("44"),
    PURPLE_BACKGROUND("45"),
    CYAN_BACKGROUND("46"),
    WHITE_BACKGROUND("47"),


    BLACK_BRIGHT("0;90"),
    RED_BRIGHT("0;91"),
    GREEN_BRIGHT("0;92"),
    YELLOW_BRIGHT("0;93"),
    BLUE_BRIGHT("0;94"),
    PURPLE_BRIGHT("0;95"),
    CYAN_BRIGHT("0;96"),
    WHITE_BRIGHT("0;97"),


    BLACK_BOLD_BRIGHT("1;90"),
    RED_BOLD_BRIGHT("1;91"),
    GREEN_BOLD_BRIGHT("1;92"),
    YELLOW_BOLD_BRIGHT("1;93"),
    BLUE_BOLD_BRIGHT("1;94"),
    PURPLE_BOLD_BRIGHT("1;95"),
    CYAN_BOLD_BRIGHT("1;96"),
    WHITE_BOLD_BRIGHT("1;97"),

    BLACK_BACKGROUND_BRIGHT("0;100"),
    RED_BACKGROUND_BRIGHT("0;101"),
    GREEN_BACKGROUND_BRIGHT("0;102"),
    YELLOW_BACKGROUND_BRIGHT("0;103"),
    BLUE_BACKGROUND_BRIGHT("0;104"),
    PURPLE_BACKGROUND_BRIGHT("0;105"),
    CYAN_BACKGROUND_BRIGHT("0;106"),
    WHITE_BACKGROUND_BRIGHT("0;107");

    private final String color;
    ConsoleColors (final String color) {
        this.color = "\033[%sm".formatted(color);
    }

    public String getColor() {
        return this.color;
    }

    @Override
    public String toString() {
        return this.color;
    }
}