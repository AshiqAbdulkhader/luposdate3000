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
package lupos.benchmark

import lupos.shared.inline.Platform
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

class DatabaseHandleBlazegraph(val workDir: String) : DatabaseHandle() {
    var processInstance: Process? = null
    override fun getThreads() = -1
    override fun getName(): String = "Blazegraph"
    override fun launch(import_file_name: String, abort: () -> Unit, action: () -> Unit) {
        File(workDir).deleteRecursively()
        File(workDir).mkdirs()
        var pwd = blazeGraphJar
        if (pwd.startsWith("./")) {
            pwd = File(".").absolutePath + "/" + pwd
        }
        val javaFileName = "/usr/lib/jvm/java-16-openjdk-amd64/bin/java"
        val javaFile = File(javaFileName)
        val cmd = mutableListOf<String>()
        if (javaFile.exists()) {
            cmd.add(javaFileName)
            cmd.add("-XX:+UnlockExperimentalVMOptions")
            cmd.add("-XX:+UseShenandoahGC")
            cmd.add("-XX:ShenandoahUncommitDelay=1000")
            cmd.add("-XX:ShenandoahGuaranteedGCInterval=10000")
        } else {
            cmd.add("java")
        }
        cmd.add("-server")
        cmd.add("-Xmx${Platform.getAvailableRam()}g")
        cmd.add("-jar")
        cmd.add(pwd)
        val p = ProcessBuilder(cmd).directory(File(workDir))
        processInstance = p.start()
        val errorstream = processInstance!!.getErrorStream()
        val errorreader = errorstream.bufferedReader()
        var errorThread = Thread {
            var errorline = errorreader.readLine()
            while (errorline != null) {
                if (errorline.contains("Exception")) {
                    abort()
                }
                println(errorline)
                errorline = errorreader.readLine()
            }
        }
        errorThread.start()
        val inputstream = processInstance!!.getInputStream()
        val inputreader = inputstream.bufferedReader()
        var inputline = inputreader.readLine()
        var inputThread = Thread {
            println(inputline)
            while (inputline != null) {
                inputline = inputreader.readLine()
            }
        }
        while (inputline != null) {
            println(inputline)
            if (inputline.startsWith("Go to ") && inputline.endsWith("/blazegraph/ to get started.")) {
                inputThread.start()
                importData(import_file_name)
                action()
                break
            }
            inputline = inputreader.readLine()
        }
        processInstance!!.destroy()
        inputreader.close()
        inputstream.close()
        inputThread.stop()
        errorThread.stop()
    }

    override fun runQuery(query: String): String {
        val encodedData = "query=${encode(query)}".encodeToByteArray()
        val u = URL("http://$hostname:9999/blazegraph/sparql")
        val conn = u.openConnection() as HttpURLConnection
        conn.setDoOutput(true)
        conn.setRequestMethod("POST")
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
        conn.setRequestProperty("Content-Length", "${encodedData.size}")
        conn.connect()
        val os = conn.getOutputStream()
        os.write(encodedData)
        val response = conn.inputStream.bufferedReader().readText()
        val code = conn.getResponseCode()
        if (code != 200) {
            throw Exception("query failed with response code $code")
        }
        return response
    }

    fun importData(file: String) {
        val encodedData = "update=LOAD <file://${File(file).absolutePath}>;".encodeToByteArray()
        val u = URL("http://$hostname:9999/blazegraph/namespace/kb/sparql")
        val conn = u.openConnection() as HttpURLConnection
        conn.setDoOutput(true)
        conn.setRequestMethod("POST")
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
        conn.setRequestProperty("Content-Length", "${encodedData.size}")
        conn.connect()
        val os = conn.getOutputStream()
        os.write(encodedData)
        val response = conn.inputStream.bufferedReader().readText()
        val code = conn.getResponseCode()
        if (code != 200) {
            throw Exception("import failed with response code $code")
        }
    }
}
