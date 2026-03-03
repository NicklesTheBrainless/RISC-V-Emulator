public class BitUtils {
    public static long signExtend(long value, int bitLength) {
        if (bitLength <= 0 || bitLength > 64)
            throw new IllegalArgumentException("bitLength must be 1 to 64");

        long shift = 64 - bitLength;
        return (value << shift) >> shift;
    }

    public static long signExtend8(int v) {
        return signExtend(v & 0xFF, 8);
    }

    public static long signExtend16(int v) {
        return signExtend(v & 0xFFFF, 16);
    }

    public static long signExtend32(int v) {
        return signExtend(v & 0xFFFF_FFFFL, 32);
    }
}
