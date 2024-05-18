/*
 * MIT License
 *
 * Copyright (c) 2024 Cyan Kneelawk.
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

package com.kneelawk.transferbeams.util;

import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

public class SimpleContainerCodec extends MapCodec<SimpleContainer> {
    private final int size;

    public SimpleContainerCodec(int size) {this.size = size;}

    @Override
    public <T> Stream<T> keys(DynamicOps<T> ops) {
        return IntStream.range(0, size).mapToObj(i -> ops.createString(String.valueOf(i)));
    }

    @Override
    public <T> DataResult<SimpleContainer> decode(DynamicOps<T> ops, MapLike<T> input) {
        SimpleContainer container = new SimpleContainer(size);
        Stream.Builder<Pair<T, T>> failed = Stream.builder();

        DataResult<Unit> result =
            input.entries().reduce(DataResult.success(Unit.INSTANCE, Lifecycle.stable()), (res, entry) -> {
                DataResult<Integer> indexRes = ops.getStringValue(entry.getFirst()).flatMap(key -> {
                    try {
                        return DataResult.success(Integer.parseInt(key));
                    } catch (NumberFormatException ignored) {
                        return DataResult.error(() -> key + " is not a number");
                    }
                });
                DataResult<ItemStack> stackRes = ItemStack.CODEC.parse(ops, entry.getSecond());
                DataResult<Pair<Integer, ItemStack>> slotRes = indexRes.apply2stable(Pair::of, stackRes);

                Optional<Pair<Integer, ItemStack>> slotOpt = slotRes.resultOrPartial();
                if (slotOpt.isPresent()) {
                    int index = slotOpt.get().getFirst();
                    ItemStack stack = slotOpt.get().getSecond();
                    container.setItem(index, stack);
                }

                if (slotRes.isError()) {
                    failed.add(entry);
                }

                return res.apply2stable((u, o) -> u, slotRes);
            }, (r1, r2) -> r1.apply2stable((u1, u2) -> u1, r2));
        
        T errors = ops.createMap(failed.build());

        return result.map(ignored -> container).setPartial(container).mapError(error -> error + " missed input: " + errors);
    }

    @Override
    public <T> RecordBuilder<T> encode(SimpleContainer input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
        for (int i = 0; i < size; i++) {
            ItemStack stack = input.getItem(i);
            if (!stack.isEmpty()) {
                prefix.add(String.valueOf(i), ItemStack.CODEC.encodeStart(ops, stack));
            }
        }
        
        return prefix;
    }
}
