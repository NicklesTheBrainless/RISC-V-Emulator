public interface Memory {

    byte readByte(int addr);
    int read4Bytes(long addr);
    long read8Bytes(int addr);

    void writeByte(int addr, byte data);
    void writeMemory(int addr, byte[] data);

    void setZeroMemory(int addr, long size);
}