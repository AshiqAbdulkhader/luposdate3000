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
package lupos.shared

public class XMLElementFromXML {
    public operator fun invoke(data: String): XMLElement? {
        return parseFromXmlHelper(data)?.first()
    }

    private fun parseFromXmlHelper(xml: String): List<XMLElement>? {
        val x = xml.replace("\n", "").replace("\r", "")
        val res = mutableListOf<XMLElement>()
        var lastindex = 0
        """((<([a-zA-Z]+)([^>]*?)>(.*?)<\/\3>)|(<([a-zA-Z]+)([^>]*?)>)|(<\?.*?\?>)|(<!--.*?-->))?""".toRegex().findAll(x).forEach { child ->
            val value = child.value
            if (value.isNotEmpty() && !value.startsWith("<?") && !value.startsWith("<!--") && child.range.first >= lastindex) {
                var nodeName = ""
                if (child.groups[3] != null) {
                    nodeName = child.groups[3]!!.value
                }
                if (child.groups[7] != null) {
                    nodeName = child.groups[7]!!.value
                }
                val childNode = XMLElement(nodeName)
                res.add(childNode)
                var nodeAttributes = ""
                if (child.groups[4] != null) {
                    nodeAttributes = child.groups[4]!!.value
                }
                if (child.groups[8] != null) {
                    nodeAttributes = child.groups[8]!!.value
                }
                """([^\s]*?)="(([^\\"]*(\\"|\\)*)*)"""".toRegex().findAll(nodeAttributes).forEach { attrMatch ->
                    if (attrMatch.groups[1] != null && attrMatch.groups[2] != null) {
                        childNode.addAttribute(attrMatch.groups[1]!!.value, attrMatch.groups[2]!!.value)
                    }
                }
                """([^\s]*?)='([^']*)'""".toRegex().findAll(nodeAttributes).forEach { attrMatch ->
                    if (attrMatch.groups[1] != null && attrMatch.groups[2] != null) {
                        childNode.addAttribute(attrMatch.groups[1]!!.value, attrMatch.groups[2]!!.value)
                    }
                }
                var content = ""
                if (child.groups[5] != null) {
                    content = child.groups[5]!!.value
                }
                if (!child.value.endsWith("</$nodeName>") && !child.value.endsWith("/>")) {
                    val search = "</$nodeName>"
                    val idx2 = x.indexOf(search, child.range.last)
                    content = x.substring(child.range.last, idx2 + search.length)
                    lastindex = idx2
                }
                if (content != "") {
                    val tmp = parseFromXmlHelper(content)
                    if (tmp == null) {
                        childNode.addContentClean(content)
                    } else {
                        childNode.addContent(tmp)
                    }
                }
            }
        }
        if (res.isEmpty() && xml.isNotEmpty()) {
            return null
        }
        return res
    }
}
