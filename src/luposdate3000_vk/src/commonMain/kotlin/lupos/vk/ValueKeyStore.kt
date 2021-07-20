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
package lupos.vk

import lupos.ProguardTestAnnotation
import lupos.shared.IBufferManager
import lupos.shared.SanityCheck
import lupos.shared.dynamicArray.ByteArrayWrapper
import lupos.shared.inline.BufferManagerPage
import lupos.shared.inline.dynamicArray.ByteArrayWrapperExt
import kotlin.jvm.JvmField
import kotlin.math.min

public class ValueKeyStore {
    public companion object {
        public const val ID_NULL: Int = -1
        internal const val PAGEID_NULL_PTR: Int = -1
        internal const val PAGE_TYPE_LEAF: Int = 0
        internal const val PAGE_TYPE_INNER: Int = 1
        internal const val RESERVED_SPACE: Int = 16
        internal const val MIN_BRANCHING: Int = 10
    }

    @JvmField
    internal val rootPageID: Int

    @JvmField
    internal val bufferManager: IBufferManager

    @JvmField
    internal var firstInnerID = ValueKeyStore.PAGEID_NULL_PTR

    @JvmField
    internal var firstLeafID = ValueKeyStore.PAGEID_NULL_PTR

    public constructor(bufferManager: IBufferManager, rootPageID: Int, initFromRootPage: Boolean) {
        this.bufferManager = bufferManager
        this.rootPageID = rootPageID
        val rootPage = bufferManager.getPage(/*SOURCE_FILE_START*/"/src/luposdate3000/src/luposdate3000_vk/src/commonMain/kotlin/lupos/vk/ValueKeyStore.kt:52"/*SOURCE_FILE_END*/, rootPageID)
        if (initFromRootPage) {
            firstLeafID = BufferManagerPage.readInt4(rootPage, 0)
            firstInnerID = BufferManagerPage.readInt4(rootPage, 4)
        } else {
            val writer = ValueKeyStoreWriter(bufferManager, PAGE_TYPE_LEAF)
            writer.close()
            firstLeafID = writer.firstLeafID
            var parent = writer
            while (parent.parentLayer != null) {
                parent = parent.parentLayer!!
            }
            firstInnerID = parent.firstLeafID
            BufferManagerPage.writeInt4(rootPage, 0, firstLeafID)
            BufferManagerPage.writeInt4(rootPage, 4, firstInnerID)
        }
        bufferManager.releasePage(/*SOURCE_FILE_START*/"/src/luposdate3000/src/luposdate3000_vk/src/commonMain/kotlin/lupos/vk/ValueKeyStore.kt:68"/*SOURCE_FILE_END*/, rootPageID)
    }

    @ProguardTestAnnotation
    public fun delete() {
        deleteContent(firstInnerID)
        bufferManager.getPage(/*SOURCE_FILE_START*/"/src/luposdate3000/src/luposdate3000_vk/src/commonMain/kotlin/lupos/vk/ValueKeyStore.kt:74"/*SOURCE_FILE_END*/, rootPageID)
        bufferManager.deletePage(/*SOURCE_FILE_START*/"/src/luposdate3000/src/luposdate3000_vk/src/commonMain/kotlin/lupos/vk/ValueKeyStore.kt:75"/*SOURCE_FILE_END*/, rootPageID)
    }

