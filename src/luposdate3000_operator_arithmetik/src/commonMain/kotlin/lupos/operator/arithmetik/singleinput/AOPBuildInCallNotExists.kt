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
package lupos.operator.arithmetik.singleinput

import lupos.operator.arithmetik.AOPBase
import lupos.shared.DictionaryValueType
import lupos.shared.EOperatorIDExt
import lupos.shared.IQuery
import lupos.shared.operator.IOPBase
import lupos.shared.operator.iterator.IteratorBundle
import kotlin.jvm.JvmField

public class AOPBuildInCallNotExists public constructor(query: IQuery, @JvmField public var child: IOPBase) : AOPBase(query, EOperatorIDExt.AOPBuildInCallNotExistsID, "AOPBuildInCallNotExists", arrayOf(child)) {
    override fun toSparql(): String = "NOT EXISTS {" + children[0].toSparql() + "}"
    override fun equals(other: Any?): Boolean = other is AOPBuildInCallNotExists && children[0] == other.children[0]
    override fun evaluateID(row: IteratorBundle): () -> DictionaryValueType = TODO("AOPBuildInCallNotExists")
    override fun enforcesBooleanOrError(): Boolean = true
    override fun cloneOP(): IOPBase = AOPBuildInCallNotExists(query, children[0].cloneOP())
    override fun replaceVariableWithUndef(name: String, existsClauses: Boolean): IOPBase {
        if (!existsClauses) {
            return this
        }
        for (i in this.getChildren().indices) {
            this.getChildren()[i] = this.getChildren()[i].replaceVariableWithUndef(name, existsClauses)
        }
        return this
    }
}
