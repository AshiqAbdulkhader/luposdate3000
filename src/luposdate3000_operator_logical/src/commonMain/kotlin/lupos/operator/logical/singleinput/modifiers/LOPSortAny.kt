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
package lupos.operator.logical.singleinput.modifiers

import lupos.operator.logical.LOPBase
import lupos.shared.EOperatorIDExt
import lupos.shared.ESortPriorityExt
import lupos.shared.ESortTypeExt
import lupos.shared.IQuery
import lupos.shared.SortHelper
import lupos.shared.operator.HistogramResult
import lupos.shared.operator.IOPBase
import kotlin.jvm.JvmField

public class LOPSortAny public constructor(query: IQuery, @JvmField public val possibleSortOrder: List<SortHelper>, child: IOPBase) : LOPBase(query, EOperatorIDExt.LOPSortAnyID, "LOPSortAny", arrayOf(child), ESortPriorityExt.SORT) {
    override fun equals(other: Any?): Boolean = other is LOPSortAny && possibleSortOrder == other.possibleSortOrder && children[0] == other.children[0]
    override fun cloneOP(): IOPBase = LOPSortAny(query, possibleSortOrder, children[0].cloneOP())
    override /*suspend*/ fun calculateHistogram(): HistogramResult {
        return children[0].getHistogram()
    }

    override fun getPossibleSortPriorities(): List<List<SortHelper>> {
        val res = mutableListOf<List<SortHelper>>()
        val requiredVariables = mutableListOf<String>()
        val sortType = ESortTypeExt.ASC
        res.add(this.possibleSortOrder)
        val tmp = mutableListOf<SortHelper>()
        for (v in requiredVariables) {
            tmp.add(SortHelper(v, sortType))
        }
        res.add(tmp)
        return res
    }
}
