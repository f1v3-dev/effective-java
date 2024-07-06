package code;

import java.util.Arrays;
import java.util.EmptyStackException;

public class Stack implements Cloneable {

    private Object[] elements;
    private int size = 0;
    private static final int DEFAULT_INITIAL_CAPACITY = 16;

    public Stack() {
        this.elements = new Object[DEFAULT_INITIAL_CAPACITY];
    }

    public void push(Object e) {
        ensureCapacity();
        elements[size++] = e;
    }

    public Object pop() {
        if (size == 0) {
            throw new EmptyStackException();
        }

        Object result = elements[--size];
        elements[size] = null; // 다 쓴 객체 참조 해제
        return result;
    }

    private void ensureCapacity() {
        if (elements.length == size) {
            elements = Arrays.copyOf(elements, 2 * size + 1);
        }
    }

    @Override
    public Stack clone() {
        try {
            Stack result = (Stack) super.clone();
            result.elements = elements.clone();
            return result;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    public static void main(String[] args) {
        Stack stack = new Stack();
        stack.push("1");
        stack.push("2");

        Stack clonedStack = stack.clone();

        System.out.println("Original Stack");
        while (stack.size > 0) {
            System.out.println(stack.pop());
        }

        System.out.println("\nCloned Stack");
        while (clonedStack.size > 0) {
            System.out.println(clonedStack.pop());
        }
    }
}
