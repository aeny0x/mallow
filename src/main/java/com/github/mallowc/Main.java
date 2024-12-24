package com.github.mallowc;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("usage: ./mallow <filepath>");
        } else {
            String source = read_file(args[0]);
            Preprocessor preprocessor = new Preprocessor(args[0]);
            source = preprocessor.include();
            Enviroment env = new Enviroment();
            Parser parser = new Parser(source);
            Ast code = parser.parseProgram();
            Evaluator evaluator = new Evaluator();
            MallowObject result = evaluator.eval(code, env);
        }
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
}