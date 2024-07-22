package io.gitlab.jfronny.globalmenu;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class Native {
    public native void init(long displayPtr);
    public native long create(long ptr);
    public native void destroy(long ptr);
    public native void setAddress(long ptr, String serviceName, String objectPath);

    private static final String problem;
    static {
        if (!System.getProperty("os.name").toLowerCase().contains("linux")) {
            problem = "Not running on Linux";
        } else if (!System.getProperty("os.arch").equals("amd64")) {
            problem = "Not running on amd64";
        } else {
            try (InputStream is = Native.class.getResourceAsStream("/libnative.so")) {
                Path path = Files.createTempFile("libnative", ".so");
                Files.copy(is, path, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                System.load(path.toString());
            } catch (Exception e) {
                problem = "Failed to load native library: " + e.getMessage();
                throw new RuntimeException(e);
            }
            problem = null;
        }
    }

    public boolean isSupported() {
        return problem == null;
    }

    public Optional<String> getProblem() {
        return Optional.ofNullable(problem);
    }
}
