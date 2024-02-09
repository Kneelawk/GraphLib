/*
 * MIT License
 *
 * Copyright (c) 2024 Kneelawk.
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

package com.kneelawk.graphlib.netutil.api;

import org.jetbrains.annotations.Nullable;

import net.minecraft.network.listener.PacketListener;

import com.kneelawk.graphlib.netutil.api.netcache.NetCache;
import com.kneelawk.graphlib.netutil.impl.NetUtilImpl;
import com.kneelawk.graphlib.netutil.impl.netcache.NetCacheImpl;

/**
 * GraphLib Net Util public interface.
 */
public class NetUtil {
    private NetUtil() {}

    /**
     * Gets the Net Util connection associated with the given Minecraft connection.
     *
     * @param listener the Minecraft connection to get the associated Net Util connection for.
     * @return the associated Net Util connection for the given Minecraft connection,
     * or {@code null} if the connection is not a valid minecraft connection.
     */
    public static @Nullable ConnectionExtra getExtra(PacketListener listener) {
        return NetUtilImpl.getOrCreateConnection(listener);
    }

    /**
     * Registers the net cache for synchronization.
     * <p>
     * Note, this <b>must</b> be called before any connections are opened.
     *
     * @param cache the cache to register.
     */
    public static void registerNetCache(NetCache<?> cache) {
        NetCacheImpl.register(cache);
    }
}
