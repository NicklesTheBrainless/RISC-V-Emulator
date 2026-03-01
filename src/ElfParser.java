import java.io.*;
import java.nio.*;
import java.nio.channels.FileChannel;
import java.util.Arrays;

public class ElfParser {

    public static void main(String[] args) throws IOException {
        ElfParser.loadAndReturnEntry("/home/nic/Programming/Java/RISC-V/src/hello_world", new SimpleMemory(345678));
    }

    private static final int EI_CLASS = 4;
    private static final int EI_DATA  = 5;

    private static final int ELFCLASS64 = 2;

    private static final int ELFDATA2LSB = 1;

    private static final int EM_RISCV = 0xF3;   // code um sicher zu stellen das es RISC-V ist

    private static final int PT_LOAD = 1;

    public static long loadAndReturnEntry(String filename, Memory memory) throws IOException {

        try (RandomAccessFile file = new RandomAccessFile(filename, "r");
             FileChannel channel = file.getChannel()) {

            ByteBuffer elfHeaderIdentifier = ByteBuffer.allocate(16);
            channel.read(elfHeaderIdentifier);
            elfHeaderIdentifier.flip();

            if (elfHeaderIdentifier.get() != 0x7F || elfHeaderIdentifier.get() != 'E' || elfHeaderIdentifier.get() != 'L' || elfHeaderIdentifier.get() != 'F')
                throw new RuntimeException("Not an ELF file");

            int elfClass = elfHeaderIdentifier.get(EI_CLASS);
            int elfData  = elfHeaderIdentifier.get(EI_DATA);

            if (elfData != ELFDATA2LSB) {
                throw new RuntimeException("Only little-endian supported");
            }

            channel.position(0);

            if (elfClass == ELFCLASS64) {
                return load64(channel, memory);
            } else {
                throw new RuntimeException("Unsupported ELF class");
            }
        }
    }

    private static long load64(FileChannel channel, Memory memory) throws IOException {

        ByteBuffer elfHeader = ByteBuffer.allocate(64);
        elfHeader.order(ByteOrder.LITTLE_ENDIAN);
        channel.read(elfHeader);
        elfHeader.flip();

        elfHeader.position(18);
        int machine = elfHeader.getShort() & 0xFFFF;
        if (machine != EM_RISCV) {
            throw new RuntimeException("Not RISC-V ELF");
        }

        elfHeader.position(0x18);
        long entry = elfHeader.getLong() - 2147483648L;
        long phOff = elfHeader.getLong();   // die position zu wo die ganzen program header hinter einander sind

        elfHeader.position(0x36);
        int phSize   = elfHeader.getShort() & 0xFFFF;
        int phAmount = elfHeader.getShort() & 0xFFFF;
        System.out.println(phAmount);

        for (int i = 0; i < phAmount; i++) {
            System.out.println();

            ByteBuffer ph = ByteBuffer.allocate(phSize);
            ph.order(ByteOrder.LITTLE_ENDIAN);

            channel.position(phOff + (long)i * phSize);
            channel.read(ph);
            ph.flip();

            int pType = ph.getInt();
            if (pType != PT_LOAD)
                continue;

            int flags = ph.getInt();    // unnötig
            long offset = ph.getLong();     // start address of the program header in the elf file
            System.out.println("Program Header Offset: 0x" + Long.toHexString(offset));
            long phVAddr = ph.getLong();    // start address in our virtual memory for the program header
            System.out.println("Program Header VMemory Address: 0x" + Long.toHexString(phVAddr));
            ph.getLong();   // p_paddr wird nicht gebraucht
            long filesz = ph.getLong();
            System.out.println("Program Header Size: 0x" + Long.toHexString(filesz));
            long memsz  = ph.getLong();
            ph.getLong();   // align wird auch nicht gebraucht

            // aus irgendein grund muss es die file size als memory size haben
            ByteBuffer segment = ByteBuffer.allocate((int)filesz);
            channel.position(offset);
            channel.read(segment);

            System.out.println(phVAddr);
            System.out.println(Arrays.toString(segment.array()));
            memory.writeMemory((int) (phVAddr - 2147483648L), segment.array());

            // manchmal gibt es so zero memory oder so
            if (memsz > filesz) {
                memory.setZeroMemory((int) (phVAddr + filesz), memsz - filesz);
            }
        }
        System.out.println("Entry: " + entry);
        return entry;
    }

}