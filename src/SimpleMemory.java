public class SimpleMemory implements Memory {

    private final byte[] bytes;

    public SimpleMemory(int size) {
        bytes = new byte[size];
    }

    @Override
    public byte readByte(int addr) {
        if (addr < 0 || addr >= bytes.length)
            throw new IllegalArgumentException("Address out of range: " + addr);

        return bytes[addr];
    }

    /* Read 32 bit little-endian, bytes are treated as unsigned */
    public int read4Bytes(long addr) {
        if (addr < 0 || addr >= bytes.length)
            throw new IllegalArgumentException("Address out of range: " + addr);

        int a0 = Byte.toUnsignedInt(bytes[(int) addr]);
        int a1 = Byte.toUnsignedInt(bytes[(int) addr + 1]);
        int a2 = Byte.toUnsignedInt(bytes[(int) addr + 2]);
        int a3 = Byte.toUnsignedInt(bytes[(int) addr + 3]);
        return a0 | (a1 << 8) | (a2 << 16) | (a3 << 24);
    }

    /* Read 64 bit little-endian, bytes are treated as unsigned */
    @Override
    public long read8Bytes(int addr) {
        if (addr < 0 || addr >= bytes.length)
            throw new IllegalArgumentException("Address out of range: " + addr);

        long b0 = Byte.toUnsignedLong(bytes[addr]);
        long b1 = Byte.toUnsignedLong(bytes[addr + 1]);
        long b2 = Byte.toUnsignedLong(bytes[addr + 2]);
        long b3 = Byte.toUnsignedLong(bytes[addr + 3]);
        long b4 = Byte.toUnsignedLong(bytes[addr + 4]);
        long b5 = Byte.toUnsignedLong(bytes[addr + 5]);
        long b6 = Byte.toUnsignedLong(bytes[addr + 6]);
        long b7 = Byte.toUnsignedLong(bytes[addr + 7]);
        return b0 | (b1 << 8) | (b2 << 16) | (b3 << 24) | (b4 << 32) | (b5 << 40) | (b6 << 48) | (b7 << 56);
    }

    @Override
    public void writeByte(int addr, byte data) {
        if (addr < 0 || addr >= bytes.length)
            throw new IllegalArgumentException("Address out of range: " + addr);

        bytes[addr] = data;
    }

    @Override
    public void writeMemory(int addr, byte[] data) {
        if (addr < 0 || addr >= bytes.length)
            throw new IllegalArgumentException("Address out of range: " + addr);

        System.arraycopy(data, 0, bytes, addr, data.length);
    }

    @Override
    public void setZeroMemory(int addr, long size) {
        for (int i = 0; i < size; i++)
            bytes[addr + i] = 0;
    }

    @Override
    public int getSize() {
        return bytes.length;
    }
}