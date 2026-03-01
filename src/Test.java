import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;

public class Test {
    public static void main(String[] args) throws IOException, URISyntaxException {

        File testsFolder =  new File(Test.class.getResource("/tests/").toURI());
        File[] tests = testsFolder.listFiles();
        for (File test : tests) {
            SimpleMemory mem = new SimpleMemory(256_000_000);
            long entry = ElfParser.loadAndReturnEntry(test.getPath(), mem);
            CPU cpu = new CPU(mem);
            cpu.run(entry);
        }


    }
}
