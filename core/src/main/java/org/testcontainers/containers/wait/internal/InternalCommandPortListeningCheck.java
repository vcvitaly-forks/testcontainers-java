package org.testcontainers.containers.wait.internal;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.Container.ExecResult;
import org.testcontainers.containers.ExecInContainerPattern;
import org.testcontainers.containers.wait.strategy.WaitStrategyTarget;

import static java.lang.String.format;

/**
 * Mechanism for testing that a socket is listening when run from the container being checked.
 */
@RequiredArgsConstructor
@Slf4j
public class InternalCommandPortListeningCheck implements java.util.concurrent.Callable<Boolean> {

    private final WaitStrategyTarget waitStrategyTarget;
    private final Set<Integer> internalPorts;

    @Override
    public Boolean call() {
        StringBuilder command = new StringBuilder("true");

        for (int internalPort : internalPorts) {
            command
                .append(" && ")
                .append(" (")
                .append(format("cat /proc/net/tcp* | awk '{print $2}' | grep -i ':0*%x'", internalPort))
                .append(" || ")
                .append(format("nc -vz -w 1 localhost %d", internalPort))
                .append(" || ")
                .append(format("/bin/bash -c '</dev/tcp/localhost/%d'", internalPort))
                .append(")");
        }

        Instant before = Instant.now();
        ExecResult result;
        try {
            result = ExecInContainerPattern.instance().execInContainer(waitStrategyTarget.getContainerInfo(), "/bin/sh", "-c", command.toString());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        final String errorMessage = format("Check for %s took %s. Result code '%d', stdout message: '%s'",
            internalPorts, Duration.between(before, Instant.now()),
            result.getExitCode(), result.getStdout()
        );
        log.trace(errorMessage);
        if (result.getExitCode() != 0) {
            throw new IllegalStateException(errorMessage);
        }

        return true;
    }
}
