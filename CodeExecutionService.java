package com.codearena.service;

import com.codearena.dto.ExecuteRequest;
import com.codearena.dto.ExecuteResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.*;

@Service
public class CodeExecutionService {

    @Value("${execution.timeout:10000}")
    private long timeout;

    @Value("${execution.memory.limit:256m}")
    private String memoryLimit;

    public ExecuteResponse execute(ExecuteRequest request) {
        String language = request.getLanguage().toLowerCase();
        String code = request.getCode();
        
        long startTime = System.currentTimeMillis();
        
        try {
            // Create temporary directory for code files
            Path tempDir = Files.createTempDirectory("code-arena-");
            
            String output;
            String error = null;
            String status = "SUCCESS";
            
            switch (language) {
                case "java":
                    output = executeJava(tempDir, code);
                    break;
                case "python":
                case "python3":
                    output = executePython(tempDir, code);
                    break;
                case "cpp":
                case "c++":
                    output = executeCpp(tempDir, code);
                    break;
                default:
                    return new ExecuteResponse(
                        null,
                        "Unsupported language: " + language,
                        0,
                        "ERROR",
                        "0MB"
                    );
            }
            
            long executionTime = System.currentTimeMillis() - startTime;
            
            // Cleanup
            deleteDirectory(tempDir.toFile());
            
            return new ExecuteResponse(output, error, executionTime, status, "0MB");
            
        } catch (TimeoutException e) {
            long executionTime = System.currentTimeMillis() - startTime;
            return new ExecuteResponse(
                null,
                "Execution timed out after " + timeout + "ms",
                executionTime,
                "TIMEOUT",
                "0MB"
            );
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            return new ExecuteResponse(
                null,
                e.getMessage(),
                executionTime,
                "ERROR",
                "0MB"
            );
        }
    }

    private String executeJava(Path tempDir, String code) throws Exception {
        // Write Java file
        String className = "Main";
        Path javaFile = tempDir.resolve(className + ".java");
        Files.writeString(javaFile, code);
        
        // Compile
        ProcessBuilder compileBuilder = new ProcessBuilder("javac", className + ".java");
        compileBuilder.directory(tempDir.toFile());
        compileBuilder.redirectErrorStream(true);
        Process compileProcess = compileBuilder.start();
        
        String compileOutput = readProcessOutput(compileProcess);
        int compileExitCode = compileProcess.waitFor();
        
        if (compileExitCode != 0) {
            return "Compilation Error:\n" + compileOutput;
        }
        
        // Run
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<String> future = executor.submit(() -> {
            ProcessBuilder runBuilder = new ProcessBuilder("java", className);
            runBuilder.directory(tempDir.toFile());
            runBuilder.redirectErrorStream(true);
            Process runProcess = runBuilder.start();
            return readProcessOutput(runProcess);
        });
        
        try {
            return future.get(timeout, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            throw e;
        } finally {
            executor.shutdownNow();
        }
    }

    private String executePython(Path tempDir, String code) throws Exception {
        // Write Python file
        Path pythonFile = tempDir.resolve("script.py");
        Files.writeString(pythonFile, code);
        
        // Run
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<String> future = executor.submit(() -> {
            ProcessBuilder runBuilder = new ProcessBuilder("python", "script.py");
            runBuilder.directory(tempDir.toFile());
            runBuilder.redirectErrorStream(true);
            Process runProcess = runBuilder.start();
            return readProcessOutput(runProcess);
        });
        
        try {
            return future.get(timeout, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            throw e;
        } finally {
            executor.shutdownNow();
        }
    }

    private String executeCpp(Path tempDir, String code) throws Exception {
        // Write C++ file
        Path cppFile = tempDir.resolve("main.cpp");
        Files.writeString(cppFile, code);
        
        // Compile
        ProcessBuilder compileBuilder = new ProcessBuilder("g++", "main.cpp", "-o", "main");
        compileBuilder.directory(tempDir.toFile());
        compileBuilder.redirectErrorStream(true);
        Process compileProcess = compileBuilder.start();
        
        String compileOutput = readProcessOutput(compileProcess);
        int compileExitCode = compileProcess.waitFor();
        
        if (compileExitCode != 0) {
            return "Compilation Error:\n" + compileOutput;
        }
        
        // Run
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<String> future = executor.submit(() -> {
            ProcessBuilder runBuilder = new ProcessBuilder("./main");
            runBuilder.directory(tempDir.toFile());
            runBuilder.redirectErrorStream(true);
            Process runProcess = runBuilder.start();
            return readProcessOutput(runProcess);
        });
        
        try {
            return future.get(timeout, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            throw e;
        } finally {
            executor.shutdownNow();
        }
    }

    private String readProcessOutput(Process process) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder output = new StringBuilder();
        String line;
        
        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
        }
        
        return output.toString().trim();
    }

    private void deleteDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        directory.delete();
    }
}
