package com.ysm.parser

/**
 * JNI wrapper for YSMParser native library.
 *
 * The native library is loaded by [YSMParserNativeLoader] before any
 * method on this class is called. Do NOT call methods on this class without
 * first calling `YSMParserNativeLoader.load()`.
 */
object YSMParser {
    /**
     * Parse a .ysm file and extract all resources to the output directory.
     *
     * @param ysmFilePath absolute or relative path to the .ysm file
     * @param outputDir   directory where the parsed project will be written
     * @return true on success
     * @throws IllegalArgumentException if either argument is null
     * @throws RuntimeException         if native parsing fails
     */
    @JvmStatic
    external fun parse(ysmFilePath: String, outputDir: String): Boolean

    /**
     * Parse .ysm data from a byte array.
     *
     * @param ysmData  raw bytes of the .ysm file
     * @param outputDir directory where the parsed project will be written
     * @return true on success
     * @throws IllegalArgumentException if data is null/empty or outputDir is null
     * @throws RuntimeException         if native parsing fails
     */
    @JvmStatic
    external fun parseBytes(ysmData: ByteArray, outputDir: String): Boolean

    /**
     * Read the YSGP version of a .ysm file without full parsing.
     *
     * @param ysmFilePath path to the .ysm file
     * @return the YSGP version number, or -1 on error
     * @throws IllegalArgumentException if the path is null
     * @throws RuntimeException         if reading fails
     */
    @JvmStatic
    external fun getVersion(ysmFilePath: String): Int
}