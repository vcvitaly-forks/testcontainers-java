package org.testcontainers.containers.wait.internal;

import java.io.IOException;
import java.util.HashSet;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.ExecInContainerPattern;
import org.testcontainers.containers.wait.strategy.WaitStrategyTarget;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class InternalCommandPortListeningCheckUnitTest {

    @Test(expected = IllegalStateException.class)
    public void thowsAnExeptionWhenBinShIsMissing() throws IOException, InterruptedException {
        final Container.ExecResult mockResult = getMockExecResultWithError();
        final ExecInContainerPattern mockExecPattern = getMockExecPattern(mockResult);
        ExecInContainerPattern.setInstance(mockExecPattern);

        final InternalCommandPortListeningCheck check = getCheck();

        check.call();
    }

    @SneakyThrows
    private ExecInContainerPattern getMockExecPattern(Container.ExecResult mockResult) {
        final ExecInContainerPattern mockExecPattern = mock(ExecInContainerPattern.class);
        when(mockExecPattern.execInContainer(any(), any())).thenReturn(mockResult);
        return mockExecPattern;
    }

    private Container.ExecResult getMockExecResultWithError() {
        final Container.ExecResult mockResult = mock(Container.ExecResult.class);
        when(mockResult.getExitCode()).thenReturn(126);
        when(mockResult.getStdout()).thenReturn(
            "OCI runtime exec failed: exec failed: container_linux.go:349: " +
                "starting container process caused \"exec: \\\"/bin/sh\\\": " +
                "stat /bin/sh: no such file or directory\": unknown\n");
        when(mockResult.getStderr()).thenReturn("");
        return mockResult;
    }

    @Test
    public void succeedsWhenThereIsNoBinShError() {
        final Container.ExecResult mockResult = getMockExecResultWithoutError();
        final ExecInContainerPattern mockExecPattern = getMockExecPattern(mockResult);
        ExecInContainerPattern.setInstance(mockExecPattern);

        final InternalCommandPortListeningCheck check = getCheck();

        assertThat(check.call()).isTrue();
    }

    private Container.ExecResult getMockExecResultWithoutError() {
        final Container.ExecResult mockResult = mock(Container.ExecResult.class);
        when(mockResult.getExitCode()).thenReturn(0);
        return mockResult;
    }

    @NotNull
    private InternalCommandPortListeningCheck getCheck() {
        return new InternalCommandPortListeningCheck(
            mock(WaitStrategyTarget.class),
            new HashSet<>()
        );
    }
}