    public fun deleteContent(root: Int) {
        var nextStage = root
        while (nextStage != ValueKeyStore.PAGEID_NULL_PTR) {
            var node = nextStage
            var first = true
            while (node != ValueKeyStore.PAGEID_NULL_PTR) {
                val page = bufferManager.getPage(/*SOURCE_FILE_START*/"/src/luposdate3000/src/luposdate3000_vk/src/commonMain/kotlin/lupos/vk/ValueKeyStore.kt:84"/*SOURCE_FILE_END*/, node)
                val nextPageID = BufferManagerPage.readInt4(page, 4)
                if (first) {
                    first = false
                    if (BufferManagerPage.readInt4(page, 0) == PAGE_TYPE_INNER) {
                        nextStage = BufferManagerPage.readInt4(page, 12)
                    } else {
                        nextStage = ValueKeyStore.PAGEID_NULL_PTR
                    }
                }
                bufferManager.deletePage(/*SOURCE_FILE_START*/"/src/luposdate3000/src/luposdate3000_vk/src/commonMain/kotlin/lupos/vk/ValueKeyStore.kt:94"/*SOURCE_FILE_END*/, node)
                node = nextPageID
            }
        }
    }

    @ProguardTestAnnotation
    public fun close() {
    }

    @ProguardTestAnnotation
    public fun getIterator(buffer: ByteArrayWrapper): ValueKeyStoreIteratorLeaf {
        return ValueKeyStoreIteratorLeaf(bufferManager, firstLeafID, buffer)
    }

    public fun hasValue(data: ByteArrayWrapper): Int {
        val buffer = ByteArrayWrapper()
        var iterator = ValueKeyStoreIteratorSearch()
        return iterator.search(data, firstLeafID, bufferManager, buffer)
    }

    public fun createValue(data: ByteArrayWrapper, value: () -> Int): Int {
        var res = ID_NULL
        val buffer = ByteArrayWrapper()
        val writer = ValueKeyStoreWriter(bufferManager, PAGE_TYPE_LEAF)
        val reader = ValueKeyStoreIteratorLeaf(bufferManager, firstLeafID, buffer)
        var dataIsInserted = false
        while (!dataIsInserted && reader.hasNext()) {
            val id = reader.next()
            if (data == buffer) {
                res = id
                dataIsInserted = true
                SanityCheck.check({ /*SOURCE_FILE_START*/"/src/luposdate3000/src/luposdate3000_vk/src/commonMain/kotlin/lupos/vk/ValueKeyStore.kt:126"/*SOURCE_FILE_END*/ }, { res != ValueKeyStore.ID_NULL })
            } else if (data < buffer) {
                res = value()
                dataIsInserted = true
                SanityCheck.check({ /*SOURCE_FILE_START*/"/src/luposdate3000/src/luposdate3000_vk/src/commonMain/kotlin/lupos/vk/ValueKeyStore.kt:130"/*SOURCE_FILE_END*/ }, { res != ValueKeyStore.ID_NULL })
                writer.write(res, data)
            }
            writer.write(id, buffer)
        }
        while (reader.hasNext()) {
            val id = reader.next()
            writer.write(id, buffer)
        }
        if (!dataIsInserted) {
            res = value()
            SanityCheck.check({ /*SOURCE_FILE_START*/"/src/luposdate3000/src/luposdate3000_vk/src/commonMain/kotlin/lupos/vk/ValueKeyStore.kt:141"/*SOURCE_FILE_END*/ }, { res != ValueKeyStore.ID_NULL })
            writer.write(res, data)
        }
        reader.close()
        writer.close()
        deleteContent(firstInnerID)
        firstLeafID = writer.firstLeafID
        var parent = writer
        while (parent.parentLayer != null) {
            parent = parent.parentLayer!!
        }
        firstInnerID = parent.firstLeafID
        val rootPage = bufferManager.getPage(/*SOURCE_FILE_START*/"/src/luposdate3000/src/luposdate3000_vk/src/commonMain/kotlin/lupos/vk/ValueKeyStore.kt:153"/*SOURCE_FILE_END*/, rootPageID)
        BufferManagerPage.writeInt4(rootPage, 0, firstLeafID)
        BufferManagerPage.writeInt4(rootPage, 4, firstInnerID)
        bufferManager.releasePage(/*SOURCE_FILE_START*/"/src/luposdate3000/src/luposdate3000_vk/src/commonMain/kotlin/lupos/vk/ValueKeyStore.kt:156"/*SOURCE_FILE_END*/, rootPageID)
        return res
    }

