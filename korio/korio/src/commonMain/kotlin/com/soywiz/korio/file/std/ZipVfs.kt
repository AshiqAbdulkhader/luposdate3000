package com.soywiz.korio.file.std

import com.soywiz.klock.*
import com.soywiz.kmem.*
import com.soywiz.korio.async.*
import com.soywiz.korio.compression.*
import com.soywiz.korio.compression.deflate.*
import com.soywiz.korio.file.*
import com.soywiz.korio.lang.*
import com.soywiz.korio.stream.*
import com.soywiz.korio.util.checksum.*
import com.soywiz.korio.util.encoding.*
import kotlinx.coroutines.channels.*
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set
import kotlin.math.*

suspend fun ZipVfs(s: AsyncStream, zipFile: VfsFile? = null, caseSensitive: Boolean = true, closeStream: Boolean = false): VfsFile {
	//val s = zipFile.open(VfsOpenMode.READ)
	var endBytes = EMPTY_BYTE_ARRAY

	if (s.getLength() <= 8L) throw IllegalArgumentException("Zip file is too small length=${s.getLength()}")

	val PK_END = byteArrayOf(0x50, 0x4B, 0x05, 0x06)
	var pk_endIndex = -1
	val fileLength = s.getLength()

	for (chunkSize in listOf(0x16, 0x100, 0x1000, 0x10000)) {
		val pos = max(0L, fileLength - chunkSize)
		s.setPosition(pos)
		val bytesLen = max(chunkSize, s.getAvailable().toIntClamp())
		val ebytes = s.readBytesExact(bytesLen)
		endBytes = ebytes
		pk_endIndex = endBytes.indexOf(PK_END)
		if (pk_endIndex >= 0) break
	}

	class ZipEntry2(
		val path: String,
		val compressionMethod: Int,
		val isDirectory: Boolean,
		val time: DosFileDateTime,
		val offset: Int,
		val inode: Long,
		val headerEntry: AsyncStream,
		val compressedSize: Long,
		val uncompressedSize: Long
	)

	fun ZipEntry2?.toStat(file: VfsFile): VfsStat {
		val vfs = file.vfs
		return if (this != null) {
			vfs.createExistsStat(
				file.path,
				isDirectory = isDirectory,
				size = uncompressedSize,
				inode = inode,
				createTime = this.time.utc
			)
		} else {
			vfs.createNonExistsStat(file.path)
		}
	}

	fun String.normalizeName() = if (caseSensitive) this.trim('/') else this.trim('/').toLowerCase()

	if (pk_endIndex < 0) throw IllegalArgumentException("Not a zip file (pk_endIndex < 0) : pk_endIndex=$pk_endIndex : ${endBytes.sliceArray(endBytes.size - 32 until endBytes.size).hex} : ${s.getLength()}")

	val data = endBytes.copyOfRange(pk_endIndex, endBytes.size).openSync()

	val files = LinkedHashMap<String, ZipEntry2>()
	val filesPerFolder = LinkedHashMap<String, MutableMap<String, ZipEntry2>>()

	@Suppress("UNUSED_VARIABLE")
	data.apply {
		//println(s)
		val magic = readS32BE()
		if (magic != 0x504B_0506) throw IllegalStateException("Not a zip file ${magic.hex} instead of ${0x504B_0102.hex}")
		val diskNumber = readU16LE()
		val startDiskNumber = readU16LE()
		val entriesOnDisk = readU16LE()
		val entriesInDirectory = readU16LE()
		val directorySize = readS32LE()
		val directoryOffset = readS32LE()
		val commentLength = readU16LE()

		//println("Zip: $entriesInDirectory")

		val ds = s.sliceWithSize(directoryOffset.toLong(), directorySize.toLong()).readAvailable().openSync()

		for (n in 0 until entriesInDirectory) {
			ds.apply {
				val magic = readS32BE()
				if (magic != 0x504B_0102) throw IllegalStateException("Not a zip file record ${magic.hex} instead of ${0x504B_0102.hex}")
				val versionMade = readU16LE()
				val versionExtract = readU16LE()
				val flags = readU16LE()
				val compressionMethod = readU16LE()
				val fileTime = readU16LE()
				val fileDate = readU16LE()
				val crc = readS32LE()
				val compressedSize = readS32LE()
				val uncompressedSize = readS32LE()
				val fileNameLength = readU16LE()
				val extraLength = readU16LE()
				val fileCommentLength = readU16LE()
				val diskNumberStart = readU16LE()
				val internalAttributes = readU16LE()
				val externalAttributes = readS32LE()
				val headerOffset = readU32LE()
				val name = readString(fileNameLength)
				val extra = readBytes(extraLength)

				val isDirectory = name.endsWith("/")
				val normalizedName = name.normalizeName()

				val baseFolder = normalizedName.substringBeforeLast('/', "")
				val baseName = normalizedName.substringAfterLast('/')

				val folder = filesPerFolder.getOrPut(baseFolder) { LinkedHashMap() }

				val headerEntry: AsyncStream = s.sliceStart(headerOffset)

				val entry = ZipEntry2(
					path = name,
					compressionMethod = compressionMethod,
					isDirectory = isDirectory,
					time = DosFileDateTime(fileTime, fileDate),
					inode = n.toLong(),
					offset = headerOffset.toInt(),
					headerEntry = headerEntry,
					compressedSize = compressedSize.unsigned,
					uncompressedSize = uncompressedSize.unsigned
				)
				val components = listOf("") + PathInfo(normalizedName).getPathFullComponents()
				for (m in 1 until components.size) {
					val f = components[m - 1]
					val c = components[m]
					if (c !in files) {
						val folder2 = filesPerFolder.getOrPut(f) { LinkedHashMap() }
						val entry2 = ZipEntry2(
							path = c,
							compressionMethod = 0,
							isDirectory = true,
							time = DosFileDateTime(0, 0),
							inode = 0L,
							offset = 0,
							headerEntry = byteArrayOf().openAsync(),
							compressedSize = 0L,
							uncompressedSize = 0L
						)
						folder2[PathInfo(c).baseName] = entry2
						files[c] = entry2
					}
				}
				//println(components)
				folder[baseName] = entry
				files[normalizedName] = entry
			}
		}

		files[""] = ZipEntry2(
			path = "",
			compressionMethod = 0,
			isDirectory = true,
			time = DosFileDateTime(0, 0),
			inode = 0L,
			offset = 0,
			headerEntry = byteArrayOf().openAsync(),
			compressedSize = 0L,
			uncompressedSize = 0L
		)
		Unit
	}

	class Impl : Vfs() {
		val vfs = this

		override suspend fun close() {
			if (closeStream) {
				s.close()
			}
		}

		override suspend fun open(path: String, mode: VfsOpenMode): AsyncStream {
			val entry = files[path.normalizeName()] ?: throw FileNotFoundException("Path: '$path'")
			if (entry.isDirectory) throw IOException("Can't open a zip directory for $mode")
			val base = entry.headerEntry.sliceStart()
			@Suppress("UNUSED_VARIABLE")
			return base.run {
				if (this.getAvailable() < 16) throw IllegalStateException("Chunk to small to be a ZIP chunk")
				if (readS32BE() != 0x504B_0304) throw IllegalStateException("Not a zip file (readS32BE() != 0x504B_0304)")
				val version = readU16LE()
				val flags = readU16LE()
				val compressionType = readU16LE()
				val fileTime = readU16LE()
				val fileDate = readU16LE()
				val crc = readS32LE()
				val compressedSize = readS32LE()
				val uncompressedSize = readS32LE()
				val fileNameLength = readU16LE()
				val extraLength = readU16LE()
				val name = readString(fileNameLength)
				val extra = readBytesExact(extraLength)
				val compressedData = readSlice(entry.compressedSize)

				when (entry.compressionMethod) {
					0 -> compressedData
					else -> {
						val method = when (entry.compressionMethod) {
							8 -> Deflate
							else -> TODO("Not implemented zip method ${entry.compressionMethod}")
						}
						//val compressed = compressedData.uncompressed(method).readAll()
						val compressed = compressedData.readAll().uncompress(method)

						if (crc != 0) {
							val computedCrc = CRC32.compute(compressed)
							if (computedCrc != crc) error("Uncompressed file crc doesn't match: expected=${crc.hex}, actual=${computedCrc.hex}")
						}

						compressed.openAsync()
					}
				}
			}
		}

		override suspend fun stat(path: String): VfsStat {
			return files[path.normalizeName()].toStat(this@Impl[path])
		}

		override suspend fun list(path: String): ReceiveChannel<VfsFile> = produce {
			for ((_, entry) in filesPerFolder[path.normalizeName()] ?: LinkedHashMap()) {
				//yield(entry.toStat(this@Impl[entry.path]))
				send(vfs[entry.path])
			}
		}

		override fun toString(): String = "ZipVfs($zipFile)"
	}

	return Impl().root
}

