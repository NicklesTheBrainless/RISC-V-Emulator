import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {

        File dic = new File("/home/nic/Programming/Java/RISC-V/src/tests");
        File[] tests = dic.listFiles();
        for (File test : tests) {
            SimpleMemory sm = new SimpleMemory(256_000_000);
            long entry = ElfParser.loadAndReturnEntry(test.getPath(), sm);
            System.out.println(Long.toBinaryString(sm.read8Bytes(0)));
            CPU cpu = new CPU(sm);
            cpu.run(entry);
        }


    }
}
