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
import lupos.operator.logical.multiinput.LOPJoin
import lupos.operator.logical.multiinput.LOPMinus
import lupos.operator.logical.multiinput.LOPUnion
import lupos.operator.logical.noinput.LOPTriple
import lupos.operator.logical.singleinput.LOPFilter
import lupos.operator.logical.singleinput.LOPProjection
import lupos.operator.logical.singleinput.modifiers.LOPReduced
import lupos.operator.logical.singleinput.modifiers.LOPSortAny
import lupos.shared.operator.IOPBase

public class LogicalOptimizerReducedDown(query: Query) : OptimizerBase(query, EOptimizerIDExt.LogicalOptimizerReducedDownID, "LogicalOptimizerReducedDown") {
    override /*suspend*/ fun optimize(node: IOPBase, parent: IOPBase?, onChange: () -> Unit): IOPBase {
        var res: IOPBase = node
        if (node is LOPReduced) {
            val child = node.getChildren()[0]
            if (child is LOPReduced) {
                res = child
                onChange()
            } else if (!node.hadPushDown) {
                node.hadPushDown = true
                if (child is LOPProjection) {
                    child.getChildren()[0] = LOPReduced(query, child.getChildren()[0])
                    onChange()
                } else if (child is LOPTriple) {
                    var flag = true
                    for (c in child.getChildren()) {
                        if (c is AOPVariable && c.name == "_") {
                            flag = false
                            break
                        }
                    }
                    if (flag) {
// keep the reduced, if_ there is a blank variable in the triple-pattern
                        res = child
                        onChange()
                    }
                } else if (child is LOPJoin) {
                    child.getChildren()[0] = LOPReduced(query, child.getChildren()[0])
                    child.getChildren()[1] = LOPReduced(query, child.getChildren()[1])
                    res = child
                    onChange()
                } else if (child is LOPUnion) {
                    child.getChildren()[0] = LOPReduced(query, child.getChildren()[0])
                    child.getChildren()[1] = LOPReduced(query, child.getChildren()[1])
                    onChange()
                } else if (child is LOPMinus) {
                    child.getChildren()[0] = LOPReduced(query, child.getChildren()[0])
                    res = child
                    onChange()
                } else if (child is LOPFilter) {
                    child.getChildren()[0] = LOPReduced(query, child.getChildren()[0])
                    res = child
                    onChange()
                } else if (child is LOPSortAny) {
                    child.getChildren()[0] = LOPReduced(query, child.getChildren()[0])
                    onChange()
                }
            }
        }
        return res
    }
}
