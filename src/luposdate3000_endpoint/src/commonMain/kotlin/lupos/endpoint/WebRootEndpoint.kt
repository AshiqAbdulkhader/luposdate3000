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
package lupos.endpoint

import lupos.shared.inline.File

public object WebRootEndpoint {

    public fun initialize(paths: MutableMap<String, PathMappingHelper>) {
        val webroot = "src/luposdate3000_spa_client/dist/" // relative to luposdate3000 or absolute path including trailling slash
        val basepath = "/" // base path in the browser url. this may be the empty path. this must include a trailing slash
        val f = File(webroot)
        if (f.exists()) {
            f.walk { p ->
                if (p.length > webroot.length) {
                    val targetPath = basepath + p.substring(webroot.length).replace("\\", "/").replace("//", "/")
                    paths[targetPath] = PathMappingHelper(true, mapOf()) { _, _, connectionOutMy ->
                        connectionOutMy.println("HTTP/1.1 200 OK")
                        if (targetPath.endsWith(".html")) {
                            connectionOutMy.println("Content-Type: text/html")
                        } else if (targetPath.endsWith(".css")) {
                            connectionOutMy.println("Content-Type: text/css")
                        } else if (targetPath.endsWith(".woff")) {
                            connectionOutMy.println("Content-Type: font/woff")
                        } else if (targetPath.endsWith(".svg")) {
                            connectionOutMy.println("Content-Type: image/svg+xml")
                        } else if (targetPath.endsWith(".js")) {
                            connectionOutMy.println("Content-Type: application/javascript")
                        } else {
                            connectionOutMy.println("Content-Type: text/plain")
                        }
                        val f2 = File(p)
                        connectionOutMy.println("Content-Length: ${f2.length()}")
                        connectionOutMy.println()
                        val buf = ByteArray(4096)
                        f2.withInputStream { input ->
                            var len = input.read(buf)
                            while (len > 0) {
                                connectionOutMy.write(buf, len)
                                len = input.read(buf)
                            }
                        }
                        true
                    }
                }
            }
        }
    }
}
