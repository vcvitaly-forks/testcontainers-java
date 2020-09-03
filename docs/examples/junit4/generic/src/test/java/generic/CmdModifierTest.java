package generic;

import org.junit.Rule;
import org.junit.Test;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class CmdModifierTest {

    // hostname {
    @Rule
    public GenericContainer theCache = new GenericContainer<>(DockerImageName.parse("redis:3.0.2"))
            .withCreateContainerCmdModifier(cmd -> cmd.withHostName("the-cache"));
    // }

    // memory {
    private long memoryInBytes = 8 * 1024 * 1024;
    private long memorySwapInBytes = 12 * 1024 * 1024;

    @Rule
    public GenericContainer memoryLimitedRedis = new GenericContainer<>(DockerImageName.parse("redis:3.0.2"))
            .withCreateContainerCmdModifier(cmd -> cmd.getHostConfig()
                .withMemory(memoryInBytes)
                .withMemorySwap(memorySwapInBytes)
            );
    // }


    @Test
    public void testHostnameModified() throws IOException, InterruptedException {
        final Container.ExecResult execResult = theCache.execInContainer("hostname");
        assertEquals("the-cache", execResult.getStdout().trim());
    }

    @Test
    public void testMemoryLimitModified() throws IOException, InterruptedException {
        final Container.ExecResult execResult = memoryLimitedRedis.execInContainer("cat", "/sys/fs/cgroup/memory/memory.limit_in_bytes");
        assertEquals("8388608", execResult.getStdout().trim());
    }
}
