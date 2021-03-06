package AST;

import java.util.ArrayList;
import java.util.Iterator;

import Utilities.Visitor;

public class Sequence<T extends AST> extends AST implements Iterable<T> {

    public ArrayList<T> children = new ArrayList<T>();

    public Sequence() {
        super(0, 0);
    }

    public Sequence(T element) {
        super(element);
        children.add(element);
    }

    public T child(int i) {
        return children.get(i);
    }

    public Sequence<T> append(T element) {
        children.add(element);
        return this;
    }

    public <S extends T> Sequence<T> merge(Sequence<S> others) {
        for (T e : others)
            children.add(e);
        return this;
    }

    public Iterator<T> iterator() {
        return children.iterator();
    }

    public void clear() {
        children.clear();
    }

    public <S extends T> Sequence<T> merge(S other) {
        children.add(other);
        return this;
    }

    public int size() {
        return children.size();
    }

    public void set(int index, T e) {
        children.set(index, e);
    }

    public <W extends Object> W visit(Visitor<W> v) {
        return v.visitSequence(this);
    }

    /**
     * Given an index it will return the element at that index.
     * added by Omar. This is repeat of child function at some point I will get rid of
     * this and use only child(). TODO.
     */
    public T getElementN(int n){
        return children.get(n);

    }
}
