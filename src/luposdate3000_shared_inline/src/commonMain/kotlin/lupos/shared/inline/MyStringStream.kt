/*
 * This file is part of the Luposdate3000 distribution (https://github.com/luposdate3000/luposdate3000).
 * Copyright (c) 2020-2021, Institute of Information Systems (Benjamin Warnke and contributors of LUPOSDATE3000), University of Luebeck
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package lupos.shared.inline

import lupos.shared.DictionaryValueHelper
import lupos.shared.DictionaryValueType
import lupos.shared.IMyInputStream
import lupos.shared.dynamicArray.ByteArrayWrapper
import lupos.shared.inline.dynamicArray.ByteArrayWrapperExt
import kotlin.jvm.JvmField

internal class MyStringStream(str: String) : IMyInputStream {

    @JvmField
    val buf8 = ByteArray(8)

    @JvmField
    val buf8Wrapper = ByteArrayWrapper(buf8, 8)

    @JvmField
    public val data = str.encodeToByteArray()

    @JvmField
    public var pos = 0
    override fun close() {
    }

    override fun read(buf: ByteArray): Int {
        var s = pos + buf.size
        var res = buf.size
        if (s > data.size) {
            s = data.size
            res = s - pos
        }
        data.copyInto(buf, 0, pos, s)
        pos = s
        return res
    }

    override fun read(buf: ByteArray, len: Int): Int {
        var s = pos + len
        var res = len
        if (s > data.size) {
            s = data.size
            res = s - pos
        }
        data.copyInto(buf, 0, pos, s)
        pos = s
        return res
    }

    override fun read(buf: ByteArray, off: Int, len: Int): Int {
        var s = pos + len
        var res = len
        if (s > data.size) {
            s = data.size
            res = s - pos
        }
        data.copyInto(buf, off, pos, s)
        pos = s
        return res
    }

    override fun readInt(): Int {
        read(buf8, 4)
        return ByteArrayWrapperExt.readInt4(buf8Wrapper, 0)
    }

    override fun readDictionaryValueType(): DictionaryValueType {
        read(buf8, DictionaryValueHelper.getSize())
        return DictionaryValueHelper.fromByteArray(buf8Wrapper, 0)
    }

    override fun readLong(): Long {
        read(buf8, 8)
        return ByteArrayWrapperExt.readLong8(buf8Wrapper, 0)
    }

    override fun readByte(): Byte {
        read(buf8, 1)
        return buf8[0]
    }

    override fun readLine(): String? {
// TODO this may break on utf-8
        var buf = mutableListOf<Byte>()
        try {
            var b = readByte()
            while (b != '\n'.code.toByte()) {
                if (b != '\r'.code.toByte()) {
                    buf.add(b)
                }
                b = readByte()
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            if (buf.size == 0) {
                return null
            }
        }
        return buf.toByteArray().decodeToString()
    }
}
