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
        if (!(args.length != 1)) {
            System.err.println("usage: ./mallow <filepath>");
        } else {
            String source = loadFile("examples/stage3.mallow");
            Parser parser = new Parser(source);
            Ast program = parser.parseProgram();
            Compiler compiler = new Compiler();
            compiler.Compile(program);
            Runtime VM = new Runtime(compiler.bytecode());
            VM.run();
        }
    }
}