public enum Instruction {

    // U-Type
    LUI   (InstType.U_TYPE, 0b0110111, -1, -1),
    AUIPC (InstType.U_TYPE, 0b0010111, -1, -1),

    // Jumps
    JAL   (InstType.J_TYPE, 0b1101111, -1, -1),
    JALR  (InstType.I_TYPE, 0b1100111, 0b000, -1),

    // Branch
    BEQ   (InstType.B_TYPE, 0b1100011, 0b000, -1),
    BNE   (InstType.B_TYPE, 0b1100011, 0b001, -1),
    BLT   (InstType.B_TYPE, 0b1100011, 0b100, -1),
    BGE   (InstType.B_TYPE, 0b1100011, 0b101, -1),
    BLTU  (InstType.B_TYPE, 0b1100011, 0b110, -1),
    BGEU  (InstType.B_TYPE, 0b1100011, 0b111, -1),

    // Loads
    LB    (InstType.I_TYPE, 0b0000011, 0b000, -1),
    LH    (InstType.I_TYPE, 0b0000011, 0b001, -1),
    LW    (InstType.I_TYPE, 0b0000011, 0b010, -1),
    LD    (InstType.I_TYPE, 0b0000011, 0b011, -1),
    LBU   (InstType.I_TYPE, 0b0000011, 0b100, -1),
    LHU   (InstType.I_TYPE, 0b0000011, 0b101, -1),
    LWU   (InstType.I_TYPE, 0b0000011, 0b110, -1),

    // Saves
    SB    (InstType.S_TYPE, 0b0100011, 0b000, -1),
    SH    (InstType.S_TYPE, 0b0100011, 0b001, -1),
    SW    (InstType.S_TYPE, 0b0100011, 0b010, -1),
    SD    (InstType.S_TYPE, 0b0100011, 0b011, -1),

    // Immediate ALU
    ADDI  (InstType.I_TYPE, 0b0010011, 0b000, -1),
    SLTI  (InstType.I_TYPE, 0b0010011, 0b010, -1),
    SLTIU (InstType.I_TYPE, 0b0010011, 0b011, -1),
    XORI  (InstType.I_TYPE, 0b0010011, 0b100, -1),
    ORI   (InstType.I_TYPE, 0b0010011, 0b110, -1),
    ANDI  (InstType.I_TYPE, 0b0010011, 0b111, -1),

    // Shift Immediate
    SLLI  (InstType.I_TYPE, 0b0010011, 0b001, 0b0000000),
    SRLI  (InstType.I_TYPE, 0b0010011, 0b101, 0b0000000),
    SRAI  (InstType.I_TYPE, 0b0010011, 0b101, 0b0100000),

    // R-Type
    ADD   (InstType.R_TYPE, 0b0110011, 0b000, 0b0000000),
    SUB   (InstType.R_TYPE, 0b0110011, 0b000, 0b0100000),
    SLL   (InstType.R_TYPE, 0b0110011, 0b001, 0b0000000),
    SLT   (InstType.R_TYPE, 0b0110011, 0b010, 0b0000000),
    SLTU  (InstType.R_TYPE, 0b0110011, 0b011, 0b0000000),
    XOR   (InstType.R_TYPE, 0b0110011, 0b100, 0b0000000),
    SRL   (InstType.R_TYPE, 0b0110011, 0b101, 0b0000000),
    SRA   (InstType.R_TYPE, 0b0110011, 0b101, 0b0100000),
    OR    (InstType.R_TYPE, 0b0110011, 0b110, 0b0000000),
    AND   (InstType.R_TYPE, 0b0110011, 0b111, 0b0000000),

    // System
    ECALL (InstType.I_TYPE, 0b1110011, 0b000, 0b0000000),
    EBREAK(InstType.I_TYPE, 0b1110011, 0b000, 0b0000001),

    // nur in 64 (keine ahnung was die machen)
    ADDIW (InstType.I_TYPE, 0b0011011, 0b000, -1),
    SLLIW (InstType.I_TYPE, 0b0011011, 0b001, 0b0000000),
    SRLIW (InstType.I_TYPE, 0b0011011, 0b101, 0b0000000),
    SRAIW (InstType.I_TYPE, 0b0011011, 0b101, 0b0100000),

    // nur in 64 (keine ahnung was die machen)
    ADDW  (InstType.R_TYPE, 0b0111011, 0b000, 0b0000000),
    SUBW  (InstType.R_TYPE, 0b0111011, 0b000, 0b0100000),
    SLLW  (InstType.R_TYPE, 0b0111011, 0b001, 0b0000000),
    SRLW  (InstType.R_TYPE, 0b0111011, 0b101, 0b0000000),
    SRAW  (InstType.R_TYPE, 0b0111011, 0b101, 0b0100000);


    public final InstType type;
    public final int opcode;
    public final int funct3;
    public final int funct7;

    Instruction(InstType type, int opcode, int funct3, int funct7) {
        this.type = type;
        this.opcode = opcode;
        this.funct3 = funct3;
        this.funct7 = funct7;
    }
}