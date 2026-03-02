
public class CPU {

    private static final double CPU_CLOCK_SPEED = 64;
    private static final long   NANOSECONDS_PER_SECOND = 1_000_000_000;

    long[] regs = new long[32];
    Memory mem;

    long counter;

    boolean halt = false;

    public CPU(Memory mem) {
        this.mem = mem;

        int sp = mem.getSize() - 16;
        sp &= ~0xF;    // 16-byte alignment

        mem.writeMemory(sp, new byte[] {0, 0, 0, 0, 0, 0, 0, 0});    // argc = 0
        mem.writeMemory(sp + 8, new byte[] {0, 0, 0, 0, 0, 0, 0, 0});    // argv = NULL

        this.regs[2] = sp;
    }

    public void run(long entry) {

        counter = entry;

        double interval = (double) NANOSECONDS_PER_SECOND / CPU_CLOCK_SPEED;
        double delta = 0;

        long lastTime = System.nanoTime();
        long currentTime;

        while (!halt) {

            currentTime = System.nanoTime();

            delta += (currentTime - lastTime) / interval;
            lastTime = currentTime;

            if (delta >= 1) {
                step();
                delta--;
            }
        }
    }

    public void step() {
        int instCode = mem.read4Bytes(Math.toIntExact(counter));
        System.out.println("Binary Instruction Code at " + counter + ": " + Integer.toBinaryString(instCode));
        Instruction inst = decodeInstruction(instCode);

        InstructionExecutor.executeInstruction(this, instCode, inst);
        counter += 4;
    }

    private Instruction decodeInstruction(int instCode) {

        int opcode =  instCode & 0b0111_1111;           // bits 0-6
        int funct3 = (instCode >>> 12) & 0b111;         // bits 12-14
        int funct7 = (instCode >>> 25) & 0b111_1111;    // bits 25-31
        if (opcode == Instruction.SLLI.opcode || opcode == Instruction.SRLI.opcode || opcode == Instruction.SRAI.opcode)
            funct7 = (instCode >>> 26) & 0b11_1111;     // bits 26-31 bei RV64I immediate shift

        for (int i = 0; i < regs.length; i++)
            if (regs[i] != 0)
                System.out.println("r" + i + ": " + regs[i]);

        // checking instructions until correct one found
        for (Instruction in : Instruction.values()) {

            if (in.opcode != opcode)
                continue;
            if (in.funct3 != -1 && in.funct3 != funct3)
                continue;
            if (in.funct7 != -1 && in.funct7 != funct7)
                continue;

            return in;
        }

        throw new IllegalArgumentException("Unknown Instruction: " + Integer.toBinaryString(instCode));
    }


}
