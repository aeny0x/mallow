package com.github.mallowc;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    private static String loadFile(String filepath) {
        StringBuilder output = new StringBuilder(new StringBuilder());
        try {
            File f = new File(filepath);
            Scanner s = new Scanner(f);
            while (s.hasNextLine()) {
                output.append(s.nextLine()).append('\n');
            }
        } catch (FileNotFoundException e) {
            System.out.printf("cannot find %s: No such file or directory", filepath);
        }
        return output.toString();
    }


    public static void main(String[] args) {
        String source = loadFile("examples/script.mallow");
        Lexer scanner = new Lexer(source);
        while (true) {
            Token t = scanner.nextToken();
            if (t.type == TokenType.EOF) break;
            System.out.println(t.type + " " + t.literal);
        }
    }
}