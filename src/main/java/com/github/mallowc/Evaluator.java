package com.github.mallowc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;
import java.util.Objects;

interface MallowObject {
    String Type();
    String Inspect();
}

class Enviroment {
    Map<String, MallowObject> store = new Hashtable<>();
    public Enviroment() {}
    Enviroment outer;



    public MallowObject get(String name) {
        MallowObject obj = store.get(name);

        if (obj == null && outer != null) {
            obj = outer.get(name);
        }

        return obj;
    }

    public MallowObject set(String name, MallowObject obj) {
        store.put(name, obj);
        return obj;
    }

    public Enviroment newEnclosedEnv(Enviroment out) {
        Enviroment env = new Enviroment();
        env.outer = out;
        return env;
    }

}

class MallowString implements MallowObject {
    String value;
    @Override
    public String Type() {
        return "string";
    }

    @Override
    public String Inspect() {
        return value;
    }

    public MallowString(String v) {
        value = v;
    }
}


class MallowInteger implements MallowObject {
    int value;

    public MallowInteger(int v) {
        value = v;
    }

    @Override
    public String Type() {
        return "integer";
    }

    @Override
    public String Inspect() {
        return String.valueOf(value);
    }
}

class MallowFunction implements MallowObject {
    Enviroment env;
    Expression body;
    Identifier parameter;

    public MallowFunction(Identifier param, Expression b, Enviroment e) {
        env = e;
        parameter = param;
        body = b;
    }

    @Override
    public String Type() {
        return "function";
    }

    @Override
    public String Inspect() {
        return "lambda x . " + body.string();
    }
}

class MallowPair implements MallowObject {
    MallowObject left;
    MallowObject right;

    public MallowPair(MallowObject l, MallowObject r) {
        left = l;
        right = r;
    }

    @Override
    public String Type() {
        return "";
    }

    @Override
    public String Inspect() {
        if (right instanceof MallowNil) {
            return left.Inspect();
        }
        return left.Inspect() + " " + right.Inspect();
    }
}

class MallowBoolean implements MallowObject {
    boolean value;

    public MallowBoolean(boolean v) {
        value = v;
    }

    @Override
    public String Type() {
        return "boolean";
    }

    @Override
    public String Inspect() {
        return String.valueOf(value);
    }
}

class MallowNil implements MallowObject {

    @Override
    public String Type() {
        return "nil";
    }

    @Override
    public String Inspect() {
        return "nil";
    }
}


public class Evaluator {
    final MallowBoolean TRUE = new MallowBoolean(true);
    final MallowBoolean FALSE = new MallowBoolean(false);
    final MallowNil NIL = new MallowNil();
    public Evaluator() {
    }

    public MallowObject eval(Object node, Enviroment env) {
        if (node instanceof Ast) {
            return evalStatement(((Ast) node).statements, env);
        } else if (node instanceof IntegerLiteral) {
            return new MallowInteger(((IntegerLiteral) node).value);
        } else if (node instanceof ExpressionStatement) {
            return eval(((ExpressionStatement) node).expr, env);
        } else if (node instanceof BooleanLiteral) {
            return nativeBooleanToMallowBoolean(((BooleanLiteral) node).value);
        } else if (node instanceof PrefixExpression) {
            MallowObject right = eval(((PrefixExpression) node).right, env);
            return evalPrefix(((PrefixExpression) node).operator, right);
        } else if (node instanceof InfixExpression) {
            MallowObject left = eval(((InfixExpression) node).left, env);
            MallowObject right = eval(((InfixExpression) node).right, env);
            return evalInfix(left, right, ((InfixExpression) node).operator);
        } else if (node instanceof IfExpression) {
            return evalIfExpression((IfExpression) node, env);
        } else if (node instanceof DefineStmt) {
            MallowObject val = eval(((DefineStmt) node).value, env);
            env.set(((DefineStmt) node).name.value, val);
        } else if (node instanceof Identifier) {
            return evalIdentifier((Identifier) node, env);
        } else if (node instanceof FunctionLiteral) {
            Identifier param = ((FunctionLiteral) node).parameter;
            Expression body = ((FunctionLiteral) node).body;
            return new MallowFunction(param, body, env);
        } else if (node instanceof FunctionCall) {
            MallowFunction fn = (MallowFunction) eval(((FunctionCall) node).function, env);
            Expression argument = ((FunctionCall) node).argument;
            MallowObject result = eval(argument, env);
            return applyFunc(fn, eval(argument, env));
        } else if (node instanceof PutsStmt) {
            MallowObject result = eval(((PutsStmt) node).value, env);
            if (result instanceof MallowPair) {
                System.out.print("[");
                System.out.print(result.Inspect());
                System.out.println("]");
            } else {
                System.out.println(result.Inspect());
            }

            return result;
        } else if (node instanceof StringLiteral) {
            return new MallowString(((StringLiteral) node).value.replace('\n', ' '));
        } else if (node instanceof PairExpr) {
            MallowObject left = eval(((PairExpr) node).left, env);
            MallowObject right = eval(((PairExpr) node).right, env);
            return new MallowPair(left, right);
        } else if (node instanceof NilLiteral) {
            return NIL;
        } else if (node instanceof CarExpr) {
            MallowPair list = (MallowPair) eval(((CarExpr) node).list, env);
            return list.left;
        } else if (node instanceof CdrExpr) {
            MallowPair list = (MallowPair) eval(((CdrExpr) node).list, env);
            return list.right;
        }


        return null;
    }

