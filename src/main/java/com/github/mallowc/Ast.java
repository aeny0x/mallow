package com.github.mallowc;

import java.math.BigDecimal;
import java.util.ArrayList;

interface Node {
    String tokenLiteral();
    String string();
}

interface Statement extends Node {
    Node no = null;
    void statementNode();
    String tokenLiteral();

    String string();
}

interface Expression extends Node {
    Node no = null;
    void expressionNode();
    String string();
}

class Identifier implements Expression {
    Token token;
    String value;

    public Identifier(Token current, String literal) {
        token = current;
        value = literal;
    }

    @Override
    public void expressionNode() {

    }

    @Override
    public String string() {
        return value;
    }

    public String tokenLiteral() {
        return token.literal;
    }

}

class IntegerLiteral implements Expression {
    Token token;
    BigDecimal value;

    public IntegerLiteral(Token current, String literal) {
        token = current;
        value = BigDecimal.valueOf(Long.parseLong(literal));
    }

    @Override
    public void expressionNode() {

    }

    @Override
    public String string() {
        return value.toString();
    }

    public String tokenLiteral() {
        return token.literal;
    }

}

class BooleanLiteral implements Expression {
    Token token;
    boolean value;

    public BooleanLiteral(Token current, boolean b) {
        token = current;
        value = b;
    }

    @Override
    public void expressionNode() {

    }

    @Override
    public String string() {
        return token.literal;
    }

    public String tokenLiteral() {
        return token.literal;
    }

}

class PrefixExpression implements Expression {
    Token token;
    String operator;
    Expression right;

    public PrefixExpression(Token current, String literal) {
        token = current;
        operator = literal;
    }

    @Override
    public void expressionNode() {

    }

    public String tokenLiteral() {
        return token.literal;
    }

    @Override
    public String string() {
        StringBuilder out = new StringBuilder();
        out.append("(");
        out.append(operator);
        out.append(" ");
        out.append(right.string());
        out.append(")");
        return out.toString();
    }
}

class InfixExpression implements Expression {
    Token token;
    Expression left;
    String operator;
    Expression right;

    public InfixExpression(Token current, String literal, Expression leftExpr) {
        token = current;
        operator= literal;
        left = leftExpr;
    }

    @Override
    public void expressionNode() {

    }

    public String tokenLiteral() {
        return token.literal;
    }

    @Override
    public String string() {
        StringBuilder out = new StringBuilder();
        out.append("(");
        out.append(left.string());
        out.append(" ").append(operator).append(" ");
        out.append(right.string());
        out.append(")");
        return out.toString();
    }
}



class DefineStmt implements Statement {
    Token token;
    Identifier name;
    Expression value;

    public DefineStmt(Token current) {
        token = current;
    }

    @Override
    public void statementNode() {

    }

    @Override
    public String tokenLiteral() {
        return token.literal;
    }

    public String string() {
        StringBuilder out  = new StringBuilder();
        out.append(tokenLiteral());
        out.append(" ");
        out.append(name.string());
        out.append(" as ");
        if (value != null) {
            out.append(value.string());
        }
        return out.toString();
    }
}

class ExpressionStatement implements Statement {
    Token token;
    Expression expr;

    public ExpressionStatement(Token current) {
        token = current;
    }

    @Override
    public void statementNode() {

    }

    public String tokenLiteral() {
        return token.literal;
    }

    @Override
    public String string() {
        if (expr != null) {
            return expr.string();
        }
        return "";
    }
}

class IfExpression implements Expression {
    Token token;
    Expression conditional;
    Expression consequence;
    Expression alternative;

    public IfExpression(Token current) {
        token = current;
    }

    @Override
    public void expressionNode() {

    }

    @Override
    public String string() {
        StringBuilder out = new StringBuilder();
        out.append("if ");
        out.append(conditional.string());
        out.append(" ");
        out.append(consequence.string());
        if (alternative != null) {
            out.append(" else ");
            out.append(alternative.string());
        }
        return out.toString();

    }

    public String tokenLiteral() {
        return token.literal;
    }
}

class FunctionLiteral implements Expression {
    Token token;
    Identifier parameter;
    Expression body;

    public FunctionLiteral(Token current) {
        token = current;
    }

    @Override
    public void expressionNode() {

    }

    public String tokenLiteral() {
        return token.literal;
    }

    @Override
    public String string() {
        StringBuilder out = new StringBuilder();
        out.append("(");
        out.append(parameter.string());
        out.append(")");
        out.append("(");
        out.append(body.string());
        out.append(")");
        return out.toString();
    }
}

public class Ast implements Node {
    ArrayList<Statement> statements;

    public Ast() {
        statements = new ArrayList<Statement>();
    }

    @Override
    public String tokenLiteral() {
        if (!statements.isEmpty()) {
            return statements.get(0).tokenLiteral();
        } else {
            return "";
        }
    }

    @Override
    public String string() {
        StringBuilder out = new StringBuilder();
        for (Statement s : statements) {
            out.append("(");
            out.append(s.string());
            out.append(")\n");
        }
        return out.toString();
    }
}
