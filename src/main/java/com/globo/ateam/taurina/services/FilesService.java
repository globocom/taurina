package com.globo.ateam.taurina.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class FilesService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final String envTmpDir = System.getenv("TMPDIR");
    private final String tmpDir = envTmpDir != null ? envTmpDir : System.getProperty("java.io.tmpdir", "/tmp");
    private final String envJmeterHome = System.getenv("JMETER_HOME");
    private final String jmeterHome = envJmeterHome != null ? envJmeterHome : System.getProperty("jmeter.home", tmpDir);
    private final String jmeterProp = System.getProperty("jmeter.properties", "UNDEF");

    @Value("${build.project}")
    private String buildProject;

    @PostConstruct
    public void setup() throws IOException {
        createDir(tmpDir());
    }

    public void createDir(String idDir) throws IOException {
        if (!Files.exists(Paths.get(idDir))) Files.createDirectory(Paths.get(idDir));
    }

    public void writeToFile(byte[] tree, String confFile) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(confFile)) {
            fos.write(tree);
        }
    }

    public String tmpDir() {
        return tmpDir + "/" + buildProject;
    }

    public String jmeterHome() {
        return jmeterHome;
    }

    public String pathIdDirectory(long testId) {
        return tmpDir() + "/" + testId;
    }

    public String pathResultFile(long testId) {
        return pathIdDirectory(testId) + "/result";
    }

    public long nextId() throws IOException {
        final Path lastIdFile = Paths.get(tmpDir() + "/last");
        final String lastIdStr = (Files.exists(lastIdFile)) ? Files.lines(lastIdFile).findAny().orElse("0") : "-1";
        final long testId = Long.parseLong(lastIdStr) + 1L;
        try (BufferedWriter writer = Files.newBufferedWriter(lastIdFile)) {
            writer.write(String.valueOf(testId));
        }
        return testId;
    }

    public String props() {
        return jmeterProp;
    }
}
