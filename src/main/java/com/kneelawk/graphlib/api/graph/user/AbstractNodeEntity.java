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

package com.kneelawk.graphlib.api.graph.user;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.kneelawk.graphlib.api.graph.NodeEntityContext;

/**
 * Abstract node entity class that handles context for you.
 */
public abstract class AbstractNodeEntity implements NodeEntity {
    /**
     * Nullable context access.
     */
    protected @Nullable NodeEntityContext ctx;

    /**
     * Constructs this node entity.
     */
    public AbstractNodeEntity() {
    }

    @Override
    public void onInit(@NotNull NodeEntityContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public @NotNull NodeEntityContext getContext() {
        if (ctx == null) throw new IllegalStateException("This NodeEntity has not been initialized yet");
        return ctx;
    }
}