internal class ZipVfsData {

}

private class DosFileDateTime(var dosTime: Int, var dosDate: Int) {
	val seconds: Int get() = 2 * dosTime.extract(0, 5)
	val minutes: Int get() = dosTime.extract(5, 6)
	val hours: Int get() = dosTime.extract(11, 5)
	val day: Int get() = dosDate.extract(0, 5)
	val month1: Int get() = dosDate.extract(5, 4)
	val fullYear: Int get() = 1980 + dosDate.extract(9, 7)
	val utc: DateTime = DateTime.createAdjusted(fullYear, month1, day, hours, minutes, seconds)
}

suspend fun VfsFile.openAsZip(caseSensitive: Boolean = true): VfsFile = ZipVfs(this.open(VfsOpenMode.READ), this, caseSensitive = caseSensitive, closeStream = true)
suspend fun AsyncStream.openAsZip(caseSensitive: Boolean = true) = ZipVfs(this, caseSensitive = caseSensitive, closeStream = false)

suspend fun <R> VfsFile.openAsZip(caseSensitive: Boolean = true, callback: suspend (VfsFile) -> R): R {
	val file = openAsZip(caseSensitive)
	try {
		return callback(file)
	} finally {
		file.vfs.close()
	}
}

suspend fun <R> AsyncStream.openAsZip(caseSensitive: Boolean = true, callback: suspend (VfsFile) -> R): R {
	val file = openAsZip(caseSensitive)
	try {
		return callback(file)
	} finally {
		//file.vfs.close()
	}
}

