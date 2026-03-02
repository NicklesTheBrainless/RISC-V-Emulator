public class InstructionExecutor {

    public static void executeInstruction(CPU cpu, int instCode, Instruction inst) {

        int rd = (instCode >>> 7) & 0x1F;
        int rs1 = (instCode >>> 15) & 0x1F;
        int rs2 = (instCode >>> 20) & 0x1F;

        int shiftAmount = (instCode >>> 20) & 0x3F;                 // 6-Bit shift amount (RV64)

        // TODO: check all of this and make readable
        long immI = signExtend(instCode >>> 20, 12);          // I-Type
        long immS = signExtend(((instCode >>> 7) & 0x1F) | ((instCode >>> 25) << 5), 12); // S-Type
        long immU = instCode & 0xFFFFF000;           // U-Type (kein sign-ext)
        long immB = signExtend(((instCode >>> 7) & 0x1) << 11 | ((instCode >>> 8) & 0xF) << 1 | ((instCode >>> 25) & 0x3F) << 5 | ((instCode >>> 31) & 0x1) << 12, 13);    // B-Type
        long immJ = signExtend(((instCode >>> 12) & 0xFF) << 12 | ((instCode >>> 20) & 0x1) << 11 | ((instCode >>> 21) & 0x3FF) << 1 | ((instCode >>> 31) & 0x1) << 20, 21);    // J-Type

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
            case LB  -> cpu.regs[rd] = signExtend8(cpu.mem.readByte((int) (x1 + immI)));
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
            case SLL -> cpu.regs[rd] = x1 << (x2 & 0x3F);
            case SLT -> cpu.regs[rd] = (x1 < x2) ? 1 : 0;
            case SLTU -> cpu.regs[rd] = (Long.compareUnsigned(x1, x2) < 0) ? 1 : 0;
            case XOR -> cpu.regs[rd] = x1 ^ x2;
            case SRL -> cpu.regs[rd] = x1 >>> (x2 & 0x3F);
            case SRA -> cpu.regs[rd] = x1 >> (x2 & 0x3F);
            case OR -> cpu.regs[rd] = x1 | x2;
            case AND -> cpu.regs[rd] = x1 & x2;

            // TODO: this shit
            /*  System  */
            case ECALL, EBREAK -> {
                System.out.println("HALT HALT HALT");
                cpu.halt = true;
            }    // Minimal-Handling
        }

        cpu.regs[0] = 0;
    }


    // TODO: check and fix sign extend function
    public static long signExtend(long value, int bitLength) {
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