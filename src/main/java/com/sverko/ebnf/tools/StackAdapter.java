package com.sverko.ebnf.tools;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

public class StackAdapter<T> implements Iterable<T> {
    private final Deque<T> deque = new ArrayDeque<>();

    /** Push auf das Stack-Top */
    public void push(T value) {
        deque.addLast(value);
    }

    /** Pop vom Stack-Top */
    public T pop() {
        T value = deque.pollLast();
        if (value == null) {
            throw new NoSuchElementException("Stack is empty");
        }
        return value;
    }

    /** Stack-Top ansehen, ohne zu entfernen */
    public T peek() {
        return deque.peekLast();
    }

    /** Alias für peek(), falls dir top() sprachlich besser gefällt */
    public T top() {
        return peek();
    }

    /** Unterstes Element ansehen */
    public T bottom() {
        return deque.peekFirst();
    }

    /** true, wenn leer */
    public boolean isEmpty() {
        return deque.isEmpty();
    }

    /** Anzahl Elemente */
    public int size() {
        return deque.size();
    }

    /** Alles löschen */
    public void clear() {
        deque.clear();
    }

    /** Bottom-up iterieren: Root -> ... -> Top */
    @Override
    public Iterator<T> iterator() {
        return deque.iterator();
    }

    /** Top-down iterieren: Top -> ... -> Root */
    public Iterator<T> reverseIterator() {
        return deque.descendingIterator();
    }

    /** Komfortabel bottom-up durchlaufen */
    public void forEachBottomUp(Consumer<? super T> consumer) {
        for (T value : deque) {
            consumer.accept(value);
        }
    }

    /** Komfortabel top-down durchlaufen */
    public void forEachTopDown(Consumer<? super T> consumer) {
        Iterator<T> it = deque.descendingIterator();
        while (it.hasNext()) {
            consumer.accept(it.next());
        }
    }

    /** Snapshot als Liste-ähnlicher String */
    @Override
    public String toString() {
        return deque.toString();
    }
}