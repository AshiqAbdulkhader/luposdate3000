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
package lupos.operator.arithmetik.noinput

import lupos.operator.arithmetik.AOPBase
import lupos.shared.EOperatorIDExt
import lupos.shared.IQuery
import lupos.shared.dynamicArray.ByteArrayWrapper
import lupos.shared.inline.DictionaryHelper
import lupos.shared.operator.IOPBase
import lupos.shared.operator.iterator.IteratorBundle

public class AOPBuildInCallSTRUUID public constructor(query: IQuery) : AOPBase(query, EOperatorIDExt.AOPBuildInCallSTRUUIDID, "AOPBuildInCallSTRUUID", arrayOf()) {
    override fun toSparql(): String = "STRUUID()"
    override fun equals(other: Any?): Boolean = other is AOPBuildInCallSTRUUID
    private val byteToHexMap = Array(256) {
        if (it == 0) {
            "00"
        } else {
            it.toString(16).padStart(2, '0')
        }
    }

    override fun evaluate(row: IteratorBundle): () -> ByteArrayWrapper {
        val buffer = ByteArrayWrapper()
        return {
            val s = StringBuilder()
            s.append("00000000-0000-0000-0000-0000")
            val uuid = query.getDictionary().createNewUUID()
            s.append(byteToHexMap[(uuid shr 24) and 0xff])
            s.append(byteToHexMap[(uuid shr 16) and 0xff])
            s.append(byteToHexMap[(uuid shr 8) and 0xff])
            s.append(byteToHexMap[uuid and 0xff])
            DictionaryHelper.stringToByteArray(buffer, s.toString())
            buffer
        }
    }

    override fun cloneOP(): IOPBase = this
}
