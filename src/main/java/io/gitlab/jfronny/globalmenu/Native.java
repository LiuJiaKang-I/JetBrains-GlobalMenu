package io.gitlab.jfronny.globalmenu;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class Native {
    public native void init(long displayPtr);
    public native long create(long ptr);
    public native void destroy(long ptr);
    public native void setAddress(long ptr, String serviceName, String objectPath);

    private static final boolean supported;
    static {
        if (!System.getProperty("os.name").toLowerCase().contains("linux")) {
            supported = false;
        } else {
            try (InputStream is = Native.class.getResourceAsStream("/libnative.so")) {
                Path path = Files.createTempFile("libnative", ".so");
                Files.copy(is, path, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                System.load(path.toString());
            } catch (Exception e) {
                supported = false;
                throw new RuntimeException(e);
            }
            supported = true;
        }
    }

    public boolean isSupported() {
        return supported;
    }
}
