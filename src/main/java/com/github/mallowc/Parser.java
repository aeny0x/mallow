package com.github.mallowc;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

enum Precedence {
    LOWEST,
    EQUALS,
    LESS_GREATER,
    SUM,
    PRODUCT,
    PREFIX,
    APPLICATION
}

interface prefixParseFunction {
    Expression apply();
}

interface infixParseFunction {
    Expression apply(Expression expr);
}

public class Parser {
    Lexer scanner;
    Token current;
    Token peek;
    ArrayList<String> errors;
    Map<TokenType, prefixParseFunction> prefixParseFunctions;
    Map<TokenType, infixParseFunction> infixParseFunctions;
    Map<TokenType, Precedence> precedences;

    public Parser(String source_code) {
        /* Initialization */
        scanner = new Lexer(source_code);
        errors = new ArrayList<String>();
        prefixParseFunctions = new Hashtable<TokenType, prefixParseFunction>();
        infixParseFunctions = new Hashtable<TokenType, infixParseFunction>();
        precedences = new Hashtable<TokenType, Precedence>();
        setPrecedenceTable();

        /* BEGIN PARSE FUNCTIONS */
        registerPrefix(TokenType.IDENTIFIER, this::parseIdentifier);
        registerPrefix(TokenType.NUMBER, this::parseInteger);
        registerPrefix(TokenType.STRING, this::parseString);
        registerPrefix(TokenType.TRUE, this::parseBoolean);
        registerPrefix(TokenType.FALSE, this::parseBoolean);
        registerPrefix(TokenType.NIL, this::parseNil);
        registerPrefix(TokenType.NOT, this::parsePrefix);
        registerPrefix(TokenType.MINUS, this::parsePrefix);
        registerPrefix(TokenType.LPAREN, this::parseGrouped);
        registerPrefix(TokenType.IF, this::parseIf);
        registerPrefix(TokenType.LAMBDA, this::parseFunction);
        registerPrefix(TokenType.PAIR, this::parsePair);
        registerPrefix(TokenType.CAR, this::parseCar);
        registerPrefix(TokenType.CDR, this::parseCdr);

        registerInfix(TokenType.PLUS, this::parseInfix);
        registerInfix(TokenType.MINUS, this::parseInfix);
        registerInfix(TokenType.ASTERISK, this::parseInfix);
        registerInfix(TokenType.SLASH, this::parseInfix);
        registerInfix(TokenType.EQUAL, this::parseInfix);
        registerInfix(TokenType.NOT_EQUAL, this::parseInfix);
        registerInfix(TokenType.GT, this::parseInfix);
        registerInfix(TokenType.LT, this::parseInfix);
        registerInfix(TokenType.MODULO, this::parseInfix);
        registerInfix(TokenType.PIPE, this::parseFunctionCall);


        /* END PARSE FUNCTIONS   */

        /* SET CURRENT AND PEEK */
        advance();
        advance();
    }

    private Expression parseCdr() {
        CdrExpr expr = new CdrExpr(current);
        advance();
        expr.list = parseExpression(Precedence.LOWEST);
        return expr;
    }

    private Expression parseCar() {
        CarExpr expr = new CarExpr(current);
        advance();
        expr.list = parseExpression(Precedence.LOWEST);
        return expr;
    }

    private Expression parseNil() {
        return new NilLiteral(current);
    }

    private Expression parseFunctionCall(Expression function) {
        FunctionCall expr = new FunctionCall(current, function);
        advance();
        expr.argument = parseExpression(Precedence.LOWEST);
        return expr;
    }

    private Expression parseString() {
        return new StringLiteral(current, current.literal);
    }

    private void advance() {
        current = peek;
        peek = scanner.nextToken();
    }

    /* BEGIN HELPER FUNCTIONS */

    private boolean currentIs(TokenType t) {
        return current.type == t;
    }

    private boolean peekIs(TokenType t) {
        return peek.type == t;
    }

    private boolean peek(TokenType t) {
        if (peekIs(t)) {
            advance();
            return true;
        } else {
            peekError(t);
            return false;
        }
    }

    private void peekError(TokenType t) {
        String msg = String.format("expected next token to be %s, got %s instead", t, peek.type);
        System.err.println(msg);
        errors.add(msg);
    }

    private void noPrefixError(TokenType t) {
        String msg = String.format("no prefix parse function for %s", t);
        System.err.println(msg);
        errors.add(msg);
    }

    private void registerPrefix(TokenType t, prefixParseFunction fn) {
        prefixParseFunctions.put(t,fn);
    }

    private void registerInfix(TokenType t, infixParseFunction fn) {
        infixParseFunctions.put(t,fn);
    }

