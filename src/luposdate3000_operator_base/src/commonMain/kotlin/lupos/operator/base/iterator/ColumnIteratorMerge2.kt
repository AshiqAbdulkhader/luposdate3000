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
package lupos.operator.base.iterator

import lupos.shared.DictionaryValueHelper
import lupos.shared.DictionaryValueType
import lupos.shared.operator.iterator.ColumnIterator
import kotlin.jvm.JvmField

public class ColumnIteratorMerge2(@JvmField public val a: ColumnIterator, @JvmField public val b: ColumnIterator) : ColumnIterator() {
    @JvmField
    public var label: Int = 3

    @JvmField
    public var aBuf: DictionaryValueType = DictionaryValueHelper.nullValue

    @JvmField
    public var bBuf: DictionaryValueType = DictionaryValueHelper.nullValue
    override /*suspend*/ fun close() {
        if (label != 0) {
            label = 0
            a.close()
            b.close()
        }
    }

    override /*suspend*/ fun next(): DictionaryValueType {
        var res: DictionaryValueType = DictionaryValueHelper.nullValue
        when (label) {
            1 -> { // call next on a, b is empty
                res = a.next()
                if (res == DictionaryValueHelper.nullValue) {
                    a.close()
                    label = 0
                }
            }
            2 -> { // call next on b, a is empty
                res = b.next()
                if (res == DictionaryValueHelper.nullValue) {
                    b.close()
                    label = 0
                }
            }
            4 -> { // call next on a, b is not empty
                aBuf = a.next()
                if (aBuf == DictionaryValueHelper.nullValue) {
                    a.close()
                    res = bBuf
                    label = 2
                } else {
                    if (aBuf < bBuf) {
                        res = aBuf
                        label = 4
                    } else {
                        res = bBuf
                        label = 5
                    }
                }
            }
            5 -> { // call next on b, a is not empty
                bBuf = b.next()
                if (bBuf == DictionaryValueHelper.nullValue) {
                    b.close()
                    res = aBuf
                    label = 1
                } else {
                    if (aBuf < bBuf) {
                        res = aBuf
                        label = 4
                    } else {
                        res = bBuf
                        label = 5
                    }
                }
            }
            3 -> { // call next on both
                aBuf = a.next()
                bBuf = b.next()
                if (aBuf == DictionaryValueHelper.nullValue && bBuf == DictionaryValueHelper.nullValue) {
                    a.close()
                    b.close()
                    label = 0
                } else if (bBuf == DictionaryValueHelper.nullValue) {
                    b.close()
                    res = aBuf
                    label = 1
                } else if (aBuf == DictionaryValueHelper.nullValue) {
                    a.close()
                    res = bBuf
                    label = 2
                } else {
                    if (aBuf < bBuf) {
                        res = aBuf
                        label = 4
                    } else {
                        res = bBuf
                        label = 5
                    }
                }
            }
        }
        return res
    }
}
