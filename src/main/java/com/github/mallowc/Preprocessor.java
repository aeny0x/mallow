package com.github.mallowc;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Preprocessor {
    String source;
    public Preprocessor(String s) {
        source = read_file(s);
    }

    private static String read_file(String filepath) {
        StringBuilder output = new StringBuilder(new StringBuilder());
        try {
            File f = new File(filepath);
            Scanner s = new Scanner(f);
            while (s.hasNext()) {
                output.append(s.next());
                if (s.hasNextLine()) {
                    output.append("\n");
                }
            }
        } catch (FileNotFoundException e) {
            System.out.printf("cannot find %s: No such file or directory\n", filepath);
        }
        return output.toString();
    }
    public String include() {
        String includePattern = "include\n\"([^\"]+)\"";
        Pattern pattern = Pattern.compile(includePattern);
        Matcher matcher = pattern.matcher(source);

        // Regular expression to match block comments (/* ... */)
        String blockCommentPattern = "/\\*.*?\\*/";
        Pattern blockCommentPatternCompiled = Pattern.compile(blockCommentPattern, Pattern.DOTALL);  // DOTALL enables . to match newlines
        Matcher blockCommentMatcher = blockCommentPatternCompiled.matcher(source);

        // Remove all block comments
        source = blockCommentMatcher.replaceAll("");

        StringBuilder expandedCode = new StringBuilder();
        int lastMatchEnd = 0;

        while (matcher.find()) {
            expandedCode.append(source, lastMatchEnd, matcher.start());

            String libName = matcher.group(1);

            expandedCode.append(read_file("std/"+libName));

            // Update the position after the matched include
            lastMatchEnd = matcher.end();
        }

        expandedCode.append(source, lastMatchEnd, source.length());

        return expandedCode.toString();
    }
}
