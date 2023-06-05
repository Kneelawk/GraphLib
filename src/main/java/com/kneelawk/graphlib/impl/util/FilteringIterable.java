/*
 * MIT License
 *
 * Copyright (c) 2023 Kneelawk.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package com.kneelawk.graphlib.impl.util;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.jetbrains.annotations.NotNull;

public class FilteringIterable<T> implements Iterable<T> {
    private final Iterable<T> delegate;
    private final Predicate<T> predicate;

    public FilteringIterable(Iterable<T> delegate, Predicate<T> predicate) {
        this.delegate = delegate;
        this.predicate = predicate;
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        return new FilteringIterator(delegate.iterator());
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        delegate.forEach(t -> {
            if (predicate.test(t)) {
                action.accept(t);
            }
        });
    }

    // Taken from: https://stackoverflow.com/a/56393939/1687581
    private class FilteringIterator implements Iterator<T> {
        private final Iterator<T> input;
        private T current = null;
        private boolean hasCurrent = false;

        private FilteringIterator(Iterator<T> input) {this.input = input;}

        @Override
        public boolean hasNext() {
            while (!hasCurrent) {
                if (!input.hasNext()) {
                    return false;
                }
                T next = input.next();
                if (predicate.test(next)) {
                    current = next;
                    hasCurrent = true;
                }
            }
            return true;
        }

        @Override
        public T next() {
            if (!hasNext()) throw new NoSuchElementException("FilteringIterator has no remaining elements");
            T next = current;
            current = null;
            hasCurrent = false;
            return next;
        }
    }
}
