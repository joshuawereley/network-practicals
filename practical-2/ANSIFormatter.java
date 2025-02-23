public class ANSIFormatter {

    private static final String ESC = "\033[";

    public String colourText(String text, String colourCode) {
        return ESC + colourCode + "m" + text + ESC + "0m";
    }

    public String clearScreen() {
        return ESC + "2J";
    }

    public String moveCursor(int row, int col) {
        return ESC + row + ";" + col + "H";
    }

    public String boldText(String text) {
        return ESC + "1m" + text + ESC + "0m";
    }
}