    public fun createValues(hasNext: () -> Boolean, next: () -> ByteArrayWrapper, onNotFound: (ByteArrayWrapper) -> Int, onFound: (ByteArrayWrapper, Int) -> Unit) {
        val buffer = ByteArrayWrapper()
        var data = buffer
        var dataIsInserted = false
        var bufferIsInserted = false
        var id = 0
        fun localNextData() {
            if (hasNext()) {
                data = next()
            } else {
                dataIsInserted = true
            }
        }
        localNextData()
        if (!dataIsInserted) {
            val writer = ValueKeyStoreWriter(bufferManager, PAGE_TYPE_LEAF)
            val reader = ValueKeyStoreIteratorLeaf(bufferManager, firstLeafID, buffer)
            fun localNextReader() {
                if (reader.hasNext()) {
                    id = reader.next()
                } else {
                    bufferIsInserted = true
                }
            }
            localNextReader()
            while (!dataIsInserted && !bufferIsInserted) {
                if (data == buffer) {
                    onFound(buffer, id)
                    writer.write(id, buffer)
                    localNextData()
                    localNextReader()
                } else if (data < buffer) {
                    val res = onNotFound(data)
                    SanityCheck.check({ /*SOURCE_FILE_START*/"/src/luposdate3000/src/luposdate3000_vk/src/commonMain/kotlin/lupos/vk/ValueKeyStore.kt:193"/*SOURCE_FILE_END*/ }, { res != ValueKeyStore.ID_NULL })
                    writer.write(res, data)
                    localNextData()
                } else {
                    writer.write(id, buffer)
                    localNextReader()
                }
            }
            while (!bufferIsInserted) {
                writer.write(id, buffer)
                localNextReader()
            }
            while (!dataIsInserted) {
                val res = onNotFound(data)
                SanityCheck.check({ /*SOURCE_FILE_START*/"/src/luposdate3000/src/luposdate3000_vk/src/commonMain/kotlin/lupos/vk/ValueKeyStore.kt:207"/*SOURCE_FILE_END*/ }, { res != ValueKeyStore.ID_NULL })
                writer.write(res, data)
                localNextData()
            }
            reader.close()
            writer.close()
            deleteContent(firstInnerID)
            firstLeafID = writer.firstLeafID
            var parent = writer
            while (parent.parentLayer != null) {
                parent = parent.parentLayer!!
            }
            firstInnerID = parent.firstLeafID
            val rootPage = bufferManager.getPage(/*SOURCE_FILE_START*/"/src/luposdate3000/src/luposdate3000_vk/src/commonMain/kotlin/lupos/vk/ValueKeyStore.kt:220"/*SOURCE_FILE_END*/, rootPageID)
            BufferManagerPage.writeInt4(rootPage, 0, firstLeafID)
            BufferManagerPage.writeInt4(rootPage, 4, firstInnerID)
            bufferManager.releasePage(/*SOURCE_FILE_START*/"/src/luposdate3000/src/luposdate3000_vk/src/commonMain/kotlin/lupos/vk/ValueKeyStore.kt:223"/*SOURCE_FILE_END*/, rootPageID)
        }
    }
}

internal class ValueKeyStoreWriter {
    @JvmField
    internal var firstLeafID: Int = ValueKeyStore.PAGEID_NULL_PTR

    @JvmField
    internal var lastPageID: Int = ValueKeyStore.PAGEID_NULL_PTR

    @JvmField
    internal var pageid: Int = ValueKeyStore.PAGEID_NULL_PTR

    @JvmField
    internal var page: ByteArray

    @JvmField
    internal var offset = 12

    @JvmField
    internal val lastBuffer = ByteArrayWrapper()

    @JvmField
    internal val pageType: Int

    @JvmField
    internal val bufferManager: IBufferManager

