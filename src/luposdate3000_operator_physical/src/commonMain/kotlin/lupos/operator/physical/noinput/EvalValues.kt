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
package lupos.operator.physical.noinput

import lupos.operator.base.iterator.ColumnIteratorMultiValue
import lupos.shared.DictionaryValueType
import lupos.shared.operator.iterator.ColumnIterator
import lupos.shared.operator.iterator.IteratorBundle

public object EvalValues {
    public operator fun invoke(rows: Int, data: Map<String, MutableList<DictionaryValueType>>): IteratorBundle {
        return if (rows == -1) {
            val outMap = mutableMapOf<String, ColumnIterator>()
            for (name in data.keys) {
                outMap[name] = ColumnIteratorMultiValue(data[name]!!)
            }
            IteratorBundle(outMap)
        } else {
            IteratorBundle(rows)
        }
    }

    public operator fun invoke(data: Map<String, MutableList<DictionaryValueType>>): IteratorBundle {
        val outMap = mutableMapOf<String, ColumnIterator>()
        for (name in data.keys) {
            outMap[name] = ColumnIteratorMultiValue(data[name]!!)
        }
        return IteratorBundle(outMap)
    }
}
