public class InstructionExecutor {

    public static void executeInstruction(CPU cpu, int instCode, Instruction inst) {

        int rd = (instCode >>> 7) & 0x1F;
        int rs1 = (instCode >>> 15) & 0x1F;
        int rs2 = (instCode >>> 20) & 0x1F;

        int shiftAmount = (instCode >>> 20) & 0b11_1111;    // 6-bit shift amount (RV64)

        long immI = signExtend(instCode >>> 20, 12);
        long immS = signExtend(((instCode >>> 7) & 0b1_1111) | ((instCode >>> 25) << 5), 12);
        long immU = instCode & 0xFFFFF000;
        long immB = getImmB(instCode);
        long immJ = getImmJ(instCode);

        long x1 = cpu.regs[rs1];
        long x2 = cpu.regs[rs2];

        long nextPC = cpu.counter + 4;

        switch (inst) {

            // U-Type
            case LUI -> cpu.regs[rd] = immU;
            case AUIPC -> cpu.regs[rd] = cpu.counter + immU;

            // J-Type
            case JAL -> {
                cpu.regs[rd] = nextPC;
                cpu.counter = (int) (cpu.counter + immJ);
            }
            case JALR -> {
                cpu.regs[rd] = nextPC;
                cpu.counter = (int) ((x1 + immI) & ~1L);
            }

            //  B-Type
            case BEQ -> {
                if (x1 == x2) cpu.counter = (int) (cpu.counter + immB);
            }
            case BNE -> {
                if (x1 != x2) cpu.counter = (int) (cpu.counter + immB);
            }
            case BLT -> {
                if (x1 < x2) cpu.counter = (int) (cpu.counter + immB);
            }
            case BGE -> {
                if (x1 >= x2) cpu.counter = (int) (cpu.counter + immB);
            }
            case BLTU -> {
                if (Long.compareUnsigned(x1, x2) < 0) cpu.counter = (int) (cpu.counter + immB);
            }
            case BGEU -> {
                if (Long.compareUnsigned(x1, x2) >= 0) cpu.counter = (int) (cpu.counter + immB);
            }

            // Loads (I-Type)
            case LB  -> cpu.regs[rd] = signExtend8(cpu.mem.readByte((int) (x1 + immI)));    // load a signed byte (sign extend)
            case LD  -> cpu.regs[rd] = cpu.mem.read8Bytes((int) (x1 + immI));

            case LWU -> cpu.regs[rd] =
                    cpu.mem.read4Bytes((int) (x1 + immI)) & 0xFFFF_FFFFL;
            case LBU -> cpu.regs[rd] = Byte.toUnsignedInt(cpu.mem.readByte((int) (x1 + immI)));
            case LH  -> cpu.regs[rd] = signExtend16(cpu.mem.read2Bytes((int) (x1 + immI)));
            case LHU -> cpu.regs[rd] = cpu.mem.read2Bytes((int) (x1 + immI)) & 0xFFFF;
            case LW  -> cpu.regs[rd] = signExtend32(cpu.mem.read4Bytes((int) (x1 + immI)));

            // Stores (S-Type)
            case SB -> {
                long rawAddr = x1 + immS;
                System.out.println("x1 " + x1 + " ");
                int  addr = (int) (rawAddr & 0xFFFF_FFFFL);
                cpu.mem.writeByte(addr, (byte) x2);

            }
            case SH -> storeHalf(cpu, x1 + immS, x2);
            case SW -> storeWord(cpu, x1 + immS, x2);

            //  ALU-I
            case ADDI -> cpu.regs[rd] = x1 + immI;
            case SLTI -> cpu.regs[rd] = (x1 < immI) ? 1 : 0;
            case SLTIU -> cpu.regs[rd] = (Long.compareUnsigned(x1, immI) < 0) ? 1 : 0;
            case XORI -> cpu.regs[rd] = x1 ^ immI;
            case ORI -> cpu.regs[rd] = x1 | immI;
            case ANDI -> cpu.regs[rd] = x1 & immI;

            //  Shift-I
            case SLLI -> cpu.regs[rd] = x1 << shiftAmount;
            case SRLI -> cpu.regs[rd] = x1 >>> shiftAmount;
            case SRAI -> cpu.regs[rd] = x1 >> shiftAmount;

            //  ALU-R
            case ADD -> cpu.regs[rd] = x1 + x2;
            case SUB -> cpu.regs[rd] = x1 - x2;
            case SLL -> cpu.regs[rd] = x1 << (x2 & 0b11_1111);
            case SLT -> cpu.regs[rd] = (x1 < x2) ? 1 : 0;
            case SLTU -> cpu.regs[rd] = (Long.compareUnsigned(x1, x2) < 0) ? 1 : 0;
            case XOR -> cpu.regs[rd] = x1 ^ x2;
            case SRL -> cpu.regs[rd] = x1 >>> (x2 & 0b11_1111);
            case SRA -> cpu.regs[rd] = x1 >> (x2 &0b11_1111);
            case OR -> cpu.regs[rd] = x1 | x2;
            case AND -> cpu.regs[rd] = x1 & x2;

            // TODO: this shit
            /*  System  */
            case ECALL, EBREAK -> {
                System.out.println("HALT HALT HALT");
                cpu.halt = true;
            }    // Minimal-Handling

            default -> System.err.println("ehmmmmmmmmmmmmmmm whjat in the name of fish happenmenmed= ");
        }

        cpu.regs[0] = 0;
    }

    private static long getImmB(int instCode) {
        int bit12     = (instCode >>> 31) & 1;
        int bit11     = (instCode >>> 7)  & 1;
        int bits10_5  = (instCode >>> 25) & 0b11_1111;
        int bits4_1   = (instCode >>> 8)  & 0b1111;
        return signExtend((bit12 << 12) | (bit11 << 11) | (bits10_5 << 5) | (bits4_1  << 1) , 13);
    }

    private static long getImmJ(int instCode) {
        int bit20     = (instCode >>> 31) & 1;
        int bits19_12 = (instCode >>> 12) & 0b1111_1111;
        int bit11     = (instCode >>> 20) & 1;
        int bits10_1  = (instCode >>> 21) & 0b11_1111_1111;
        return signExtend((bit20 << 20) | (bits19_12 << 12) | (bit11 << 11) | (bits10_1 << 1), 21);
    }

    private static long signExtend(long value, int bitLength) {
        if (bitLength <= 0 || bitLength > 64)
            throw new IllegalArgumentException("bitLength must be 1 to 64");

        long shift = 64 - bitLength;
        return (value << shift) >> shift;
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

    private static void storeHalf(CPU cpu, long addr, long data) {
        cpu.mem.writeByte((int) addr, (byte) (data & 0xFF));
        cpu.mem.writeByte((int) addr + 1, (byte) ((data >> 8) & 0xFF));
    }

    private static void storeWord(CPU cpu, long addr, long data) {
        byte[] w = {(byte) (data & 0xFF), (byte) ((data >> 8) & 0xFF), (byte) ((data >> 16) & 0xFF), (byte) ((data >> 24) & 0xFF)};
        cpu.mem.writeMemory((int) addr, w);
    }
}