    @JvmField
    internal var parentLayer: ValueKeyStoreWriter? = null

    @JvmField
    internal var counter = 0

    @JvmField
    internal var lastChildPageID = ValueKeyStore.PAGEID_NULL_PTR

    internal constructor(bufferManager: IBufferManager, pageType: Int) : this(bufferManager, pageType, ValueKeyStore.PAGEID_NULL_PTR)

    private fun writeHeader() {
        BufferManagerPage.writeInt4(page, 0, pageType)
        BufferManagerPage.writeInt4(page, 4, ValueKeyStore.PAGEID_NULL_PTR)
        if (pageType == ValueKeyStore.PAGE_TYPE_INNER) {
            offset = 16
            BufferManagerPage.writeInt4(page, 12, lastChildPageID)
        } else {
            offset = 12
        }
        BufferManagerPage.writeInt4(page, 8, offset)
    }

    internal constructor(bufferManager: IBufferManager, pageType: Int, childPageID: Int) {
        this.bufferManager = bufferManager
        this.pageType = pageType
        pageid = bufferManager.allocPage(/*SOURCE_FILE_START*/"/src/luposdate3000/src/luposdate3000_vk/src/commonMain/kotlin/lupos/vk/ValueKeyStore.kt:279"/*SOURCE_FILE_END*/)
        page = bufferManager.getPage(/*SOURCE_FILE_START*/"/src/luposdate3000/src/luposdate3000_vk/src/commonMain/kotlin/lupos/vk/ValueKeyStore.kt:280"/*SOURCE_FILE_END*/, pageid)
        firstLeafID = pageid
        lastChildPageID = childPageID
        writeHeader()
    }

    @Suppress("NOTHING_TO_INLINE")
    internal inline fun write(id: Int, buffer: ByteArrayWrapper) {
        write(ValueKeyStore.PAGEID_NULL_PTR, id, buffer)
    }

    internal fun write(childPageID: Int, id: Int, buffer: ByteArrayWrapper) {
        write(childPageID, id, buffer) {
            if (parentLayer == null) {
                parentLayer = ValueKeyStoreWriter(bufferManager, ValueKeyStore.PAGE_TYPE_INNER, firstLeafID)
            }
            parentLayer!!.write(pageid, id, buffer)
        }
        lastChildPageID = childPageID
    }

