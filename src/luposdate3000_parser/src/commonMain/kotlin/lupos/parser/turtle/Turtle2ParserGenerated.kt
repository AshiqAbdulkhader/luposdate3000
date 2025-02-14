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
package lupos.parser.turtle

import lupos.shared.IMyInputStream
import lupos.shared.Luposdate3000Exception
import kotlin.jvm.JvmField

internal open class ParserException(msg: String) : Luposdate3000Exception("ParserContext", msg)
internal class ParserExceptionEOF : ParserException("EOF")
internal class ParserExceptionUnexpectedChar(context: ParserContext) : ParserException("unexpected char 0x${context.c.toString(16)} at ${context.line}:${context.column}")
internal class ParserContext(@JvmField internal val input: IMyInputStream) {
    internal companion object {
        const val EOF = 0x7fffffff
    }

    @JvmField
    internal var c: Int = 0

    @JvmField
    internal var line = 1

    @JvmField
    internal var column = 0

    @JvmField
    internal val outBuffer = StringBuilder()

    @JvmField
    internal val inBuf = ByteArray(8192)

    @JvmField
    internal var inBufPosition = 0

    @JvmField
    internal var inBufSize = 0

    @JvmField
    internal var flagrN = false

    @Suppress("NOTHING_TO_INLINE")
    internal inline fun clear() {
        outBuffer.clear()
    }

    @Suppress("NOTHING_TO_INLINE")
    internal inline fun getValue(): String {
        return outBuffer.toString()
    }

    fun append() {
        if (c <= 0xd7ff || (c in 0xe000..0xffff)) {
            outBuffer.append(c.toChar())
            next()
        } else {
            c -= 0x100000
            outBuffer.append((0xd800 + ((c shr 10) and 0x03ff)).toChar())
            outBuffer.append((0xdc00 + (c and 0x03ff)).toChar())
            next()
        }
    }

    fun next() {
        if (inBufPosition >= inBufSize) {
            if (c == EOF) {
                throw ParserExceptionEOF()
            } else {
                inBufSize = input.read(inBuf)
                inBufPosition = 0
                if (inBufSize <= 0) {
                    c = EOF
                    return
                }
            }
        }
        val t: Int = inBuf[inBufPosition++].toInt() and 0xff
        if ((t and 0x80) == 0) {
            // 1byte
            c = t
            if ((c == '\r'.code) || (c == '\n'.code)) {
                if (!flagrN) {
                    flagrN = true
                    line++
                    column = 1
                }
            } else {
                column++
                flagrN = false
            }
        } else if ((t and 0x20) == 0) {
            // 2byte
            flagrN = false
            c = (t and 0x1f) shl 6
            if (inBufPosition >= inBufSize) {
                inBufSize = input.read(inBuf)
                inBufPosition = 0
                if (inBufSize <= 0) {
                    c = EOF
                    return
                }
            }
            c = c or (inBuf[inBufPosition++].toInt() and 0x3f)
            column++
        } else if ((t and 0x10) == 0) {
            // 3byte
            flagrN = false
            c = (t and 0x0f) shl 12
            if (inBufPosition >= inBufSize) {
                inBufSize = input.read(inBuf)
                inBufPosition = 0
                if (inBufSize <= 0) {
                    c = EOF
                    return
                }
            }
            c = c or ((inBuf[inBufPosition++].toInt() and 0x3f) shl 6)
            if (inBufPosition >= inBufSize) {
                inBufSize = input.read(inBuf)
                inBufPosition = 0
                if (inBufSize <= 0) {
                    c = EOF
                    return
                }
            }
            c = c or (inBuf[inBufPosition++].toInt() and 0x3f)
            column++
        } else {
            // 4byte
            flagrN = false
            c = (t and 0x07) shl 18
            if (inBufPosition >= inBufSize) {
                inBufSize = input.read(inBuf)
                inBufPosition = 0
                if (inBufSize <= 0) {
                    c = EOF
                    return
                }
            }
            c = c or ((inBuf[inBufPosition++].toInt() and 0x3f) shl 12)
            if (inBufPosition >= inBufSize) {
                inBufSize = input.read(inBuf)
                inBufPosition = 0
                if (inBufSize <= 0) {
                    c = EOF
                    return
                }
            }
            c = c or ((inBuf[inBufPosition++].toInt() and 0x3f) shl 6)
            if (inBufPosition >= inBufSize) {
                inBufSize = input.read(inBuf)
                inBufPosition = 0
                if (inBufSize <= 0) {
                    c = EOF
                    return
                }
            }
            c = c or (inBuf[inBufPosition++].toInt() and 0x3f)
            column++
        }
    }

    init {
        next()
    }
}

