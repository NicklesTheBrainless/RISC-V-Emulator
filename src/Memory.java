public interface Memory {

    byte readByte(int addr);
    short read2Bytes(int addr);
    int read4Bytes(int addr);
    long read8Bytes(int addr);

    void writeByte(int addr, byte data);
    void write2Bytes(int addr, short data);
    void write4Bytes(int addr, int data);
    void write8Bytes(int addr, long data);

    void writeMemory(int addr, byte[] data);

    void setZeroMemory(int addr, int size);

    int getSize();
}