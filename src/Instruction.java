public enum Instruction {

    // Upper immediate (U-Type)
    LUI   (0b0110111, -1, -1),
    AUIPC (0b0010111, -1, -1),

    // Jumps (J-Type))
    JAL   (0b1101111, -1, -1),
    JALR  (0b1100111, 0b000, -1),

    // Branches (B-Type)
    BEQ   (0b1100011, 0b000, -1),
    BNE   (0b1100011, 0b001, -1),
    BLT   (0b1100011, 0b100, -1),
    BGE   (0b1100011, 0b101, -1),
    BLTU  (0b1100011, 0b110, -1),
    BGEU  (0b1100011, 0b111, -1),

    // Loads (I-Type)
    LB    (0b0000011, 0b000, -1),
    LH    (0b0000011, 0b001, -1),
    LW    (0b0000011, 0b010, -1),
    LD    (0b0000011, 0b011, -1),
    LBU   (0b0000011, 0b100, -1),
    LHU   (0b0000011, 0b101, -1),
    LWU   (0b0000011, 0b110, -1),

    // Stores (S-Type)
    SB    (0b0100011, 0b000, -1),
    SH    (0b0100011, 0b001, -1),
    SW    (0b0100011, 0b010, -1),
    SD    (0b0100011, 0b011, -1),

    // Immediate ALU (I-Type)
    ADDI  (0b0010011, 0b000, -1),
    SLTI  (0b0010011, 0b010, -1),
    SLTIU (0b0010011, 0b011, -1),
    XORI  (0b0010011, 0b100, -1),
    ORI   (0b0010011, 0b110, -1),
    ANDI  (0b0010011, 0b111, -1),

    // Shift Immediate (I-Type)
    SLLI  (0b0010011, 0b001, 0b0000000),
    SRLI  (0b0010011, 0b101, 0b0000000),
    SRAI  (0b0010011, 0b101, 0b0100000),

    // RV64 ALU Immediate (I-Type)
    ADDIW (0b0011011, 0b000, -1),
    SLLIW (0b0011011, 0b001, 0b0000000),
    SRLIW (0b0011011, 0b101, 0b0000000),
    SRAIW (0b0011011, 0b101, 0b0100000),

    // ALU Register (R-Type)
    ADD   (0b0110011, 0b000, 0b0000000),
    SUB   (0b0110011, 0b000, 0b0100000),
    SLL   (0b0110011, 0b001, 0b0000000),
    SLT   (0b0110011, 0b010, 0b0000000),
    SLTU  (0b0110011, 0b011, 0b0000000),
    XOR   (0b0110011, 0b100, 0b0000000),
    SRL   (0b0110011, 0b101, 0b0000000),
    SRA   (0b0110011, 0b101, 0b0100000),
    OR    (0b0110011, 0b110, 0b0000000),
    AND   (0b0110011, 0b111, 0b0000000),

    // RV64 ALU Registers (R-Type)
    ADDW  (0b0111011, 0b000, 0b0000000),
    SUBW  (0b0111011, 0b000, 0b0100000),
    SLLW  (0b0111011, 0b001, 0b0000000),
    SRLW  (0b0111011, 0b101, 0b0000000),
    SRAW  (0b0111011, 0b101, 0b0100000),

    // System
    ECALL (0b1110011, -1, -1),
    EBREAK(0b1110011, 0b000, 0b0000001);


    public final int opcode;
    public final int funct3;
    public final int funct7;

    Instruction(int opcode, int funct3, int funct7) {
        this.opcode = opcode;
        this.funct3 = funct3;
        this.funct7 = funct7;
    }
}