    internal inline fun write(childPageID: Int, id: Int, buffer: ByteArrayWrapper, onNextEntryPoint: () -> Unit) {
        SanityCheck.check({ /*SOURCE_FILE_START*/"/src/luposdate3000/src/luposdate3000_vk/src/commonMain/kotlin/lupos/vk/ValueKeyStore.kt:302"/*SOURCE_FILE_END*/ }, { offset <= BufferManagerPage.BUFFER_MANAGER_PAGE_SIZE_IN_BYTES - ValueKeyStore.RESERVED_SPACE })
        SanityCheck.check({ /*SOURCE_FILE_START*/"/src/luposdate3000/src/luposdate3000_vk/src/commonMain/kotlin/lupos/vk/ValueKeyStore.kt:303"/*SOURCE_FILE_END*/ }, { id != ValueKeyStore.ID_NULL })
        counter++
        var common = ByteArrayWrapperExt.commonBytes(buffer, lastBuffer)
        ByteArrayWrapperExt.copyInto(buffer, lastBuffer, false)
        BufferManagerPage.writeInt4(page, offset, id)
        BufferManagerPage.writeInt4(page, offset + 4, ByteArrayWrapperExt.getSize(buffer))
        BufferManagerPage.writeInt4(page, offset + 8, common)
        offset += 12
        if (pageType == ValueKeyStore.PAGE_TYPE_INNER) {
            BufferManagerPage.writeInt4(page, offset, childPageID)
            offset += 4
        }
        lastPageID = pageid
        val len = ByteArrayWrapperExt.getSize(buffer)
        var bufferOffset = common
        var writeEntryPoint = false
        while (bufferOffset < len) {
            val l1 = min(BufferManagerPage.BUFFER_MANAGER_PAGE_SIZE_IN_BYTES - offset, len - bufferOffset)
            if (l1 > 0) {
                BufferManagerPage.copyFrom(page, ByteArrayWrapperExt.getBuf(buffer), offset, bufferOffset, bufferOffset + l1)
                bufferOffset += l1
                offset += l1
            }
            if (offset > BufferManagerPage.BUFFER_MANAGER_PAGE_SIZE_IN_BYTES - ValueKeyStore.RESERVED_SPACE) {
                var nextpageid = bufferManager.allocPage(/*SOURCE_FILE_START*/"/src/luposdate3000/src/luposdate3000_vk/src/commonMain/kotlin/lupos/vk/ValueKeyStore.kt:327"/*SOURCE_FILE_END*/)
                BufferManagerPage.writeInt4(page, 4, nextpageid)
                bufferManager.releasePage(/*SOURCE_FILE_START*/"/src/luposdate3000/src/luposdate3000_vk/src/commonMain/kotlin/lupos/vk/ValueKeyStore.kt:329"/*SOURCE_FILE_END*/, pageid)
                pageid = nextpageid
                page = bufferManager.getPage(/*SOURCE_FILE_START*/"/src/luposdate3000/src/luposdate3000_vk/src/commonMain/kotlin/lupos/vk/ValueKeyStore.kt:331"/*SOURCE_FILE_END*/, nextpageid)
                writeHeader()
                writeEntryPoint = true
            }
        }
        if (writeEntryPoint) {
            BufferManagerPage.writeInt4(page, 8, offset)
            ByteArrayWrapperExt.setSize(lastBuffer, 0, false)
            if (counter > ValueKeyStore.MIN_BRANCHING) {
                counter = 0
                onNextEntryPoint()
            }
        }
    }

    internal fun close() {
        if (offset <= BufferManagerPage.BUFFER_MANAGER_PAGE_SIZE_IN_BYTES - ValueKeyStore.RESERVED_SPACE) {
            BufferManagerPage.writeInt4(page, offset, ValueKeyStore.ID_NULL)
        }
        bufferManager.releasePage(/*SOURCE_FILE_START*/"/src/luposdate3000/src/luposdate3000_vk/src/commonMain/kotlin/lupos/vk/ValueKeyStore.kt:350"/*SOURCE_FILE_END*/, pageid)
        parentLayer?.close()
    }
}

public class ValueKeyStoreIteratorLeaf internal constructor(@JvmField internal val bufferManager: IBufferManager, startPageID: Int, @JvmField internal val buffer: ByteArrayWrapper) {
    @JvmField
    internal var pageid = startPageID

    @JvmField
    internal var page = bufferManager.getPage(/*SOURCE_FILE_START*/"/src/luposdate3000/src/luposdate3000_vk/src/commonMain/kotlin/lupos/vk/ValueKeyStore.kt:360"/*SOURCE_FILE_END*/, pageid)

    @JvmField
    internal var nextPageID = BufferManagerPage.readInt4(page, 4)

    @JvmField
    internal var offset = BufferManagerPage.readInt4(page, 8)
    public fun hasNext(): Boolean {
        if (pageid == ValueKeyStore.PAGEID_NULL_PTR) {
            return false
        } else {
            SanityCheck.check({ /*SOURCE_FILE_START*/"/src/luposdate3000/src/luposdate3000_vk/src/commonMain/kotlin/lupos/vk/ValueKeyStore.kt:371"/*SOURCE_FILE_END*/ }, { BufferManagerPage.readInt4(page, 0) == ValueKeyStore.PAGE_TYPE_LEAF })
            return BufferManagerPage.readInt4(page, offset) != ValueKeyStore.ID_NULL
        }
    }

