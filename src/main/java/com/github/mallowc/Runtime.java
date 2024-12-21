package com.github.mallowc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Stack;

public class Runtime {
    ArrayList<Byte> instructions;
    ArrayList<MallowObject> constant_pool;
    Stack<MallowObject> STACK;
    int IP = 0;
    final MallowBoolean TRUE = new MallowBoolean(true);
    final MallowBoolean FALSE = new MallowBoolean(false);
    final MallowNil NIL = new MallowNil();

    public Runtime(Bytecode b) {
        STACK = new Stack<MallowObject>();
        constant_pool = b.constant_pool;
        instructions = b.instructions;
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

        for (int IP = 0; IP < instructions.size(); IP++) {
            Byte operation = instructions.get(IP);
            switch (operation) {
                case 0:
                    byte index = instructions.get(IP+1);
                    STACK.add(constant_pool.get(index));
                    IP += 1;
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
                    STACK.pop();
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
                default:
                    System.err.println("opcode not implemented");

            }

            System.out.print("[ ");
            for (var i : STACK) {
                System.out.print(i.string() + " ");
            }
            System.out.print("]\n");
        }
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
