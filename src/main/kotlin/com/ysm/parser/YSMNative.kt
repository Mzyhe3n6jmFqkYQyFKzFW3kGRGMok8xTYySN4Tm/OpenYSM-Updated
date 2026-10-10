@file:Suppress("unused")

package com.ysm.parser

/**
 * JNI wrapper for low-level native algorithms used by YSM.
 *
 * Exposes CityHash, Zstd, XChaCha20, ModifiedChaCha, and MT19937 primitives
 * directly to Java/Kotlin. All methods are static and thread-safe.
 */
object YSMNative {
    // ── CityHash ──────────────────────────────────────────────────────────

    external fun cityHash64(data: ByteArray): Long

    external fun cityHash64WithSeed(data: ByteArray, seed: Long): Long

    external fun cityHash128(data: ByteArray): LongArray

    external fun cityHash128WithSeed(data: ByteArray, seedLow: Long, seedHigh: Long): LongArray

    // ── Zstd ──────────────────────────────────────────────────────────────

    external fun zstdDecompress(data: ByteArray): ByteArray

    external fun zstdCompress(data: ByteArray, level: Int): ByteArray

    // ── XChaCha20 ─────────────────────────────────────────────────────────

    /**
     * @param key   32-byte key
     * @param iv    24-byte nonce
     * @param rounds number of rounds (10, 20, or 30)
     */
    external fun xchacha20Encrypt(data: ByteArray, key: ByteArray, iv: ByteArray, rounds: Int): ByteArray

    /**
     * Decryption is the same operation as encryption for XChaCha20.
     */
    external fun xchacha20Decrypt(data: ByteArray, key: ByteArray, iv: ByteArray, rounds: Int): ByteArray

    /**
     * YSM-specific modified ChaCha decryptor used by V3 resources.
     *
     * @param key   32-byte key
     * @param iv    24-byte nonce
     * @param seed  CityHash seed controlling block updates
     */
    external fun modifiedChaChaDecrypt(data: ByteArray, key: ByteArray, iv: ByteArray, seed: Long): ByteArray

    // ── MT19937 (stateful) ────────────────────────────────────────────────

    /**
     * Create a new MT19937-64 RNG instance.
     * @return opaque handle for subsequent calls
     */
    external fun mt19937Create(seed: Long): Long

    /** Return the next 64-bit random value from the generator. */
    external fun mt19937Next(handle: Long): Long

    /** Fill and return [count] random bytes from the generator. */
    external fun mt19937GenerateBytes(handle: Long, count: Int): ByteArray

    /** Destroy the generator and release native resources. */
    external fun mt19937Destroy(handle: Long)

    /**
     * 解压 YSM 魔改的 ZSTD 数据。
     * 底层会自动执行 wash (洗白) 操作，然后进行标准 ZSTD 解压。
     *
     * @param data 压缩且被混淆过的 byte 数组
     * @return 解压后的原始 byte 数组
     */
    external fun ysmZstdDecompress(data: ByteArray): ByteArray

    /**
     * 将数据进行标准 ZSTD 压缩，并混淆为 YSM 魔改格式。
     * 底层会先进行标准 ZSTD 压缩，然后自动执行 obfuscate (弄脏) 操作。
     *
     * @param data 需要压缩的原始 byte 数组
     * @param level ZSTD 压缩等级 (通常推荐 3，最大通常支持到 22)
     * @return 压缩且混淆后的 byte 数组
     */
    external fun ysmZstdCompress(data: ByteArray, level: Int): ByteArray
}