    private MallowObject applyFunc(MallowFunction fn, MallowObject argument) {
        Enviroment extendedEnv = extendFunctionEnv(fn, argument);
        MallowObject result = eval(fn.body, extendedEnv);
        return result;
    }

    private Enviroment extendFunctionEnv(MallowFunction fn, MallowObject argument) {
        Enviroment extend = fn.env.newEnclosedEnv(fn.env);
        extend.set(fn.parameter.value, argument);
        return extend;
    }

    private MallowObject evalIdentifier(Identifier node, Enviroment env) {
        MallowObject val = env.get(node.value);

        if (val == null) {
            System.err.printf("identifier not found %s\n", node.value);
            System.exit(1);
        }

        return val;
    }

    private MallowObject evalIfExpression(IfExpression node, Enviroment env) {
        MallowObject cond = eval(node.conditional, env);
        if (isTruthy(cond)) {
            return eval(node.consequence, env);
        } else if (node.alternative != null) {
            return eval(node.alternative, env);
        } else {
            return NIL;
        }
    }

    private boolean isTruthy(MallowObject cond) {
        if (cond == NIL) {
            return false;
        } else if (cond == TRUE) {
            return true;
        } else if (cond == FALSE) {
            return false;
        } else {
            return true;
        }
}

    private MallowObject evalInfix(MallowObject left, MallowObject right, String operator) {
        if (left instanceof MallowInteger && right instanceof MallowInteger) {
            return evalIntegerExpression(((MallowInteger) left).value, ((MallowInteger) right).value, operator);
        }

        if (left instanceof MallowBoolean && right instanceof MallowBoolean) {
            switch (operator) {
                case "=":
                    return nativeBooleanToMallowBoolean(((MallowBoolean) left).value == ((MallowBoolean) right).value);
                case "~=":
                    return nativeBooleanToMallowBoolean(((MallowBoolean) left).value != ((MallowBoolean) right).value);
            }
        }

        if (left instanceof MallowNil && right instanceof MallowNil) {
            switch (operator) {
                case "=":
                    return TRUE;
                case "~=":
                    return FALSE;
            }
        }

        return NIL;

    }

    private MallowObject evalIntegerExpression(int left, int right, String operator) {
        switch (operator) {
            case "+":
                return new MallowInteger(left + right);
            case "-":
                return new MallowInteger(left - right);
            case "*":
                return new MallowInteger(left * right);
            case "/":
                return new MallowInteger(left / right);
            case "mod":
                return new MallowInteger(left % right);
            case "<":
                return nativeBooleanToMallowBoolean(left < right);
            case ">":
                return nativeBooleanToMallowBoolean(left > right);
            case "=":
                return nativeBooleanToMallowBoolean(left == right);
            case "~=":
                return nativeBooleanToMallowBoolean(left != right);
            default:
                return NIL;
        }
    }

    private MallowObject evalPrefix(String operator, MallowObject right) {
        switch (operator) {
            case "not":
                return evalNotOperator(right);
            case "-":
                return evalNegateOperator(right);
            default:
                return NIL;
        }
    }

    private MallowObject evalNegateOperator(MallowObject right) {
        if (!(right instanceof MallowInteger)) {
            return NIL;
        }
        return new MallowInteger(-((MallowInteger) right).value);
    }

    private MallowObject evalNotOperator(MallowObject right) {
        if (right.equals(TRUE)) {
            return FALSE;
        } else if (right.equals(FALSE)) {
            return TRUE;
        } else if (right.equals(NIL)) {
            return TRUE;
        }
        return FALSE;
    }

    private MallowObject nativeBooleanToMallowBoolean(boolean value) {
        if (value) {
            return TRUE;
        }
        return FALSE;
    }

    private MallowObject evalStatement(ArrayList<Statement> statements, Enviroment env) {
        MallowObject obj = NIL;
        for (Statement s : statements) {
            obj = eval(s, env);
        }
        return obj;
    }

}
