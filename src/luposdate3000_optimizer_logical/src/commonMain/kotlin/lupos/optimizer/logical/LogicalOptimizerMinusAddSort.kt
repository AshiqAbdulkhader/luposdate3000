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
package lupos.optimizer.logical

import lupos.operator.arithmetik.noinput.AOPVariable
import lupos.operator.base.Query
import lupos.operator.logical.multiinput.LOPMinus
import lupos.operator.logical.singleinput.LOPProjection
import lupos.operator.logical.singleinput.modifiers.LOPReduced
import lupos.operator.logical.singleinput.modifiers.LOPSortAny
import lupos.shared.ESortTypeExt
import lupos.shared.SortHelper
import lupos.shared.operator.IOPBase

public class LogicalOptimizerMinusAddSort(query: Query) : OptimizerBase(query, EOptimizerIDExt.LogicalOptimizerMinusAddSortID, "LogicalOptimizerMinusAddSort") {
    override /*suspend*/ fun optimize(node: IOPBase, parent: IOPBase?, onChange: () -> Unit): IOPBase {
        val res: IOPBase = node
        if (node is LOPMinus) {
            if (!node.hadSortPushDown) {
                node.hadSortPushDown = true
                val provided = node.getChildren()[0].getProvidedVariableNames().intersect(node.getChildren()[1].getProvidedVariableNames())
                node.getChildren()[1] = LOPReduced(query, LOPSortAny(query, provided.map { SortHelper(it, ESortTypeExt.FAST) }, LOPProjection(query, provided.map { AOPVariable(query, it) }.toMutableList(), node.getChildren()[1])))
                node.getChildren()[0] = LOPSortAny(query, provided.map { SortHelper(it, ESortTypeExt.FAST) }, LOPProjection(query, provided.map { AOPVariable(query, it) }.toMutableList(), node.getChildren()[0]))
                onChange()
            }
        }
        return res
    }
}
