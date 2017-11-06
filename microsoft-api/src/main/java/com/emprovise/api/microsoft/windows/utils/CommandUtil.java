package com.emprovise.api.microsoft.windows.utils;

import java.io.IOException;

public class CommandUtil {

    /**
     * @param cmdArray  array containing the command to call and its arguments
     *                  or a single specified system command.
     * @return {@link String} object containing the output response from the command.
     * @throws IOException
     * @throws InterruptedException
     */
    public static String executeCommand(String... cmdArray) throws IOException, InterruptedException {

        Process process;

        if(cmdArray == null || cmdArray.length == 0) {
            throw new IllegalArgumentException("Empty command");
        }

        if(cmdArray.length == 1) {
            process = Runtime.getRuntime().exec(cmdArray[0]);
        } else {
            process = Runtime.getRuntime().exec(cmdArray);
        }

        StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream());
        StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream());

        errorGobbler.start();
        outputGobbler.start();

        int exitCode = process.waitFor();
        String errors = errorGobbler.getString();

        if(exitCode != 0 && !errors.isEmpty()) {
            throw new RuntimeException(errors);
        }

        return outputGobbler.getString();
    }
}