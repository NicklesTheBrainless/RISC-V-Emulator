import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

public class Test {

    public static String currentTest;

    public static void main(String[] args) throws IOException, URISyntaxException {

        File testsFolder =  new File(Test.class.getResource("/tests/").toURI());
        File[] tests = testsFolder.listFiles();
        for (File test : tests) {
            currentTest = test.getName();
            SimpleMemory mem = new SimpleMemory(256_000_000);
            long entry = ElfParser.loadAndReturnEntry(test.getPath(), mem, -2147483648L);
            CPU cpu = new CPU(mem);
            cpu.run(entry);
        }


    }
}
