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
import java.util.function.Function;

import org.jetbrains.annotations.NotNull;

public class FlatMappingIterable<T, R> implements Iterable<R> {
    private final Iterable<T> delegate;
    private final Function<T, Iterable<R>> mapper;

    public FlatMappingIterable(Iterable<T> delegate, Function<T, Iterable<R>> mapper) {
        this.delegate = delegate;
        this.mapper = mapper;
    }

    @NotNull
    @Override
    public Iterator<R> iterator() {
        return new FlatMappingIterator(delegate.iterator());
    }

    @Override
    public void forEach(Consumer<? super R> action) {
        delegate.forEach(t -> mapper.apply(t).forEach(action));
    }

    private class FlatMappingIterator implements Iterator<R> {
        private final Iterator<T> input;
        private Iterator<R> currentIterator = null;

        public FlatMappingIterator(Iterator<T> input) {
            this.input = input;
        }

        @Override
        public boolean hasNext() {
            while (currentIterator == null || !currentIterator.hasNext()) {
                if (input.hasNext()) {
                    currentIterator = mapper.apply(input.next()).iterator();
                } else {
                    return false;
                }
            }
            return true;
        }

        @Override
        public R next() {
            if (!hasNext()) throw new NoSuchElementException("FlatMappingIterator has no more elements");
            return currentIterator.next();
        }
    }
}
