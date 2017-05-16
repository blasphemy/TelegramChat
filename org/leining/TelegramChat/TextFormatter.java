package org.leining.TelegramChat;

public class TextFormatter {
    public static String stripMarkdown(String input) {
        String output;
        output = input.replace("*", "\\*");
        output = output.replace("_", "\\_");
        output = output.replace("`", "\\`");
        output = output.replace("[", "\\[");
        return output;
    }
}
