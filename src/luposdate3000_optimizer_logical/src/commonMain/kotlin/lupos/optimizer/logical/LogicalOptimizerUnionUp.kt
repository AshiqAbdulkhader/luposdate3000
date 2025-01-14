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

import lupos.operator.base.Query
import lupos.operator.logical.multiinput.LOPJoin
import lupos.operator.logical.multiinput.LOPUnion
import lupos.shared.operator.IOPBase

public class LogicalOptimizerUnionUp(query: Query) : OptimizerBase(query, EOptimizerIDExt.LogicalOptimizerUnionUpID, "LogicalOptimizerUnionUp") {
    override /*suspend*/ fun optimize(node: IOPBase, parent: IOPBase?, onChange: () -> Unit): IOPBase {
        var res: IOPBase = node
        if (node is LOPJoin && !node.optional) {
            val childA = node.getChildren()[0]
            val childB = node.getChildren()[1]
            if (childA is LOPUnion) {
                res = LOPUnion(query, LOPJoin(query, childA.getChildren()[0], childB, node.optional), LOPJoin(query, childA.getChildren()[1], childB.cloneOP(), node.optional))
                onChange()
            } else if (childB is LOPUnion) {
                res = LOPUnion(query, LOPJoin(query, childA, childB.getChildren()[0], node.optional), LOPJoin(query, childA.cloneOP(), childB.getChildren()[1], node.optional))
                onChange()
            }
        }
        return res
    }
}