/**
 * @TODO: Kotlin.JS BUG:
 * ReferenceError: $this$ is not defined
 * at Coroutine$await$lambda.local$this$await (korio.js:38946:37)
 * at Coroutine$await$lambda.doResume (korio.js:626:34)
 * at file:///Users/soywiz/projects/korlibs/korio/build/node_modules/korio.js:603:25
 */
suspend fun VfsFile.createZipFromTree(): ByteArray {
	val buf = ByteArrayBuilder()
	val mem = MemorySyncStream(buf)
	this.createZipFromTreeTo(mem.toAsync())
	return buf.toByteArray()
}

suspend fun VfsFile.createZipFromTreeTo(s: AsyncStream) {
	val entries = arrayListOf<ZipEntry>()
	addZipFileEntryTree(s, this, entries)
	val directoryStart = s.position

	for (entry in entries) {
		addDirEntry(s, entry)
	}
	val directoryEnd = s.position
	val comment = byteArrayOf()

	s.writeSync {
		writeString("PK\u0005\u0006")
		write16LE(0)
		write16LE(0)
		write16LE(entries.size)
		write16LE(entries.size)
		write32LE((directoryEnd - directoryStart).toInt())
		write32LE(directoryStart.toInt())
		write16LE(comment.size)
		writeBytes(comment)
	}
}

