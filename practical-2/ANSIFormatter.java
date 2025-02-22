public class ANSIFormatter {

    private String esc;

    public ANSIFormatter() {
        esc = "\033[";
    }

    public String colourText(String text, String colourCode) {
        return esc + colourCode + "m" + text + esc + "0m";
    }

    public String clearScreen() {
        return esc + "2J";
    }

    public String moveCursor(int row, int col) {
        return esc + row + ";" + col + "H";
    }
}
