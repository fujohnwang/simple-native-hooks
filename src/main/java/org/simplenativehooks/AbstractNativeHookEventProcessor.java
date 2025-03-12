package org.simplenativehooks;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ProcessBuilder.Redirect;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.simplenativehooks.staticResources.AbstractBootstrapResource;
import org.simplenativehooks.utilities.StringUtilities;
import org.slf4j.LoggerFactory;

public abstract class AbstractNativeHookEventProcessor {
    private static final Logger LOGGER = Logger.getLogger(AbstractNativeHookEventProcessor.class.getName());
    protected org.slf4j.Logger logger = LoggerFactory.getLogger(AbstractBootstrapResource.class.getName());
    private static final long TIMEOUT_MS = 2000;

    private boolean withSudo; // Run as root.
    private Process process;
    private Thread stdoutThread, stderrThread, forceDestroyThread;

    public void setRunWithSudo() {
        this.withSudo = true;
    }

    public void setRunWithoutSudo() {
        this.withSudo = false;
    }

    public abstract String getName();

    public abstract File getExecutionDir();

    public abstract String[] getCommand();

    public abstract void processStdout(String line);

    public abstract void processStderr(String line);

    public final void start() {
        logger.info("monitor native hook start event with exec dir=" + getExecutionDir());
        if (process != null || stdoutThread != null || stderrThread != null) {
            LOGGER.warning("Hook is already running...");
            return;
        }
        File executableDir = getExecutionDir();
        if (!executableDir.isDirectory()) {
            LOGGER.warning(getName() + " executable directory " + getExecutionDir().getAbsolutePath() + " does not exist or is not a directory.");
            return;
        }

        String[] command = getCommand();
        if (withSudo) {
            String[] commandWithSudo = new String[command.length + 1];
            System.arraycopy(command, 0, commandWithSudo, 1, command.length);
            commandWithSudo[0] = "sudo";
            command = commandWithSudo;
        }
        final String[] runningCommand = command;
        logger.info(getName() + ": running command $" + StringUtilities.join(command, " "));
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.directory(executableDir);
            if (withSudo) {
                processBuilder.redirectInput(Redirect.INHERIT);
            }

            logger.info("kick off process: " + StringUtilities.join(command, " "));
            process = processBuilder.start();
            BufferedReader bufferStdout = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader bufferStderr = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            logger.info("start stdout thread of native hook...");
            stdoutThread = new Thread() {
                @Override
                public void run() {
                    try {
                        processStdout(bufferStdout);
                    } catch (Exception e) {
                        LOGGER.log(Level.WARNING, "Exception encountered reading stdout of command " + runningCommand, e);
                        logger.warn("Exception encountered reading stdout of command " + StringUtilities.join(runningCommand, " ") + "\n" + e.getMessage());
                    }
                }
            };
            stdoutThread.start();

            logger.info("start stderr thread of native hook...");
            stderrThread = new Thread() {
                @Override
                public void run() {
                    try {
                        processStderr(bufferStderr);
                    } catch (Exception e) {
                        LOGGER.log(Level.WARNING, "Exception encountered reading stderr of command " + runningCommand, e);
                        logger.warn("Exception encountered reading stderr of command " + StringUtilities.join(runningCommand, " ") + "\n" + e.getMessage());
                    }
                }
            };
            stderrThread.start();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Exception encountered while running command " + command, e);
            logger.warn("Exception encountered while running command " + StringUtilities.join(runningCommand, " ") + "\n" + e.getMessage());
            reset();
        }
    }

    public final void stop() throws InterruptedException {
        if (forceDestroyThread != null) {
            LOGGER.info("Waiting for termination...");
            return;
        }

        forceDestroyThread = new Thread() {
            @Override
            public void run() {
                process.destroy();
                LOGGER.info("Native hook process for " + AbstractNativeHookEventProcessor.this.getName() + " destroyed.");

                try {
                    Thread.sleep(TIMEOUT_MS);
                } catch (InterruptedException e) {
                    LOGGER.log(Level.WARNING, "Interrupted while waiting for " + getName() + " to terminate", e);
                }

                if (process.isAlive()) {
                    LOGGER.info("Forcing " + getName() + " termination");
                    process.destroyForcibly();
                }
            }
        };
        forceDestroyThread.start();
        forceDestroyThread.join();
        stdoutThread.join();
        stderrThread.join();
        reset();
    }

    public final boolean isRunning() {
        return forceDestroyThread == null &&
                stdoutThread != null &&
                stderrThread != null &&
                stdoutThread.isAlive() &&
                stderrThread.isAlive();
    }

    private void processStdout(BufferedReader reader) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            String trimmed = line.trim();
            if (trimmed.length() == 0) {
                continue;
            }

            try {
                processStdout(line);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Exception when processing stdout for " + getName() + ". " + e.getMessage(), e);
            }
        }
    }

    private void processStderr(BufferedReader reader) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            String trimmed = line.trim();
            if (trimmed.length() == 0) {
                continue;
            }

            try {
                processStderr(line);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Exception when processing stderr for " + getName() + ". " + e.getMessage(), e);
            }
        }
    }

    private void reset() {
        process = null;
        stdoutThread = null;
        stderrThread = null;
        forceDestroyThread = null;
    }
}