    public fun next(): Int {
        SanityCheck.check({ /*SOURCE_FILE_START*/"/src/luposdate3000/src/luposdate3000_vk/src/commonMain/kotlin/lupos/vk/ValueKeyStore.kt:377"/*SOURCE_FILE_END*/ }, { BufferManagerPage.readInt4(page, 0) == ValueKeyStore.PAGE_TYPE_LEAF })
        val id = BufferManagerPage.readInt4(page, offset)
        val len = BufferManagerPage.readInt4(page, offset + 4)
        var bufferOffset = BufferManagerPage.readInt4(page, offset + 8)
        offset += 12
        ByteArrayWrapperExt.setSize(buffer, len, true)
        while (bufferOffset < len) {
            val l1 = min(BufferManagerPage.BUFFER_MANAGER_PAGE_SIZE_IN_BYTES - offset, len - bufferOffset)
            if (l1 > 0) {
                page.copyInto(ByteArrayWrapperExt.getBuf(buffer), bufferOffset, offset, offset + l1)
                bufferOffset += l1
                offset += l1
            }
            if (offset > BufferManagerPage.BUFFER_MANAGER_PAGE_SIZE_IN_BYTES - ValueKeyStore.RESERVED_SPACE) {
                bufferManager.releasePage(/*SOURCE_FILE_START*/"/src/luposdate3000/src/luposdate3000_vk/src/commonMain/kotlin/lupos/vk/ValueKeyStore.kt:391"/*SOURCE_FILE_END*/, pageid)
                pageid = nextPageID
                if (pageid == ValueKeyStore.PAGEID_NULL_PTR) {
                    SanityCheck.check({ /*SOURCE_FILE_START*/"/src/luposdate3000/src/luposdate3000_vk/src/commonMain/kotlin/lupos/vk/ValueKeyStore.kt:394"/*SOURCE_FILE_END*/ }, { bufferOffset == len })
                } else {
                    page = bufferManager.getPage(/*SOURCE_FILE_START*/"/src/luposdate3000/src/luposdate3000_vk/src/commonMain/kotlin/lupos/vk/ValueKeyStore.kt:396"/*SOURCE_FILE_END*/, pageid)
                    SanityCheck.check({ /*SOURCE_FILE_START*/"/src/luposdate3000/src/luposdate3000_vk/src/commonMain/kotlin/lupos/vk/ValueKeyStore.kt:397"/*SOURCE_FILE_END*/ }, { BufferManagerPage.readInt4(page, 0) == ValueKeyStore.PAGE_TYPE_LEAF })
                    nextPageID = BufferManagerPage.readInt4(page, 4)
                    offset = 12
                }
            }
        }
        return id
    }

    public fun close() {
        if (pageid != ValueKeyStore.PAGEID_NULL_PTR) {
            SanityCheck.check({ /*SOURCE_FILE_START*/"/src/luposdate3000/src/luposdate3000_vk/src/commonMain/kotlin/lupos/vk/ValueKeyStore.kt:408"/*SOURCE_FILE_END*/ }, { BufferManagerPage.readInt4(page, 0) == ValueKeyStore.PAGE_TYPE_LEAF })
            bufferManager.releasePage(/*SOURCE_FILE_START*/"/src/luposdate3000/src/luposdate3000_vk/src/commonMain/kotlin/lupos/vk/ValueKeyStore.kt:409"/*SOURCE_FILE_END*/, pageid)
        }
    }
}

internal class ValueKeyStoreIteratorSearch {