internal inline fun parse_dot(
    context: ParserContext,
    crossinline onDOT: () -> Unit
) {
    context.clear()
    error@ while (true) {
        when (parse_dot_helper_0(context.c)) {
            0 -> {
                context.append()
                onDOT()
                return
            }
            else -> {
                break@error
            }
        }
    }
    throw ParserExceptionUnexpectedChar(context)
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun parse_dot_helper_0(c: Int): Int {
    return if (c == 0x2e) {
        0
    } else {
        1
    }
}

internal inline fun parse_ws(
    context: ParserContext,
    crossinline onSKIP_WS: () -> Unit
) {
    context.clear()
    error@ while (true) {
        loop1@ while (true) {
            when (context.c) {
                0x9, 0xa, 0xd, 0x20 -> {
                    context.append()
                }
                else -> {
                    break@loop1
                }
            }
        }
        onSKIP_WS()
        return
    }
}

internal inline fun parse_ws_forced(
    context: ParserContext,
    crossinline onSKIP_WS_FORCED: () -> Unit
) {
    context.clear()
    error@ while (true) {
        when (parse_ws_forced_helper_0(context.c)) {
            0 -> {
                context.append()
                loop3@ while (true) {
                    when (context.c) {
                        0x9, 0xa, 0xd, 0x20 -> {
                            context.append()
                        }
                        else -> {
                            break@loop3
                        }
                    }
                }
                onSKIP_WS_FORCED()
                return
            }
            else -> {
                break@error
            }
        }
    }
    throw ParserExceptionUnexpectedChar(context)
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun parse_ws_forced_helper_0(c: Int): Int {
    return if (c < 0x9) {
        1
    } else if (c <= 0xa) {
        0
    } else if (c < 0xd) {
        1
    } else if (c <= 0xd) {
        0
    } else if (c < 0x20) {
        1
    } else if (c <= 0x20) {
        0
    } else {
        1
    }
}

internal inline fun parse_statement(
    context: ParserContext,
    crossinline onBASE: () -> Unit,
    crossinline onPREFIX: () -> Unit,
    crossinline onBASEA: () -> Unit,
    crossinline onPREFIXA: () -> Unit,
    crossinline onIRIREF: () -> Unit,
    crossinline onPNAME_NS: () -> Unit,
    crossinline onBLANK_NODE_LABEL: () -> Unit
) {
    context.clear()
    error@ while (true) {
        when (parse_statement_helper_0(context.c)) {
            0 -> {
                context.append()
                when (parse_statement_helper_1(context.c)) {
                    0 -> {
                        context.append()
                        when (parse_statement_helper_2(context.c)) {
                            0 -> {
                                context.append()
                                when (parse_statement_helper_3(context.c)) {
                                    0 -> {
                                        context.append()
                                        onBASE()
                                        return
                                    }
                                    else -> {
                                        break@error
                                    }
                                }
                            }
                            else -> {
                                break@error
                            }
                        }
                    }
                    else -> {
                        break@error
                    }
                }
            }
            1 -> {
                context.append()
                when (parse_statement_helper_4(context.c)) {
                    0 -> {
                        context.append()
                        when (parse_statement_helper_3(context.c)) {
                            0 -> {
                                context.append()
                                when (parse_statement_helper_5(context.c)) {
                                    0 -> {
                                        context.append()
                                        when (parse_statement_helper_6(context.c)) {
                                            0 -> {
                                                context.append()
                                                when (parse_statement_helper_7(context.c)) {
                                                    0 -> {
                                                        context.append()
                                                        onPREFIX()
                                                        return
                                                    }
                                                    else -> {
                                                        break@error
                                                    }
                                                }
                                            }
                                            else -> {
                                                break@error
                                            }
                                        }
                                    }
                                    else -> {
                                        break@error
                                    }
                                }
                            }
                            else -> {
                                break@error
                            }
                        }
                    }
                    else -> {
                        break@error
                    }
                }
            }
            2 -> {
                context.append()
                when (parse_statement_helper_8(context.c)) {
                    0 -> {
                        context.append()
                        when (parse_statement_helper_9(context.c)) {
                            0 -> {
                                context.append()
                                when (parse_statement_helper_10(context.c)) {
                                    0 -> {
                                        context.append()
                                        when (parse_statement_helper_11(context.c)) {
                                            0 -> {
                                                context.append()
                                                onBASEA()
                                                return
                                            }
                                            else -> {
                                                break@error
                                            }
                                        }
                                    }
                                    else -> {
                                        break@error
                                    }
                                }
                            }
                            else -> {
                                break@error
                            }
                        }
                    }
                    1 -> {
                        context.append()
                        when (parse_statement_helper_12(context.c)) {
                            0 -> {
                                context.append()
                                when (parse_statement_helper_11(context.c)) {
                                    0 -> {
                                        context.append()
                                        when (parse_statement_helper_13(context.c)) {
                                            0 -> {
                                                context.append()
                                                when (parse_statement_helper_14(context.c)) {
                                                    0 -> {
                                                        context.append()
                                                        when (parse_statement_helper_15(context.c)) {
                                                            0 -> {
                                                                context.append()
                                                                onPREFIXA()
                                                                return
                                                            }
                                                            else -> {
                                                                break@error
                                                            }
                                                        }
                                                    }
                                                    else -> {
                                                        break@error
                                                    }
                                                }
                                            }
                                            else -> {
                                                break@error
                                            }
                                        }
                                    }
                                    else -> {
                                        break@error
                                    }
                                }
                            }
                            else -> {
                                break@error
                            }
                        }
                    }
                    else -> {
                        break@error
                    }
                }
            }
            3 -> {
                context.append()
                loop3@ while (true) {
                    when (parse_statement_helper_16(context.c)) {
                        0 -> {
                            context.append()
                            continue@loop3
                        }
                        1 -> {
                            context.append()
                            when (parse_statement_helper_17(context.c)) {
                                0 -> {
                                    context.append()
                                    when (parse_statement_helper_18(context.c)) {
                                        0 -> {
                                            context.append()
                                            when (parse_statement_helper_18(context.c)) {
                                                0 -> {
                                                    context.append()
                                                    when (parse_statement_helper_18(context.c)) {
                                                        0 -> {
                                                            context.append()
                                                            when (parse_statement_helper_18(context.c)) {
                                                                0 -> {
                                                                    context.append()
                                                                    continue@loop3
                                                                }
                                                                else -> {
                                                                    break@error
                                                                }
                                                            }
                                                        }
                                                        else -> {
                                                            break@error
                                                        }
                                                    }
                                                }
                                                else -> {
                                                    break@error
                                                }
                                            }
                                        }
                                        else -> {
                                            break@error
                                        }
                                    }
                                }
                                1 -> {
                                    context.append()
                                    when (parse_statement_helper_18(context.c)) {
                                        0 -> {
                                            context.append()
                                            when (parse_statement_helper_18(context.c)) {
                                                0 -> {
                                                    context.append()
                                                    when (parse_statement_helper_18(context.c)) {
                                                        0 -> {
                                                            context.append()
                                                            when (parse_statement_helper_18(context.c)) {
                                                                0 -> {
                                                                    context.append()
                                                                    when (parse_statement_helper_18(context.c)) {
                                                                        0 -> {
                                                                            context.append()
                                                                            when (parse_statement_helper_18(context.c)) {
                                                                                0 -> {
                                                                                    context.append()
                                                                                    when (parse_statement_helper_18(context.c)) {
                                                                                        0 -> {
                                                                                            context.append()
                                                                                            when (parse_statement_helper_18(context.c)) {
                                                                                                0 -> {
                                                                                                    context.append()
                                                                                                    continue@loop3
                                                                                                }
                                                                                                else -> {
                                                                                                    break@error
                                                                                                }
                                                                                            }
                                                                                        }
                                                                                        else -> {
                                                                                            break@error
                                                                                        }
                                                                                    }
                                                                                }
                                                                                else -> {
                                                                                    break@error
                                                                                }
                                                                            }
                                                                        }
                                                                        else -> {
                                                                            break@error
                                                                        }
                                                                    }
                                                                }
                                                                else -> {
                                                                    break@error
                                                                }
                                                            }
                                                        }
                                                        else -> {
                                                            break@error
                                                        }
                                                    }
                                                }
                                                else -> {
                                                    break@error
                                                }
                                            }
                                        }
                                        else -> {
                                            break@error
                                        }
                                    }
                                }
                                else -> {
                                    break@error
                                }
                            }
                        }
                        else -> {
                            break@loop3
                        }
                    }
                }
                when (parse_statement_helper_19(context.c)) {
                    0 -> {
                        context.append()
                        onIRIREF()
                        return
                    }
                    else -> {
                        break@error
                    }
                }
            }
            4 -> {
                context.append()
                loop3@ while (true) {
                    loop4@ while (true) {
                        when (context.c) {
                            0x2e -> {
                                context.append()
                            }
                            else -> {
                                break@loop4
                            }
                        }
                    }
                    when (parse_statement_helper_20(context.c)) {
                        0 -> {
                            context.append()
                            continue@loop3
                        }
                        else -> {
                            break@loop3
                        }
                    }
                }
                when (parse_statement_helper_21(context.c)) {
                    0 -> {
                        context.append()
                        onPNAME_NS()
                        return
                    }
                    else -> {
                        break@error
                    }
                }
            }
            5 -> {
                context.append()
                onPNAME_NS()
                return
            }
            6 -> {
                context.append()
                when (parse_statement_helper_21(context.c)) {
                    0 -> {
                        context.append()
                        when (parse_statement_helper_22(context.c)) {
                            0 -> {
                                context.append()
                                loop7@ while (true) {
                                    loop8@ while (true) {
                                        when (context.c) {
                                            0x2e -> {
                                                context.append()
                                            }
                                            else -> {
                                                break@loop8
                                            }
                                        }
                                    }
                                    when (parse_statement_helper_20(context.c)) {
                                        0 -> {
                                            context.append()
                                            continue@loop7
                                        }
                                        else -> {
                                            break@loop7
                                        }
                                    }
                                }
                                onBLANK_NODE_LABEL()
                                return
                            }
                            else -> {
                                break@error
                            }
                        }
                    }
                    else -> {
                        break@error
                    }
                }
            }
            else -> {
                break@error
            }
        }
    }
    throw ParserExceptionUnexpectedChar(context)
}

internal fun parse_statement_helper_0(c: Int): Int {
    if (c < 0x3a) {
        return 7
    } else if (c <= 0x3a) {
        return 5
    } else if (c < 0x3c) {
        return 7
    } else if (c <= 0x3c) {
        return 3
    } else if (c < 0x40) {
        return 7
    } else if (c <= 0x40) {
        return 2
    } else if (c < 0x41) {
        return 7
    } else if (c <= 0x5a) {
        return 4
    } else if (c <= 0x42) {
        return 0
    } else if (c < 0x50) {
        return 7
    } else if (c <= 0x50) {
        return 1
    } else if (c < 0x5f) {
        return 7
    } else if (c <= 0x5f) {
        return 6
    } else if (c < 0x61) {
        return 7
    } else if (c <= 0x7a) {
        return 4
    } else if (c < 0xc0) {
        return 7
    } else if (c <= 0xd6) {
        return 4
    } else if (c < 0xd8) {
        return 7
    } else if (c <= 0xf6) {
        return 4
    } else if (c < 0xf8) {
        return 7
    } else if (c <= 0x2ff) {
        return 4
    } else if (c < 0x370) {
        return 7
    } else if (c <= 0x37d) {
        return 4
    } else if (c < 0x37f) {
        return 7
    } else if (c <= 0x1fff) {
        return 4
    } else if (c < 0x200c) {
        return 7
    } else if (c <= 0x200d) {
        return 4
    } else if (c < 0x2070) {
        return 7
    } else if (c <= 0x218f) {
        return 4
    } else if (c < 0x2c00) {
        return 7
    } else if (c <= 0x2fef) {
        return 4
    } else if (c < 0x3001) {
        return 7
    } else if (c <= 0xd7ff) {
        return 4
    } else if (c < 0xf900) {
        return 7
    } else if (c <= 0xfdcf) {
        return 4
    } else if (c < 0xfdf0) {
        return 7
    } else if (c <= 0xfffd) {
        return 4
    } else if (c < 0x10000) {
        return 7
    } else if (c <= 0x1fffff) {
        return 4
    } else {
        return 7
    }
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun parse_statement_helper_1(c: Int): Int {
    return if (c == 0x41) {
        0
    } else {
        1
    }
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun parse_statement_helper_2(c: Int): Int {
    return if (c == 0x53) {
        0
    } else {
        1
    }
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun parse_statement_helper_3(c: Int): Int {
    return if (c == 0x45) {
        0
    } else {
        1
    }
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun parse_statement_helper_4(c: Int): Int {
    return if (c == 0x52) {
        0
    } else {
        1
    }
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun parse_statement_helper_5(c: Int): Int {
    return if (c == 0x46) {
        0
    } else {
        1
    }
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun parse_statement_helper_6(c: Int): Int {
    return if (c == 0x49) {
        0
    } else {
        1
    }
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun parse_statement_helper_7(c: Int): Int {
    return if (c == 0x58) {
        0
    } else {
        1
    }
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun parse_statement_helper_8(c: Int): Int {
    return if (c < 0x62) {
        2
    } else if (c <= 0x62) {
        0
    } else if (c < 0x70) {
        2
    } else if (c <= 0x70) {
        1
    } else {
        2
    }
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun parse_statement_helper_9(c: Int): Int {
    return if (c == 0x61) {
        0
    } else {
        1
    }
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun parse_statement_helper_10(c: Int): Int {
    return if (c == 0x73) {
        0
    } else {
        1
    }
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun parse_statement_helper_11(c: Int): Int {
    return if (c == 0x65) {
        0
    } else {
        1
    }
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun parse_statement_helper_12(c: Int): Int {
    return if (c == 0x72) {
        0
    } else {
        1
    }
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun parse_statement_helper_13(c: Int): Int {
    return if (c == 0x66) {
        0
    } else {
        1
    }
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun parse_statement_helper_14(c: Int): Int {
    return if (c == 0x69) {
        0
    } else {
        1
    }
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun parse_statement_helper_15(c: Int): Int {
    return if (c == 0x78) {
        0
    } else {
        1
    }
}

internal fun parse_statement_helper_16(c: Int): Int {
    if (c < 0x21) {
        return 2
    } else if (c <= 0x21) {
        return 0
    } else if (c < 0x23) {
        return 2
    } else if (c <= 0x3b) {
        return 0
    } else if (c < 0x3d) {
        return 2
    } else if (c <= 0x3d) {
        return 0
    } else if (c < 0x3f) {
        return 2
    } else if (c <= 0x5b) {
        return 0
    } else if (c < 0x5c) {
        return 2
    } else if (c <= 0x5c) {
        return 1
    } else if (c < 0x5d) {
        return 2
    } else if (c <= 0x5d) {
        return 0
    } else if (c < 0x5f) {
        return 2
    } else if (c <= 0x5f) {
        return 0
    } else if (c < 0x61) {
        return 2
    } else if (c <= 0x7a) {
        return 0
    } else if (c < 0x7e) {
        return 2
    } else if (c <= 0x1fffff) {
        return 0
    } else {
        return 2
    }
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun parse_statement_helper_17(c: Int): Int {
    return if (c < 0x55) {
        2
    } else if (c <= 0x55) {
        1
    } else if (c < 0x75) {
        2
    } else if (c <= 0x75) {
        0
    } else {
        2
    }
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun parse_statement_helper_18(c: Int): Int {
    return if (c < 0x30) {
        1
    } else if (c <= 0x39) {
        0
    } else if (c < 0x41) {
        1
    } else if (c <= 0x46) {
        0
    } else if (c < 0x61) {
        1
    } else if (c <= 0x66) {
        0
    } else {
        1
    }
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun parse_statement_helper_19(c: Int): Int {
    return if (c == 0x3e) {
        0
    } else {
        1
    }
}

internal fun parse_statement_helper_20(c: Int): Int {
    if (c < 0x2d) {
        return 1
    } else if (c <= 0x2d) {
        return 0
    } else if (c < 0x30) {
        return 1
    } else if (c <= 0x39) {
        return 0
    } else if (c < 0x41) {
        return 1
    } else if (c <= 0x5a) {
        return 0
    } else if (c < 0x5f) {
        return 1
    } else if (c <= 0x5f) {
        return 0
    } else if (c < 0x61) {
        return 1
    } else if (c <= 0x7a) {
        return 0
    } else if (c < 0xb7) {
        return 1
    } else if (c <= 0xb7) {
        return 0
    } else if (c < 0xc0) {
        return 1
    } else if (c <= 0xd6) {
        return 0
    } else if (c < 0xd8) {
        return 1
    } else if (c <= 0xf6) {
        return 0
    } else if (c < 0xf8) {
        return 1
    } else if (c <= 0x37d) {
        return 0
    } else if (c < 0x37f) {
        return 1
    } else if (c <= 0x1fff) {
        return 0
    } else if (c < 0x200c) {
        return 1
    } else if (c <= 0x200d) {
        return 0
    } else if (c < 0x203f) {
        return 1
    } else if (c <= 0x2040) {
        return 0
    } else if (c < 0x2070) {
        return 1
    } else if (c <= 0x218f) {
        return 0
    } else if (c < 0x2c00) {
        return 1
    } else if (c <= 0x2fef) {
        return 0
    } else if (c < 0x3001) {
        return 1
    } else if (c <= 0xd7ff) {
        return 0
    } else if (c < 0xf900) {
        return 1
    } else if (c <= 0xfdcf) {
        return 0
    } else if (c < 0xfdf0) {
        return 1
    } else if (c <= 0xfffd) {
        return 0
    } else if (c < 0x10000) {
        return 1
    } else if (c <= 0x1fffff) {
        return 0
    } else {
        return 1
    }
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun parse_statement_helper_21(c: Int): Int {
    return if (c == 0x3a) {
        0
    } else {
        1
    }
}

internal fun parse_statement_helper_22(c: Int): Int {
    if (c < 0x30) {
        return 1
    } else if (c <= 0x39) {
        return 0
    } else if (c < 0x41) {
        return 1
    } else if (c <= 0x5a) {
        return 0
    } else if (c < 0x5f) {
        return 1
    } else if (c <= 0x5f) {
        return 0
    } else if (c < 0x61) {
        return 1
    } else if (c <= 0x7a) {
        return 0
    } else if (c < 0xc0) {
        return 1
    } else if (c <= 0xd6) {
        return 0
    } else if (c < 0xd8) {
        return 1
    } else if (c <= 0xf6) {
        return 0
    } else if (c < 0xf8) {
        return 1
    } else if (c <= 0x2ff) {
        return 0
    } else if (c < 0x370) {
        return 1
    } else if (c <= 0x37d) {
        return 0
    } else if (c < 0x37f) {
        return 1
    } else if (c <= 0x1fff) {
        return 0
    } else if (c < 0x200c) {
        return 1
    } else if (c <= 0x200d) {
        return 0
    } else if (c < 0x2070) {
        return 1
    } else if (c <= 0x218f) {
        return 0
    } else if (c < 0x2c00) {
        return 1
    } else if (c <= 0x2fef) {
        return 0
    } else if (c < 0x3001) {
        return 1
    } else if (c <= 0xd7ff) {
        return 0
    } else if (c < 0xf900) {
        return 1
    } else if (c <= 0xfdcf) {
        return 0
    } else if (c < 0xfdf0) {
        return 1
    } else if (c <= 0xfffd) {
        return 0
    } else if (c < 0x10000) {
        return 1
    } else if (c <= 0x1fffff) {
        return 0
    } else {
        return 1
    }
}

internal inline fun parse_base(
    context: ParserContext,
    crossinline onIRIREF: () -> Unit
) {
    context.clear()
    error@ while (true) {
        when (parse_base_helper_0(context.c)) {
            0 -> {
                context.append()
                loop3@ while (true) {
                    when (parse_base_helper_1(context.c)) {
                        0 -> {
                            context.append()
                            continue@loop3
                        }
                        1 -> {
                            context.append()
                            when (parse_base_helper_2(context.c)) {
                                0 -> {
                                    context.append()
                                    when (parse_base_helper_3(context.c)) {
                                        0 -> {
                                            context.append()
                                            when (parse_base_helper_3(context.c)) {
                                                0 -> {
                                                    context.append()
                                                    when (parse_base_helper_3(context.c)) {
                                                        0 -> {
                                                            context.append()
                                                            when (parse_base_helper_3(context.c)) {
                                                                0 -> {
                                                                    context.append()
                                                                    continue@loop3
                                                                }
                                                                else -> {
                                                                    break@error
                                                                }
                                                            }
                                                        }
                                                        else -> {
                                                            break@error
                                                        }
                                                    }
                                                }
                                                else -> {
                                                    break@error
                                                }
                                            }
                                        }
                                        else -> {
                                            break@error
                                        }
                                    }
                                }
                                1 -> {
                                    context.append()
                                    when (parse_base_helper_3(context.c)) {
                                        0 -> {
                                            context.append()
                                            when (parse_base_helper_3(context.c)) {
                                                0 -> {
                                                    context.append()
                                                    when (parse_base_helper_3(context.c)) {
                                                        0 -> {
                                                            context.append()
                                                            when (parse_base_helper_3(context.c)) {
                                                                0 -> {
                                                                    context.append()
                                                                    when (parse_base_helper_3(context.c)) {
                                                                        0 -> {
                                                                            context.append()
                                                                            when (parse_base_helper_3(context.c)) {
                                                                                0 -> {
                                                                                    context.append()
                                                                                    when (parse_base_helper_3(context.c)) {
                                                                                        0 -> {
                                                                                            context.append()
                                                                                            when (parse_base_helper_3(context.c)) {
                                                                                                0 -> {
                                                                                                    context.append()
                                                                                                    continue@loop3
                                                                                                }
                                                                                                else -> {
                                                                                                    break@error
                                                                                                }
                                                                                            }
                                                                                        }
                                                                                        else -> {
                                                                                            break@error
                                                                                        }
                                                                                    }
                                                                                }
                                                                                else -> {
                                                                                    break@error
                                                                                }
                                                                            }
                                                                        }
                                                                        else -> {
                                                                            break@error
                                                                        }
                                                                    }
                                                                }
                                                                else -> {
                                                                    break@error
                                                                }
                                                            }
                                                        }
                                                        else -> {
                                                            break@error
                                                        }
                                                    }
                                                }
                                                else -> {
                                                    break@error
                                                }
                                            }
                                        }
                                        else -> {
                                            break@error
                                        }
                                    }
                                }
                                else -> {
                                    break@error
                                }
                            }
                        }
                        else -> {
                            break@loop3
                        }
                    }
                }
                when (parse_base_helper_4(context.c)) {
                    0 -> {
                        context.append()
                        onIRIREF()
                        return
                    }
                    else -> {
                        break@error
                    }
                }
            }
            else -> {
                break@error
            }
        }
    }
    throw ParserExceptionUnexpectedChar(context)
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun parse_base_helper_0(c: Int): Int {
    return if (c == 0x3c) {
        0
    } else {
        1
    }
}

internal fun parse_base_helper_1(c: Int): Int {
    if (c < 0x21) {
        return 2
    } else if (c <= 0x21) {
        return 0
    } else if (c < 0x23) {
        return 2
    } else if (c <= 0x3b) {
        return 0
    } else if (c < 0x3d) {
        return 2
    } else if (c <= 0x3d) {
        return 0
    } else if (c < 0x3f) {
        return 2
    } else if (c <= 0x5b) {
        return 0
    } else if (c < 0x5c) {
        return 2
    } else if (c <= 0x5c) {
        return 1
    } else if (c < 0x5d) {
        return 2
    } else if (c <= 0x5d) {
        return 0
    } else if (c < 0x5f) {
        return 2
    } else if (c <= 0x5f) {
        return 0
    } else if (c < 0x61) {
        return 2
    } else if (c <= 0x7a) {
        return 0
    } else if (c < 0x7e) {
        return 2
    } else if (c <= 0x1fffff) {
        return 0
    } else {
        return 2
    }
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun parse_base_helper_2(c: Int): Int {
    return if (c < 0x55) {
        2
    } else if (c <= 0x55) {
        1
    } else if (c < 0x75) {
        2
    } else if (c <= 0x75) {
        0
    } else {
        2
    }
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun parse_base_helper_3(c: Int): Int {
    return if (c < 0x30) {
        1
    } else if (c <= 0x39) {
        0
    } else if (c < 0x41) {
        1
    } else if (c <= 0x46) {
        0
    } else if (c < 0x61) {
        1
    } else if (c <= 0x66) {
        0
    } else {
        1
    }
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun parse_base_helper_4(c: Int): Int {
    return if (c == 0x3e) {
        0
    } else {
        1
    }
}

internal inline fun parse_prefix(
    context: ParserContext,
    crossinline onPNAME_NS: () -> Unit
) {
    context.clear()
    error@ while (true) {
        when (parse_prefix_helper_0(context.c)) {
            0 -> {
                context.append()
                loop3@ while (true) {
                    loop4@ while (true) {
                        when (context.c) {
                            0x2e -> {
                                context.append()
                            }
                            else -> {
                                break@loop4
                            }
                        }
                    }
                    when (parse_prefix_helper_1(context.c)) {
                        0 -> {
                            context.append()
                            continue@loop3
                        }
                        else -> {
                            break@loop3
                        }
                    }
                }
                when (parse_prefix_helper_2(context.c)) {
                    0 -> {
                        context.append()
                        onPNAME_NS()
                        return
                    }
                    else -> {
                        break@error
                    }
                }
            }
            1 -> {
                context.append()
                onPNAME_NS()
                return
            }
            else -> {
                break@error
            }
        }
    }
    throw ParserExceptionUnexpectedChar(context)
}

internal fun parse_prefix_helper_0(c: Int): Int {
    if (c < 0x3a) {
        return 2
    } else if (c <= 0x3a) {
        return 1
    } else if (c < 0x41) {
        return 2
    } else if (c <= 0x5a) {
        return 0
    } else if (c < 0x61) {
        return 2
    } else if (c <= 0x7a) {
        return 0
    } else if (c < 0xc0) {
        return 2
    } else if (c <= 0xd6) {
        return 0
    } else if (c < 0xd8) {
        return 2
    } else if (c <= 0xf6) {
        return 0
    } else if (c < 0xf8) {
        return 2
    } else if (c <= 0x2ff) {
        return 0
    } else if (c < 0x370) {
        return 2
    } else if (c <= 0x37d) {
        return 0
    } else if (c < 0x37f) {
        return 2
    } else if (c <= 0x1fff) {
        return 0
    } else if (c < 0x200c) {
        return 2
    } else if (c <= 0x200d) {
        return 0
    } else if (c < 0x2070) {
        return 2
    } else if (c <= 0x218f) {
        return 0
    } else if (c < 0x2c00) {
        return 2
    } else if (c <= 0x2fef) {
        return 0
    } else if (c < 0x3001) {
        return 2
    } else if (c <= 0xd7ff) {
        return 0
    } else if (c < 0xf900) {
        return 2
    } else if (c <= 0xfdcf) {
        return 0
    } else if (c < 0xfdf0) {
        return 2
    } else if (c <= 0xfffd) {
        return 0
    } else if (c < 0x10000) {
        return 2
    } else if (c <= 0x1fffff) {
        return 0
    } else {
        return 2
    }
}

internal fun parse_prefix_helper_1(c: Int): Int {
    if (c < 0x2d) {
        return 1
    } else if (c <= 0x2d) {
        return 0
    } else if (c < 0x30) {
        return 1
    } else if (c <= 0x39) {
        return 0
    } else if (c < 0x41) {
        return 1
    } else if (c <= 0x5a) {
        return 0
    } else if (c < 0x5f) {
        return 1
    } else if (c <= 0x5f) {
        return 0
    } else if (c < 0x61) {
        return 1
    } else if (c <= 0x7a) {
        return 0
    } else if (c < 0xb7) {
        return 1
    } else if (c <= 0xb7) {
        return 0
    } else if (c < 0xc0) {
        return 1
    } else if (c <= 0xd6) {
        return 0
    } else if (c < 0xd8) {
        return 1
    } else if (c <= 0xf6) {
        return 0
    } else if (c < 0xf8) {
        return 1
    } else if (c <= 0x37d) {
        return 0
    } else if (c < 0x37f) {
        return 1
    } else if (c <= 0x1fff) {
        return 0
    } else if (c < 0x200c) {
        return 1
    } else if (c <= 0x200d) {
        return 0
    } else if (c < 0x203f) {
        return 1
    } else if (c <= 0x2040) {
        return 0
    } else if (c < 0x2070) {
        return 1
    } else if (c <= 0x218f) {
        return 0
    } else if (c < 0x2c00) {
        return 1
    } else if (c <= 0x2fef) {
        return 0
    } else if (c < 0x3001) {
        return 1
    } else if (c <= 0xd7ff) {
        return 0
    } else if (c < 0xf900) {
        return 1
    } else if (c <= 0xfdcf) {
        return 0
    } else if (c < 0xfdf0) {
        return 1
    } else if (c <= 0xfffd) {
        return 0
    } else if (c < 0x10000) {
        return 1
    } else if (c <= 0x1fffff) {
        return 0
    } else {
        return 1
    }
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun parse_prefix_helper_2(c: Int): Int {
    return if (c == 0x3a) {
        0
    } else {
        1
    }
}

internal inline fun parse_prefix2(
    context: ParserContext,
    crossinline onIRIREF: () -> Unit
) {
    context.clear()
    error@ while (true) {
        when (parse_prefix2_helper_0(context.c)) {
            0 -> {
                context.append()
                loop3@ while (true) {
                    when (parse_prefix2_helper_1(context.c)) {
                        0 -> {
                            context.append()
                            continue@loop3
                        }
                        1 -> {
                            context.append()
                            when (parse_prefix2_helper_2(context.c)) {
                                0 -> {
                                    context.append()
                                    when (parse_prefix2_helper_3(context.c)) {
                                        0 -> {
                                            context.append()
                                            when (parse_prefix2_helper_3(context.c)) {
                                                0 -> {
                                                    context.append()
                                                    when (parse_prefix2_helper_3(context.c)) {
                                                        0 -> {
                                                            context.append()
                                                            when (parse_prefix2_helper_3(context.c)) {
                                                                0 -> {
                                                                    context.append()
                                                                    continue@loop3
                                                                }
                                                                else -> {
                                                                    break@error
                                                                }
                                                            }
                                                        }
                                                        else -> {
                                                            break@error
                                                        }
                                                    }
                                                }
                                                else -> {
                                                    break@error
                                                }
                                            }
                                        }
                                        else -> {
                                            break@error
                                        }
                                    }
                                }
                                1 -> {
                                    context.append()
                                    when (parse_prefix2_helper_3(context.c)) {
                                        0 -> {
                                            context.append()
                                            when (parse_prefix2_helper_3(context.c)) {
                                                0 -> {
                                                    context.append()
                                                    when (parse_prefix2_helper_3(context.c)) {
                                                        0 -> {
                                                            context.append()
                                                            when (parse_prefix2_helper_3(context.c)) {
                                                                0 -> {
                                                                    context.append()
                                                                    when (parse_prefix2_helper_3(context.c)) {
                                                                        0 -> {
                                                                            context.append()
                                                                            when (parse_prefix2_helper_3(context.c)) {
                                                                                0 -> {
                                                                                    context.append()
                                                                                    when (parse_prefix2_helper_3(context.c)) {
                                                                                        0 -> {
                                                                                            context.append()
                                                                                            when (parse_prefix2_helper_3(context.c)) {
                                                                                                0 -> {
                                                                                                    context.append()
                                                                                                    continue@loop3
                                                                                                }
                                                                                                else -> {
                                                                                                    break@error
                                                                                                }
                                                                                            }
                                                                                        }
                                                                                        else -> {
                                                                                            break@error
                                                                                        }
                                                                                    }
                                                                                }
                                                                                else -> {
                                                                                    break@error
                                                                                }
                                                                            }
                                                                        }
                                                                        else -> {
                                                                            break@error
                                                                        }
                                                                    }
                                                                }
                                                                else -> {
                                                                    break@error
                                                                }
                                                            }
                                                        }
                                                        else -> {
                                                            break@error
                                                        }
                                                    }
                                                }
                                                else -> {
                                                    break@error
                                                }
                                            }
                                        }
                                        else -> {
                                            break@error
                                        }
                                    }
                                }
                                else -> {
                                    break@error
                                }
                            }
                        }
                        else -> {
                            break@loop3
                        }
                    }
                }
                when (parse_prefix2_helper_4(context.c)) {
                    0 -> {
                        context.append()
                        onIRIREF()
                        return
                    }
                    else -> {
                        break@error
                    }
                }
            }
            else -> {
                break@error
            }
        }
    }
    throw ParserExceptionUnexpectedChar(context)
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun parse_prefix2_helper_0(c: Int): Int {
    return if (c == 0x3c) {
        0
    } else {
        1
    }
}

internal fun parse_prefix2_helper_1(c: Int): Int {
    if (c < 0x21) {
        return 2
    } else if (c <= 0x21) {
        return 0
    } else if (c < 0x23) {
        return 2
    } else if (c <= 0x3b) {
        return 0
    } else if (c < 0x3d) {
        return 2
    } else if (c <= 0x3d) {
        return 0
    } else if (c < 0x3f) {
        return 2
    } else if (c <= 0x5b) {
        return 0
    } else if (c < 0x5c) {
        return 2
    } else if (c <= 0x5c) {
        return 1
    } else if (c < 0x5d) {
        return 2
    } else if (c <= 0x5d) {
        return 0
    } else if (c < 0x5f) {
        return 2
    } else if (c <= 0x5f) {
        return 0
    } else if (c < 0x61) {
        return 2
    } else if (c <= 0x7a) {
        return 0
    } else if (c < 0x7e) {
        return 2
    } else if (c <= 0x1fffff) {
        return 0
    } else {
        return 2
    }
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun parse_prefix2_helper_2(c: Int): Int {
    return if (c < 0x55) {
        2
    } else if (c <= 0x55) {
        1
    } else if (c < 0x75) {
        2
    } else if (c <= 0x75) {
        0
    } else {
        2
    }
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun parse_prefix2_helper_3(c: Int): Int {
    return if (c < 0x30) {
        1
    } else if (c <= 0x39) {
        0
    } else if (c < 0x41) {
        1
    } else if (c <= 0x46) {
        0
    } else if (c < 0x61) {
        1
    } else if (c <= 0x66) {
        0
    } else {
        1
    }
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun parse_prefix2_helper_4(c: Int): Int {
    return if (c == 0x3e) {
        0
    } else {
        1
    }
}

internal inline fun parse_predicate(
    context: ParserContext,
    crossinline onVERBA: () -> Unit,
    crossinline onIRIREF: () -> Unit,
    crossinline onPNAME_NS: () -> Unit
) {
    context.clear()
    error@ while (true) {
        when (parse_predicate_helper_0(context.c)) {
            0 -> {
                context.append()
                onVERBA()
                return
            }
            1 -> {
                context.append()
                loop3@ while (true) {
                    when (parse_predicate_helper_1(context.c)) {
                        0 -> {
                            context.append()
                            continue@loop3
                        }
                        1 -> {
                            context.append()
                            when (parse_predicate_helper_2(context.c)) {
                                0 -> {
                                    context.append()
                                    when (parse_predicate_helper_3(context.c)) {
                                        0 -> {
                                            context.append()
                                            when (parse_predicate_helper_3(context.c)) {
                                                0 -> {
                                                    context.append()
                                                    when (parse_predicate_helper_3(context.c)) {
                                                        0 -> {
                                                            context.append()
                                                            when (parse_predicate_helper_3(context.c)) {
                                                                0 -> {
                                                                    context.append()
                                                                    continue@loop3
                                                                }
                                                                else -> {
                                                                    break@error
                                                                }
                                                            }
                                                        }
                                                        else -> {
                                                            break@error
                                                        }
                                                    }
                                                }
                                                else -> {
                                                    break@error
                                                }
                                            }
                                        }
                                        else -> {
                                            break@error
                                        }
                                    }
                                }
                                1 -> {
                                    context.append()
                                    when (parse_predicate_helper_3(context.c)) {
                                        0 -> {
                                            context.append()
                                            when (parse_predicate_helper_3(context.c)) {
                                                0 -> {
                                                    context.append()
                                                    when (parse_predicate_helper_3(context.c)) {
                                                        0 -> {
                                                            context.append()
                                                            when (parse_predicate_helper_3(context.c)) {
                                                                0 -> {
                                                                    context.append()
                                                                    when (parse_predicate_helper_3(context.c)) {
                                                                        0 -> {
                                                                            context.append()
                                                                            when (parse_predicate_helper_3(context.c)) {
                                                                                0 -> {
                                                                                    context.append()
                                                                                    when (parse_predicate_helper_3(context.c)) {
                                                                                        0 -> {
                                                                                            context.append()
                                                                                            when (parse_predicate_helper_3(context.c)) {
                                                                                                0 -> {
                                                                                                    context.append()
                                                                                                    continue@loop3
                                                                                                }
                                                                                                else -> {
                                                                                                    break@error
                                                                                                }
                                                                                            }
                                                                                        }
                                                                                        else -> {
                                                                                            break@error
                                                                                        }
                                                                                    }
                                                                                }
                                                                                else -> {
                                                                                    break@error
                                                                                }
                                                                            }
                                                                        }
                                                                        else -> {
                                                                            break@error
                                                                        }
                                                                    }
                                                                }
                                                                else -> {
                                                                    break@error
                                                                }
                                                            }
                                                        }
                                                        else -> {
                                                            break@error
                                                        }
                                                    }
                                                }
                                                else -> {
                                                    break@error
                                                }
                                            }
                                        }
                                        else -> {
                                            break@error
                                        }
                                    }
                                }
                                else -> {
                                    break@error
                                }
                            }
                        }
                        else -> {
                            break@loop3
                        }
                    }
                }
                when (parse_predicate_helper_4(context.c)) {
                    0 -> {
                        context.append()
                        onIRIREF()
                        return
                    }
                    else -> {
                        break@error
                    }
                }
            }
            2 -> {
                context.append()
                loop3@ while (true) {
                    loop4@ while (true) {
                        when (context.c) {
                            0x2e -> {
                                context.append()
                            }
                            else -> {
                                break@loop4
                            }
                        }
                    }
                    when (parse_predicate_helper_5(context.c)) {
                        0 -> {
                            context.append()
                            continue@loop3
                        }
                        else -> {
                            break@loop3
                        }
                    }
                }
                when (parse_predicate_helper_6(context.c)) {
                    0 -> {
                        context.append()
                        onPNAME_NS()
                        return
                    }
                    else -> {
                        break@error
                    }
                }
            }
            3 -> {
                context.append()
                onPNAME_NS()
                return
            }
            else -> {
                break@error
            }
        }
    }
    throw ParserExceptionUnexpectedChar(context)
}

internal fun parse_predicate_helper_0(c: Int): Int {
    if (c < 0x3a) {
        return 4
    } else if (c <= 0x3a) {
        return 3
    } else if (c < 0x3c) {
        return 4
    } else if (c <= 0x3c) {
        return 1
    } else if (c < 0x41) {
        return 4
    } else if (c <= 0x5a) {
        return 2
    } else if (c < 0x61) {
        return 4
    } else if (c <= 0x61) {
        return 0
    } else if (c <= 0x7a) {
        return 2
    } else if (c < 0xc0) {
        return 4
    } else if (c <= 0xd6) {
        return 2
    } else if (c < 0xd8) {
        return 4
    } else if (c <= 0xf6) {
        return 2
    } else if (c < 0xf8) {
        return 4
    } else if (c <= 0x2ff) {
        return 2
    } else if (c < 0x370) {
        return 4
    } else if (c <= 0x37d) {
        return 2
    } else if (c < 0x37f) {
        return 4
    } else if (c <= 0x1fff) {
        return 2
    } else if (c < 0x200c) {
        return 4
    } else if (c <= 0x200d) {
        return 2
    } else if (c < 0x2070) {
        return 4
    } else if (c <= 0x218f) {
        return 2
    } else if (c < 0x2c00) {
        return 4
    } else if (c <= 0x2fef) {
        return 2
    } else if (c < 0x3001) {
        return 4
    } else if (c <= 0xd7ff) {
        return 2
    } else if (c < 0xf900) {
        return 4
    } else if (c <= 0xfdcf) {
        return 2
    } else if (c < 0xfdf0) {
        return 4
    } else if (c <= 0xfffd) {
        return 2
    } else if (c < 0x10000) {
        return 4
    } else if (c <= 0x1fffff) {
        return 2
    } else {
        return 4
    }
}

internal fun parse_predicate_helper_1(c: Int): Int {
    if (c < 0x21) {
        return 2
    } else if (c <= 0x21) {
        return 0
    } else if (c < 0x23) {
        return 2
    } else if (c <= 0x3b) {
        return 0
    } else if (c < 0x3d) {
        return 2
    } else if (c <= 0x3d) {
        return 0
    } else if (c < 0x3f) {
        return 2
    } else if (c <= 0x5b) {
        return 0
    } else if (c < 0x5c) {
        return 2
    } else if (c <= 0x5c) {
        return 1
    } else if (c < 0x5d) {
        return 2
    } else if (c <= 0x5d) {
        return 0
    } else if (c < 0x5f) {
        return 2
    } else if (c <= 0x5f) {
        return 0
    } else if (c < 0x61) {
        return 2
    } else if (c <= 0x7a) {
        return 0
    } else if (c < 0x7e) {
        return 2
    } else if (c <= 0x1fffff) {
        return 0
    } else {
        return 2
    }
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun parse_predicate_helper_2(c: Int): Int {
    return if (c < 0x55) {
        2
    } else if (c <= 0x55) {
        1
    } else if (c < 0x75) {
        2
    } else if (c <= 0x75) {
        0
    } else {
        2
    }
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun parse_predicate_helper_3(c: Int): Int {
    return if (c < 0x30) {
        1
    } else if (c <= 0x39) {
        0
    } else if (c < 0x41) {
        1
    } else if (c <= 0x46) {
        0
    } else if (c < 0x61) {
        1
    } else if (c <= 0x66) {
        0
    } else {
        1
    }
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun parse_predicate_helper_4(c: Int): Int {
    return if (c == 0x3e) {
        0
    } else {
        1
    }
}

internal fun parse_predicate_helper_5(c: Int): Int {
    if (c < 0x2d) {
        return 1
    } else if (c <= 0x2d) {
        return 0
    } else if (c < 0x30) {
        return 1
    } else if (c <= 0x39) {
        return 0
    } else if (c < 0x41) {
        return 1
    } else if (c <= 0x5a) {
        return 0
    } else if (c < 0x5f) {
        return 1
    } else if (c <= 0x5f) {
        return 0
    } else if (c < 0x61) {
        return 1
    } else if (c <= 0x7a) {
        return 0
    } else if (c < 0xb7) {
        return 1
    } else if (c <= 0xb7) {
        return 0
    } else if (c < 0xc0) {
        return 1
    } else if (c <= 0xd6) {
        return 0
    } else if (c < 0xd8) {
        return 1
    } else if (c <= 0xf6) {
        return 0
    } else if (c < 0xf8) {
        return 1
    } else if (c <= 0x37d) {
        return 0
    } else if (c < 0x37f) {
        return 1
    } else if (c <= 0x1fff) {
        return 0
    } else if (c < 0x200c) {
        return 1
    } else if (c <= 0x200d) {
        return 0
    } else if (c < 0x203f) {
        return 1
    } else if (c <= 0x2040) {
        return 0
    } else if (c < 0x2070) {
        return 1
    } else if (c <= 0x218f) {
        return 0
    } else if (c < 0x2c00) {
        return 1
    } else if (c <= 0x2fef) {
        return 0
    } else if (c < 0x3001) {
        return 1
    } else if (c <= 0xd7ff) {
        return 0
    } else if (c < 0xf900) {
        return 1
    } else if (c <= 0xfdcf) {
        return 0
    } else if (c < 0xfdf0) {
        return 1
    } else if (c <= 0xfffd) {
        return 0
    } else if (c < 0x10000) {
        return 1
    } else if (c <= 0x1fffff) {
        return 0
    } else {
        return 1
    }
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun parse_predicate_helper_6(c: Int): Int {
    return if (c == 0x3a) {
        0
    } else {
        1
    }
}

internal inline fun parse_obj(
    context: ParserContext,
    crossinline onIRIREF: () -> Unit,
    crossinline onPNAME_NS: () -> Unit,
    crossinline onBLANK_NODE_LABEL: () -> Unit,
    crossinline onSTRING_LITERAL_QUOTE: () -> Unit,
    crossinline onSTRING_LITERAL_SINGLE_QUOTE: () -> Unit,
    crossinline onSTRING_LITERAL_LONG_SINGLE_QUOTE: () -> Unit,
    crossinline onSTRING_LITERAL_LONG_QUOTE: () -> Unit,
    crossinline onINTEGER: () -> Unit,
    crossinline onDECIMAL: () -> Unit,
    crossinline onDOUBLE: () -> Unit,
    crossinline onBOOLEAN: () -> Unit
) {
    context.clear()
    error@ while (true) {
        when (parse_obj_helper_0(context.c)) {
            0 -> {
                context.append()
                loop3@ while (true) {
                    when (parse_obj_helper_1(context.c)) {
                        0 -> {
                            context.append()
                            continue@loop3
                        }
                        1 -> {
                            context.append()
                            when (parse_obj_helper_2(context.c)) {
                                0 -> {
                                    context.append()
                                    when (parse_obj_helper_3(context.c)) {
                                        0 -> {
                                            context.append()
                                            when (parse_obj_helper_3(context.c)) {
                                                0 -> {
                                                    context.append()
                                                    when (parse_obj_helper_3(context.c)) {
                                                        0 -> {
                                                            context.append()
                                                            when (parse_obj_helper_3(context.c)) {
                                                                0 -> {
                                                                    context.append()
                                                                    continue@loop3
                                                                }
                                                                else -> {
                                                                    break@error
                                                                }
                                                            }
                                                        }
                                                        else -> {
                                                            break@error
                                                        }
                                                    }
                                                }
                                                else -> {
                                                    break@error
                                                }
                                            }
                                        }
                                        else -> {
                                            break@error
                                        }
                                    }
                                }
                                1 -> {
                                    context.append()
                                    when (parse_obj_helper_3(context.c)) {
                                        0 -> {
                                            context.append()
                                            when (parse_obj_helper_3(context.c)) {
                                                0 -> {
                                                    context.append()
                                                    when (parse_obj_helper_3(context.c)) {
                                                        0 -> {
                                                            context.append()
                                                            when (parse_obj_helper_3(context.c)) {
                                                                0 -> {
                                                                    context.append()
                                                                    when (parse_obj_helper_3(context.c)) {
                                                                        0 -> {
                                                                            context.append()
                                                                            when (parse_obj_helper_3(context.c)) {
                                                                                0 -> {
                                                                                    context.append()
                                                                                    when (parse_obj_helper_3(context.c)) {
                                                                                        0 -> {
                                                                                            context.append()
                                                                                            when (parse_obj_helper_3(context.c)) {
                                                                                                0 -> {
                                                                                                    context.append()
                                                                                                    continue@loop3
                                                                                                }
                                                                                                else -> {
                                                                                                    break@error
                                                                                                }
                                                                                            }
                                                                                        }
                                                                                        else -> {
                                                                                            break@error
                                                                                        }
                                                                                    }
                                                                                }
                                                                                else -> {
                                                                                    break@error
                                                                                }
                                                                            }
                                                                        }
                                                                        else -> {
                                                                            break@error
                                                                        }
                                                                    }
                                                                }
                                                                else -> {
                                                                    break@error
                                                                }
                                                            }
                                                        }
                                                        else -> {
                                                            break@error
                                                        }
                                                    }
                                                }
                                                else -> {
                                                    break@error
                                                }
                                            }
                                        }
                                        else -> {
                                            break@error
                                        }
                                    }
                                }
                                else -> {
                                    break@error
                                }
                            }
                        }
                        else -> {
                            break@loop3
                        }
                    }
                }
                when (parse_obj_helper_4(context.c)) {
                    0 -> {
                        context.append()
                        onIRIREF()
                        return
                    }
                    else -> {
                        break@error
                    }
                }
            }
            1 -> {
                context.append()
                loop3@ while (true) {
                    loop4@ while (true) {
                        when (context.c) {
                            0x2e -> {
                                context.append()
                            }
                            else -> {
                                break@loop4
                            }
                        }
                    }
                    when (parse_obj_helper_5(context.c)) {
                        0 -> {
                            context.append()
                            continue@loop3
                        }
                        else -> {
                            break@loop3
                        }
                    }
                }
                when (parse_obj_helper_6(context.c)) {
                    0 -> {
                        context.append()
                        onPNAME_NS()
                        return
                    }
                    else -> {
                        break@error
                    }
                }
            }
            2 -> {
                context.append()
                onPNAME_NS()
                return
            }
            3 -> {
                context.append()
                when (parse_obj_helper_6(context.c)) {
                    0 -> {
                        context.append()
                        when (parse_obj_helper_7(context.c)) {
                            0 -> {
                                context.append()
                                loop7@ while (true) {
                                    loop8@ while (true) {
                                        when (context.c) {
                                            0x2e -> {
                                                context.append()
                                            }
                                            else -> {
                                                break@loop8
                                            }
                                        }
                                    }
                                    when (parse_obj_helper_5(context.c)) {
                                        0 -> {
                                            context.append()
                                            continue@loop7
                                        }
                                        else -> {
                                            break@loop7
                                        }
                                    }
                                }
                                onBLANK_NODE_LABEL()
                                return
                            }
                            else -> {
                                break@error
                            }
                        }
                    }
                    else -> {
                        break@error
                    }
                }
            }
            4 -> {
                context.append()
                when (parse_obj_helper_8(context.c)) {
                    0 -> {
                        context.append()
                        loop5@ while (true) {
                            when (parse_obj_helper_9(context.c)) {
                                0 -> {
                                    context.append()
                                    continue@loop5
                                }
                                1 -> {
                                    context.append()
                                    when (parse_obj_helper_10(context.c)) {
                                        0 -> {
                                            context.append()
                                            continue@loop5
                                        }
                                        1 -> {
                                            context.append()
                                            when (parse_obj_helper_3(context.c)) {
                                                0 -> {
                                                    context.append()
                                                    when (parse_obj_helper_3(context.c)) {
                                                        0 -> {
                                                            context.append()
                                                            when (parse_obj_helper_3(context.c)) {
                                                                0 -> {
                                                                    context.append()
                                                                    when (parse_obj_helper_3(context.c)) {
                                                                        0 -> {
                                                                            context.append()
                                                                            continue@loop5
                                                                        }
                                                                        else -> {
                                                                            break@error
                                                                        }
                                                                    }
                                                                }
                                                                else -> {
                                                                    break@error
                                                                }
                                                            }
                                                        }
                                                        else -> {
                                                            break@error
                                                        }
                                                    }
                                                }
                                                else -> {
                                                    break@error
                                                }
                                            }
                                        }
                                        2 -> {
                                            context.append()
                                            when (parse_obj_helper_3(context.c)) {
                                                0 -> {
                                                    context.append()
                                                    when (parse_obj_helper_3(context.c)) {
                                                        0 -> {
                                                            context.append()
                                                            when (parse_obj_helper_3(context.c)) {
                                                                0 -> {
                                                                    context.append()
                                                                    when (parse_obj_helper_3(context.c)) {
                                                                        0 -> {
                                                                            context.append()
                                                                            when (parse_obj_helper_3(context.c)) {
                                                                                0 -> {
                                                                                    context.append()
                                                                                    when (parse_obj_helper_3(context.c)) {
                                                                                        0 -> {
                                                                                            context.append()
                                                                                            when (parse_obj_helper_3(context.c)) {
                                                                                                0 -> {
                                                                                                    context.append()
                                                                                                    when (parse_obj_helper_3(context.c)) {
                                                                                                        0 -> {
                                                                                                            context.append()
                                                                                                            continue@loop5
                                                                                                        }
                                                                                                        else -> {
                                                                                                            break@error
                                                                                                        }
                                                                                                    }
                                                                                                }
                                                                                                else -> {
                                                                                                    break@error
                                                                                                }
                                                                                            }
                                                                                        }
                                                                                        else -> {
                                                                                            break@error
                                                                                        }
                                                                                    }
                                                                                }
                                                                                else -> {
                                                                                    break@error
                                                                                }
                                                                            }
                                                                        }
                                                                        else -> {
                                                                            break@error
                                                                        }
                                                                    }
                                                                }
                                                                else -> {
                                                                    break@error
                                                                }
                                                            }
                                                        }
                                                        else -> {
                                                            break@error
                                                        }
                                                    }
                                                }
                                                else -> {
                                                    break@error
                                                }
                                            }
                                        }
                                        else -> {
                                            break@error
                                        }
                                    }
                                }
                                else -> {
                                    break@loop5
                                }
                            }
                        }
                        when (parse_obj_helper_11(context.c)) {
                            0 -> {
                                context.append()
                                onSTRING_LITERAL_QUOTE()
                                return
                            }
                            else -> {
                                break@error
                            }
                        }
                    }
                    1 -> {
                        context.append()
                        when (parse_obj_helper_10(context.c)) {
                            0 -> {
                                context.append()
                                loop7@ while (true) {
                                    when (parse_obj_helper_9(context.c)) {
                                        0 -> {
                                            context.append()
                                            continue@loop7
                                        }
                                        1 -> {
                                            context.append()
                                            when (parse_obj_helper_10(context.c)) {
                                                0 -> {
                                                    context.append()
                                                    continue@loop7
                                                }
                                                1 -> {
                                                    context.append()
                                                    when (parse_obj_helper_3(context.c)) {
                                                        0 -> {
                                                            context.append()
                                                            when (parse_obj_helper_3(context.c)) {
                                                                0 -> {
                                                                    context.append()
                                                                    when (parse_obj_helper_3(context.c)) {
                                                                        0 -> {
                                                                            context.append()
                                                                            when (parse_obj_helper_3(context.c)) {
                                                                                0 -> {
                                                                                    context.append()
                                                                                    continue@loop7
                                                                                }
                                                                                else -> {
                                                                                    break@error
                                                                                }
                                                                            }
                                                                        }
                                                                        else -> {
                                                                            break@error
                                                                        }
                                                                    }
                                                                }
                                                                else -> {
                                                                    break@error
                                                                }
                                                            }
                                                        }
                                                        else -> {
                                                            break@error
                                                        }
                                                    }
                                                }
                                                2 -> {
                                                    context.append()
                                                    when (parse_obj_helper_3(context.c)) {
                                                        0 -> {
                                                            context.append()
                                                            when (parse_obj_helper_3(context.c)) {
                                                                0 -> {
                                                                    context.append()
                                                                    when (parse_obj_helper_3(context.c)) {
                                                                        0 -> {
                                                                            context.append()
                                                                            when (parse_obj_helper_3(context.c)) {
                                                                                0 -> {
                                                                                    context.append()
                                                                                    when (parse_obj_helper_3(context.c)) {
                                                                                        0 -> {
                                                                                            context.append()
                                                                                            when (parse_obj_helper_3(context.c)) {
                                                                                                0 -> {
                                                                                                    context.append()
                                                                                                    when (parse_obj_helper_3(context.c)) {
                                                                                                        0 -> {
                                                                                                            context.append()
                                                                                                            when (parse_obj_helper_3(context.c)) {
                                                                                                                0 -> {
                                                                                                                    context.append()
                                                                                                                    continue@loop7
                                                                                                                }
                                                                                                                else -> {
                                                                                                                    break@error
                                                                                                                }
                                                                                                            }
                                                                                                        }
                                                                                                        else -> {
                                                                                                            break@error
                                                                                                        }
                                                                                                    }
                                                                                                }
                                                                                                else -> {
                                                                                                    break@error
                                                                                                }
                                                                                            }
                                                                                        }
                                                                                        else -> {
                                                                                            break@error
                                                                                        }
                                                                                    }
                                                                                }
                                                                                else -> {
                                                                                    break@error
                                                                                }
                                                                            }
                                                                        }
                                                                        else -> {
                                                                            break@error
                                                                        }
                                                                    }
                                                                }
                                                                else -> {
                                                                    break@error
                                                                }
                                                            }
                                                        }
                                                        else -> {
                                                            break@error
                                                        }
                                                    }
                                                }
                                                else -> {
                                                    break@error
                                                }
                                            }
                                        }
                                        else -> {
                                            break@loop7
                                        }
                                    }
                                }
                                when (parse_obj_helper_11(context.c)) {
                                    0 -> {
                                        context.append()
                                        onSTRING_LITERAL_QUOTE()
                                        return
                                    }
                                    else -> {
                                        break@error
                                    }
                                }
                            }
                            1 -> {
                                context.append()
                                when (parse_obj_helper_3(context.c)) {
                                    0 -> {
                                        context.append()
                                        when (parse_obj_helper_3(context.c)) {
                                            0 -> {
                                                context.append()
                                                when (parse_obj_helper_3(context.c)) {
                                                    0 -> {
                                                        context.append()
                                                        when (parse_obj_helper_3(context.c)) {
                                                            0 -> {
                                                                context.append()
                                                                loop15@ while (true) {
                                                                    when (parse_obj_helper_9(context.c)) {
                                                                        0 -> {
                                                                            context.append()
                                                                            continue@loop15
                                                                        }
                                                                        1 -> {
                                                                            context.append()
                                                                            when (parse_obj_helper_10(context.c)) {
                                                                                0 -> {
                                                                                    context.append()
                                                                                    continue@loop15
                                                                                }
                                                                                1 -> {
                                                                                    context.append()
                                                                                    when (parse_obj_helper_3(context.c)) {
                                                                                        0 -> {
                                                                                            context.append()
                                                                                            when (parse_obj_helper_3(context.c)) {
                                                                                                0 -> {
                                                                                                    context.append()
                                                                                                    when (parse_obj_helper_3(context.c)) {
                                                                                                        0 -> {
                                                                                                            context.append()
                                                                                                            when (parse_obj_helper_3(context.c)) {
                                                                                                                0 -> {
                                                                                                                    context.append()
                                                                                                                    continue@loop15
                                                                                                                }
                                                                                                                else -> {
                                                                                                                    break@error
                                                                                                                }
                                                                                                            }
                                                                                                        }
                                                                                                        else -> {
                                                                                                            break@error
                                                                                                        }
                                                                                                    }
                                                                                                }
                                                                                                else -> {
                                                                                                    break@error
                                                                                                }
                                                                                            }
                                                                                        }
                                                                                        else -> {
                                                                                            break@error
                                                                                        }
                                                                                    }
                                                                                }
                                                                                2 -> {
                                                                                    context.append()
                                                                                    when (parse_obj_helper_3(context.c)) {
                                                                                        0 -> {
                                                                                            context.append()
                                                                                            when (parse_obj_helper_3(context.c)) {
                                                                                                0 -> {
                                                                                                    context.append()
                                                                                                    when (parse_obj_helper_3(context.c)) {
                                                                                                        0 -> {
                                                                                                            context.append()
                                                                                                            when (parse_obj_helper_3(context.c)) {
                                                                                                                0 -> {
                                                                                                                    context.append()
                                                                                                                    when (parse_obj_helper_3(context.c)) {
                                                                                                                        0 -> {
                                                                                                                            context.append()
                                                                                                                            when (parse_obj_helper_3(context.c)) {
                                                                                                                                0 -> {
                                                                                                                                    context.append()
                                                                                                                                    when (parse_obj_helper_3(context.c)) {
                                                                                                                                        0 -> {
                                                                                                                                            context.append()
                                                                                                                                            when (parse_obj_helper_3(context.c)) {
                                                                                                                                                0 -> {
                                                                                                                                                    context.append()
                                                                                                                                                    continue@loop15
                                                                                                                                                }
                                                                                                                                                else -> {
                                                                                                                                                    break@error
                                                                                                                                                }
                                                                                                                                            }
                                                                                                                                        }
                                                                                                                                        else -> {
                                                                                                                                            break@error
                                                                                                                                        }
                                                                                                                                    }
                                                                                                                                }
                                                                                                                                else -> {
                                                                                                                                    break@error
                                                                                                                                }
                                                                                                                            }
                                                                                                                        }
                                                                                                                        else -> {
                                                                                                                            break@error
                                                                                                                        }
                                                                                                                    }
                                                                                                                }
                                                                                                                else -> {
                                                                                                                    break@error
                                                                                                                }
                                                                                                            }
                                                                                                        }
                                                                                                        else -> {
                                                                                                            break@error
                                                                                                        }
                                                                                                    }
                                                                                                }
                                                                                                else -> {
                                                                                                    break@error
                                                                                                }
                                                                                            }
                                                                                        }
                                                                                        else -> {
                                                                                            break@error
                                                                                        }
                                                                                    }
                                                                                }
                                                                                else -> {
                                                                                    break@error
                                                                                }
                                                                            }
                                                                        }
                                                                        else -> {
                                                                            break@loop15
                                                                        }
                                                                    }
                                                                }
                                                                when (parse_obj_helper_11(context.c)) {
                                                                    0 -> {
                                                                        context.append()
                                                                        onSTRING_LITERAL_QUOTE()
                                                                        return
                                                                    }
                                                                    else -> {
                                                                        break@error
                                                                    }
                                                                }
                                                            }
                                                            else -> {
                                                                break@error
                                                            }
                                                        }
                                                    }
                                                    else -> {
                                                        break@error
                                                    }
                                                }
                                            }
                                            else -> {
                                                break@error
                                            }
                                        }
                                    }
                                    else -> {
                                        break@error
                                    }
                                }
                            }
                            2 -> {
                                context.append()
                                when (parse_obj_helper_3(context.c)) {
                                    0 -> {
                                        context.append()
                                        when (parse_obj_helper_3(context.c)) {
                                            0 -> {
                                                context.append()
                                                when (parse_obj_helper_3(context.c)) {
                                                    0 -> {
                                                        context.append()
                                                        when (parse_obj_helper_3(context.c)) {
                                                            0 -> {
                                                                context.append()
                                                                when (parse_obj_helper_3(context.c)) {
                                                                    0 -> {
                                                                        context.append()
                                                                        when (parse_obj_helper_3(context.c)) {
                                                                            0 -> {
                                                                                context.append()
                                                                                when (parse_obj_helper_3(context.c)) {
                                                                                    0 -> {
                                                                                        context.append()
                                                                                        when (parse_obj_helper_3(context.c)) {
                                                                                            0 -> {
                                                                                                context.append()
                                                                                                loop23@ while (true) {
                                                                                                    when (parse_obj_helper_9(context.c)) {
                                                                                                        0 -> {
                                                                                                            context.append()
                                                                                                            continue@loop23
                                                                                                        }
                                                                                                        1 -> {
                                                                                                            context.append()
                                                                                                            when (parse_obj_helper_10(context.c)) {
                                                                                                                0 -> {
                                                                                                                    context.append()
                                                                                                                    continue@loop23
                                                                                                                }
                                                                                                                1 -> {
                                                                                                                    context.append()
                                                                                                                    when (parse_obj_helper_3(context.c)) {
                                                                                                                        0 -> {
                                                                                                                            context.append()
                                                                                                                            when (parse_obj_helper_3(context.c)) {
                                                                                                                                0 -> {
                                                                                                                                    context.append()
                                                                                                                                    when (parse_obj_helper_3(context.c)) {
                                                                                                                                        0 -> {
                                                                                                                                            context.append()
                                                                                                                                            when (parse_obj_helper_3(context.c)) {
                                                                                                                                                0 -> {
                                                                                                                                                    context.append()
                                                                                                                                                    continue@loop23
                                                                                                                                                }
                                                                                                                                                else -> {
                                                                                                                                                    break@error
                                                                                                                                                }
                                                                                                                                            }
                                                                                                                                        }
                                                                                                                                        else -> {
                                                                                                                                            break@error
                                                                                                                                        }
                                                                                                                                    }
                                                                                                                                }
                                                                                                                                else -> {
                                                                                                                                    break@error
                                                                                                                                }
                                                                                                                            }
                                                                                                                        }
                                                                                                                        else -> {
                                                                                                                            break@error
                                                                                                                        }
                                                                                                                    }
                                                                                                                }
                                                                                                                2 -> {
                                                                                                                    context.append()
                                                                                                                    when (parse_obj_helper_3(context.c)) {
                                                                                                                        0 -> {
                                                                                                                            context.append()
                                                                                                                            when (parse_obj_helper_3(context.c)) {
                                                                                                                                0 -> {
                                                                                                                                    context.append()
                                                                                                                                    when (parse_obj_helper_3(context.c)) {
                                                                                                                                        0 -> {
                                                                                                                                            context.append()
                                                                                                                                            when (parse_obj_helper_3(context.c)) {
                                                                                                                                                0 -> {
                                                                                                                                                    context.append()
                                                                                                                                                    when (parse_obj_helper_3(context.c)) {
                                                                                                                                                        0 -> {
                                                                                                                                                            context.append()
                                                                                                                                                            when (parse_obj_helper_3(context.c)) {
                                                                                                                                                                0 -> {
                                                                                                                                                                    context.append()
                                                                                                                                                                    when (parse_obj_helper_3(context.c)) {
                                                                                                                                                                        0 -> {
                                                                                                                                                                            context.append()
                                                                                                                                                                            when (parse_obj_helper_3(context.c)) {
                                                                                                                                                                                0 -> {
                                                                                                                                                                                    context.append()
                                                                                                                                                                                    continue@loop23
                                                                                                                                                                                }
                                                                                                                                                                                else -> {
                                                                                                                                                                                    break@error
                                                                                                                                                                                }
                                                                                                                                                                            }
                                                                                                                                                                        }
                                                                                                                                                                        else -> {
                                                                                                                                                                            break@error
                                                                                                                                                                        }
                                                                                                                                                                    }
                                                                                                                                                                }
                                                                                                                                                                else -> {
                                                                                                                                                                    break@error
                                                                                                                                                                }
                                                                                                                                                            }
                                                                                                                                                        }
                                                                                                                                                        else -> {
                                                                                                                                                            break@error
                                                                                                                                                        }
                                                                                                                                                    }
                                                                                                                                                }
                                                                                                                                                else -> {
                                                                                                                                                    break@error
                                                                                                                                                }
                                                                                                                                            }
                                                                                                                                        }
                                                                                                                                        else -> {
                                                                                                                                            break@error
                                                                                                                                        }
                                                                                                                                    }
                                                                                                                                }
                                                                                                                                else -> {
                                                                                                                                    break@error
                                                                                                                                }
                                                                                                                            }
                                                                                                                        }
                                                                                                                        else -> {
                                                                                                                            break@error
                                                                                                                        }
                                                                                                                    }
                                                                                                                }
                                                                                                                else -> {
                                                                                                                    break@error
                                                                                                                }
                                                                                                            }
                                                                                                        }
                                                                                                        else -> {
                                                                                                            break@loop23
                                                                                                        }
                                                                                                    }
                                                                                                }
                                                                                                when (parse_obj_helper_11(context.c)) {
                                                                                                    0 -> {
                                                                                                        context.append()
                                                                                                        onSTRING_LITERAL_QUOTE()
                                                                                                        return
                                                                                                    }
                                                                                                    else -> {
                                                                                                        break@error
                                                                                                    }
                                                                                                }
                                                                                            }
                                                                                            else -> {
                                                                                                break@error
                                                                                            }
                                                                                        }
                                                                                    }
                                                                                    else -> {
                                                                                        break@error
                                                                                    }
                                                                                }
                                                                            }
                                                                            else -> {
                                                                                break@error
                                                                            }
                                                                        }
                                                                    }
                                                                    else -> {
                                                                        break@error
                                                                    }
                                                                }
                                                            }
                                                            else -> {
                                                                break@error
                                                            }
                                                        }
                                                    }
                                                    else -> {
                                                        break@error
                                                    }
                                                }
                                            }
                                            else -> {
                                                break@error
                                            }
                                        }
                                    }
                                    else -> {
                                        break@error
                                    }
                                }
                            }
                            else -> {
                                break@error
                            }
                        }
                    }
                    2 -> {
                        context.append()
                        when (parse_obj_helper_11(context.c)) {
                            0 -> {
                                context.append()
                                loop7@ while (true) {
                                    when (parse_obj_helper_12(context.c)) {
                                        0 -> {
                                            context.append()
                                            continue@loop7
                                        }
                                        1 -> {
                                            context.append()
                                            when (parse_obj_helper_10(context.c)) {
                                                0 -> {
                                                    context.append()
                                                    continue@loop7
                                                }
                                                1 -> {
                                                    context.append()
                                                    when (parse_obj_helper_3(context.c)) {
                                                        0 -> {
                                                            context.append()
                                                            when (parse_obj_helper_3(context.c)) {
                                                                0 -> {
                                                                    context.append()
                                                                    when (parse_obj_helper_3(context.c)) {
                                                                        0 -> {
                                                                            context.append()
                                                                            when (parse_obj_helper_3(context.c)) {
                                                                                0 -> {
                                                                                    context.append()
                                                                                    continue@loop7
                                                                                }
                                                                                else -> {
                                                                                    break@error
                                                                                }
                                                                            }
                                                                        }
                                                                        else -> {
                                                                            break@error
                                                                        }
                                                                    }
                                                                }
                                                                else -> {
                                                                    break@error
                                                                }
                                                            }
                                                        }
                                                        else -> {
                                                            break@error
                                                        }
                                                    }
                                                }
                                                2 -> {
                                                    context.append()
                                                    when (parse_obj_helper_3(context.c)) {
                                                        0 -> {
                                                            context.append()
                                                            when (parse_obj_helper_3(context.c)) {
                                                                0 -> {
                                                                    context.append()
                                                                    when (parse_obj_helper_3(context.c)) {
                                                                        0 -> {
                                                                            context.append()
                                                                            when (parse_obj_helper_3(context.c)) {
                                                                                0 -> {
                                                                                    context.append()
                                                                                    when (parse_obj_helper_3(context.c)) {
                                                                                        0 -> {
                                                                                            context.append()
                                                                                            when (parse_obj_helper_3(context.c)) {
                                                                                                0 -> {
                                                                                                    context.append()
                                                                                                    when (parse_obj_helper_3(context.c)) {
                                                                                                        0 -> {
                                                                                                            context.append()
                                                                                                            when (parse_obj_helper_3(context.c)) {
                                                                                                                0 -> {
                                                                                                                    context.append()
                                                                                                                    continue@loop7
                                                                                                                }
                                                                                                                else -> {
                                                                                                                    break@error
                                                                                                                }
                                                                                                            }
                                                                                                        }
                                                                                                        else -> {
                                                                                                            break@error
                                                                                                        }
                                                                                                    }
                                                                                                }
                                                                                                else -> {
                                                                                                    break@error
                                                                                                }
                                                                                            }
                                                                                        }
                                                                                        else -> {
                                                                                            break@error
                                                                                        }
                                                                                    }
                                                                                }
                                                                                else -> {
                                                                                    break@error
                                                                                }
                                                                            }
                                                                        }
                                                                        else -> {
                                                                            break@error
                                                                        }
                                                                    }
                                                                }
                                                                else -> {
                                                                    break@error
                                                                }
                                                            }
                                                        }
                                                        else -> {
                                                            break@error
                                                        }
                                                    }
                                                }
                                                else -> {
                                                    break@error
                                                }
                                            }
                                        }
                                        2 -> {
                                            context.append()
                                            when (parse_obj_helper_12(context.c)) {
                                                0 -> {
                                                    context.append()
                                                    continue@loop7
                                                }
                                                1 -> {
                                                    context.append()
                                                    when (parse_obj_helper_10(context.c)) {
                                                        0 -> {
                                                            context.append()
                                                            continue@loop7
                                                        }
                                                        1 -> {
                                                            context.append()
                                                            when (parse_obj_helper_3(context.c)) {
                                                                0 -> {
                                                                    context.append()
                                                                    when (parse_obj_helper_3(context.c)) {
                                                                        0 -> {
                                                                            context.append()
                                                                            when (parse_obj_helper_3(context.c)) {
                                                                                0 -> {
                                                                                    context.append()
                                                                                    when (parse_obj_helper_3(context.c)) {
                                                                                        0 -> {
                                                                                            context.append()
                                                                                            continue@loop7
                                                                                        }
                                                                                        else -> {
                                                                                            break@error
                                                                                        }
                                                                                    }
                                                                                }
                                                                                else -> {
                                                                                    break@error
                                                                                }
                                                                            }
                                                                        }
                                                                        else -> {
                                                                            break@error
                                                                        }
                                                                    }
                                                                }
                                                                else -> {
                                                                    break@error
                                                                }
                                                            }
                                                        }
                                                        2 -> {
                                                            context.append()
                                                            when (parse_obj_helper_3(context.c)) {
                                                                0 -> {
                                                                    context.append()
                                                                    when (parse_obj_helper_3(context.c)) {
                                                                        0 -> {
                                                                            context.append()
                                                                            when (parse_obj_helper_3(context.c)) {
                                                                                0 -> {
                                                                                    context.append()
                                                                                    when (parse_obj_helper_3(context.c)) {
                                                                                        0 -> {
                                                                                            context.append()
                                                                                            when (parse_obj_helper_3(context.c)) {
                                                                                                0 -> {
                                                                                                    context.append()
                                                                                                    when (parse_obj_helper_3(context.c)) {
                                                                                                        0 -> {
                                                                                                            context.append()
                                                                                                            when (parse_obj_helper_3(context.c)) {
                                                                                                                0 -> {
                                                                                                                    context.append()
                                                                                                                    when (parse_obj_helper_3(context.c)) {
                                                                                                                        0 -> {
                                                                                                                            context.append()
                                                                                                                            continue@loop7
                                                                                                                        }
                                                                                                                        else -> {
                                                                                                                            break@error
                                                                                                                        }
                                                                                                                    }
                                                                                                                }
                                                                                                                else -> {
                                                                                                                    break@error
                                                                                                                }
                                                                                                            }
                                                                                                        }
                                                                                                        else -> {
                                                                                                            break@error
                                                                                                        }
                                                                                                    }
                                                                                                }
                                                                                                else -> {
                                                                                                    break@error
                                                                                                }
                                                                                            }
                                                                                        }
                                                                                        else -> {
                                                                                            break@error
                                                                                        }
                                                                                    }
                                                                                }
                                                                                else -> {
                                                                                    break@error
                                                                                }
                                                                            }
                                                                        }
                                                                        else -> {
                                                                            break@error
                                                                        }
                                                                    }
                                                                }
                                                                else -> {
                                                                    break@error
                                                                }
                                                            }
                                                        }
                                                        else -> {
                                                            break@error
                                                        }
                                                    }
                                                }
                                                2 -> {
                                                    context.append()
                                                    when (parse_obj_helper_12(context.c)) {
                                                        0 -> {
                                                            context.append()
                                                            continue@loop7
                                                        }
                                                        1 -> {
                                                            context.append()
                                                            when (parse_obj_helper_10(context.c)) {
                                                                0 -> {
                                                                    context.append()
                                                                    continue@loop7
                                                                }
                                                                1 -> {
                                                                    context.append()
                                                                    when (parse_obj_helper_3(context.c)) {
                                                                        0 -> {
                                                                            context.append()
                                                                            when (parse_obj_helper_3(context.c)) {
                                                                                0 -> {
                                                                                    context.append()
                                                                                    when (parse_obj_helper_3(context.c)) {
                                                                                        0 -> {
                                                                                            context.append()
                                                                                            when (parse_obj_helper_3(context.c)) {
                                                                                                0 -> {
                                                                                                    context.append()
                                                                                                    continue@loop7
                                                                                                }
                                                                                                else -> {
                                                                                                    break@error
                                                                                                }
                                                                                            }
                                                                                        }
                                                                                        else -> {
                                                                                            break@error
                                                                                        }
                                                                                    }
                                                                                }
                                                                                else -> {
                                                                                    break@error
                                                                                }
                                                                            }
                                                                        }
                                                                        else -> {
                                                                            break@error
                                                                        }
                                                                    }
                                                                }
                                                                2 -> {
                                                                    context.append()
                                                                    when (parse_obj_helper_3(context.c)) {
                                                                        0 -> {
                                                                            context.append()
                                                                            when (parse_obj_helper_3(context.c)) {
                                                                                0 -> {
                                                                                    context.append()
                                                                                    when (parse_obj_helper_3(context.c)) {
                                                                                        0 -> {
                                                                                            context.append()
                                                                                            when (parse_obj_helper_3(context.c)) {
                                                                                                0 -> {
                                                                                                    context.append()
                                                                                                    when (parse_obj_helper_3(context.c)) {
                                                                                                        0 -> {
                                                                                                            context.append()
                                                                                                            when (parse_obj_helper_3(context.c)) {
                                                                                                                0 -> {
                                                                                                                    context.append()
                                                                                                                    when (parse_obj_helper_3(context.c)) {
                                                                                                                        0 -> {
                                                                                                                            context.append()
                                                                                                                            when (parse_obj_helper_3(context.c)) {
                                                                                                                                0 -> {
                                                                                                                                    context.append()
                                                                                                                                    continue@loop7
                                                                                                                                }
                                                                                                                                else -> {
                                                                                                                                    break@error
                                                                                                                                }
                                                                                                                            }
                                                                                                                        }
                                                                                                                        else -> {
                                                                                                                            break@error
                                                                                                                        }
                                                                                                                    }
                                                                                                                }
                                                                                                                else -> {
                                                                                                                    break@error
                                                                                                                }
                                                                                                            }
                                                                                                        }
                                                                                                        else -> {
                                                                                                            break@error
                                                                                                        }
                                                                                                    }
                                                                                                }
                                                                                                else -> {
                                                                                                    break@error
                                                                                                }
                                                                                            }
                                                                                        }
                                                                                        else -> {
                                                                                            break@error
                                                                                        }
                                                                                    }
                                                                                }
                                                                                else -> {
                                                                                    break@error
                                                                                }
                                                                            }
                                                                        }
                                                                        else -> {
                                                                            break@error
                                                                        }
                                                                    }
                                                                }
                                                                else -> {
                                                                    break@error
                                                                }
                                                            }
                                                        }
                                                        2 -> {
                                                            context.append()
                                                            onSTRING_LITERAL_LONG_QUOTE()
                                                            return
                                                        }
                                                        else -> {
                                                            break@error
                                                        }
                                                    }
                                                }
                                                else -> {
                                                    break@error
                                                }
                                            }
                                        }
                                        else -> {
                                            break@loop7
                                        }
                                    }
                                }
                                break@error
                            }
                            else -> {
                                onSTRING_LITERAL_QUOTE()
                                return
                            }
                        }
                    }
                    else -> {
                        break@error
                    }
                }
            }
            5 -> {
                context.append()
                when (parse_obj_helper_13(context.c)) {
                    0 -> {
                        context.append()
                        loop5@ while (true) {
                            when (parse_obj_helper_14(context.c)) {
                                0 -> {
                                    context.append()
                                    continue@loop5
                                }
                                1 -> {
                                    context.append()
                                    when (parse_obj_helper_10(context.c)) {
                                        0 -> {
                                            context.append()
                                            continue@loop5
                                        }
                                        1 -> {
                                            context.append()
                                            when (parse_obj_helper_3(context.c)) {
                                                0 -> {
                                                    context.append()
                                                    when (parse_obj_helper_3(context.c)) {
                                                        0 -> {
                                                            context.append()
                                                            when (parse_obj_helper_3(context.c)) {
                                                                0 -> {
                                                                    context.append()
                                                                    when (parse_obj_helper_3(context.c)) {
                                                                        0 -> {
                                                                            context.append()
                                                                            continue@loop5
                                                                        }
                                                                        else -> {
                                                                            break@error
                                                                        }
                                                                    }
                                                                }
                                                                else -> {
                                                                    break@error
                                                                }
                                                            }
                                                        }
                                                        else -> {
                                                            break@error
                                                        }
                                                    }
                                                }
                                                else -> {
                                                    break@error
                                                }
                                            }
                                        }
                                        2 -> {
                                            context.append()
                                            when (parse_obj_helper_3(context.c)) {
                                                0 -> {
                                                    context.append()
                                                    when (parse_obj_helper_3(context.c)) {
                                                        0 -> {
                                                            context.append()
                                                            when (parse_obj_helper_3(context.c)) {
                                                                0 -> {
                                                                    context.append()
                                                                    when (parse_obj_helper_3(context.c)) {
                                                                        0 -> {
                                                                            context.append()
                                                                            when (parse_obj_helper_3(context.c)) {
                                                                                0 -> {
                                                                                    context.append()
                                                                                    when (parse_obj_helper_3(context.c)) {
                                                                                        0 -> {
                                                                                            context.append()
                                                                                            when (parse_obj_helper_3(context.c)) {
                                                                                                0 -> {
                                                                                                    context.append()
                                                                                                    when (parse_obj_helper_3(context.c)) {
                                                                                                        0 -> {
                                                                                                            context.append()
                                                                                                            continue@loop5
                                                                                                        }
                                                                                                        else -> {
                                                                                                            break@error
                                                                                                        }
                                                                                                    }
                                                                                                }
                                                                                                else -> {
                                                                                                    break@error
                                                                                                }
                                                                                            }
                                                                                        }
                                                                                        else -> {
                                                                                            break@error
                                                                                        }
                                                                                    }
                                                                                }
                                                                                else -> {
                                                                                    break@error
                                                                                }
                                                                            }
                                                                        }
                                                                        else -> {
                                                                            break@error
                                                                        }
                                                                    }
                                                                }
                                                                else -> {
                                                                    break@error
                                                                }
                                                            }
                                                        }
                                                        else -> {
                                                            break@error
                                                        }
                                                    }
                                                }
                                                else -> {
                                                    break@error
                                                }
                                            }
                                        }
                                        else -> {
                                            break@error
                                        }
                                    }
                                }
                                else -> {
                                    break@loop5
                                }
                            }
                        }
                        when (parse_obj_helper_15(context.c)) {
                            0 -> {
                                context.append()
                                onSTRING_LITERAL_SINGLE_QUOTE()
                                return
                            }
                            else -> {
                                break@error
                            }
                        }
                    }
                    1 -> {
                        context.append()
                        when (parse_obj_helper_10(context.c)) {
                            0 -> {
                                context.append()
                                loop7@ while (true) {
                                    when (parse_obj_helper_14(context.c)) {
                                        0 -> {
                                            context.append()
                                            continue@loop7
                                        }
                                        1 -> {
                                            context.append()
                                            when (parse_obj_helper_10(context.c)) {
                                                0 -> {
                                                    context.append()
                                                    continue@loop7
                                                }
                                                1 -> {
                                                    context.append()
                                                    when (parse_obj_helper_3(context.c)) {
                                                        0 -> {
                                                            context.append()
                                                            when (parse_obj_helper_3(context.c)) {
                                                                0 -> {
                                                                    context.append()
                                                                    when (parse_obj_helper_3(context.c)) {
                                                                        0 -> {
                                                                            context.append()
                                                                            when (parse_obj_helper_3(context.c)) {
                                                                                0 -> {
                                                                                    context.append()
                                                                                    continue@loop7
                                                                                }
                                                                                else -> {
                                                                                    break@error
                                                                                }
                                                                            }
                                                                        }
                                                                        else -> {
                                                                            break@error
                                                                        }
                                                                    }
                                                                }
                                                                else -> {
                                                                    break@error
                                                                }
                                                            }
                                                        }
                                                        else -> {
                                                            break@error
                                                        }
                                                    }
                                                }
                                                2 -> {
                                                    context.append()
                                                    when (parse_obj_helper_3(context.c)) {
                                                        0 -> {
                                                            context.append()
                                                            when (parse_obj_helper_3(context.c)) {
                                                                0 -> {
                                                                    context.append()
                                                                    when (parse_obj_helper_3(context.c)) {
                                                                        0 -> {
                                                                            context.append()
                                                                            when (parse_obj_helper_3(context.c)) {
                                                                                0 -> {
                                                                                    context.append()
                                                                                    when (parse_obj_helper_3(context.c)) {
                                                                                        0 -> {
                                                                                            context.append()
                                                                                            when (parse_obj_helper_3(context.c)) {
                                                                                                0 -> {
                                                                                                    context.append()
                                                                                                    when (parse_obj_helper_3(context.c)) {
                                                                                                        0 -> {
                                                                                                            context.append()
                                                                                                            when (parse_obj_helper_3(context.c)) {
                                                                                                                0 -> {
                                                                                                                    context.append()
                                                                                                                    continue@loop7
                                                                                                                }
                                                                                                                else -> {
                                                                                                                    break@error
                                                                                                                }
                                                                                                            }
                                                                                                        }
                                                                                                        else -> {
                                                                                                            break@error
                                                                                                        }
                                                                                                    }
                                                                                                }
                                                                                                else -> {
                                                                                                    break@error
                                                                                                }
                                                                                            }
                                                                                        }
                                                                                        else -> {
                                                                                            break@error
                                                                                        }
                                                                                    }
                                                                                }
                                                                                else -> {
                                                                                    break@error
                                                                                }
                                                                            }
                                                                        }
                                                                        else -> {
                                                                            break@error
                                                                        }
                                                                    }
                                                                }
                                                                else -> {
                                                                    break@error
                                                                }
                                                            }
                                                        }
                                                        else -> {
                                                            break@error
                                                        }
                                                    }
                                                }
                                                else -> {
                                                    break@error
                                                }
                                            }
                                        }
                                        else -> {
                                            break@loop7
                                        }
                                    }
                                }
                                when (parse_obj_helper_15(context.c)) {
                                    0 -> {
                                        context.append()
                                        onSTRING_LITERAL_SINGLE_QUOTE()
                                        return
                                    }
                                    else -> {
                                        break@error
                                    }
                                }
                            }
                            1 -> {
                                context.append()
                                when (parse_obj_helper_3(context.c)) {
                                    0 -> {
                                        context.append()
                                        when (parse_obj_helper_3(context.c)) {
                                            0 -> {
                                                context.append()
                                                when (parse_obj_helper_3(context.c)) {
                                                    0 -> {
                                                        context.append()
                                                        when (parse_obj_helper_3(context.c)) {
                                                            0 -> {
                                                                context.append()
                                                                loop15@ while (true) {
                                                                    when (parse_obj_helper_14(context.c)) {
                                                                        0 -> {
                                                                            context.append()
                                                                            continue@loop15
                                                                        }
                                                                        1 -> {
                                                                            context.append()
                                                                            when (parse_obj_helper_10(context.c)) {
                                                                                0 -> {
                                                                                    context.append()
                                                                                    continue@loop15
                                                                                }
                                                                                1 -> {
                                                                                    context.append()
                                                                                    when (parse_obj_helper_3(context.c)) {
                                                                                        0 -> {
                                                                                            context.append()
                                                                                            when (parse_obj_helper_3(context.c)) {
                                                                                                0 -> {
                                                                                                    context.append()
                                                                                                    when (parse_obj_helper_3(context.c)) {
                                                                                                        0 -> {
                                                                                                            context.append()
                                                                                                            when (parse_obj_helper_3(context.c)) {
                                                                                                                0 -> {
                                                                                                                    context.append()
                                                                                                                    continue@loop15
                                                                                                                }
                                                                                                                else -> {
                                                                                                                    break@error
                                                                                                                }
                                                                                                            }
                                                                                                        }
                                                                                                        else -> {
                                                                                                            break@error
                                                                                                        }
                                                                                                    }
                                                                                                }
                                                                                                else -> {
                                                                                                    break@error
                                                                                                }
                                                                                            }
                                                                                        }
                                                                                        else -> {
                                                                                            break@error
                                                                                        }
                                                                                    }
                                                                                }
                                                                                2 -> {
                                                                                    context.append()
                                                                                    when (parse_obj_helper_3(context.c)) {
                                                                                        0 -> {
                                                                                            context.append()
                                                                                            when (parse_obj_helper_3(context.c)) {
                                                                                                0 -> {
                                                                                                    context.append()
                                                                                                    when (parse_obj_helper_3(context.c)) {
                                                                                                        0 -> {
                                                                                                            context.append()
                                                                                                            when (parse_obj_helper_3(context.c)) {
                                                                                                                0 -> {
                                                                                                                    context.append()
                                                                                                                    when (parse_obj_helper_3(context.c)) {
                                                                                                                        0 -> {
                                                                                                                            context.append()
                                                                                                                            when (parse_obj_helper_3(context.c)) {
                                                                                                                                0 -> {
                                                                                                                                    context.append()
                                                                                                                                    when (parse_obj_helper_3(context.c)) {
                                                                                                                                        0 -> {
                                                                                                                                            context.append()
                                                                                                                                            when (parse_obj_helper_3(context.c)) {
                                                                                                                                                0 -> {
                                                                                                                                                    context.append()
                                                                                                                                                    continue@loop15
                                                                                                                                                }
                                                                                                                                                else -> {
                                                                                                                                                    break@error
                                                                                                                                                }
                                                                                                                                            }
                                                                                                                                        }
                                                                                                                                        else -> {
                                                                                                                                            break@error
                                                                                                                                        }
                                                                                                                                    }
                                                                                                                                }
                                                                                                                                else -> {
                                                                                                                                    break@error
                                                                                                                                }
                                                                                                                            }
                                                                                                                        }
                                                                                                                        else -> {
                                                                                                                            break@error
                                                                                                                        }
                                                                                                                    }
                                                                                                                }
                                                                                                                else -> {
                                                                                                                    break@error
                                                                                                                }
                                                                                                            }
                                                                                                        }
                                                                                                        else -> {
                                                                                                            break@error
                                                                                                        }
                                                                                                    }
                                                                                                }
                                                                                                else -> {
                                                                                                    break@error
                                                                                                }
                                                                                            }
                                                                                        }
                                                                                        else -> {
                                                                                            break@error
                                                                                        }
                                                                                    }
                                                                                }
                                                                                else -> {
                                                                                    break@error
                                                                                }
                                                                            }
                                                                        }
                                                                        else -> {
                                                                            break@loop15
                                                                        }
                                                                    }
                                                                }
                                                                when (parse_obj_helper_15(context.c)) {
                                                                    0 -> {
                                                                        context.append()
                                                                        onSTRING_LITERAL_SINGLE_QUOTE()
                                                                        return
                                                                    }
                                                                    else -> {
                                                                        break@error
                                                                    }
                                                                }
                                                            }
                                                            else -> {
                                                                break@error
                                                            }
                                                        }
                                                    }
                                                    else -> {
                                                        break@error
                                                    }
                                                }
                                            }
                                            else -> {
                                                break@error
                                            }
                                        }
                                    }
                                    else -> {
                                        break@error
                                    }
                                }
                            }
                            2 -> {
                                context.append()
                                when (parse_obj_helper_3(context.c)) {
                                    0 -> {
                                        context.append()
                                        when (parse_obj_helper_3(context.c)) {
                                            0 -> {
                                                context.append()
                                                when (parse_obj_helper_3(context.c)) {
                                                    0 -> {
                                                        context.append()
                                                        when (parse_obj_helper_3(context.c)) {
                                                            0 -> {
                                                                context.append()
                                                                when (parse_obj_helper_3(context.c)) {
                                                                    0 -> {
                                                                        context.append()
                                                                        when (parse_obj_helper_3(context.c)) {
                                                                            0 -> {
                                                                                context.append()
                                                                                when (parse_obj_helper_3(context.c)) {
                                                                                    0 -> {
                                                                                        context.append()
                                                                                        when (parse_obj_helper_3(context.c)) {
                                                                                            0 -> {
                                                                                                context.append()
                                                                                                loop23@ while (true) {
                                                                                                    when (parse_obj_helper_14(context.c)) {
                                                                                                        0 -> {
                                                                                                            context.append()
                                                                                                            continue@loop23
                                                                                                        }
                                                                                                        1 -> {
                                                                                                            context.append()
                                                                                                            when (parse_obj_helper_10(context.c)) {
                                                                                                                0 -> {
                                                                                                                    context.append()
                                                                                                                    continue@loop23
                                                                                                                }
                                                                                                                1 -> {
                                                                                                                    context.append()
                                                                                                                    when (parse_obj_helper_3(context.c)) {
                                                                                                                        0 -> {
                                                                                                                            context.append()
                                                                                                                            when (parse_obj_helper_3(context.c)) {
                                                                                                                                0 -> {
                                                                                                                                    context.append()
                                                                                                                                    when (parse_obj_helper_3(context.c)) {
                                                                                                                                        0 -> {
                                                                                                                                            context.append()
                                                                                                                                            when (parse_obj_helper_3(context.c)) {
                                                                                                                                                0 -> {
                                                                                                                                                    context.append()
                                                                                                                                                    continue@loop23
                                                                                                                                                }
                                                                                                                                                else -> {
                                                                                                                                                    break@error
                                                                                                                                                }
                                                                                                                                            }
                                                                                                                                        }
                                                                                                                                        else -> {
                                                                                                                                            break@error
                                                                                                                                        }
                                                                                                                                    }
                                                                                                                                }
                                                                                                                                else -> {
                                                                                                                                    break@error
                                                                                                                                }
                                                                                                                            }
                                                                                                                        }
                                                                                                                        else -> {
                                                                                                                            break@error
                                                                                                                        }
                                                                                                                    }
                                                                                                                }
                                                                                                                2 -> {
                                                                                                                    context.append()
                                                                                                                    when (parse_obj_helper_3(context.c)) {
                                                                                                                        0 -> {
                                                                                                                            context.append()
                                                                                                                            when (parse_obj_helper_3(context.c)) {
                                                                                                                                0 -> {
                                                                                                                                    context.append()
                                                                                                                                    when (parse_obj_helper_3(context.c)) {
                                                                                                                                        0 -> {
                                                                                                                                            context.append()
                                                                                                                                            when (parse_obj_helper_3(context.c)) {
                                                                                                                                                0 -> {
                                                                                                                                                    context.append()
                                                                                                                                                    when (parse_obj_helper_3(context.c)) {
                                                                                                                                                        0 -> {
                                                                                                                                                            context.append()
                                                                                                                                                            when (parse_obj_helper_3(context.c)) {
                                                                                                                                                                0 -> {
                                                                                                                                                                    context.append()
                                                                                                                                                                    when (parse_obj_helper_3(context.c)) {
                                                                                                                                                                        0 -> {
                                                                                                                                                                            context.append()
                                                                                                                                                                            when (parse_obj_helper_3(context.c)) {
                                                                                                                                                                                0 -> {
                                                                                                                                                                                    context.append()
                                                                                                                                                                                    continue@loop23
                                                                                                                                                                                }
                                                                                                                                                                                else -> {
                                                                                                                                                                                    break@error
                                                                                                                                                                                }
                                                                                                                                                                            }
                                                                                                                                                                        }
                                                                                                                                                                        else -> {
                                                                                                                                                                            break@error
                                                                                                                                                                        }
                                                                                                                                                                    }
                                                                                                                                                                }
                                                                                                                                                                else -> {
                                                                                                                                                                    break@error
                                                                                                                                                                }
                                                                                                                                                            }
                                                                                                                                                        }
                                                                                                                                                        else -> {
                                                                                                                                                            break@error
                                                                                                                                                        }
                                                                                                                                                    }
                                                                                                                                                }
                                                                                                                                                else -> {
                                                                                                                                                    break@error
                                                                                                                                                }
                                                                                                                                            }
                                                                                                                                        }
                                                                                                                                        else -> {
                                                                                                                                            break@error
                                                                                                                                        }
                                                                                                                                    }
                                                                                                                                }
                                                                                                                                else -> {
                                                                                                                                    break@error
                                                                                                                                }
                                                                                                                            }
                                                                                                                        }
                                                                                                                        else -> {
                                                                                                                            break@error
                                                                                                                        }
                                                                                                                    }
                                                                                                                }
                                                                                                                else -> {
                                                                                                                    break@error
                                                                                                                }
                                                                                                            }
                                                                                                        }
                                                                                                        else -> {
                                                                                                            break@loop23
                                                                                                        }
                                                                                                    }
                                                                                                }
                                                                                                when (parse_obj_helper_15(context.c)) {
                                                                                                    0 -> {
                                                                                                        context.append()
                                                                                                        onSTRING_LITERAL_SINGLE_QUOTE()
                                                                                                        return
                                                                                                    }
                                                                                                    else -> {
                                                                                                        break@error
                                                                                                    }
                                                                                                }
                                                                                            }
                                                                                            else -> {
                                                                                                break@error
                                                                                            }
                                                                                        }
                                                                                    }
                                                                                    else -> {
                                                                                        break@error
                                                                                    }
                                                                                }
                                                                            }
                                                                            else -> {
                                                                                break@error
                                                                            }
                                                                        }
                                                                    }
                                                                    else -> {
                                                                        break@error
                                                                    }
                                                                }
                                                            }
                                                            else -> {
                                                                break@error
                                                            }
                                                        }
                                                    }
                                                    else -> {
                                                        break@error
                                                    }
                                                }
                                            }
                                            else -> {
                                                break@error
                                            }
                                        }
                                    }
                                    else -> {
                                        break@error
                                    }
                                }
                            }
                            else -> {
                                break@error
                            }
                        }
                    }
                    2 -> {
                        context.append()
                        when (parse_obj_helper_15(context.c)) {
                            0 -> {
                                context.append()
                                loop7@ while (true) {
                                    when (parse_obj_helper_16(context.c)) {
                                        0 -> {
                                            context.append()
                                            continue@loop7
                                        }
                                        1 -> {
                                            context.append()
                                            when (parse_obj_helper_10(context.c)) {
                                                0 -> {
                                                    context.append()
                                                    continue@loop7
                                                }
                                                1 -> {
                                                    context.append()
                                                    when (parse_obj_helper_3(context.c)) {
                                                        0 -> {
                                                            context.append()
                                                            when (parse_obj_helper_3(context.c)) {
                                                                0 -> {
                                                                    context.append()
                                                                    when (parse_obj_helper_3(context.c)) {
                                                                        0 -> {
                                                                            context.append()
                                                                            when (parse_obj_helper_3(context.c)) {
                                                                                0 -> {
                                                                                    context.append()
                                                                                    continue@loop7
                                                                                }
                                                                                else -> {
                                                                                    break@error
                                                                                }
                                                                            }
                                                                        }
                                                                        else -> {
                                                                            break@error
                                                                        }
                                                                    }
                                                                }
                                                                else -> {
                                                                    break@error
                                                                }
                                                            }
                                                        }
                                                        else -> {
                                                            break@error
                                                        }
                                                    }
                                                }
                                                2 -> {
                                                    context.append()
                                                    when (parse_obj_helper_3(context.c)) {
                                                        0 -> {
                                                            context.append()
                                                            when (parse_obj_helper_3(context.c)) {
                                                                0 -> {
                                                                    context.append()
                                                                    when (parse_obj_helper_3(context.c)) {
                                                                        0 -> {
                                                                            context.append()
                                                                            when (parse_obj_helper_3(context.c)) {
                                                                                0 -> {
                                                                                    context.append()
                                                                                    when (parse_obj_helper_3(context.c)) {
                                                                                        0 -> {
                                                                                            context.append()
                                                                                            when (parse_obj_helper_3(context.c)) {
                                                                                                0 -> {
                                                                                                    context.append()
                                                                                                    when (parse_obj_helper_3(context.c)) {
                                                                                                        0 -> {
                                                                                                            context.append()
                                                                                                            when (parse_obj_helper_3(context.c)) {
                                                                                                                0 -> {
                                                                                                                    context.append()
                                                                                                                    continue@loop7
                                                                                                                }
                                                                                                                else -> {
                                                                                                                    break@error
                                                                                                                }
                                                                                                            }
                                                                                                        }
                                                                                                        else -> {
                                                                                                            break@error
                                                                                                        }
                                                                                                    }
                                                                                                }
                                                                                                else -> {
                                                                                                    break@error
                                                                                                }
                                                                                            }
                                                                                        }
                                                                                        else -> {
                                                                                            break@error
                                                                                        }
                                                                                    }
                                                                                }
                                                                                else -> {
                                                                                    break@error
                                                                                }
                                                                            }
                                                                        }
                                                                        else -> {
                                                                            break@error
                                                                        }
                                                                    }
                                                                }
                                                                else -> {
                                                                    break@error
                                                                }
                                                            }
                                                        }
                                                        else -> {
                                                            break@error
                                                        }
                                                    }
                                                }
                                                else -> {
                                                    break@error
                                                }
                                            }
                                        }
                                        2 -> {
                                            context.append()
                                            when (parse_obj_helper_16(context.c)) {
                                                0 -> {
                                                    context.append()
                                                    continue@loop7
                                                }
                                                1 -> {
                                                    context.append()
                                                    when (parse_obj_helper_10(context.c)) {
                                                        0 -> {
                                                            context.append()
                                                            continue@loop7
                                                        }
                                                        1 -> {
                                                            context.append()
                                                            when (parse_obj_helper_3(context.c)) {
                                                                0 -> {
                                                                    context.append()
                                                                    when (parse_obj_helper_3(context.c)) {
                                                                        0 -> {
                                                                            context.append()
                                                                            when (parse_obj_helper_3(context.c)) {
                                                                                0 -> {
                                                                                    context.append()
                                                                                    when (parse_obj_helper_3(context.c)) {
                                                                                        0 -> {
                                                                                            context.append()
                                                                                            continue@loop7
                                                                                        }
                                                                                        else -> {
                                                                                            break@error
                                                                                        }
                                                                                    }
                                                                                }
                                                                                else -> {
                                                                                    break@error
                                                                                }
                                                                            }
                                                                        }
                                                                        else -> {
                                                                            break@error
                                                                        }
                                                                    }
                                                                }
                                                                else -> {
                                                                    break@error
                                                                }
                                                            }
                                                        }
                                                        2 -> {
                                                            context.append()
                                                            when (parse_obj_helper_3(context.c)) {
                                                                0 -> {
                                                                    context.append()
                                                                    when (parse_obj_helper_3(context.c)) {
                                                                        0 -> {
                                                                            context.append()
                                                                            when (parse_obj_helper_3(context.c)) {
                                                                                0 -> {
                                                                                    context.append()
                                                                                    when (parse_obj_helper_3(context.c)) {
                                                                                        0 -> {
                                                                                            context.append()
                                                                                            when (parse_obj_helper_3(context.c)) {
                                                                                                0 -> {
                                                                                                    context.append()
                                                                                                    when (parse_obj_helper_3(context.c)) {
                                                                                                        0 -> {
                                                                                                            context.append()
                                                                                                            when (parse_obj_helper_3(context.c)) {
                                                                                                                0 -> {
                                                                                                                    context.append()
                                                                                                                    when (parse_obj_helper_3(context.c)) {
                                                                                                                        0 -> {
                                                                                                                            context.append()
                                                                                                                            continue@loop7
                                                                                                                        }
                                                                                                                        else -> {
                                                                                                                            break@error
                                                                                                                        }
                                                                                                                    }
                                                                                                                }
                                                                                                                else -> {
                                                                                                                    break@error
                                                                                                                }
                                                                                                            }
                                                                                                        }
                                                                                                        else -> {
                                                                                                            break@error
                                                                                                        }
                                                                                                    }
                                                                                                }
                                                                                                else -> {
                                                                                                    break@error
                                                                                                }
                                                                                            }
                                                                                        }
                                                                                        else -> {
                                                                                            break@error
                                                                                        }
                                                                                    }
                                                                                }
                                                                                else -> {
                                                                                    break@error
                                                                                }
                                                                            }
                                                                        }
                                                                        else -> {
                                                                            break@error
                                                                        }
                                                                    }
                                                                }
                                                                else -> {
                                                                    break@error
                                                                }
                                                            }
                                                        }
                                                        else -> {
                                                            break@error
                                                        }
                                                    }
                                                }
                                                2 -> {
                                                    context.append()
                                                    when (parse_obj_helper_16(context.c)) {
                                                        0 -> {
                                                            context.append()
                                                            continue@loop7
                                                        }
                                                        1 -> {
                                                            context.append()
                                                            when (parse_obj_helper_10(context.c)) {
                                                                0 -> {
                                                                    context.append()
                                                                    continue@loop7
                                                                }
                                                                1 -> {
                                                                    context.append()
                                                                    when (parse_obj_helper_3(context.c)) {
                                                                        0 -> {
                                                                            context.append()
                                                                            when (parse_obj_helper_3(context.c)) {
                                                                                0 -> {
                                                                                    context.append()
                                                                                    when (parse_obj_helper_3(context.c)) {
                                                                                        0 -> {
                                                                                            context.append()
                                                                                            when (parse_obj_helper_3(context.c)) {
                                                                                                0 -> {
                                                                                                    context.append()
                                                                                                    continue@loop7
                                                                                                }
                                                                                                else -> {
                                                                                                    break@error
                                                                                                }
                                                                                            }
                                                                                        }
                                                                                        else -> {
                                                                                            break@error
                                                                                        }
                                                                                    }
                                                                                }
                                                                                else -> {
                                                                                    break@error
                                                                                }
                                                                            }
                                                                        }
                                                                        else -> {
                                                                            break@error
                                                                        }
                                                                    }
                                                                }
                                                                2 -> {
                                                                    context.append()
                                                                    when (parse_obj_helper_3(context.c)) {
                                                                        0 -> {
                                                                            context.append()
                                                                            when (parse_obj_helper_3(context.c)) {
                                                                                0 -> {
                                                                                    context.append()
                                                                                    when (parse_obj_helper_3(context.c)) {
                                                                                        0 -> {
                                                                                            context.append()
                                                                                            when (parse_obj_helper_3(context.c)) {
                                                                                                0 -> {
                                                                                                    context.append()
                                                                                                    when (parse_obj_helper_3(context.c)) {
                                                                                                        0 -> {
                                                                                                            context.append()
                                                                                                            when (parse_obj_helper_3(context.c)) {
                                                                                                                0 -> {
                                                                                                                    context.append()
                                                                                                                    when (parse_obj_helper_3(context.c)) {
                                                                                                                        0 -> {
                                                                                                                            context.append()
                                                                                                                            when (parse_obj_helper_3(context.c)) {
                                                                                                                                0 -> {
                                                                                                                                    context.append()
                                                                                                                                    continue@loop7
                                                                                                                                }
                                                                                                                                else -> {
                                                                                                                                    break@error
                                                                                                                                }
                                                                                                                            }
                                                                                                                        }
                                                                                                                        else -> {
                                                                                                                            break@error
                                                                                                                        }
                                                                                                                    }
                                                                                                                }
                                                                                                                else -> {
                                                                                                                    break@error
                                                                                                                }
                                                                                                            }
                                                                                                        }
                                                                                                        else -> {
                                                                                                            break@error
                                                                                                        }
                                                                                                    }
                                                                                                }
                                                                                                else -> {
                                                                                                    break@error
                                                                                                }
                                                                                            }
                                                                                        }
                                                                                        else -> {
                                                                                            break@error
                                                                                        }
                                                                                    }
                                                                                }
                                                                                else -> {
                                                                                    break@error
                                                                                }
                                                                            }
                                                                        }
                                                                        else -> {
                                                                            break@error
                                                                        }
                                                                    }
                                                                }
                                                                else -> {
                                                                    break@error
                                                                }
                                                            }
                                                        }
                                                        2 -> {
                                                            context.append()
                                                            onSTRING_LITERAL_LONG_SINGLE_QUOTE()
                                                            return
                                                        }
                                                        else -> {
                                                            break@error
                                                        }
                                                    }
                                                }
                                                else -> {
                                                    break@error
                                                }
                                            }
                                        }
                                        else -> {
                                            break@loop7
                                        }
                                    }
                                }
                                break@error
                            }
                            else -> {
                                onSTRING_LITERAL_SINGLE_QUOTE()
                                return
                            }
                        }
                    }
                    else -> {
                        break@error
                    }
                }
            }
            6 -> {
                context.append()
                loop3@ while (true) {
                    when (context.c) {
                        0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39 -> {
                            context.append()
                        }
                        else -> {
                            break@loop3
                        }
                    }
                }
                when (parse_obj_helper_17(context.c)) {
                    0 -> {
                        context.append()
                        when (parse_obj_helper_18(context.c)) {
                            0 -> {
                                context.append()
                                loop7@ while (true) {
                                    when (context.c) {
                                        0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39 -> {
                                            context.append()
                                        }
                                        else -> {
                                            break@loop7
                                        }
                                    }
                                }
                                when (parse_obj_helper_19(context.c)) {
                                    0 -> {
                                        context.append()
                                        when (parse_obj_helper_20(context.c)) {
                                            0 -> {
                                                context.append()
                                                loop11@ while (true) {
                                                    when (context.c) {
                                                        0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39 -> {
                                                            context.append()
                                                        }
                                                        else -> {
                                                            break@loop11
                                                        }
                                                    }
                                                }
                                                onDOUBLE()
                                                return
                                            }
                                            1 -> {
                                                context.append()
                                                when (parse_obj_helper_21(context.c)) {
                                                    0 -> {
                                                        context.append()
                                                        loop13@ while (true) {
                                                            when (context.c) {
                                                                0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39 -> {
                                                                    context.append()
                                                                }
                                                                else -> {
                                                                    break@loop13
                                                                }
                                                            }
                                                        }
                                                        onDOUBLE()
                                                        return
                                                    }
                                                    else -> {
                                                        break@error
                                                    }
                                                }
                                            }
                                            else -> {
                                                break@error
                                            }
                                        }
                                    }
                                    else -> {
                                        onDECIMAL()
                                        return
                                    }
                                }
                            }
                            1 -> {
                                context.append()
                                when (parse_obj_helper_20(context.c)) {
                                    0 -> {
                                        context.append()
                                        loop9@ while (true) {
                                            when (context.c) {
                                                0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39 -> {
                                                    context.append()
                                                }
                                                else -> {
                                                    break@loop9
                                                }
                                            }
                                        }
                                        onDOUBLE()
                                        return
                                    }
                                    1 -> {
                                        context.append()
                                        when (parse_obj_helper_21(context.c)) {
                                            0 -> {
                                                context.append()
                                                loop11@ while (true) {
                                                    when (context.c) {
                                                        0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39 -> {
                                                            context.append()
                                                        }
                                                        else -> {
                                                            break@loop11
                                                        }
                                                    }
                                                }
                                                onDOUBLE()
                                                return
                                            }
                                            else -> {
                                                break@error
                                            }
                                        }
                                    }
                                    else -> {
                                        break@error
                                    }
                                }
                            }
                            else -> {
                                break@error
                            }
                        }
                    }
                    1 -> {
                        context.append()
                        when (parse_obj_helper_20(context.c)) {
                            0 -> {
                                context.append()
                                loop7@ while (true) {
                                    when (context.c) {
                                        0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39 -> {
                                            context.append()
                                        }
                                        else -> {
                                            break@loop7
                                        }
                                    }
                                }
                                onDOUBLE()
                                return
                            }
                            1 -> {
                                context.append()
                                when (parse_obj_helper_21(context.c)) {
                                    0 -> {
                                        context.append()
                                        loop9@ while (true) {
                                            when (context.c) {
                                                0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39 -> {
                                                    context.append()
                                                }
                                                else -> {
                                                    break@loop9
                                                }
                                            }
                                        }
                                        onDOUBLE()
                                        return
                                    }
                                    else -> {
                                        break@error
                                    }
                                }
                            }
                            else -> {
                                break@error
                            }
                        }
                    }
                    else -> {
                        onINTEGER()
                        return
                    }
                }
            }
            7 -> {
                context.append()
                when (parse_obj_helper_22(context.c)) {
                    0 -> {
                        context.append()
                        loop5@ while (true) {
                            when (context.c) {
                                0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39 -> {
                                    context.append()
                                }
                                else -> {
                                    break@loop5
                                }
                            }
                        }
                        when (parse_obj_helper_17(context.c)) {
                            0 -> {
                                context.append()
                                when (parse_obj_helper_18(context.c)) {
                                    0 -> {
                                        context.append()
                                        loop9@ while (true) {
                                            when (context.c) {
                                                0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39 -> {
                                                    context.append()
                                                }
                                                else -> {
                                                    break@loop9
                                                }
                                            }
                                        }
                                        when (parse_obj_helper_19(context.c)) {
                                            0 -> {
                                                context.append()
                                                when (parse_obj_helper_20(context.c)) {
                                                    0 -> {
                                                        context.append()
                                                        loop13@ while (true) {
                                                            when (context.c) {
                                                                0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39 -> {
                                                                    context.append()
                                                                }
                                                                else -> {
                                                                    break@loop13
                                                                }
                                                            }
                                                        }
                                                        onDOUBLE()
                                                        return
                                                    }
                                                    1 -> {
                                                        context.append()
                                                        when (parse_obj_helper_21(context.c)) {
                                                            0 -> {
                                                                context.append()
                                                                loop15@ while (true) {
                                                                    when (context.c) {
                                                                        0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39 -> {
                                                                            context.append()
                                                                        }
                                                                        else -> {
                                                                            break@loop15
                                                                        }
                                                                    }
                                                                }
                                                                onDOUBLE()
                                                                return
                                                            }
                                                            else -> {
                                                                break@error
                                                            }
                                                        }
                                                    }
                                                    else -> {
                                                        break@error
                                                    }
                                                }
                                            }
                                            else -> {
                                                onDECIMAL()
                                                return
                                            }
                                        }
                                    }
                                    1 -> {
                                        context.append()
                                        when (parse_obj_helper_20(context.c)) {
                                            0 -> {
                                                context.append()
                                                loop11@ while (true) {
                                                    when (context.c) {
                                                        0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39 -> {
                                                            context.append()
                                                        }
                                                        else -> {
                                                            break@loop11
                                                        }
                                                    }
                                                }
                                                onDOUBLE()
                                                return
                                            }
                                            1 -> {
                                                context.append()
                                                when (parse_obj_helper_21(context.c)) {
                                                    0 -> {
                                                        context.append()
                                                        loop13@ while (true) {
                                                            when (context.c) {
                                                                0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39 -> {
                                                                    context.append()
                                                                }
                                                                else -> {
                                                                    break@loop13
                                                                }
                                                            }
                                                        }
                                                        onDOUBLE()
                                                        return
                                                    }
                                                    else -> {
                                                        break@error
                                                    }
                                                }
                                            }
                                            else -> {
                                                break@error
                                            }
                                        }
                                    }
                                    else -> {
                                        break@error
                                    }
                                }
                            }
                            1 -> {
                                context.append()
                                when (parse_obj_helper_20(context.c)) {
                                    0 -> {
                                        context.append()
                                        loop9@ while (true) {
                                            when (context.c) {
                                                0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39 -> {
                                                    context.append()
                                                }
                                                else -> {
                                                    break@loop9
                                                }
                                            }
                                        }
                                        onDOUBLE()
                                        return
                                    }
                                    1 -> {
                                        context.append()
                                        when (parse_obj_helper_21(context.c)) {
                                            0 -> {
                                                context.append()
                                                loop11@ while (true) {
                                                    when (context.c) {
                                                        0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39 -> {
                                                            context.append()
                                                        }
                                                        else -> {
                                                            break@loop11
                                                        }
                                                    }
                                                }
                                                onDOUBLE()
                                                return
                                            }
                                            else -> {
                                                break@error
                                            }
                                        }
                                    }
                                    else -> {
                                        break@error
                                    }
                                }
                            }
                            else -> {
                                onINTEGER()
                                return
                            }
                        }
                    }
                    1 -> {
                        context.append()
                        when (parse_obj_helper_21(context.c)) {
                            0 -> {
                                context.append()
                                loop7@ while (true) {
                                    when (context.c) {
                                        0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39 -> {
                                            context.append()
                                        }
                                        else -> {
                                            break@loop7
                                        }
                                    }
                                }
                                when (parse_obj_helper_19(context.c)) {
                                    0 -> {
                                        context.append()
                                        when (parse_obj_helper_20(context.c)) {
                                            0 -> {
                                                context.append()
                                                loop11@ while (true) {
                                                    when (context.c) {
                                                        0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39 -> {
                                                            context.append()
                                                        }
                                                        else -> {
                                                            break@loop11
                                                        }
                                                    }
                                                }
                                                onDOUBLE()
                                                return
                                            }
                                            1 -> {
                                                context.append()
                                                when (parse_obj_helper_21(context.c)) {
                                                    0 -> {
                                                        context.append()
                                                        loop13@ while (true) {
                                                            when (context.c) {
                                                                0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39 -> {
                                                                    context.append()
                                                                }
                                                                else -> {
                                                                    break@loop13
                                                                }
                                                            }
                                                        }
                                                        onDOUBLE()
                                                        return
                                                    }
                                                    else -> {
                                                        break@error
                                                    }
                                                }
                                            }
                                            else -> {
                                                break@error
                                            }
                                        }
                                    }
                                    else -> {
                                        onDECIMAL()
                                        return
                                    }
                                }
                            }
                            else -> {
                                break@error
                            }
                        }
                    }
                    else -> {
                        break@error
                    }
                }
            }
            8 -> {
                context.append()
                when (parse_obj_helper_21(context.c)) {
                    0 -> {
                        context.append()
                        loop5@ while (true) {
                            when (context.c) {
                                0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39 -> {
                                    context.append()
                                }
                                else -> {
                                    break@loop5
                                }
                            }
                        }
                        when (parse_obj_helper_19(context.c)) {
                            0 -> {
                                context.append()
                                when (parse_obj_helper_20(context.c)) {
                                    0 -> {
                                        context.append()
                                        loop9@ while (true) {
                                            when (context.c) {
                                                0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39 -> {
                                                    context.append()
                                                }
                                                else -> {
                                                    break@loop9
                                                }
                                            }
                                        }
                                        onDOUBLE()
                                        return
                                    }
                                    1 -> {
                                        context.append()
                                        when (parse_obj_helper_21(context.c)) {
                                            0 -> {
                                                context.append()
                                                loop11@ while (true) {
                                                    when (context.c) {
                                                        0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39 -> {
                                                            context.append()
                                                        }
                                                        else -> {
                                                            break@loop11
                                                        }
                                                    }
                                                }
                                                onDOUBLE()
                                                return
                                            }
                                            else -> {
                                                break@error
                                            }
                                        }
                                    }
                                    else -> {
                                        break@error
                                    }
                                }
                            }
                            else -> {
                                onDECIMAL()
                                return
                            }
                        }
                    }
                    else -> {
                        break@error
                    }
                }
            }
            9 -> {
                context.append()
                when (parse_obj_helper_23(context.c)) {
                    0 -> {
                        context.append()
                        when (parse_obj_helper_24(context.c)) {
                            0 -> {
                                context.append()
                                when (parse_obj_helper_25(context.c)) {
                                    0 -> {
                                        context.append()
                                        when (parse_obj_helper_26(context.c)) {
                                            0 -> {
                                                context.append()
                                                when (parse_obj_helper_27(context.c)) {
                                                    0 -> {
                                                        context.append()
                                                        when (parse_obj_helper_28(context.c)) {
                                                            0 -> {
                                                                context.append()
                                                                when (parse_obj_helper_29(context.c)) {
                                                                    0 -> {
                                                                        context.append()
                                                                        when (parse_obj_helper_25(context.c)) {
                                                                            0 -> {
                                                                                context.append()
                                                                                onBOOLEAN()
                                                                                return
                                                                            }
                                                                            else -> {
                                                                                break@error
                                                                            }
                                                                        }
                                                                    }
                                                                    else -> {
                                                                        break@error
                                                                    }
                                                                }
                                                            }
                                                            else -> {
                                                                break@error
                                                            }
                                                        }
                                                    }
                                                    else -> {
                                                        break@error
                                                    }
                                                }
                                            }
                                            else -> {
                                                break@error
                                            }
                                        }
                                    }
                                    else -> {
                                        break@error
                                    }
                                }
                            }
                            else -> {
                                break@error
                            }
                        }
                    }
                    else -> {
                        break@error
                    }
                }
            }
            else -> {
                break@error
            }
        }
    }
    throw ParserExceptionUnexpectedChar(context)
}

internal fun parse_obj_helper_0(c: Int): Int {
    if (c < 0x22) {
        return 10
    } else if (c <= 0x22) {
        return 4
    } else if (c < 0x27) {
        return 10
    } else if (c <= 0x27) {
        return 5
    } else if (c < 0x2b) {
        return 10
    } else if (c <= 0x2b) {
        return 7
    } else if (c < 0x2d) {
        return 10
    } else if (c <= 0x2d) {
        return 7
    } else if (c < 0x2e) {
        return 10
    } else if (c <= 0x2e) {
        return 8
    } else if (c < 0x30) {
        return 10
    } else if (c <= 0x39) {
        return 6
    } else if (c < 0x3a) {
        return 10
    } else if (c <= 0x3a) {
        return 2
    } else if (c < 0x3c) {
        return 10
    } else if (c <= 0x3c) {
        return 0
    } else if (c < 0x41) {
        return 10
    } else if (c <= 0x5a) {
        return 1
    } else if (c < 0x5f) {
        return 10
    } else if (c <= 0x5f) {
        return 3
    } else if (c < 0x61) {
        return 10
    } else if (c <= 0x7a) {
        return 1
    } else if (c <= 0x74) {
        return 9
    } else if (c < 0xc0) {
        return 10
    } else if (c <= 0xd6) {
        return 1
    } else if (c < 0xd8) {
        return 10
    } else if (c <= 0xf6) {
        return 1
    } else if (c < 0xf8) {
        return 10
    } else if (c <= 0x2ff) {
        return 1
    } else if (c < 0x370) {
        return 10
    } else if (c <= 0x37d) {
        return 1
    } else if (c < 0x37f) {
        return 10
    } else if (c <= 0x1fff) {
        return 1
    } else if (c < 0x200c) {
        return 10
    } else if (c <= 0x200d) {
        return 1
    } else if (c < 0x2070) {
        return 10
    } else if (c <= 0x218f) {
        return 1
    } else if (c < 0x2c00) {
        return 10
    } else if (c <= 0x2fef) {
        return 1
    } else if (c < 0x3001) {
        return 10
    } else if (c <= 0xd7ff) {
        return 1
    } else if (c < 0xf900) {
        return 10
    } else if (c <= 0xfdcf) {
        return 1
    } else if (c < 0xfdf0) {
        return 10
    } else if (c <= 0xfffd) {
        return 1
    } else if (c < 0x10000) {
        return 10
    } else if (c <= 0x1fffff) {
        return 1
    } else {
        return 10
    }
}

internal fun parse_obj_helper_1(c: Int): Int {
    if (c < 0x21) {
        return 2
    } else if (c <= 0x21) {
        return 0
    } else if (c < 0x23) {
        return 2
    } else if (c <= 0x3b) {
        return 0
    } else if (c < 0x3d) {
        return 2
    } else if (c <= 0x3d) {
        return 0
    } else if (c < 0x3f) {
        return 2
    } else if (c <= 0x5b) {
        return 0
    } else if (c < 0x5c) {
        return 2
    } else if (c <= 0x5c) {
        return 1
    } else if (c < 0x5d) {
        return 2
    } else if (c <= 0x5d) {
        return 0
    } else if (c < 0x5f) {
        return 2
    } else if (c <= 0x5f) {
        return 0
    } else if (c < 0x61) {
        return 2
    } else if (c <= 0x7a) {
        return 0
    } else if (c < 0x7e) {
        return 2
    } else if (c <= 0x1fffff) {
        return 0
    } else {
        return 2
    }
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun parse_obj_helper_2(c: Int): Int {
    return if (c < 0x55) {
        2
    } else if (c <= 0x55) {
        1
    } else if (c < 0x75) {
        2
    } else if (c <= 0x75) {
        0
    } else {
        2
    }
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun parse_obj_helper_3(c: Int): Int {
    return if (c < 0x30) {
        1
    } else if (c <= 0x39) {
        0
    } else if (c < 0x41) {
        1
    } else if (c <= 0x46) {
        0
    } else if (c < 0x61) {
        1
    } else if (c <= 0x66) {
        0
    } else {
        1
    }
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun parse_obj_helper_4(c: Int): Int {
    return if (c == 0x3e) {
        0
    } else {
        1
    }
}

internal fun parse_obj_helper_5(c: Int): Int {
    if (c < 0x2d) {
        return 1
    } else if (c <= 0x2d) {
        return 0
    } else if (c < 0x30) {
        return 1
    } else if (c <= 0x39) {
        return 0
    } else if (c < 0x41) {
        return 1
    } else if (c <= 0x5a) {
        return 0
    } else if (c < 0x5f) {
        return 1
    } else if (c <= 0x5f) {
        return 0
    } else if (c < 0x61) {
        return 1
    } else if (c <= 0x7a) {
        return 0
    } else if (c < 0xb7) {
        return 1
    } else if (c <= 0xb7) {
        return 0
    } else if (c < 0xc0) {
        return 1
    } else if (c <= 0xd6) {
        return 0
    } else if (c < 0xd8) {
        return 1
    } else if (c <= 0xf6) {
        return 0
    } else if (c < 0xf8) {
        return 1
    } else if (c <= 0x37d) {
        return 0
    } else if (c < 0x37f) {
        return 1
    } else if (c <= 0x1fff) {
        return 0
    } else if (c < 0x200c) {
        return 1
    } else if (c <= 0x200d) {
        return 0
    } else if (c < 0x203f) {
        return 1
    } else if (c <= 0x2040) {
        return 0
    } else if (c < 0x2070) {
        return 1
    } else if (c <= 0x218f) {
        return 0
    } else if (c < 0x2c00) {
        return 1
    } else if (c <= 0x2fef) {
        return 0
    } else if (c < 0x3001) {
        return 1
    } else if (c <= 0xd7ff) {
        return 0
    } else if (c < 0xf900) {
        return 1
    } else if (c <= 0xfdcf) {
        return 0
    } else if (c < 0xfdf0) {
        return 1
    } else if (c <= 0xfffd) {
        return 0
    } else if (c < 0x10000) {
        return 1
    } else if (c <= 0x1fffff) {
        return 0
    } else {
        return 1
    }
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun parse_obj_helper_6(c: Int): Int {
    return if (c == 0x3a) {
        0
    } else {
        1
    }
}

internal fun parse_obj_helper_7(c: Int): Int {
    if (c < 0x30) {
        return 1
    } else if (c <= 0x39) {
        return 0
    } else if (c < 0x41) {
        return 1
    } else if (c <= 0x5a) {
        return 0
    } else if (c < 0x5f) {
        return 1
    } else if (c <= 0x5f) {
        return 0
    } else if (c < 0x61) {
        return 1
    } else if (c <= 0x7a) {
        return 0
    } else if (c < 0xc0) {
        return 1
    } else if (c <= 0xd6) {
        return 0
    } else if (c < 0xd8) {
        return 1
    } else if (c <= 0xf6) {
        return 0
    } else if (c < 0xf8) {
        return 1
    } else if (c <= 0x2ff) {
        return 0
    } else if (c < 0x370) {
        return 1
    } else if (c <= 0x37d) {
        return 0
    } else if (c < 0x37f) {
        return 1
    } else if (c <= 0x1fff) {
        return 0
    } else if (c < 0x200c) {
        return 1
    } else if (c <= 0x200d) {
        return 0
    } else if (c < 0x2070) {
        return 1
    } else if (c <= 0x218f) {
        return 0
    } else if (c < 0x2c00) {
        return 1
    } else if (c <= 0x2fef) {
        return 0
    } else if (c < 0x3001) {
        return 1
    } else if (c <= 0xd7ff) {
        return 0
    } else if (c < 0xf900) {
        return 1
    } else if (c <= 0xfdcf) {
        return 0
    } else if (c < 0xfdf0) {
        return 1
    } else if (c <= 0xfffd) {
        return 0
    } else if (c < 0x10000) {
        return 1
    } else if (c <= 0x1fffff) {
        return 0
    } else {
        return 1
    }
}

internal fun parse_obj_helper_8(c: Int): Int {
    if (c <= 0x9) {
        return 0
    } else if (c < 0xb) {
        return 3
    } else if (c <= 0xc) {
        return 0
    } else if (c < 0xe) {
        return 3
    } else if (c <= 0x21) {
        return 0
    } else if (c < 0x22) {
        return 3
    } else if (c <= 0x22) {
        return 2
    } else if (c < 0x23) {
        return 3
    } else if (c <= 0x5b) {
        return 0
    } else if (c < 0x5c) {
        return 3
    } else if (c <= 0x5c) {
        return 1
    } else if (c < 0x5d) {
        return 3
    } else if (c <= 0x1fffff) {
        return 0
    } else {
        return 3
    }
}

internal fun parse_obj_helper_9(c: Int): Int {
    if (c <= 0x9) {
        return 0
    } else if (c < 0xb) {
        return 2
    } else if (c <= 0xc) {
        return 0
    } else if (c < 0xe) {
        return 2
    } else if (c <= 0x21) {
        return 0
    } else if (c < 0x23) {
        return 2
    } else if (c <= 0x5b) {
        return 0
    } else if (c < 0x5c) {
        return 2
    } else if (c <= 0x5c) {
        return 1
    } else if (c < 0x5d) {
        return 2
    } else if (c <= 0x1fffff) {
        return 0
    } else {
        return 2
    }
}

internal fun parse_obj_helper_10(c: Int): Int {
    if (c < 0x22) {
        return 3
    } else if (c <= 0x22) {
        return 0
    } else if (c < 0x27) {
        return 3
    } else if (c <= 0x27) {
        return 0
    } else if (c < 0x55) {
        return 3
    } else if (c <= 0x55) {
        return 2
    } else if (c < 0x5c) {
        return 3
    } else if (c <= 0x5c) {
        return 0
    } else if (c < 0x62) {
        return 3
    } else if (c <= 0x62) {
        return 0
    } else if (c < 0x66) {
        return 3
    } else if (c <= 0x66) {
        return 0
    } else if (c < 0x6e) {
        return 3
    } else if (c <= 0x6e) {
        return 0
    } else if (c < 0x72) {
        return 3
    } else if (c <= 0x72) {
        return 0
    } else if (c < 0x74) {
        return 3
    } else if (c <= 0x74) {
        return 0
    } else if (c < 0x75) {
        return 3
    } else if (c <= 0x75) {
        return 1
    } else {
        return 3
    }
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun parse_obj_helper_11(c: Int): Int {
    return if (c == 0x22) {
        0
    } else {
        1
    }
}

internal fun parse_obj_helper_12(c: Int): Int {
    if (c <= 0x21) {
        return 0
    } else if (c < 0x22) {
        return 3
    } else if (c <= 0x22) {
        return 2
    } else if (c < 0x23) {
        return 3
    } else if (c <= 0x5b) {
        return 0
    } else if (c < 0x5c) {
        return 3
    } else if (c <= 0x5c) {
        return 1
    } else if (c < 0x5d) {
        return 3
    } else if (c <= 0x1fffff) {
        return 0
    } else {
        return 3
    }
}

internal fun parse_obj_helper_13(c: Int): Int {
    if (c <= 0x9) {
        return 0
    } else if (c < 0xb) {
        return 3
    } else if (c <= 0xc) {
        return 0
    } else if (c < 0xe) {
        return 3
    } else if (c <= 0x26) {
        return 0
    } else if (c < 0x27) {
        return 3
    } else if (c <= 0x27) {
        return 2
    } else if (c < 0x28) {
        return 3
    } else if (c <= 0x5b) {
        return 0
    } else if (c < 0x5c) {
        return 3
    } else if (c <= 0x5c) {
        return 1
    } else if (c < 0x5d) {
        return 3
    } else if (c <= 0x1fffff) {
        return 0
    } else {
        return 3
    }
}

internal fun parse_obj_helper_14(c: Int): Int {
    if (c <= 0x9) {
        return 0
    } else if (c < 0xb) {
        return 2
    } else if (c <= 0xc) {
        return 0
    } else if (c < 0xe) {
        return 2
    } else if (c <= 0x26) {
        return 0
    } else if (c < 0x28) {
        return 2
    } else if (c <= 0x5b) {
        return 0
    } else if (c < 0x5c) {
        return 2
    } else if (c <= 0x5c) {
        return 1
    } else if (c < 0x5d) {
        return 2
    } else if (c <= 0x1fffff) {
        return 0
    } else {
        return 2
    }
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun parse_obj_helper_15(c: Int): Int {
    return if (c == 0x27) {
        0
    } else {
        1
    }
}

internal fun parse_obj_helper_16(c: Int): Int {
    if (c <= 0x26) {
        return 0
    } else if (c < 0x27) {
        return 3
    } else if (c <= 0x27) {
        return 2
    } else if (c < 0x28) {
        return 3
    } else if (c <= 0x5b) {
        return 0
    } else if (c < 0x5c) {
        return 3
    } else if (c <= 0x5c) {
        return 1
    } else if (c < 0x5d) {
        return 3
    } else if (c <= 0x1fffff) {
        return 0
    } else {
        return 3
    }
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun parse_obj_helper_17(c: Int): Int {
    return if (c < 0x2e) {
        2
    } else if (c <= 0x2e) {
        0
    } else if (c < 0x45) {
        2
    } else if (c <= 0x45) {
        1
    } else if (c < 0x65) {
        2
    } else if (c <= 0x65) {
        1
    } else {
        2
    }
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun parse_obj_helper_18(c: Int): Int {
    return if (c < 0x30) {
        2
    } else if (c <= 0x39) {
        0
    } else if (c < 0x45) {
        2
    } else if (c <= 0x45) {
        1
    } else if (c < 0x65) {
        2
    } else if (c <= 0x65) {
        1
    } else {
        2
    }
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun parse_obj_helper_19(c: Int): Int {
    return if (c < 0x45) {
        1
    } else if (c <= 0x45) {
        0
    } else if (c < 0x65) {
        1
    } else if (c <= 0x65) {
        0
    } else {
        1
    }
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun parse_obj_helper_20(c: Int): Int {
    return if (c < 0x2b) {
        2
    } else if (c <= 0x2b) {
        1
    } else if (c < 0x2d) {
        2
    } else if (c <= 0x2d) {
        1
    } else if (c < 0x30) {
        2
    } else if (c <= 0x39) {
        0
    } else {
        2
    }
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun parse_obj_helper_21(c: Int): Int {
    return if (c < 0x30) {
        1
    } else if (c <= 0x39) {
        0
    } else {
        1
    }
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun parse_obj_helper_22(c: Int): Int {
    return if (c < 0x2e) {
        2
    } else if (c <= 0x2e) {
        1
    } else if (c < 0x30) {
        2
    } else if (c <= 0x39) {
        0
    } else {
        2
    }
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun parse_obj_helper_23(c: Int): Int {
    return if (c == 0x72) {
        0
    } else {
        1
    }
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun parse_obj_helper_24(c: Int): Int {
    return if (c == 0x75) {
        0
    } else {
        1
    }
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun parse_obj_helper_25(c: Int): Int {
    return if (c == 0x65) {
        0
    } else {
        1
    }
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun parse_obj_helper_26(c: Int): Int {
    return if (c == 0x66) {
        0
    } else {
        1
    }
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun parse_obj_helper_27(c: Int): Int {
    return if (c == 0x61) {
        0
    } else {
        1
    }
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun parse_obj_helper_28(c: Int): Int {
    return if (c == 0x6c) {
        0
    } else {
        1
    }
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun parse_obj_helper_29(c: Int): Int {
    return if (c == 0x73) {
        0
    } else {
        1
    }
}

internal inline fun parse_triple_end(
    context: ParserContext,
    crossinline onPREDICATE_LISTA: () -> Unit,
    crossinline onOBJECT_LISTA: () -> Unit,
    crossinline onDOT: () -> Unit
) {
    context.clear()
    error@ while (true) {
        when (parse_triple_end_helper_0(context.c)) {
            0 -> {
                context.append()
                onPREDICATE_LISTA()
                return
            }
            1 -> {
                context.append()
                onOBJECT_LISTA()
                return
            }
            2 -> {
                context.append()
                onDOT()
                return
            }
            else -> {
                break@error
            }
        }
    }
    throw ParserExceptionUnexpectedChar(context)
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun parse_triple_end_helper_0(c: Int): Int {
    return if (c < 0x2c) {
        3
    } else if (c <= 0x2c) {
        1
    } else if (c < 0x2e) {
        3
    } else if (c <= 0x2e) {
        2
    } else if (c < 0x3b) {
        3
    } else if (c <= 0x3b) {
        0
    } else {
        3
    }
}

internal inline fun parse_triple_end_or_object_iri(
    context: ParserContext,
    crossinline onPN_LOCAL: () -> Unit,
    crossinline onPREDICATE_LISTA: () -> Unit,
    crossinline onOBJECT_LISTA: () -> Unit,
    crossinline onDOT: () -> Unit,
    crossinline onSKIP_WS_FORCED: () -> Unit
) {
    context.clear()
    error@ while (true) {
        when (parse_triple_end_or_object_iri_helper_0(context.c)) {
            0 -> {
                context.append()
                loop3@ while (true) {
                    loop4@ while (true) {
                        when (context.c) {
                            0x2e -> {
                                context.append()
                            }
                            else -> {
                                break@loop4
                            }
                        }
                    }
                    when (parse_triple_end_or_object_iri_helper_1(context.c)) {
                        0 -> {
                            context.append()
                            continue@loop3
                        }
                        1 -> {
                            context.append()
                            when (parse_triple_end_or_object_iri_helper_2(context.c)) {
                                0 -> {
                                    context.append()
                                    when (parse_triple_end_or_object_iri_helper_2(context.c)) {
                                        0 -> {
                                            context.append()
                                            continue@loop3
                                        }
                                        else -> {
                                            break@error
                                        }
                                    }
                                }
                                else -> {
                                    break@error
                                }
                            }
                        }
                        2 -> {
                            context.append()
                            when (parse_triple_end_or_object_iri_helper_3(context.c)) {
                                0 -> {
                                    context.append()
                                    continue@loop3
                                }
                                else -> {
                                    break@error
                                }
                            }
                        }
                        else -> {
                            break@loop3
                        }
                    }
                }
                onPN_LOCAL()
                return
            }
            1 -> {
                context.append()
                when (parse_triple_end_or_object_iri_helper_2(context.c)) {
                    0 -> {
                        context.append()
                        when (parse_triple_end_or_object_iri_helper_2(context.c)) {
                            0 -> {
                                context.append()
                                loop7@ while (true) {
                                    loop8@ while (true) {
                                        when (context.c) {
                                            0x2e -> {
                                                context.append()
                                            }
                                            else -> {
                                                break@loop8
                                            }
                                        }
                                    }
                                    when (parse_triple_end_or_object_iri_helper_1(context.c)) {
                                        0 -> {
                                            context.append()
                                            continue@loop7
                                        }
                                        1 -> {
                                            context.append()
                                            when (parse_triple_end_or_object_iri_helper_2(context.c)) {
                                                0 -> {
                                                    context.append()
                                                    when (parse_triple_end_or_object_iri_helper_2(context.c)) {
                                                        0 -> {
                                                            context.append()
                                                            continue@loop7
                                                        }
                                                        else -> {
                                                            break@error
                                                        }
                                                    }
                                                }
                                                else -> {
                                                    break@error
                                                }
                                            }
                                        }
                                        2 -> {
                                            context.append()
                                            when (parse_triple_end_or_object_iri_helper_3(context.c)) {
                                                0 -> {
                                                    context.append()
                                                    continue@loop7
                                                }
                                                else -> {
                                                    break@error
                                                }
                                            }
                                        }
                                        else -> {
                                            break@loop7
                                        }
                                    }
                                }
                                onPN_LOCAL()
                                return
                            }
                            else -> {
                                break@error
                            }
                        }
                    }
                    else -> {
                        break@error
                    }
                }
            }
            2 -> {
                context.append()
                when (parse_triple_end_or_object_iri_helper_3(context.c)) {
                    0 -> {
                        context.append()
                        loop5@ while (true) {
                            loop6@ while (true) {
                                when (context.c) {
                                    0x2e -> {
                                        context.append()
                                    }
                                    else -> {
                                        break@loop6
                                    }
                                }
                            }
                            when (parse_triple_end_or_object_iri_helper_1(context.c)) {
                                0 -> {
                                    context.append()
                                    continue@loop5
                                }
                                1 -> {
                                    context.append()
                                    when (parse_triple_end_or_object_iri_helper_2(context.c)) {
                                        0 -> {
                                            context.append()
                                            when (parse_triple_end_or_object_iri_helper_2(context.c)) {
                                                0 -> {
                                                    context.append()
                                                    continue@loop5
                                                }
                                                else -> {
                                                    break@error
                                                }
                                            }
                                        }
                                        else -> {
                                            break@error
                                        }
                                    }
                                }
                                2 -> {
                                    context.append()
                                    when (parse_triple_end_or_object_iri_helper_3(context.c)) {
                                        0 -> {
                                            context.append()
                                            continue@loop5
                                        }
                                        else -> {
                                            break@error
                                        }
                                    }
                                }
                                else -> {
                                    break@loop5
                                }
                            }
                        }
                        onPN_LOCAL()
                        return
                    }
                    else -> {
                        break@error
                    }
                }
            }
            3 -> {
                context.append()
                onPREDICATE_LISTA()
                return
            }
            4 -> {
                context.append()
                onOBJECT_LISTA()
                return
            }
            5 -> {
                context.append()
                onDOT()
                return
            }
            6 -> {
                context.append()
                loop3@ while (true) {
                    when (context.c) {
                        0x9, 0xa, 0xd, 0x20 -> {
                            context.append()
                        }
                        else -> {
                            break@loop3
                        }
                    }
                }
                onSKIP_WS_FORCED()
                return
            }
            else -> {
                break@error
            }
        }
    }
    throw ParserExceptionUnexpectedChar(context)
}

internal fun parse_triple_end_or_object_iri_helper_0(c: Int): Int {
    if (c < 0x9) {
        return 7
    } else if (c <= 0xa) {
        return 6
    } else if (c < 0xd) {
        return 7
    } else if (c <= 0xd) {
        return 6
    } else if (c < 0x20) {
        return 7
    } else if (c <= 0x20) {
        return 6
    } else if (c < 0x25) {
        return 7
    } else if (c <= 0x25) {
        return 1
    } else if (c < 0x2c) {
        return 7
    } else if (c <= 0x2c) {
        return 4
    } else if (c < 0x2e) {
        return 7
    } else if (c <= 0x2e) {
        return 5
    } else if (c < 0x30) {
        return 7
    } else if (c <= 0x3a) {
        return 0
    } else if (c < 0x3b) {
        return 7
    } else if (c <= 0x3b) {
        return 3
    } else if (c < 0x41) {
        return 7
    } else if (c <= 0x5a) {
        return 0
    } else if (c < 0x5c) {
        return 7
    } else if (c <= 0x5c) {
        return 2
    } else if (c < 0x5f) {
        return 7
    } else if (c <= 0x5f) {
        return 0
    } else if (c < 0x61) {
        return 7
    } else if (c <= 0x7a) {
        return 0
    } else if (c < 0xc0) {
        return 7
    } else if (c <= 0xd6) {
        return 0
    } else if (c < 0xd8) {
        return 7
    } else if (c <= 0xf6) {
        return 0
    } else if (c < 0xf8) {
        return 7
    } else if (c <= 0x2ff) {
        return 0
    } else if (c < 0x370) {
        return 7
    } else if (c <= 0x37d) {
        return 0
    } else if (c < 0x37f) {
        return 7
    } else if (c <= 0x1fff) {
        return 0
    } else if (c < 0x200c) {
        return 7
    } else if (c <= 0x200d) {
        return 0
    } else if (c < 0x2070) {
        return 7
    } else if (c <= 0x218f) {
        return 0
    } else if (c < 0x2c00) {
        return 7
    } else if (c <= 0x2fef) {
        return 0
    } else if (c < 0x3001) {
        return 7
    } else if (c <= 0xd7ff) {
        return 0
    } else if (c < 0xf900) {
        return 7
    } else if (c <= 0xfdcf) {
        return 0
    } else if (c < 0xfdf0) {
        return 7
    } else if (c <= 0xfffd) {
        return 0
    } else if (c < 0x10000) {
        return 7
    } else if (c <= 0x1fffff) {
        return 0
    } else {
        return 7
    }
}

internal fun parse_triple_end_or_object_iri_helper_1(c: Int): Int {
    if (c < 0x25) {
        return 3
    } else if (c <= 0x25) {
        return 1
    } else if (c < 0x2d) {
        return 3
    } else if (c <= 0x2d) {
        return 0
    } else if (c < 0x30) {
        return 3
    } else if (c <= 0x3a) {
        return 0
    } else if (c < 0x41) {
        return 3
    } else if (c <= 0x5a) {
        return 0
    } else if (c < 0x5c) {
        return 3
    } else if (c <= 0x5c) {
        return 2
    } else if (c < 0x5f) {
        return 3
    } else if (c <= 0x5f) {
        return 0
    } else if (c < 0x61) {
        return 3
    } else if (c <= 0x7a) {
        return 0
    } else if (c < 0xb7) {
        return 3
    } else if (c <= 0xb7) {
        return 0
    } else if (c < 0xc0) {
        return 3
    } else if (c <= 0xd6) {
        return 0
    } else if (c < 0xd8) {
        return 3
    } else if (c <= 0xf6) {
        return 0
    } else if (c < 0xf8) {
        return 3
    } else if (c <= 0x37d) {
        return 0
    } else if (c < 0x37f) {
        return 3
    } else if (c <= 0x1fff) {
        return 0
    } else if (c < 0x200c) {
        return 3
    } else if (c <= 0x200d) {
        return 0
    } else if (c < 0x203f) {
        return 3
    } else if (c <= 0x2040) {
        return 0
    } else if (c < 0x2070) {
        return 3
    } else if (c <= 0x218f) {
        return 0
    } else if (c < 0x2c00) {
        return 3
    } else if (c <= 0x2fef) {
        return 0
    } else if (c < 0x3001) {
        return 3
    } else if (c <= 0xd7ff) {
        return 0
    } else if (c < 0xf900) {
        return 3
    } else if (c <= 0xfdcf) {
        return 0
    } else if (c < 0xfdf0) {
        return 3
    } else if (c <= 0xfffd) {
        return 0
    } else if (c < 0x10000) {
        return 3
    } else if (c <= 0x1fffff) {
        return 0
    } else {
        return 3
    }
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun parse_triple_end_or_object_iri_helper_2(c: Int): Int {
    return if (c < 0x30) {
        1
    } else if (c <= 0x39) {
        0
    } else if (c < 0x41) {
        1
    } else if (c <= 0x46) {
        0
    } else if (c < 0x61) {
        1
    } else if (c <= 0x66) {
        0
    } else {
        1
    }
}

internal fun parse_triple_end_or_object_iri_helper_3(c: Int): Int {
    if (c < 0x21) {
        return 1
    } else if (c <= 0x21) {
        return 0
    } else if (c < 0x23) {
        return 1
    } else if (c <= 0x2f) {
        return 0
    } else if (c < 0x3b) {
        return 1
    } else if (c <= 0x3b) {
        return 0
    } else if (c < 0x3d) {
        return 1
    } else if (c <= 0x3d) {
        return 0
    } else if (c < 0x3f) {
        return 1
    } else if (c <= 0x40) {
        return 0
    } else if (c < 0x5f) {
        return 1
    } else if (c <= 0x5f) {
        return 0
    } else if (c < 0x7e) {
        return 1
    } else if (c <= 0x7e) {
        return 0
    } else {
        return 1
    }
}

internal inline fun parse_triple_end_or_object_string(
    context: ParserContext,
    crossinline onLANGTAG: () -> Unit,
    crossinline onIRIA: () -> Unit,
    crossinline onPREDICATE_LISTA: () -> Unit,
    crossinline onOBJECT_LISTA: () -> Unit,
    crossinline onDOT: () -> Unit,
    crossinline onSKIP_WS_FORCED: () -> Unit
) {
    context.clear()
    error@ while (true) {
        when (parse_triple_end_or_object_string_helper_0(context.c)) {
            0 -> {
                context.append()
                when (parse_triple_end_or_object_string_helper_1(context.c)) {
                    0 -> {
                        context.append()
                        loop5@ while (true) {
                            when (context.c) {
                                0x41, 0x42, 0x43, 0x44, 0x45, 0x46, 0x47, 0x48, 0x49, 0x4a, 0x4b, 0x4c, 0x4d, 0x4e, 0x4f, 0x50, 0x51, 0x52, 0x53, 0x54, 0x55, 0x56, 0x57, 0x58, 0x59, 0x5a, 0x61, 0x62, 0x63, 0x64, 0x65, 0x66, 0x67, 0x68, 0x69, 0x6a, 0x6b, 0x6c, 0x6d, 0x6e, 0x6f, 0x70, 0x71, 0x72, 0x73, 0x74, 0x75, 0x76, 0x77, 0x78, 0x79, 0x7a -> {
                                    context.append()
                                }
                                else -> {
                                    break@loop5
                                }
                            }
                        }
                        loop5@ while (true) {
                            when (parse_triple_end_or_object_string_helper_2(context.c)) {
                                0 -> {
                                    context.append()
                                    when (parse_triple_end_or_object_string_helper_3(context.c)) {
                                        0 -> {
                                            context.append()
                                            loop10@ while (true) {
                                                when (context.c) {
                                                    0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x41, 0x42, 0x43, 0x44, 0x45, 0x46, 0x47, 0x48, 0x49, 0x4a, 0x4b, 0x4c, 0x4d, 0x4e, 0x4f, 0x50, 0x51, 0x52, 0x53, 0x54, 0x55, 0x56, 0x57, 0x58, 0x59, 0x5a, 0x61, 0x62, 0x63, 0x64, 0x65, 0x66, 0x67, 0x68, 0x69, 0x6a, 0x6b, 0x6c, 0x6d, 0x6e, 0x6f, 0x70, 0x71, 0x72, 0x73, 0x74, 0x75, 0x76, 0x77, 0x78, 0x79, 0x7a -> {
                                                        context.append()
                                                    }
                                                    else -> {
                                                        break@loop10
                                                    }
                                                }
                                            }
                                            continue@loop5
                                        }
                                        else -> {
                                            break@error
                                        }
                                    }
                                }
                                else -> {
                                    break@loop5
                                }
                            }
                        }
                        onLANGTAG()
                        return
                    }
                    else -> {
                        break@error
                    }
                }
            }
            1 -> {
                context.append()
                when (parse_triple_end_or_object_string_helper_4(context.c)) {
                    0 -> {
                        context.append()
                        onIRIA()
                        return
                    }
                    else -> {
                        break@error
                    }
                }
            }
            2 -> {
                context.append()
                onPREDICATE_LISTA()
                return
            }
            3 -> {
                context.append()
                onOBJECT_LISTA()
                return
            }
            4 -> {
                context.append()
                onDOT()
                return
            }
            5 -> {
                context.append()
                loop3@ while (true) {
                    when (context.c) {
                        0x9, 0xa, 0xd, 0x20 -> {
                            context.append()
                        }
                        else -> {
                            break@loop3
                        }
                    }
                }
                onSKIP_WS_FORCED()
                return
            }
            else -> {
                break@error
            }
        }
    }
    throw ParserExceptionUnexpectedChar(context)
}

internal fun parse_triple_end_or_object_string_helper_0(c: Int): Int {
    if (c < 0x9) {
        return 6
    } else if (c <= 0xa) {
        return 5
    } else if (c < 0xd) {
        return 6
    } else if (c <= 0xd) {
        return 5
    } else if (c < 0x20) {
        return 6
    } else if (c <= 0x20) {
        return 5
    } else if (c < 0x2c) {
        return 6
    } else if (c <= 0x2c) {
        return 3
    } else if (c < 0x2e) {
        return 6
    } else if (c <= 0x2e) {
        return 4
    } else if (c < 0x3b) {
        return 6
    } else if (c <= 0x3b) {
        return 2
    } else if (c < 0x40) {
        return 6
    } else if (c <= 0x40) {
        return 0
    } else if (c < 0x5e) {
        return 6
    } else if (c <= 0x5e) {
        return 1
    } else {
        return 6
    }
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun parse_triple_end_or_object_string_helper_1(c: Int): Int {
    return if (c < 0x41) {
        1
    } else if (c <= 0x5a) {
        0
    } else if (c < 0x61) {
        1
    } else if (c <= 0x7a) {
        0
    } else {
        1
    }
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun parse_triple_end_or_object_string_helper_2(c: Int): Int {
    return if (c == 0x2d) {
        0
    } else {
        1
    }
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun parse_triple_end_or_object_string_helper_3(c: Int): Int {
    return if (c < 0x30) {
        1
    } else if (c <= 0x39) {
        0
    } else if (c < 0x41) {
        1
    } else if (c <= 0x5a) {
        0
    } else if (c < 0x61) {
        1
    } else if (c <= 0x7a) {
        0
    } else {
        1
    }
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun parse_triple_end_or_object_string_helper_4(c: Int): Int {
    return if (c == 0x5e) {
        0
    } else {
        1
    }
}

internal inline fun parse_triple_end_or_object_string_typed(
    context: ParserContext,
    crossinline onIRIREF: () -> Unit,
    crossinline onPNAME_NS: () -> Unit
) {
    context.clear()
    error@ while (true) {
        when (parse_triple_end_or_object_string_typed_helper_0(context.c)) {
            0 -> {
                context.append()
                loop3@ while (true) {
                    when (parse_triple_end_or_object_string_typed_helper_1(context.c)) {
                        0 -> {
                            context.append()
                            continue@loop3
                        }
                        1 -> {
                            context.append()
                            when (parse_triple_end_or_object_string_typed_helper_2(context.c)) {
                                0 -> {
                                    context.append()
                                    when (parse_triple_end_or_object_string_typed_helper_3(context.c)) {
                                        0 -> {
                                            context.append()
                                            when (parse_triple_end_or_object_string_typed_helper_3(context.c)) {
                                                0 -> {
                                                    context.append()
                                                    when (parse_triple_end_or_object_string_typed_helper_3(context.c)) {
                                                        0 -> {
                                                            context.append()
                                                            when (parse_triple_end_or_object_string_typed_helper_3(context.c)) {
                                                                0 -> {
                                                                    context.append()
                                                                    continue@loop3
                                                                }
                                                                else -> {
                                                                    break@error
                                                                }
                                                            }
                                                        }
                                                        else -> {
                                                            break@error
                                                        }
                                                    }
                                                }
                                                else -> {
                                                    break@error
                                                }
                                            }
                                        }
                                        else -> {
                                            break@error
                                        }
                                    }
                                }
                                1 -> {
                                    context.append()
                                    when (parse_triple_end_or_object_string_typed_helper_3(context.c)) {
                                        0 -> {
                                            context.append()
                                            when (parse_triple_end_or_object_string_typed_helper_3(context.c)) {
                                                0 -> {
                                                    context.append()
                                                    when (parse_triple_end_or_object_string_typed_helper_3(context.c)) {
                                                        0 -> {
                                                            context.append()
                                                            when (parse_triple_end_or_object_string_typed_helper_3(context.c)) {
                                                                0 -> {
                                                                    context.append()
                                                                    when (parse_triple_end_or_object_string_typed_helper_3(context.c)) {
                                                                        0 -> {
                                                                            context.append()
                                                                            when (parse_triple_end_or_object_string_typed_helper_3(context.c)) {
                                                                                0 -> {
                                                                                    context.append()
                                                                                    when (parse_triple_end_or_object_string_typed_helper_3(context.c)) {
                                                                                        0 -> {
                                                                                            context.append()
                                                                                            when (parse_triple_end_or_object_string_typed_helper_3(context.c)) {
                                                                                                0 -> {
                                                                                                    context.append()
                                                                                                    continue@loop3
                                                                                                }
                                                                                                else -> {
                                                                                                    break@error
                                                                                                }
                                                                                            }
                                                                                        }
                                                                                        else -> {
                                                                                            break@error
                                                                                        }
                                                                                    }
                                                                                }
                                                                                else -> {
                                                                                    break@error
                                                                                }
                                                                            }
                                                                        }
                                                                        else -> {
                                                                            break@error
                                                                        }
                                                                    }
                                                                }
                                                                else -> {
                                                                    break@error
                                                                }
                                                            }
                                                        }
                                                        else -> {
                                                            break@error
                                                        }
                                                    }
                                                }
                                                else -> {
                                                    break@error
                                                }
                                            }
                                        }
                                        else -> {
                                            break@error
                                        }
                                    }
                                }
                                else -> {
                                    break@error
                                }
                            }
                        }
                        else -> {
                            break@loop3
                        }
                    }
                }
                when (parse_triple_end_or_object_string_typed_helper_4(context.c)) {
                    0 -> {
                        context.append()
                        onIRIREF()
                        return
                    }
                    else -> {
                        break@error
                    }
                }
            }
            1 -> {
                context.append()
                loop3@ while (true) {
                    loop4@ while (true) {
                        when (context.c) {
                            0x2e -> {
                                context.append()
                            }
                            else -> {
                                break@loop4
                            }
                        }
                    }
                    when (parse_triple_end_or_object_string_typed_helper_5(context.c)) {
                        0 -> {
                            context.append()
                            continue@loop3
                        }
                        else -> {
                            break@loop3
                        }
                    }
                }
                when (parse_triple_end_or_object_string_typed_helper_6(context.c)) {
                    0 -> {
                        context.append()
                        onPNAME_NS()
                        return
                    }
                    else -> {
                        break@error
                    }
                }
            }
            2 -> {
                context.append()
                onPNAME_NS()
                return
            }
            else -> {
                break@error
            }
        }
    }
    throw ParserExceptionUnexpectedChar(context)
}

internal fun parse_triple_end_or_object_string_typed_helper_0(c: Int): Int {
    if (c < 0x3a) {
        return 3
    } else if (c <= 0x3a) {
        return 2
    } else if (c < 0x3c) {
        return 3
    } else if (c <= 0x3c) {
        return 0
    } else if (c < 0x41) {
        return 3
    } else if (c <= 0x5a) {
        return 1
    } else if (c < 0x61) {
        return 3
    } else if (c <= 0x7a) {
        return 1
    } else if (c < 0xc0) {
        return 3
    } else if (c <= 0xd6) {
        return 1
    } else if (c < 0xd8) {
        return 3
    } else if (c <= 0xf6) {
        return 1
    } else if (c < 0xf8) {
        return 3
    } else if (c <= 0x2ff) {
        return 1
    } else if (c < 0x370) {
        return 3
    } else if (c <= 0x37d) {
        return 1
    } else if (c < 0x37f) {
        return 3
    } else if (c <= 0x1fff) {
        return 1
    } else if (c < 0x200c) {
        return 3
    } else if (c <= 0x200d) {
        return 1
    } else if (c < 0x2070) {
        return 3
    } else if (c <= 0x218f) {
        return 1
    } else if (c < 0x2c00) {
        return 3
    } else if (c <= 0x2fef) {
        return 1
    } else if (c < 0x3001) {
        return 3
    } else if (c <= 0xd7ff) {
        return 1
    } else if (c < 0xf900) {
        return 3
    } else if (c <= 0xfdcf) {
        return 1
    } else if (c < 0xfdf0) {
        return 3
    } else if (c <= 0xfffd) {
        return 1
    } else if (c < 0x10000) {
        return 3
    } else if (c <= 0x1fffff) {
        return 1
    } else {
        return 3
    }
}

internal fun parse_triple_end_or_object_string_typed_helper_1(c: Int): Int {
    if (c < 0x21) {
        return 2
    } else if (c <= 0x21) {
        return 0
    } else if (c < 0x23) {
        return 2
    } else if (c <= 0x3b) {
        return 0
    } else if (c < 0x3d) {
        return 2
    } else if (c <= 0x3d) {
        return 0
    } else if (c < 0x3f) {
        return 2
    } else if (c <= 0x5b) {
        return 0
    } else if (c < 0x5c) {
        return 2
    } else if (c <= 0x5c) {
        return 1
    } else if (c < 0x5d) {
        return 2
    } else if (c <= 0x5d) {
        return 0
    } else if (c < 0x5f) {
        return 2
    } else if (c <= 0x5f) {
        return 0
    } else if (c < 0x61) {
        return 2
    } else if (c <= 0x7a) {
        return 0
    } else if (c < 0x7e) {
        return 2
    } else if (c <= 0x1fffff) {
        return 0
    } else {
        return 2
    }
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun parse_triple_end_or_object_string_typed_helper_2(c: Int): Int {
    return if (c < 0x55) {
        2
    } else if (c <= 0x55) {
        1
    } else if (c < 0x75) {
        2
    } else if (c <= 0x75) {
        0
    } else {
        2
    }
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun parse_triple_end_or_object_string_typed_helper_3(c: Int): Int {
    return if (c < 0x30) {
        1
    } else if (c <= 0x39) {
        0
    } else if (c < 0x41) {
        1
    } else if (c <= 0x46) {
        0
    } else if (c < 0x61) {
        1
    } else if (c <= 0x66) {
        0
    } else {
        1
    }
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun parse_triple_end_or_object_string_typed_helper_4(c: Int): Int {
    return if (c == 0x3e) {
        0
    } else {
        1
    }
}

internal fun parse_triple_end_or_object_string_typed_helper_5(c: Int): Int {
    if (c < 0x2d) {
        return 1
    } else if (c <= 0x2d) {
        return 0
    } else if (c < 0x30) {
        return 1
    } else if (c <= 0x39) {
        return 0
    } else if (c < 0x41) {
        return 1
    } else if (c <= 0x5a) {
        return 0
    } else if (c < 0x5f) {
        return 1
    } else if (c <= 0x5f) {
        return 0
    } else if (c < 0x61) {
        return 1
    } else if (c <= 0x7a) {
        return 0
    } else if (c < 0xb7) {
        return 1
    } else if (c <= 0xb7) {
        return 0
    } else if (c < 0xc0) {
        return 1
    } else if (c <= 0xd6) {
        return 0
    } else if (c < 0xd8) {
        return 1
    } else if (c <= 0xf6) {
        return 0
    } else if (c < 0xf8) {
        return 1
    } else if (c <= 0x37d) {
        return 0
    } else if (c < 0x37f) {
        return 1
    } else if (c <= 0x1fff) {
        return 0
    } else if (c < 0x200c) {
        return 1
    } else if (c <= 0x200d) {
        return 0
    } else if (c < 0x203f) {
        return 1
    } else if (c <= 0x2040) {
        return 0
    } else if (c < 0x2070) {
        return 1
    } else if (c <= 0x218f) {
        return 0
    } else if (c < 0x2c00) {
        return 1
    } else if (c <= 0x2fef) {
        return 0
    } else if (c < 0x3001) {
        return 1
    } else if (c <= 0xd7ff) {
        return 0
    } else if (c < 0xf900) {
        return 1
    } else if (c <= 0xfdcf) {
        return 0
    } else if (c < 0xfdf0) {
        return 1
    } else if (c <= 0xfffd) {
        return 0
    } else if (c < 0x10000) {
        return 1
    } else if (c <= 0x1fffff) {
        return 0
    } else {
        return 1
    }
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun parse_triple_end_or_object_string_typed_helper_6(c: Int): Int {
    return if (c == 0x3a) {
        0
    } else {
        1
    }
}

internal inline fun parse_triple_end_or_object_string_typed_iri(
    context: ParserContext,
    crossinline onPN_LOCAL: () -> Unit,
    crossinline onPREDICATE_LISTA: () -> Unit,
    crossinline onOBJECT_LISTA: () -> Unit,
    crossinline onDOT: () -> Unit,
    crossinline onSKIP_WS_FORCED: () -> Unit
) {
    context.clear()
    error@ while (true) {
        when (parse_triple_end_or_object_string_typed_iri_helper_0(context.c)) {
            0 -> {
                context.append()
                loop3@ while (true) {
                    loop4@ while (true) {
                        when (context.c) {
                            0x2e -> {
                                context.append()
                            }
                            else -> {
                                break@loop4
                            }
                        }
                    }
                    when (parse_triple_end_or_object_string_typed_iri_helper_1(context.c)) {
                        0 -> {
                            context.append()
                            continue@loop3
                        }
                        1 -> {
                            context.append()
                            when (parse_triple_end_or_object_string_typed_iri_helper_2(context.c)) {
                                0 -> {
                                    context.append()
                                    when (parse_triple_end_or_object_string_typed_iri_helper_2(context.c)) {
                                        0 -> {
                                            context.append()
                                            continue@loop3
                                        }
                                        else -> {
                                            break@error
                                        }
                                    }
                                }
                                else -> {
                                    break@error
                                }
                            }
                        }
                        2 -> {
                            context.append()
                            when (parse_triple_end_or_object_string_typed_iri_helper_3(context.c)) {
                                0 -> {
                                    context.append()
                                    continue@loop3
                                }
                                else -> {
                                    break@error
                                }
                            }
                        }
                        else -> {
                            break@loop3
                        }
                    }
                }
                onPN_LOCAL()
                return
            }
            1 -> {
                context.append()
                when (parse_triple_end_or_object_string_typed_iri_helper_2(context.c)) {
                    0 -> {
                        context.append()
                        when (parse_triple_end_or_object_string_typed_iri_helper_2(context.c)) {
                            0 -> {
                                context.append()
                                loop7@ while (true) {
                                    loop8@ while (true) {
                                        when (context.c) {
                                            0x2e -> {
                                                context.append()
                                            }
                                            else -> {
                                                break@loop8
                                            }
                                        }
                                    }
                                    when (parse_triple_end_or_object_string_typed_iri_helper_1(context.c)) {
                                        0 -> {
                                            context.append()
                                            continue@loop7
                                        }
                                        1 -> {
                                            context.append()
                                            when (parse_triple_end_or_object_string_typed_iri_helper_2(context.c)) {
                                                0 -> {
                                                    context.append()
                                                    when (parse_triple_end_or_object_string_typed_iri_helper_2(context.c)) {
                                                        0 -> {
                                                            context.append()
                                                            continue@loop7
                                                        }
                                                        else -> {
                                                            break@error
                                                        }
                                                    }
                                                }
                                                else -> {
                                                    break@error
                                                }
                                            }
                                        }
                                        2 -> {
                                            context.append()
                                            when (parse_triple_end_or_object_string_typed_iri_helper_3(context.c)) {
                                                0 -> {
                                                    context.append()
                                                    continue@loop7
                                                }
                                                else -> {
                                                    break@error
                                                }
                                            }
                                        }
                                        else -> {
                                            break@loop7
                                        }
                                    }
                                }
                                onPN_LOCAL()
                                return
                            }
                            else -> {
                                break@error
                            }
                        }
                    }
                    else -> {
                        break@error
                    }
                }
            }
            2 -> {
                context.append()
                when (parse_triple_end_or_object_string_typed_iri_helper_3(context.c)) {
                    0 -> {
                        context.append()
                        loop5@ while (true) {
                            loop6@ while (true) {
                                when (context.c) {
                                    0x2e -> {
                                        context.append()
                                    }
                                    else -> {
                                        break@loop6
                                    }
                                }
                            }
                            when (parse_triple_end_or_object_string_typed_iri_helper_1(context.c)) {
                                0 -> {
                                    context.append()
                                    continue@loop5
                                }
                                1 -> {
                                    context.append()
                                    when (parse_triple_end_or_object_string_typed_iri_helper_2(context.c)) {
                                        0 -> {
                                            context.append()
                                            when (parse_triple_end_or_object_string_typed_iri_helper_2(context.c)) {
                                                0 -> {
                                                    context.append()
                                                    continue@loop5
                                                }
                                                else -> {
                                                    break@error
                                                }
                                            }
                                        }
                                        else -> {
                                            break@error
                                        }
                                    }
                                }
                                2 -> {
                                    context.append()
                                    when (parse_triple_end_or_object_string_typed_iri_helper_3(context.c)) {
                                        0 -> {
                                            context.append()
                                            continue@loop5
                                        }
                                        else -> {
                                            break@error
                                        }
                                    }
                                }
                                else -> {
                                    break@loop5
                                }
                            }
                        }
                        onPN_LOCAL()
                        return
                    }
                    else -> {
                        break@error
                    }
                }
            }
            3 -> {
                context.append()
                onPREDICATE_LISTA()
                return
            }
            4 -> {
                context.append()
                onOBJECT_LISTA()
                return
            }
            5 -> {
                context.append()
                onDOT()
                return
            }
            6 -> {
                context.append()
                loop3@ while (true) {
                    when (context.c) {
                        0x9, 0xa, 0xd, 0x20 -> {
                            context.append()
                        }
                        else -> {
                            break@loop3
                        }
                    }
                }
                onSKIP_WS_FORCED()
                return
            }
            else -> {
                break@error
            }
        }
    }
    throw ParserExceptionUnexpectedChar(context)
}

internal fun parse_triple_end_or_object_string_typed_iri_helper_0(c: Int): Int {
    if (c < 0x9) {
        return 7
    } else if (c <= 0xa) {
        return 6
    } else if (c < 0xd) {
        return 7
    } else if (c <= 0xd) {
        return 6
    } else if (c < 0x20) {
        return 7
    } else if (c <= 0x20) {
        return 6
    } else if (c < 0x25) {
        return 7
    } else if (c <= 0x25) {
        return 1
    } else if (c < 0x2c) {
        return 7
    } else if (c <= 0x2c) {
        return 4
    } else if (c < 0x2e) {
        return 7
    } else if (c <= 0x2e) {
        return 5
    } else if (c < 0x30) {
        return 7
    } else if (c <= 0x3a) {
        return 0
    } else if (c < 0x3b) {
        return 7
    } else if (c <= 0x3b) {
        return 3
    } else if (c < 0x41) {
        return 7
    } else if (c <= 0x5a) {
        return 0
    } else if (c < 0x5c) {
        return 7
    } else if (c <= 0x5c) {
        return 2
    } else if (c < 0x5f) {
        return 7
    } else if (c <= 0x5f) {
        return 0
    } else if (c < 0x61) {
        return 7
    } else if (c <= 0x7a) {
        return 0
    } else if (c < 0xc0) {
        return 7
    } else if (c <= 0xd6) {
        return 0
    } else if (c < 0xd8) {
        return 7
    } else if (c <= 0xf6) {
        return 0
    } else if (c < 0xf8) {
        return 7
    } else if (c <= 0x2ff) {
        return 0
    } else if (c < 0x370) {
        return 7
    } else if (c <= 0x37d) {
        return 0
    } else if (c < 0x37f) {
        return 7
    } else if (c <= 0x1fff) {
        return 0
    } else if (c < 0x200c) {
        return 7
    } else if (c <= 0x200d) {
        return 0
    } else if (c < 0x2070) {
        return 7
    } else if (c <= 0x218f) {
        return 0
    } else if (c < 0x2c00) {
        return 7
    } else if (c <= 0x2fef) {
        return 0
    } else if (c < 0x3001) {
        return 7
    } else if (c <= 0xd7ff) {
        return 0
    } else if (c < 0xf900) {
        return 7
    } else if (c <= 0xfdcf) {
        return 0
    } else if (c < 0xfdf0) {
        return 7
    } else if (c <= 0xfffd) {
        return 0
    } else if (c < 0x10000) {
        return 7
    } else if (c <= 0x1fffff) {
        return 0
    } else {
        return 7
    }
}

internal fun parse_triple_end_or_object_string_typed_iri_helper_1(c: Int): Int {
    if (c < 0x25) {
        return 3
    } else if (c <= 0x25) {
        return 1
    } else if (c < 0x2d) {
        return 3
    } else if (c <= 0x2d) {
        return 0
    } else if (c < 0x30) {
        return 3
    } else if (c <= 0x3a) {
        return 0
    } else if (c < 0x41) {
        return 3
    } else if (c <= 0x5a) {
        return 0
    } else if (c < 0x5c) {
        return 3
    } else if (c <= 0x5c) {
        return 2
    } else if (c < 0x5f) {
        return 3
    } else if (c <= 0x5f) {
        return 0
    } else if (c < 0x61) {
        return 3
    } else if (c <= 0x7a) {
        return 0
    } else if (c < 0xb7) {
        return 3
    } else if (c <= 0xb7) {
        return 0
    } else if (c < 0xc0) {
        return 3
    } else if (c <= 0xd6) {
        return 0
    } else if (c < 0xd8) {
        return 3
    } else if (c <= 0xf6) {
        return 0
    } else if (c < 0xf8) {
        return 3
    } else if (c <= 0x37d) {
        return 0
    } else if (c < 0x37f) {
        return 3
    } else if (c <= 0x1fff) {
        return 0
    } else if (c < 0x200c) {
        return 3
    } else if (c <= 0x200d) {
        return 0
    } else if (c < 0x203f) {
        return 3
    } else if (c <= 0x2040) {
        return 0
    } else if (c < 0x2070) {
        return 3
    } else if (c <= 0x218f) {
        return 0
    } else if (c < 0x2c00) {
        return 3
    } else if (c <= 0x2fef) {
        return 0
    } else if (c < 0x3001) {
        return 3
    } else if (c <= 0xd7ff) {
        return 0
    } else if (c < 0xf900) {
        return 3
    } else if (c <= 0xfdcf) {
        return 0
    } else if (c < 0xfdf0) {
        return 3
    } else if (c <= 0xfffd) {
        return 0
    } else if (c < 0x10000) {
        return 3
    } else if (c <= 0x1fffff) {
        return 0
    } else {
        return 3
    }
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun parse_triple_end_or_object_string_typed_iri_helper_2(c: Int): Int {
    return if (c < 0x30) {
        1
    } else if (c <= 0x39) {
        0
    } else if (c < 0x41) {
        1
    } else if (c <= 0x46) {
        0
    } else if (c < 0x61) {
        1
    } else if (c <= 0x66) {
        0
    } else {
        1
    }
}

internal fun parse_triple_end_or_object_string_typed_iri_helper_3(c: Int): Int {
    if (c < 0x21) {
        return 1
    } else if (c <= 0x21) {
        return 0
    } else if (c < 0x23) {
        return 1
    } else if (c <= 0x2f) {
        return 0
    } else if (c < 0x3b) {
        return 1
    } else if (c <= 0x3b) {
        return 0
    } else if (c < 0x3d) {
        return 1
    } else if (c <= 0x3d) {
        return 0
    } else if (c < 0x3f) {
        return 1
    } else if (c <= 0x40) {
        return 0
    } else if (c < 0x5f) {
        return 1
    } else if (c <= 0x5f) {
        return 0
    } else if (c < 0x7e) {
        return 1
    } else if (c <= 0x7e) {
        return 0
    } else {
        return 1
    }
}

internal inline fun parse_subject_iri_or_ws(
    context: ParserContext,
    crossinline onPN_LOCAL: () -> Unit,
    crossinline onSKIP_WS_FORCED: () -> Unit
) {
    context.clear()
    error@ while (true) {
        when (parse_subject_iri_or_ws_helper_0(context.c)) {
            0 -> {
                context.append()
                loop3@ while (true) {
                    loop4@ while (true) {
                        when (context.c) {
                            0x2e -> {
                                context.append()
                            }
                            else -> {
                                break@loop4
                            }
                        }
                    }
                    when (parse_subject_iri_or_ws_helper_1(context.c)) {
                        0 -> {
                            context.append()
                            continue@loop3
                        }
                        1 -> {
                            context.append()
                            when (parse_subject_iri_or_ws_helper_2(context.c)) {
                                0 -> {
                                    context.append()
                                    when (parse_subject_iri_or_ws_helper_2(context.c)) {
                                        0 -> {
                                            context.append()
                                            continue@loop3
                                        }
                                        else -> {
                                            break@error
                                        }
                                    }
                                }
                                else -> {
                                    break@error
                                }
                            }
                        }
                        2 -> {
                            context.append()
                            when (parse_subject_iri_or_ws_helper_3(context.c)) {
                                0 -> {
                                    context.append()
                                    continue@loop3
                                }
                                else -> {
                                    break@error
                                }
                            }
                        }
                        else -> {
                            break@loop3
                        }
                    }
                }
                onPN_LOCAL()
                return
            }
            1 -> {
                context.append()
                when (parse_subject_iri_or_ws_helper_2(context.c)) {
                    0 -> {
                        context.append()
                        when (parse_subject_iri_or_ws_helper_2(context.c)) {
                            0 -> {
                                context.append()
                                loop7@ while (true) {
                                    loop8@ while (true) {
                                        when (context.c) {
                                            0x2e -> {
                                                context.append()
                                            }
                                            else -> {
                                                break@loop8
                                            }
                                        }
                                    }
                                    when (parse_subject_iri_or_ws_helper_1(context.c)) {
                                        0 -> {
                                            context.append()
                                            continue@loop7
                                        }
                                        1 -> {
                                            context.append()
                                            when (parse_subject_iri_or_ws_helper_2(context.c)) {
                                                0 -> {
                                                    context.append()
                                                    when (parse_subject_iri_or_ws_helper_2(context.c)) {
                                                        0 -> {
                                                            context.append()
                                                            continue@loop7
                                                        }
                                                        else -> {
                                                            break@error
                                                        }
                                                    }
                                                }
                                                else -> {
                                                    break@error
                                                }
                                            }
                                        }
                                        2 -> {
                                            context.append()
                                            when (parse_subject_iri_or_ws_helper_3(context.c)) {
                                                0 -> {
                                                    context.append()
                                                    continue@loop7
                                                }
                                                else -> {
                                                    break@error
                                                }
                                            }
                                        }
                                        else -> {
                                            break@loop7
                                        }
                                    }
                                }
                                onPN_LOCAL()
                                return
                            }
                            else -> {
                                break@error
                            }
                        }
                    }
                    else -> {
                        break@error
                    }
                }
            }
            2 -> {
                context.append()
                when (parse_subject_iri_or_ws_helper_3(context.c)) {
                    0 -> {
                        context.append()
                        loop5@ while (true) {
                            loop6@ while (true) {
                                when (context.c) {
                                    0x2e -> {
                                        context.append()
                                    }
                                    else -> {
                                        break@loop6
                                    }
                                }
                            }
                            when (parse_subject_iri_or_ws_helper_1(context.c)) {
                                0 -> {
                                    context.append()
                                    continue@loop5
                                }
                                1 -> {
                                    context.append()
                                    when (parse_subject_iri_or_ws_helper_2(context.c)) {
                                        0 -> {
                                            context.append()
                                            when (parse_subject_iri_or_ws_helper_2(context.c)) {
                                                0 -> {
                                                    context.append()
                                                    continue@loop5
                                                }
                                                else -> {
                                                    break@error
                                                }
                                            }
                                        }
                                        else -> {
                                            break@error
                                        }
                                    }
                                }
                                2 -> {
                                    context.append()
                                    when (parse_subject_iri_or_ws_helper_3(context.c)) {
                                        0 -> {
                                            context.append()
                                            continue@loop5
                                        }
                                        else -> {
                                            break@error
                                        }
                                    }
                                }
                                else -> {
                                    break@loop5
                                }
                            }
                        }
                        onPN_LOCAL()
                        return
                    }
                    else -> {
                        break@error
                    }
                }
            }
            3 -> {
                context.append()
                loop3@ while (true) {
                    when (context.c) {
                        0x9, 0xa, 0xd, 0x20 -> {
                            context.append()
                        }
                        else -> {
                            break@loop3
                        }
                    }
                }
                onSKIP_WS_FORCED()
                return
            }
            else -> {
                break@error
            }
        }
    }
    throw ParserExceptionUnexpectedChar(context)
}

internal fun parse_subject_iri_or_ws_helper_0(c: Int): Int {
    if (c < 0x9) {
        return 4
    } else if (c <= 0xa) {
        return 3
    } else if (c < 0xd) {
        return 4
    } else if (c <= 0xd) {
        return 3
    } else if (c < 0x20) {
        return 4
    } else if (c <= 0x20) {
        return 3
    } else if (c < 0x25) {
        return 4
    } else if (c <= 0x25) {
        return 1
    } else if (c < 0x30) {
        return 4
    } else if (c <= 0x3a) {
        return 0
    } else if (c < 0x41) {
        return 4
    } else if (c <= 0x5a) {
        return 0
    } else if (c < 0x5c) {
        return 4
    } else if (c <= 0x5c) {
        return 2
    } else if (c < 0x5f) {
        return 4
    } else if (c <= 0x5f) {
        return 0
    } else if (c < 0x61) {
        return 4
    } else if (c <= 0x7a) {
        return 0
    } else if (c < 0xc0) {
        return 4
    } else if (c <= 0xd6) {
        return 0
    } else if (c < 0xd8) {
        return 4
    } else if (c <= 0xf6) {
        return 0
    } else if (c < 0xf8) {
        return 4
    } else if (c <= 0x2ff) {
        return 0
    } else if (c < 0x370) {
        return 4
    } else if (c <= 0x37d) {
        return 0
    } else if (c < 0x37f) {
        return 4
    } else if (c <= 0x1fff) {
        return 0
    } else if (c < 0x200c) {
        return 4
    } else if (c <= 0x200d) {
        return 0
    } else if (c < 0x2070) {
        return 4
    } else if (c <= 0x218f) {
        return 0
    } else if (c < 0x2c00) {
        return 4
    } else if (c <= 0x2fef) {
        return 0
    } else if (c < 0x3001) {
        return 4
    } else if (c <= 0xd7ff) {
        return 0
    } else if (c < 0xf900) {
        return 4
    } else if (c <= 0xfdcf) {
        return 0
    } else if (c < 0xfdf0) {
        return 4
    } else if (c <= 0xfffd) {
        return 0
    } else if (c < 0x10000) {
        return 4
    } else if (c <= 0x1fffff) {
        return 0
    } else {
        return 4
    }
}

internal fun parse_subject_iri_or_ws_helper_1(c: Int): Int {
    if (c < 0x25) {
        return 3
    } else if (c <= 0x25) {
        return 1
    } else if (c < 0x2d) {
        return 3
    } else if (c <= 0x2d) {
        return 0
    } else if (c < 0x30) {
        return 3
    } else if (c <= 0x3a) {
        return 0
    } else if (c < 0x41) {
        return 3
    } else if (c <= 0x5a) {
        return 0
    } else if (c < 0x5c) {
        return 3
    } else if (c <= 0x5c) {
        return 2
    } else if (c < 0x5f) {
        return 3
    } else if (c <= 0x5f) {
        return 0
    } else if (c < 0x61) {
        return 3
    } else if (c <= 0x7a) {
        return 0
    } else if (c < 0xb7) {
        return 3
    } else if (c <= 0xb7) {
        return 0
    } else if (c < 0xc0) {
        return 3
    } else if (c <= 0xd6) {
        return 0
    } else if (c < 0xd8) {
        return 3
    } else if (c <= 0xf6) {
        return 0
    } else if (c < 0xf8) {
        return 3
    } else if (c <= 0x37d) {
        return 0
    } else if (c < 0x37f) {
        return 3
    } else if (c <= 0x1fff) {
        return 0
    } else if (c < 0x200c) {
        return 3
    } else if (c <= 0x200d) {
        return 0
    } else if (c < 0x203f) {
        return 3
    } else if (c <= 0x2040) {
        return 0
    } else if (c < 0x2070) {
        return 3
    } else if (c <= 0x218f) {
        return 0
    } else if (c < 0x2c00) {
        return 3
    } else if (c <= 0x2fef) {
        return 0
    } else if (c < 0x3001) {
        return 3
    } else if (c <= 0xd7ff) {
        return 0
    } else if (c < 0xf900) {
        return 3
    } else if (c <= 0xfdcf) {
        return 0
    } else if (c < 0xfdf0) {
        return 3
    } else if (c <= 0xfffd) {
        return 0
    } else if (c < 0x10000) {
        return 3
    } else if (c <= 0x1fffff) {
        return 0
    } else {
        return 3
    }
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun parse_subject_iri_or_ws_helper_2(c: Int): Int {
    return if (c < 0x30) {
        1
    } else if (c <= 0x39) {
        0
    } else if (c < 0x41) {
        1
    } else if (c <= 0x46) {
        0
    } else if (c < 0x61) {
        1
    } else if (c <= 0x66) {
        0
    } else {
        1
    }
}

internal fun parse_subject_iri_or_ws_helper_3(c: Int): Int {
    if (c < 0x21) {
        return 1
    } else if (c <= 0x21) {
        return 0
    } else if (c < 0x23) {
        return 1
    } else if (c <= 0x2f) {
        return 0
    } else if (c < 0x3b) {
        return 1
    } else if (c <= 0x3b) {
        return 0
    } else if (c < 0x3d) {
        return 1
    } else if (c <= 0x3d) {
        return 0
    } else if (c < 0x3f) {
        return 1
    } else if (c <= 0x40) {
        return 0
    } else if (c < 0x5f) {
        return 1
    } else if (c <= 0x5f) {
        return 0
    } else if (c < 0x7e) {
        return 1
    } else if (c <= 0x7e) {
        return 0
    } else {
        return 1
    }
}

internal inline fun parse_predicate_iri_or_ws(
    context: ParserContext,
    crossinline onPN_LOCAL: () -> Unit,
    crossinline onSKIP_WS_FORCED: () -> Unit
) {
    context.clear()
    error@ while (true) {
        when (parse_predicate_iri_or_ws_helper_0(context.c)) {
            0 -> {
                context.append()
                loop3@ while (true) {
                    loop4@ while (true) {
                        when (context.c) {
                            0x2e -> {
                                context.append()
                            }
                            else -> {
                                break@loop4
                            }
                        }
                    }
                    when (parse_predicate_iri_or_ws_helper_1(context.c)) {
                        0 -> {
                            context.append()
                            continue@loop3
                        }
                        1 -> {
                            context.append()
                            when (parse_predicate_iri_or_ws_helper_2(context.c)) {
                                0 -> {
                                    context.append()
                                    when (parse_predicate_iri_or_ws_helper_2(context.c)) {
                                        0 -> {
                                            context.append()
                                            continue@loop3
                                        }
                                        else -> {
                                            break@error
                                        }
                                    }
                                }
                                else -> {
                                    break@error
                                }
                            }
                        }
                        2 -> {
                            context.append()
                            when (parse_predicate_iri_or_ws_helper_3(context.c)) {
                                0 -> {
                                    context.append()
                                    continue@loop3
                                }
                                else -> {
                                    break@error
                                }
                            }
                        }
                        else -> {
                            break@loop3
                        }
                    }
                }
                onPN_LOCAL()
                return
            }
            1 -> {
                context.append()
                when (parse_predicate_iri_or_ws_helper_2(context.c)) {
                    0 -> {
                        context.append()
                        when (parse_predicate_iri_or_ws_helper_2(context.c)) {
                            0 -> {
                                context.append()
                                loop7@ while (true) {
                                    loop8@ while (true) {
                                        when (context.c) {
                                            0x2e -> {
                                                context.append()
                                            }
                                            else -> {
                                                break@loop8
                                            }
                                        }
                                    }
                                    when (parse_predicate_iri_or_ws_helper_1(context.c)) {
                                        0 -> {
                                            context.append()
                                            continue@loop7
                                        }
                                        1 -> {
                                            context.append()
                                            when (parse_predicate_iri_or_ws_helper_2(context.c)) {
                                                0 -> {
                                                    context.append()
                                                    when (parse_predicate_iri_or_ws_helper_2(context.c)) {
                                                        0 -> {
                                                            context.append()
                                                            continue@loop7
                                                        }
                                                        else -> {
                                                            break@error
                                                        }
                                                    }
                                                }
                                                else -> {
                                                    break@error
                                                }
                                            }
                                        }
                                        2 -> {
                                            context.append()
                                            when (parse_predicate_iri_or_ws_helper_3(context.c)) {
                                                0 -> {
                                                    context.append()
                                                    continue@loop7
                                                }
                                                else -> {
                                                    break@error
                                                }
                                            }
                                        }
                                        else -> {
                                            break@loop7
                                        }
                                    }
                                }
                                onPN_LOCAL()
                                return
                            }
                            else -> {
                                break@error
                            }
                        }
                    }
                    else -> {
                        break@error
                    }
                }
            }
            2 -> {
                context.append()
                when (parse_predicate_iri_or_ws_helper_3(context.c)) {
                    0 -> {
                        context.append()
                        loop5@ while (true) {
                            loop6@ while (true) {
                                when (context.c) {
                                    0x2e -> {
                                        context.append()
                                    }
                                    else -> {
                                        break@loop6
                                    }
                                }
                            }
                            when (parse_predicate_iri_or_ws_helper_1(context.c)) {
                                0 -> {
                                    context.append()
                                    continue@loop5
                                }
                                1 -> {
                                    context.append()
                                    when (parse_predicate_iri_or_ws_helper_2(context.c)) {
                                        0 -> {
                                            context.append()
                                            when (parse_predicate_iri_or_ws_helper_2(context.c)) {
                                                0 -> {
                                                    context.append()
                                                    continue@loop5
                                                }
                                                else -> {
                                                    break@error
                                                }
                                            }
                                        }
                                        else -> {
                                            break@error
                                        }
                                    }
                                }
                                2 -> {
                                    context.append()
                                    when (parse_predicate_iri_or_ws_helper_3(context.c)) {
                                        0 -> {
                                            context.append()
                                            continue@loop5
                                        }
                                        else -> {
                                            break@error
                                        }
                                    }
                                }
                                else -> {
                                    break@loop5
                                }
                            }
                        }
                        onPN_LOCAL()
                        return
                    }
                    else -> {
                        break@error
                    }
                }
            }
            3 -> {
                context.append()
                loop3@ while (true) {
                    when (context.c) {
                        0x9, 0xa, 0xd, 0x20 -> {
                            context.append()
                        }
                        else -> {
                            break@loop3
                        }
                    }
                }
                onSKIP_WS_FORCED()
                return
            }
            else -> {
                break@error
            }
        }
    }
    throw ParserExceptionUnexpectedChar(context)
}

internal fun parse_predicate_iri_or_ws_helper_0(c: Int): Int {
    if (c < 0x9) {
        return 4
    } else if (c <= 0xa) {
        return 3
    } else if (c < 0xd) {
        return 4
    } else if (c <= 0xd) {
        return 3
    } else if (c < 0x20) {
        return 4
    } else if (c <= 0x20) {
        return 3
    } else if (c < 0x25) {
        return 4
    } else if (c <= 0x25) {
        return 1
    } else if (c < 0x30) {
        return 4
    } else if (c <= 0x3a) {
        return 0
    } else if (c < 0x41) {
        return 4
    } else if (c <= 0x5a) {
        return 0
    } else if (c < 0x5c) {
        return 4
    } else if (c <= 0x5c) {
        return 2
    } else if (c < 0x5f) {
        return 4
    } else if (c <= 0x5f) {
        return 0
    } else if (c < 0x61) {
        return 4
    } else if (c <= 0x7a) {
        return 0
    } else if (c < 0xc0) {
        return 4
    } else if (c <= 0xd6) {
        return 0
    } else if (c < 0xd8) {
        return 4
    } else if (c <= 0xf6) {
        return 0
    } else if (c < 0xf8) {
        return 4
    } else if (c <= 0x2ff) {
        return 0
    } else if (c < 0x370) {
        return 4
    } else if (c <= 0x37d) {
        return 0
    } else if (c < 0x37f) {
        return 4
    } else if (c <= 0x1fff) {
        return 0
    } else if (c < 0x200c) {
        return 4
    } else if (c <= 0x200d) {
        return 0
    } else if (c < 0x2070) {
        return 4
    } else if (c <= 0x218f) {
        return 0
    } else if (c < 0x2c00) {
        return 4
    } else if (c <= 0x2fef) {
        return 0
    } else if (c < 0x3001) {
        return 4
    } else if (c <= 0xd7ff) {
        return 0
    } else if (c < 0xf900) {
        return 4
    } else if (c <= 0xfdcf) {
        return 0
    } else if (c < 0xfdf0) {
        return 4
    } else if (c <= 0xfffd) {
        return 0
    } else if (c < 0x10000) {
        return 4
    } else if (c <= 0x1fffff) {
        return 0
    } else {
        return 4
    }
}

internal fun parse_predicate_iri_or_ws_helper_1(c: Int): Int {
    if (c < 0x25) {
        return 3
    } else if (c <= 0x25) {
        return 1
    } else if (c < 0x2d) {
        return 3
    } else if (c <= 0x2d) {
        return 0
    } else if (c < 0x30) {
        return 3
    } else if (c <= 0x3a) {
        return 0
    } else if (c < 0x41) {
        return 3
    } else if (c <= 0x5a) {
        return 0
    } else if (c < 0x5c) {
        return 3
    } else if (c <= 0x5c) {
        return 2
    } else if (c < 0x5f) {
        return 3
    } else if (c <= 0x5f) {
        return 0
    } else if (c < 0x61) {
        return 3
    } else if (c <= 0x7a) {
        return 0
    } else if (c < 0xb7) {
        return 3
    } else if (c <= 0xb7) {
        return 0
    } else if (c < 0xc0) {
        return 3
    } else if (c <= 0xd6) {
        return 0
    } else if (c < 0xd8) {
        return 3
    } else if (c <= 0xf6) {
        return 0
    } else if (c < 0xf8) {
        return 3
    } else if (c <= 0x37d) {
        return 0
    } else if (c < 0x37f) {
        return 3
    } else if (c <= 0x1fff) {
        return 0
    } else if (c < 0x200c) {
        return 3
    } else if (c <= 0x200d) {
        return 0
    } else if (c < 0x203f) {
        return 3
    } else if (c <= 0x2040) {
        return 0
    } else if (c < 0x2070) {
        return 3
    } else if (c <= 0x218f) {
        return 0
    } else if (c < 0x2c00) {
        return 3
    } else if (c <= 0x2fef) {
        return 0
    } else if (c < 0x3001) {
        return 3
    } else if (c <= 0xd7ff) {
        return 0
    } else if (c < 0xf900) {
        return 3
    } else if (c <= 0xfdcf) {
        return 0
    } else if (c < 0xfdf0) {
        return 3
    } else if (c <= 0xfffd) {
        return 0
    } else if (c < 0x10000) {
        return 3
    } else if (c <= 0x1fffff) {
        return 0
    } else {
        return 3
    }
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun parse_predicate_iri_or_ws_helper_2(c: Int): Int {
    return if (c < 0x30) {
        1
    } else if (c <= 0x39) {
        0
    } else if (c < 0x41) {
        1
    } else if (c <= 0x46) {
        0
    } else if (c < 0x61) {
        1
    } else if (c <= 0x66) {
        0
    } else {
        1
    }
}

internal fun parse_predicate_iri_or_ws_helper_3(c: Int): Int {
    if (c < 0x21) {
        return 1
    } else if (c <= 0x21) {
        return 0
    } else if (c < 0x23) {
        return 1
    } else if (c <= 0x2f) {
        return 0
    } else if (c < 0x3b) {
        return 1
    } else if (c <= 0x3b) {
        return 0
    } else if (c < 0x3d) {
        return 1
    } else if (c <= 0x3d) {
        return 0
    } else if (c < 0x3f) {
        return 1
    } else if (c <= 0x40) {
        return 0
    } else if (c < 0x5f) {
        return 1
    } else if (c <= 0x5f) {
        return 0
    } else if (c < 0x7e) {
        return 1
    } else if (c <= 0x7e) {
        return 0
    } else {
        return 1
    }
}
