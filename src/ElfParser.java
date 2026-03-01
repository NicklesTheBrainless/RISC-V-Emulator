import java.io.*;
import java.nio.*;
import java.nio.channels.FileChannel;
import java.util.Arrays;

public class ElfParser {

    private static final int EI_CLASS = 4;
    private static final int EI_DATA  = 5;

    private static final int ELFCLASS64 = 2;
    private static final int ELFDATA_LITTLE_ENDIAN = 1;
    private static final int ELFMACHINE_RISCV = 0xF3;

    private static final int PTYPE_LOAD = 1;

    public static int loadAndReturnEntry(String filename, Memory memory, long memOffset) throws IOException {

        try (RandomAccessFile file = new RandomAccessFile(filename, "r");
             FileChannel channel = file.getChannel()) {

            ByteBuffer elfHeader = ByteBuffer.allocate(32);
            channel.read(elfHeader);
            elfHeader.flip();

            if (elfHeader.get() != 0x7F || elfHeader.get() != 'E' || elfHeader.get() != 'L' || elfHeader.get() != 'F')
                throw new RuntimeException("Not an ELF file!");

            int elfClass = elfHeader.get(EI_CLASS);
            int elfData  = elfHeader.get(EI_DATA);
            elfHeader.position(0x12);
            int machine = elfHeader.getShort() & 0xFFFF;

            if (elfData != ELFDATA_LITTLE_ENDIAN)
                throw new RuntimeException("Only little-endian supported!");

            if (elfClass != ELFCLASS64)
                throw new RuntimeException("Only RV64I supported!");

            if (machine != ELFMACHINE_RISCV)
                throw new RuntimeException("Not RISC-V ELF!");

            // reset file channel position
            channel.position(0);

            return loadRV64I(channel, memory, memOffset);
        }
    }

    private static int loadRV64I(FileChannel channel, Memory memory, long memOffset) throws IOException {

        ByteBuffer elfHeader = ByteBuffer.allocate(64);
        elfHeader.order(ByteOrder.LITTLE_ENDIAN);
        channel.read(elfHeader);
        elfHeader.flip();

        elfHeader.position(0x18);
        long entry = elfHeader.getLong() + memOffset;
        long phFilePos = elfHeader.getLong();   // die position zu wo die ganzen program header hinter einander sind

        elfHeader.position(0x36);
        int phSize   = elfHeader.getShort() & 0xFFFF;
        int phAmount = elfHeader.getShort() & 0xFFFF;
        System.out.println("Amount of Program Headers: " + phAmount);

        // read all program headers and segments
        for (int i = 0; i < phAmount; i++) {
            System.out.println();

            ByteBuffer ph = ByteBuffer.allocate(phSize);
            ph.order(ByteOrder.LITTLE_ENDIAN);

            channel.position(phFilePos + (long) i * phSize);
            channel.read(ph);
            ph.flip();

            int pType = ph.getInt();
            if (pType != PTYPE_LOAD)
                continue;

            ph.getInt();     // p_flags (not used)
            long segFilePos = ph.getLong();    // p_offset (address of the segment in the elf file)
            System.out.println("Program Header Address: 0x" + Long.toHexString(segFilePos));
            long segMemAddr = ph.getLong();    // p_vaddr (start address in our virtual memory for the program header)
            System.out.println("Program Header VMemory Address: 0x" + Long.toHexString(segMemAddr));
            ph.getLong();    // p_paddr (not used)
            long segFileSize = ph.getLong();
            System.out.println("Program Header Size: 0x" + Long.toHexString(segFileSize));
            long segMemSize  = ph.getLong();
            ph.getLong();    // p_align (not used)

            // read segment
            ByteBuffer segment = ByteBuffer.allocate((int) segFileSize);
            channel.position(segFilePos);
            channel.read(segment);
            memory.writeMemory((int) (segMemAddr + memOffset), segment.array());

            // set zero memory, if it exists
            if (segMemSize > segFileSize)
                memory.setZeroMemory((int) (segMemAddr + memOffset + segFileSize), segMemSize - segFileSize);
        }
        System.out.println("Program Entry: " + entry);
        return Math.toIntExact(entry);
    }

}