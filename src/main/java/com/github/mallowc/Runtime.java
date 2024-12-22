package com.github.mallowc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Stack;

class Frame {
    MallowCompiledFunction function;
    int IP;
    public Frame(MallowCompiledFunction fn) {
        function = fn;
        IP = - 1;
    }
    public ArrayList<Byte> instructions() {
        return function.instructions;
    }
}



public class Runtime {
    ArrayList<MallowObject> constant_pool;
    Stack<MallowObject> STACK;
    final MallowBoolean TRUE = new MallowBoolean(true);
    final MallowBoolean FALSE = new MallowBoolean(false);
    final MallowNil NIL = new MallowNil();

    ArrayList<MallowObject> GLOBALS;
    ArrayList<Frame> FRAMES;
    int frameIndex;

    public Runtime(Bytecode b) {
        STACK = new Stack<MallowObject>();
        constant_pool = b.constant_pool;
        GLOBALS = new ArrayList<MallowObject>();
        FRAMES = new ArrayList<Frame>();
        MallowCompiledFunction mainFn = new MallowCompiledFunction(b.instructions);
        Frame MAIN_FRAME = new Frame(mainFn);
        FRAMES.add(MAIN_FRAME);
        frameIndex = 1;
    }

    private Frame currentFrame() {
        return FRAMES.get(frameIndex - 1);
    }

    private void pushFrame(Frame f) {
        FRAMES.add(frameIndex, f);
        frameIndex++;
    }

    private Frame popFrame() {
        frameIndex--;
        return FRAMES.get(frameIndex);
    }


    private void executeComparison(Byte operation) {
        MallowObject right = STACK.pop();
        MallowObject left = STACK.pop();

        if (right instanceof MallowInteger && left instanceof MallowInteger) {
            executeIntegerComparison(operation, (MallowInteger) left, (MallowInteger) right);
        }

    }

    private void executeIntegerComparison(Byte operation, MallowInteger left, MallowInteger right) {
        BigDecimal leftValue = left.value;
        BigDecimal rightValue = right.value;
        switch (operation) {
            case 8:
                STACK.add(BoolToMallowBool(Objects.equals(rightValue, leftValue)));
                break;
            case 9:
                STACK.add(BoolToMallowBool(!Objects.equals(rightValue, leftValue)));
                break;
            case 10:
                STACK.add(BoolToMallowBool(leftValue.compareTo(rightValue) > 0));
                break;
            case 11:
                STACK.add(BoolToMallowBool(leftValue.compareTo(rightValue) < 0));
                break;
            default:
                System.err.printf("Unknown operator %d", operation);

        }
    }

    private MallowObject BoolToMallowBool(boolean e) {
        if (e) {
            return TRUE;
        }
        return FALSE;
    }

    public void run() {
        MallowInteger a, b ,c , result;
        int IP;
        ArrayList<Byte> instructions;
        byte operation;

        while (currentFrame().IP < currentFrame().instructions().size() - 1) {
            currentFrame().IP++;

            IP = currentFrame().IP;
            instructions = currentFrame().instructions();
            operation = instructions.get(IP);
            switch (operation) {
                case 0:
                    byte index = instructions.get(IP+1);
                    STACK.add(constant_pool.get(index));
                    currentFrame().IP += 1;
                    break;
                case 1:
                    a = (MallowInteger) STACK.pop();
                    b = (MallowInteger) STACK.pop();
                    result = new MallowInteger(a.value.add(b.value));
                    STACK.add(result);
                    break;
                case 2:
                    a = (MallowInteger) STACK.pop();
                    b = (MallowInteger) STACK.pop();
                    result = new MallowInteger(b.value.subtract(a.value));
                    STACK.add(result);
                    break;
                case 3:
                    a = (MallowInteger) STACK.pop();
                    b = (MallowInteger) STACK.pop();
                    result = new MallowInteger(a.value.multiply(b.value));
                    STACK.add(result);
                    break;
                case 4:
                    a = (MallowInteger) STACK.pop();
                    b = (MallowInteger) STACK.pop();
                    result = new MallowInteger(b.value.divide(a.value));
                    STACK.add(result);
                    break;
                case 5:
                    System.out.println(STACK.peek().string());
                    break;
                case 6:
                    STACK.add(TRUE);
                    break;
                case 7:
                    STACK.add(FALSE);
                    break;
                case 8:
                    executeComparison(operation);
                    break;
                case 9:
                    executeComparison(operation);
                    break;
                case 10:
                    executeComparison(operation);
                    break;
                case 11:
                    executeComparison(operation);
                    break;
                case 12:
                    executeNotOperator();
                    break;
                case 13:
                    executeNegateOperator();
                    break;
                case 14:
                    int offset = executeJump(IP + 1, IP + 2);
                    currentFrame().IP += 2;
                    if (!isTrue(STACK.pop())) {
                        IP = offset - 1;
                    }
                    break;
                case 15:
                    STACK.add(NIL);
                    break;
                case 16:
                    offset = executeJump(IP+1, IP + 2);
                    currentFrame().IP = offset - 1;
                    break;
                case 17:
                    a = (MallowInteger) STACK.pop();
                    b = (MallowInteger) STACK.pop();
                    result = new MallowInteger(b.value.remainder(a.value));
                    STACK.add(result);
                    break;
                case 18:
                    byte globalIndex = instructions.get(IP+1);
                    currentFrame().IP += 1;
                    GLOBALS.add(globalIndex, STACK.pop());
                    break;
                case 19:
                    globalIndex = instructions.get(IP+1);
                    currentFrame().IP += 1;
                    STACK.add(GLOBALS.get(globalIndex));
                    break;
                case 20:
                    MallowCompiledFunction fn = (MallowCompiledFunction) STACK.pop();
                    Frame f = new Frame(fn);
                    pushFrame(f);
                    break;
                case 21:
                    MallowObject returnVal = STACK.pop();
                    popFrame();
                    STACK.add(returnVal);
                    break;
                default:
                    System.err.println("opcode not implemented");

            }
        }
    }

    private boolean isTrue(MallowObject pop) {
        if (pop instanceof MallowBoolean) {
            return ((MallowBoolean) pop).value;
        } else {
            return true;
        }
    }

    private int executeJump(int f, int s) {
        byte first = currentFrame().instructions().get(f);
        byte second = currentFrame().instructions().get(s);
        return (byte) (first << 8 | second);
    }

    private void executeNegateOperator() {
        MallowInteger x = (MallowInteger) STACK.pop();
        MallowInteger result = new MallowInteger(x.value.negate());
        STACK.add(result);
    }

    private void executeNotOperator() {
        MallowBoolean operand = (MallowBoolean) STACK.pop();
        if (operand.equals(TRUE)) {
            STACK.add(FALSE);
        } else if (operand.equals(FALSE)) {
            STACK.add(TRUE);
        } else {
            STACK.add(FALSE);
        }
    }

}