private suspend fun addZipFileEntry(s: AsyncStream, entry: VfsFile): ZipEntry {
	val size = entry.size().toInt()
	val versionMadeBy = 0x314
	val extractVersion = 10
	val flags = 2048
	//val compressionMethod = 8 // Deflate
	val compressionMethod = 0 // Store
	val date = 0
	val time = 0
	val crc32 = entry.checksum(CRC32)
	val name = entry.fullName.trim('/')
	val nameBytes = name.toByteArray(UTF8)
	val extraBytes = byteArrayOf()
	val compressedSize = size
	val uncompressedSize = size

	val headerOffset = s.position
	s.writeSync {
		writeString("PK\u0003\u0004")
		write16LE(extractVersion)
		write16LE(flags)
		write16LE(compressionMethod)
		write16LE(date)
		write16LE(time)
		write32LE(crc32)
		write32LE(compressedSize)
		write32LE(uncompressedSize)
		write16LE(nameBytes.size)
		write16LE(extraBytes.size)
		writeBytes(nameBytes)
		writeBytes(extraBytes)
	}
	s.writeFile(entry)

	return ZipEntry(
		versionMadeBy = versionMadeBy,
		extractVersion = extractVersion,
		headerOffset = headerOffset,
		compressionMethod = compressionMethod,
		flags = flags,
		date = date,
		time = time,
		crc32 = crc32,
		compressedSize = compressedSize,
		uncompressedSize = uncompressedSize,
		nameBytes = nameBytes,
		extraBytes = extraBytes,
		commentBytes = byteArrayOf(),
		diskNumberStart = 0,
		internalAttributes = 0,
		externalAttributes = 0
	)
}

private suspend fun addZipFileEntryTree(s: AsyncStream, entry: VfsFile, entries: MutableList<ZipEntry>) {
	if (entry.isDirectory()) {
		for (it in entry.list()) addZipFileEntryTree(s, it, entries)
	} else {
		entries += addZipFileEntry(s, entry)
	}
}

private class ZipEntry(
	val versionMadeBy: Int,
	val extractVersion: Int,
	val headerOffset: Long,
	val compressionMethod: Int,
	val flags: Int,
	val date: Int,
	val time: Int,
	val crc32: Int,
	val compressedSize: Int,
	val uncompressedSize: Int,
	val nameBytes: ByteArray,
	val extraBytes: ByteArray,
	val diskNumberStart: Int,
	val internalAttributes: Int,
	val externalAttributes: Int,
	val commentBytes: ByteArray
)

private suspend fun addDirEntry(s: AsyncStream, e: ZipEntry) {
	s.writeSync {
		writeString("PK\u0001\u0002")
		write16LE(e.versionMadeBy)
		write16LE(e.extractVersion)
		write16LE(e.flags)
		write16LE(e.compressionMethod)
		write16LE(e.date)
		write16LE(e.time)
		write32LE(e.crc32)
		write32LE(e.compressedSize)
		write32LE(e.uncompressedSize)
		write16LE(e.nameBytes.size)
		write16LE(e.extraBytes.size)
		write16LE(e.commentBytes.size)
		write16LE(e.diskNumberStart)
		write16LE(e.internalAttributes)
		write32LE(e.externalAttributes)
		write32LE(e.headerOffset.toInt())
		writeBytes(e.nameBytes)
		writeBytes(e.extraBytes)
		writeBytes(e.commentBytes)
	}
}

private fun ByteArray.indexOf(other: ByteArray): Int {
	val full = this
	for (n in 0 until full.size - other.size) if (other.indices.all { full[n + it] == other[it] }) return n
	return -1
}
