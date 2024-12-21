package com.github.mallowc;

import java.math.BigDecimal;
import java.util.ArrayList;

enum Opcode {
    CONSTANT,
    ADD, SUB, MUL, DIV,
    POP,
    TRUE, FALSE, EQUAL, NOT_EQUAL, GT, LT, NOT,
    NEGATE,JUMP_IF_NOT_TRUE, NIL, JUMP
}

interface MallowObject {
    String string();
}

class MallowInteger implements MallowObject {
    BigDecimal value;

    public MallowInteger(BigDecimal v) {
        value = v;
    }

    public String string() {
        return String.valueOf(value);
    }
}

class MallowBoolean implements MallowObject {
    boolean value;

    public MallowBoolean(boolean b) {
        value = b;
    }

    @Override
    public String string() {
        return String.valueOf(value);
    }
}

class MallowNil implements MallowObject {
    @Override
    public String string() {
        return "NIL";
    }
}


class Bytecode {
    ArrayList<Byte> instructions;
    ArrayList<MallowObject> constant_pool;

    public Bytecode(ArrayList<Byte> ins, ArrayList<MallowObject> constants) {
        instructions = ins;
        constant_pool = constants;
    }
}

public class Compiler {
    ArrayList<Byte> instructions;
    ArrayList<MallowObject> constant_pool;

    public Compiler() {
        constant_pool = new ArrayList<MallowObject>();
        instructions = new ArrayList<Byte>();
    }

    public Bytecode bytecode() {
        return new Bytecode(instructions, constant_pool);
    }

    private byte addConstant(MallowObject obj) {
        constant_pool.add(obj);
        return (byte) (constant_pool.size() - 1);
    }

    private void emitConstant(Opcode opcode, byte index) {
        instructions.add((byte) opcode.ordinal());
        instructions.add(index);
    }

    private void emit(Opcode opcode) {
        instructions.add((byte) opcode.ordinal());
    }

    public void Compile(Object node) {
        if (node instanceof Ast) {
            for (Statement s : ((Ast) node).statements) {
                Compile(s);
            }
        } else if (node instanceof ExpressionStatement) {
            Compile(((ExpressionStatement) node).expr);
            emit(Opcode.POP);
        } else if (node instanceof InfixExpression) {
            Compile(((InfixExpression) node).left);
            Compile(((InfixExpression) node).right);
            switch (((InfixExpression) node).operator) {
                case "+":
                    emit(Opcode.ADD);
                    break;
                case "-":
                    emit(Opcode.SUB);
                    break;
                case "*":
                    emit(Opcode.MUL);
                    break;
                case "/":
                    emit(Opcode.DIV);
                    break;
                case "=":
                    emit(Opcode.EQUAL);
                    break;
                case "~=":
                    emit(Opcode.NOT_EQUAL);
                    break;
                case ">":
                    emit(Opcode.GT);
                    break;
                case "<":
                    emit(Opcode.LT);
                    break;

                default:
                    System.err.printf("Unknown operator %s", ((InfixExpression) node).operator);
            }
        } else if  (node instanceof IntegerLiteral) {
            MallowInteger integer = new MallowInteger(((IntegerLiteral) node).value);
            emitConstant(Opcode.CONSTANT, addConstant(integer));
        } else if (node instanceof  BooleanLiteral) {
            if (((BooleanLiteral) node).value) {
                emit(Opcode.TRUE);
            } else {
                emit(Opcode.FALSE);
            }
        } else if (node instanceof PrefixExpression) {
            Compile(((PrefixExpression) node).right);
            switch (((PrefixExpression) node).operator) {
                case "not":
                    emit(Opcode.NOT);
                    break;
                case "-":
                    emit(Opcode.NEGATE);
                    break;
                default:
                    System.err.printf("Unknown operator %s", ((PrefixExpression) node).operator);
            }
        } else if (node instanceof IfExpression) {
            Compile(((IfExpression) node).conditional);
            int index = emitJump(Opcode.JUMP_IF_NOT_TRUE);
            Compile(((IfExpression) node).consequence);

            if (lastIsPop()) {
                removeLastPop();
            }

            int jumpPos = emitJump(Opcode.JUMP);

            int consequenceOffset = instructions.size();
            patch_jump(index, consequenceOffset);

            if (((IfExpression) node).alternative == null) {
                emit(Opcode.NIL);
            } else {
                Compile(((IfExpression) node).alternative);
                if (lastIsPop()) {
                    removeLastPop();
                }
            }

            int alternativeOffset = instructions.size();
            patch_jump(jumpPos, alternativeOffset);


        }
        System.err.println(instructions);
    }
    private int emitJump(Opcode opcode) {
        emit(opcode);
        instructions.add((byte) 0xff);
        instructions.add((byte) 0xff);
        return instructions.size() - 3;
    }

    private void patch_jump(int index, int offset) {
        byte first = (byte) ((offset & 0xff00) >> 8);
        byte second = (byte) (offset & 0x00ff);
        instructions.set(index + 1, (byte) first);
        instructions.set(index + 2, (byte) second);
    }

    private boolean lastIsPop() {
        byte instruction = instructions.get(instructions.size() - 1);
        if (instruction == 5) {
            return true;
        } else {
            return false;
        }
    }

    private void removeLastPop() {
        instructions.remove(instructions.size() - 1);
    }


}
