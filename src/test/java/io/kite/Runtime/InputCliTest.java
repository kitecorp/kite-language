package io.kite.Runtime;

import java.io.InputStream;

public abstract class InputCliTest extends InputTest {
    private InputStream sysInBackup;

//    @BeforeEach
//    void each() {
//        sysInBackup = System.in;
//    }
//
//    @AfterEach
//    void cleanup() {
//        System.setIn(sysInBackup);
//    }

//    @Override
//    protected void setInput(String input) {
//        System.setIn(new ByteArrayInputStream(input.getBytes()));
//    }
//
//    @Override
//    protected List<InputResolver> getChainResolver() {
//        return List.of(
////                new FileResolver(environment, FileHelpers.loadInputDefaultsFiles()),
////                new EnvResolver(global),
//                new CliResolver(global)
//        );
//    }
}
