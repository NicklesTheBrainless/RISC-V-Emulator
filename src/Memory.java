public interface Memory {

    byte readByte(int addr);
    short read2Bytes(int addr);
    int read4Bytes(int addr);
    long read8Bytes(int addr);

    void writeByte(int addr, byte data);
    void writeMemory(int addr, byte[] data);

    void setZeroMemory(int addr, long size);

    int getSize();
}