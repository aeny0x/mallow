package com.github.mallowc;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

public class Lexer {
    String input;
    int position = 0;
    int readPosition = 0;
    char ch = 0;
    private Map<String, TokenType> keywords = new Hashtable<>();

    private void setKeywordTable() {
        keywords.put("define", TokenType.DEFINE);
        keywords.put("as", TokenType.AS);
        // keywords.put("match", TokenType.MATCH);
        keywords.put("end", TokenType.END);
        keywords.put("puts", TokenType.PUTS);
        keywords.put("true", TokenType.TRUE);
        keywords.put("false", TokenType.FALSE);
        keywords.put("nil", TokenType.NIL);
        keywords.put("not", TokenType.NOT);
        keywords.put("if", TokenType.IF);
        keywords.put("then", TokenType.THEN);
        keywords.put("else", TokenType.ELSE);
        keywords.put("or", TokenType.OR);
        keywords.put("and", TokenType.AND);
        keywords.put("mod", TokenType.MODULO);
        keywords.put("lambda", TokenType.LAMBDA);
    }

    private TokenType lookUpIdentifier(String s) {
        TokenType t = keywords.get(s);
        if (t == null) {
            return TokenType.IDENTIFIER;
        } else {
            return t;
        }
    }

    public Lexer(String source) {
        input = source;
        setKeywordTable();
        advance();
    }

    private void advance() {
        if (readPosition >= input.length()) {
            ch = 0;
        } else {
            ch = input.charAt(readPosition);
        }
        position = readPosition;
        readPosition += 1;
    }

    private void skipWhiteSpace() {
        while (Character.isWhitespace(ch)) {
            advance();
        }
    }

    // Add in the future: string interpolation using ^ or +
    //                    f"{expr}"
    private String readString() {
        int pos = position + 1;
        while (true) {
            advance();
            if (ch == '"' || ch == 0) break;
        }
        return input.substring(pos,position);
    }

    private String readIdentifier() {
        int pos = position;
        while (Character.isLetter(ch) || ch == '?' || Character.isDigit(ch)) {
            advance();
        }
        return input.substring(pos,position);
    }

    // In the future numbers will be float64, but for now lets just work with integers.
    private String readNumber() {
        int pos = position;
        while (Character.isDigit(ch)) {
            advance();
        }
        return input.substring(pos,position);
    }

    private char peek() {
        if (readPosition >= input.length()) {
            return 0;
        } else {
            return input.charAt(readPosition);
        }
    }

    private boolean isAtEnd() {
        return peek() == 0;
    }

    public Token nextToken() {
        Token tok;
        skipWhiteSpace();
        switch (ch) {
            case '"':
                tok = new Token(TokenType.STRING, readString());
                break;
            case '+':
                tok = new Token(TokenType.PLUS, String.valueOf(ch));
                break;
            case '-':
                if (peek() == '>') {
                    tok = new Token(TokenType.ARROW, "->");
                } else {
                    tok = new Token(TokenType.MINUS, String.valueOf(ch));
                }
                break;

            case '.':
                tok = new Token(TokenType.DOT, String.valueOf(ch));
                break;

            case '/':
                tok = new Token(TokenType.SLASH, String.valueOf(ch));
                break;

            case '*':
                tok = new Token(TokenType.ASTERISK, String.valueOf(ch));
                break;

            case '|':
                tok = new Token(TokenType.PIPE, String.valueOf(ch));
                break;

            case '_':
                tok = new Token(TokenType.UNDERLINE, String.valueOf(ch));
                break;

            case '=':
                tok = new Token(TokenType.EQUAL, String.valueOf(ch));
                break;

            case '~':
                if (peek() == '=') {
                    advance();
                    tok = new Token(TokenType.NOT_EQUAL, "~=");
                } else {
                    tok = new Token(TokenType.ILLEGAL, String.valueOf(ch));
                }
                break;

            case '(':
                tok = new Token(TokenType.LPAREN, String.valueOf(ch));
                break;

            case ')':
                tok = new Token(TokenType.RPAREN, String.valueOf(ch));
                break;

            case '<':
                tok = new Token(TokenType.LT, String.valueOf(ch));
                break;

            case '>':
                tok = new Token(TokenType.GT, String.valueOf(ch));
                break;

            case 0:
                tok = new Token(TokenType.EOF, String.valueOf(ch));
                break;

            default:
                if (Character.isLetter(ch)) {
                    String literal = readIdentifier();
                    tok = new Token(lookUpIdentifier(literal), literal);
                    return tok;

                } else if (Character.isDigit(ch)) {
                    tok = new Token(TokenType.NUMBER, readNumber());
                    return tok;
                } else {
                    tok = new Token(TokenType.ILLEGAL, String.valueOf(ch));
                    break;
                }
        }

        advance();
        return tok;
    }

}
