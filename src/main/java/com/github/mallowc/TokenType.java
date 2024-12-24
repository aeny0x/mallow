package com.github.mallowc;

public enum TokenType {
    // Literals
    IDENTIFIER,
    STRING,
    NUMBER,

    // Keywords
    DEFINE, AS, END,
    LAMBDA,
    TRUE,
    FALSE,
    NIL,
    NOT,
    OR,
    AND,
    IF,
    THEN,
    ELSE,
    PUTS,
    MODULO,

    // Characters
    LPAREN, RPAREN,
    PLUS, MINUS, SLASH, ASTERISK,
    PIPE,
    EQUAL, NOT_EQUAL, LT, GT,
    PAIR,

    // Other
    ILLEGAL,
    EOF, DOT, SEMICOLON, CAR, CDR,
}
