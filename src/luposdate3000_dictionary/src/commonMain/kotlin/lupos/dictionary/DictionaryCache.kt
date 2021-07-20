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
package lupos.dictionary
import lupos.shared.DictionaryValueHelper
import lupos.shared.DictionaryValueType
import lupos.shared.DictionaryValueTypeArray
import lupos.shared.Luposdate3000Instance
import lupos.shared.dictionary.DictionaryExt
import lupos.shared.dynamicArray.ByteArrayWrapper
import lupos.shared.inline.dynamicArray.ByteArrayWrapperExt
public class DictionaryCache(private val instance: Luposdate3000Instance) {
    private val value_capacity = instance.dictionaryCacheCapacity
    private var offset = 0
    private val value_ids = DictionaryValueTypeArray(value_capacity) { DictionaryValueHelper.booleanTrueValue }
    private val value_content = Array(value_capacity) {
        val tmp = ByteArrayWrapper()
        ByteArrayWrapperExt.copyInto(DictionaryExt.booleanTrueValue3, tmp, false)
        tmp
    }

    public fun getValueByContent(buffer: ByteArrayWrapper): DictionaryValueType {
        for (i in 0 until value_capacity) {
            if (ByteArrayWrapperExt.compare_fast(value_content[i], buffer) == 0) {
                return value_ids[i]
            }
        }
        return DictionaryValueHelper.nullValue
    }

    public fun getValueById(buffer: ByteArrayWrapper, id: DictionaryValueType): Boolean {
        for (i in 0 until value_capacity) {
            if (value_ids[i] == id) {
                ByteArrayWrapperExt.copyInto(value_content[i], buffer, false)
                return true
            }
        }
        return false
    }

    public fun insertValuePair(buffer: ByteArrayWrapper, id: DictionaryValueType) {
        for (i in 0 until value_capacity) {
            if (value_ids[i] == id) {
                return
            }
        }
        for (i in 0 until value_capacity) {
            if (ByteArrayWrapperExt.compare_fast(value_content[i], buffer) == 0) {
                return // to be save, otherwise we miss the case where a local dictionary id is upgraded to a global one
            }
        }
        ByteArrayWrapperExt.copyInto(buffer, value_content[offset], false)
        value_ids[offset] = id
        offset++
    }
}