    @Suppress("NOTHING_TO_INLINE")
    internal inline fun search(target: ByteArrayWrapper, startpageid: Int, bufferManager: IBufferManager, buffer: ByteArrayWrapper): Int {
        var pageid = startpageid
        while (true) {
            var page = bufferManager.getPage(/*SOURCE_FILE_START*/"/src/luposdate3000/src/luposdate3000_vk/src/commonMain/kotlin/lupos/vk/ValueKeyStore.kt:420"/*SOURCE_FILE_END*/, pageid)
            var nextPageID = BufferManagerPage.readInt4(page, 4)
            var offset = BufferManagerPage.readInt4(page, 8)
            var pageType = BufferManagerPage.readInt4(page, 0)
            var childPageID = BufferManagerPage.readInt4(page, 12) // only valid if "pageType == ValueKeyStore.PAGE_TYPE_INNER"
            var lastID = ValueKeyStore.ID_NULL
            localloop@ while (pageid != ValueKeyStore.PAGEID_NULL_PTR && BufferManagerPage.readInt4(page, offset) != ValueKeyStore.ID_NULL) {
                var localChildPageID = childPageID
                lastID = BufferManagerPage.readInt4(page, offset)
                val len = BufferManagerPage.readInt4(page, offset + 4)
                var bufferOffset = BufferManagerPage.readInt4(page, offset + 8)
                offset += 12
                if (pageType == ValueKeyStore.PAGE_TYPE_INNER) {
                    localChildPageID = BufferManagerPage.readInt4(page, offset)
                    offset += 4
                }
                ByteArrayWrapperExt.setSize(buffer, len, true)
                while (bufferOffset < len) {
                    val l1 = min(BufferManagerPage.BUFFER_MANAGER_PAGE_SIZE_IN_BYTES - offset, len - bufferOffset)
                    if (l1 > 0) {
                        page.copyInto(ByteArrayWrapperExt.getBuf(buffer), bufferOffset, offset, offset + l1)
                        bufferOffset += l1
                        offset += l1
                    }
                    if (offset > BufferManagerPage.BUFFER_MANAGER_PAGE_SIZE_IN_BYTES - ValueKeyStore.RESERVED_SPACE) {
                        bufferManager.releasePage(/*SOURCE_FILE_START*/"/src/luposdate3000/src/luposdate3000_vk/src/commonMain/kotlin/lupos/vk/ValueKeyStore.kt:445"/*SOURCE_FILE_END*/, pageid)
                        pageid = nextPageID
                        if (pageid == ValueKeyStore.PAGEID_NULL_PTR) {
                            SanityCheck.check({ /*SOURCE_FILE_START*/"/src/luposdate3000/src/luposdate3000_vk/src/commonMain/kotlin/lupos/vk/ValueKeyStore.kt:448"/*SOURCE_FILE_END*/ }, { bufferOffset == len })
                        } else {
                            page = bufferManager.getPage(/*SOURCE_FILE_START*/"/src/luposdate3000/src/luposdate3000_vk/src/commonMain/kotlin/lupos/vk/ValueKeyStore.kt:450"/*SOURCE_FILE_END*/, pageid)
                            SanityCheck.check({ /*SOURCE_FILE_START*/"/src/luposdate3000/src/luposdate3000_vk/src/commonMain/kotlin/lupos/vk/ValueKeyStore.kt:451"/*SOURCE_FILE_END*/ }, { BufferManagerPage.readInt4(page, 0) == ValueKeyStore.PAGE_TYPE_LEAF })
                            nextPageID = BufferManagerPage.readInt4(page, 4)
                            offset = 12
                        }
                    }
                }
                if (target <= buffer) {
                    break@localloop
                } else {
                    childPageID = localChildPageID
                }
            }
            if (pageid != ValueKeyStore.PAGEID_NULL_PTR) {
                bufferManager.releasePage(/*SOURCE_FILE_START*/"/src/luposdate3000/src/luposdate3000_vk/src/commonMain/kotlin/lupos/vk/ValueKeyStore.kt:464"/*SOURCE_FILE_END*/, pageid)
            }
            if (pageType == ValueKeyStore.PAGE_TYPE_INNER) {
                pageid = childPageID
            } else if (target == buffer) {
                return lastID
            } else {
                return ValueKeyStore.ID_NULL
            }
        }
    }
}