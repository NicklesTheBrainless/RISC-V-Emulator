public class Switcherino {
    public static void enactInstruction(CPU cpu, int instCode, Instruction inst) {

        /* - Register-Felder und Immediate-Werte aus dem Code schneiden -- */
        int rd = (instCode >>> 7) & 0x1F;
        int rs1 = (instCode >>> 15) & 0x1F;
        int rs2 = (instCode >>> 20) & 0x1F;

        int shamt = (instCode >>> 20) & 0x3F;                 // 6-Bit Shift-Amount (RV64)

        long immI = signExtend(instCode >>> 20, 12);          // I-Type
        long immS = signExtend(((instCode >>> 7) & 0x1F) | ((instCode >>> 25) << 5), 12); // S-Type
        long immU = instCode & 0xFFFFF000;           // U-Type (kein sign-ext)
        long immB = signExtend(((instCode >>> 7) & 0x1) << 11 | ((instCode >>> 8) & 0xF) << 1 | ((instCode >>> 25) & 0x3F) << 5 | ((instCode >>> 31) & 0x1) << 12, 13);    // B-Type

        long immJ = signExtend(((instCode >>> 12) & 0xFF) << 12 | ((instCode >>> 20) & 0x1) << 11 | ((instCode >>> 21) & 0x3FF) << 1 | ((instCode >>> 31) & 0x1) << 20, 21);    // J-Type

        long x1 = cpu.regs[rs1];
        long x2 = cpu.regs[rs2];

        long nextPC = cpu.counter;  // counter verweist bereits auf die Folgeinstr.

        /*  Befehls-Ausführung  */
        switch (inst) {

            /*  U-Type  */
            case LUI -> cpu.regs[rd] = immU;
            case AUIPC -> cpu.regs[rd] = (cpu.counter - 4) + immU;

            /*  J-Type  */
            case JAL -> {
                cpu.regs[rd] = nextPC;
                cpu.counter = (int) ((cpu.counter - 4) + immJ);
            }
            case JALR -> {
                cpu.regs[rd] = nextPC;
                cpu.counter = (int) ((x1 + immI) & ~1L);
            }

            /*  B-Type  */
            case BEQ -> {
                if (x1 == x2) cpu.counter = (int) ((cpu.counter - 4) + immB);
            }
            case BNE -> {
                if (x1 != x2) cpu.counter = (int) ((cpu.counter - 4) + immB);
            }
            case BLT -> {
                if (x1 < x2) cpu.counter = (int) ((cpu.counter - 4) + immB);
            }
            case BGE -> {
                if (x1 >= x2) cpu.counter = (int) ((cpu.counter - 4) + immB);
            }
            case BLTU -> {
                if (Long.compareUnsigned(x1, x2) < 0) cpu.counter = (int) ((cpu.counter - 4) + immB);
            }
            case BGEU -> {
                if (Long.compareUnsigned(x1, x2) >= 0) cpu.counter = (int) ((cpu.counter - 4) + immB);
            }

            /*  Loads (I-Type)  */
            case LB -> cpu.regs[rd] = signExtend8(cpu.mem.readByte((int) (x1 + immI)));
            case LBU -> cpu.regs[rd] = Byte.toUnsignedInt(cpu.mem.readByte((int) (x1 + immI)));
            case LH -> cpu.regs[rd] = signExtend16(half(cpu, x1 + immI));
            case LHU -> cpu.regs[rd] = half(cpu, x1 + immI) & 0xFFFF;
            case LW -> cpu.regs[rd] = signExtend32(cpu.mem.read4Bytes((int) (x1 + immI)));

            /*  Stores (S-Type)  */
            case SB -> {
                long rawAddr = x1 + immS;
                System.out.println("x1 " + x1 + " ");
                int  addr = (int) (rawAddr & 0xFFFF_FFFFL);
                cpu.mem.writeByte(addr, (byte) x2);

            }
            case SH -> storeHalf(cpu, x1 + immS, x2);
            case SW -> storeWord(cpu, x1 + immS, x2);

            /*  ALU-I  */
            case ADDI -> cpu.regs[rd] = x1 + immI;
            case SLTI -> cpu.regs[rd] = (x1 < immI) ? 1 : 0;
            case SLTIU -> cpu.regs[rd] = (Long.compareUnsigned(x1, immI) < 0) ? 1 : 0;
            case XORI -> cpu.regs[rd] = x1 ^ immI;
            case ORI -> cpu.regs[rd] = x1 | immI;
            case ANDI -> cpu.regs[rd] = x1 & immI;

            /*  Shift-I  */
            case SLLI -> cpu.regs[rd] = x1 << shamt;
            case SRLI -> cpu.regs[rd] = x1 >>> shamt;
            case SRAI -> cpu.regs[rd] = x1 >> shamt;

            /*  ALU-R  */
            case ADD -> cpu.regs[rd] = x1 + x2;
            case SUB -> cpu.regs[rd] = x1 - x2;
            case SLL -> cpu.regs[rd] = x1 << (x2 & 0x3F);
            case SLT -> cpu.regs[rd] = (x1 < x2) ? 1 : 0;
            case SLTU -> cpu.regs[rd] = (Long.compareUnsigned(x1, x2) < 0) ? 1 : 0;
            case XOR -> cpu.regs[rd] = x1 ^ x2;
            case SRL -> cpu.regs[rd] = x1 >>> (x2 & 0x3F);
            case SRA -> cpu.regs[rd] = x1 >> (x2 & 0x3F);
            case OR -> cpu.regs[rd] = x1 | x2;
            case AND -> cpu.regs[rd] = x1 & x2;

            /*  System  */
            case ECALL, EBREAK -> {
                System.out.println("HALT HALT HALT");
                cpu.halt = true;
            }    // Minimal-Handling
        }

        /* Register x0 bleibt laut Spezifikation immer 0 */
        cpu.regs[0] = 0;
    }


    private static long signExtend(long value, int bits) {
        long mask = 1L << (bits - 1);
        return (value ^ mask) - mask;
    }

    private static long signExtend8(int v) {
        return signExtend(v & 0xFF, 8);
    }

    private static long signExtend16(int v) {
        return signExtend(v & 0xFFFF, 16);
    }

    private static long signExtend32(int v) {
        return signExtend(v & 0xFFFF_FFFFL, 32);
    }

    private static int half(CPU cpu, long addr) {
        // zwei Bytes einlesen (Little-Endian)
        int lo = Byte.toUnsignedInt(cpu.mem.readByte((int) addr));
        int hi = Byte.toUnsignedInt(cpu.mem.readByte((int) addr + 1));
        return lo | (hi << 8);
    }

    private static void storeHalf(CPU cpu, long addr, long data) {
        cpu.mem.writeByte((int) addr, (byte) (data & 0xFF));
        cpu.mem.writeByte((int) addr + 1, (byte) ((data >> 8) & 0xFF));
    }

    private static void storeWord(CPU cpu, long addr, long data) {
        byte[] w = {(byte) (data & 0xFF), (byte) ((data >> 8) & 0xFF), (byte) ((data >> 16) & 0xFF), (byte) ((data >> 24) & 0xFF)};
        cpu.mem.writeMemory((int) addr, w);
    }
}