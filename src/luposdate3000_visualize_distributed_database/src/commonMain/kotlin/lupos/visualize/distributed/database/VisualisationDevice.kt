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
package lupos.visualize.distributed.database

public class VisualisationDevice(public val id: Int, public val hasDatabaseStore: Boolean, public val hasDatabaseQuery: Boolean, public val hasSensor: Boolean) {
    public var x: Double = 0.0
    public var y: Double = 0.0
    public var xnew: Double = 0.0
    public var ynew: Double = 0.0
    override fun equals(other: Any?): Boolean = other is VisualisationDevice && id == other.id
    override fun hashCode(): Int = id
    override fun toString(): String = "VisualisationDevice(id=$id, hasDatabase=$hasDatabaseStore:$hasDatabaseQuery, hasSensor=$hasSensor)"
}
