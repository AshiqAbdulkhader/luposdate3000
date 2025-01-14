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
import lupos.operator.base.OPBaseCompound
import lupos.operator.base.Query
import lupos.operator.logical.multiinput.LOPMinus
import lupos.operator.logical.multiinput.LOPUnion
import lupos.operator.logical.singleinput.LOPProjection
import lupos.operator.logical.singleinput.modifiers.LOPDistinct
import lupos.operator.logical.singleinput.modifiers.LOPReduced
import lupos.shared.operator.IOPBase

public class LogicalOptimizerProjectionUp(query: Query) : OptimizerBase(query, EOptimizerIDExt.LogicalOptimizerProjectionUpID, "LogicalOptimizerProjectionUp") {
    override /*suspend*/ fun optimize(node: IOPBase, parent: IOPBase?, onChange: () -> Unit): IOPBase {
        var res: IOPBase = node
        if (node !is LOPProjection && node !is OPBaseCompound && node !is LOPUnion && node !is LOPMinus && node !is LOPReduced && node !is LOPDistinct) {
            for (i in node.getChildren().indices) {
                val child = node.getChildren()[i]
                if (child is LOPProjection) {
                    res = LOPProjection(query, node.getProvidedVariableNames().map { AOPVariable(query, it) }.toMutableList(), node)
                    node.getChildren()[i] = child.getChildren()[0]
                    onChange()
                    break
                }
            }
        }
        return res
    }
}
