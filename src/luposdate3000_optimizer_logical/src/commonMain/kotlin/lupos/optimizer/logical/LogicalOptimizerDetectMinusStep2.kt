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

import lupos.operator.arithmetik.noinput.AOPConstant
import lupos.operator.arithmetik.noinput.AOPVariable
import lupos.operator.base.Query
import lupos.operator.logical.multiinput.LOPMinus
import lupos.operator.logical.singleinput.LOPBind
import lupos.shared.DictionaryValueHelper
import lupos.shared.operator.IOPBase

public class LogicalOptimizerDetectMinusStep2(query: Query) : OptimizerBase(query, EOptimizerIDExt.LogicalOptimizerDetectMinusStep2ID, "LogicalOptimizerDetectMinusStep2") {
    override /*suspend*/ fun optimize(node: IOPBase, parent: IOPBase?, onChange: () -> Unit): IOPBase {
        var res: IOPBase = node
        if (node is LOPMinus) {
            val tmp = node.tmpFakeVariables.toMutableSet()
            tmp.removeAll(node.getChildren()[0].getProvidedVariableNames())
            if (tmp.size > 0) {
                for (v in tmp) {
                    res = LOPBind(query, AOPVariable(query, v), AOPConstant(query, DictionaryValueHelper.undefValue), res)
                }
                onChange()
                node.tmpFakeVariables = listOf()
            }
        }
        return res
    }
}
