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
package lupos.simulator_db.luposdate3000

import lupos.shared.IQuery
import lupos.shared.MemoryTable
import lupos.shared.dynamicArray.ByteArrayWrapper
import simora.applications.scenario.parking.IPackage_Database

internal class PendingWork(
    val queryID: Int,
    val data: ByteArrayWrapper,
    val dataID: Int,
    val destinations: Map<Int, Int>,
    val dependencies: Set<Int>,
    val onFinish: IPackage_Database?,
    val expectedResult: MemoryTable?,
    val verifyAction: () -> Unit,
    val query: IQuery,
) {
    internal companion object {
        var pendingWorkCounter = 0
    }

    val pendingWorkID = pendingWorkCounter++
}
