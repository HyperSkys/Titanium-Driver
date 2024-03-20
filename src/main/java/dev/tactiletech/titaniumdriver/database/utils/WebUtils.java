package dev.tactiletech.titaniumdriver.database.utils;

public class WebUtils {
    public static String getBytes(String input) {
        StringBuilder binary = new StringBuilder();
        for (char c : input.toCharArray()) {
            int val = c;
            for (int i = 0; i < 8; i++) {
                binary.append((val & 128) == 0 ? 0 : 1);
                val <<= 1;
            }
        }
        return binary.toString();
    }
}