    private void setPrecedenceTable() {
        // ARITHMETIC PRECEDENCE TABLE
        precedences.put(TokenType.EQUAL, Precedence.EQUALS);
        precedences.put(TokenType.NOT_EQUAL, Precedence.EQUALS);
        precedences.put(TokenType.GT, Precedence.LESS_GREATER);
        precedences.put(TokenType.LT, Precedence.LESS_GREATER);
        precedences.put(TokenType.PLUS, Precedence.SUM);
        precedences.put(TokenType.MINUS, Precedence.SUM);
        precedences.put(TokenType.ASTERISK, Precedence.PRODUCT);
        precedences.put(TokenType.SLASH, Precedence.PRODUCT);
        precedences.put(TokenType.MODULO, Precedence.PRODUCT);
        precedences.put(TokenType.PIPE, Precedence.APPLICATION);
    }

    private Precedence peekPrecedence() {
        Precedence p = precedences.get(peek.type);
        if (p != null) {
            return p;
        }
        return Precedence.LOWEST;
    }

    private Precedence currentPrecedence() {
        Precedence p = precedences.get(current.type);
        if (p != null) {
            return p;
        }
        return Precedence.LOWEST;
    }
    /* END HELPER FUNCTIONS */

    /* BEGIN PARSE FUNCTIONS */
    private Statement parseStatement() {
        switch (current.type) {
            case DEFINE:
                return parseDefineStatement();
            case PUTS:
                return parsePuts();
            default:
                return parseExpressionStatement();
        }
    }

    private DefineStmt parseDefineStatement() {
        DefineStmt stmt = new DefineStmt(current);

        if (!peek(TokenType.IDENTIFIER)) {
            return null;
        }

        stmt.name = new Identifier(current, current.literal);

        if (!peek(TokenType.AS)) {
            return null;
        }

        advance();
        stmt.value = parseExpression(Precedence.LOWEST);

        if (peekIs(TokenType.END)) {
            advance();
        }

        return stmt;
    }

    private ExpressionStatement parseExpressionStatement() {
        ExpressionStatement stmt = new ExpressionStatement(current);
        stmt.expr = parseExpression(Precedence.LOWEST);


        return stmt;
    }

    private Expression parseExpression(Precedence p) {
        prefixParseFunction prefix = prefixParseFunctions.get(current.type);
        if (prefix == null) {
            noPrefixError(current.type);
            return null;
        }
        Expression leftExpr = prefix.apply();

        while ((p.ordinal() < peekPrecedence().ordinal())) {
            infixParseFunction infix = infixParseFunctions.get(peek.type);
            if (infix == null) {
                return leftExpr;
            }
            advance();
            leftExpr = infix.apply(leftExpr);
        }

        return leftExpr;
    }


    Expression parseIdentifier() {
        return new Identifier(current, current.literal);
    }

    Expression parseInteger() {
        return new IntegerLiteral(current, current.literal);
    }

    Expression parseBoolean() {
        return new BooleanLiteral(current, currentIs(TokenType.TRUE));
    }

    Expression parsePrefix() {
        PrefixExpression expr = new PrefixExpression(current, current.literal);
        advance();
        expr.right = parseExpression(Precedence.PREFIX);
        return expr;
    }

    Expression parseInfix(Expression left) {
        InfixExpression expr = new InfixExpression(current, current.literal, left);
        Precedence p = currentPrecedence();
        advance();
        expr.right = parseExpression(p);
        return expr;

    }

    Expression parseGrouped() {
        advance();
        Expression expr = parseExpression(Precedence.LOWEST);
        if (!peek(TokenType.RPAREN)) {
            return null;
        }
        return expr;
    }

    Expression parseIf() {
        IfExpression expr = new IfExpression(current);

        advance();
        expr.conditional = parseExpression(Precedence.LOWEST);

        if (!peek(TokenType.THEN)) {
            return null;
        }

        advance();
        expr.consequence = parseExpression(Precedence.LOWEST);

        if (peekIs(TokenType.ELSE)) {
            advance();
            advance();
            expr.alternative = parseExpression(Precedence.LOWEST);
        }
        return expr;

    }

    Expression parseFunction() {
        // lambda <formal parameter> . <expression>
        FunctionLiteral literal = new FunctionLiteral(current);

        if (!peek(TokenType.IDENTIFIER)) {
            return null;
        }

        literal.parameter = new Identifier(current, current.literal);

        if (!peek(TokenType.DOT)) {
            return null;
        }

        advance();
        literal.body = parseExpression(Precedence.LOWEST);
        return literal;

    }

    PutsStmt parsePuts() {
        PutsStmt stmt = new PutsStmt(current);
        advance();
        stmt.value = parseExpression(Precedence.LOWEST);
        return stmt;
    }

    PairExpr parsePair() {
        PairExpr expr = new PairExpr(current);
        advance();
        expr.left = parseExpression(Precedence.LOWEST);
        advance();
        expr.right = parseExpression(Precedence.LOWEST);
        return expr;
    }



    /* END PARSE FUNCTIONS */

    public Ast parseProgram() {
        Ast program = new Ast();
        while (current.type != TokenType.EOF) {
            Statement stmt = parseStatement();
            if (stmt != null) {
                program.statements.add(stmt);
            }
            advance();
        }
        return program;
    }

}
