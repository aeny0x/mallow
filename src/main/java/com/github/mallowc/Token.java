package com.github.mallowc;

public class Token {
    String literal;
    TokenType type;

    public Token(TokenType t, String s) {
        literal = s;
        type = t;
    }
}
