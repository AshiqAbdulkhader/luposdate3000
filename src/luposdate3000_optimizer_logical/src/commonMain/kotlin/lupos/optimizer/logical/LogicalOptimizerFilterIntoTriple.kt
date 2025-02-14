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

import lupos.operator.arithmetik.multiinput.AOPEQ
import lupos.operator.arithmetik.noinput.AOPConstant
import lupos.operator.arithmetik.noinput.AOPVariable
import lupos.operator.base.Query
import lupos.operator.logical.noinput.LOPTriple
import lupos.operator.logical.singleinput.LOPBind
import lupos.operator.logical.singleinput.LOPFilter
import lupos.shared.operator.IOPBase

public class LogicalOptimizerFilterIntoTriple(query: Query) : OptimizerBase(query, EOptimizerIDExt.LogicalOptimizerFilterIntoTripleID, "LogicalOptimizerFilterIntoTriple") {
    override /*suspend*/ fun optimize(node: IOPBase, parent: IOPBase?, onChange: () -> Unit): IOPBase {
        var res: IOPBase = node
        if (node is LOPFilter) {
            val loptriple = node.getChildren()[0]
            val aopcompare = node.getChildren()[1]
            if (loptriple is LOPTriple && aopcompare is AOPEQ) {
                for (c in 0 until 2) {
                    val compareVar = aopcompare.getChildren()[1 - c]
                    val compareVal = aopcompare.getChildren()[c]
                    if (compareVal is AOPConstant && compareVar is AOPVariable) {
                        for (i in 0 until 3) {
                            val tmp = loptriple.getChildren()[i]
                            if (tmp is AOPVariable) {
                                if (tmp.name == compareVar.name) {
                                    onChange()
                                    loptriple.getChildren()[i] = compareVal
                                    res = LOPBind(query, compareVar, compareVal, loptriple)
                                }
                            }
                        }
                    }
                }
            }
        }
        return res
    }
}
