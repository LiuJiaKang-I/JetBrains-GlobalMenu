package io.gitlab.jfronny.globalmenu;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class Native {
    public native void init(long displayPtr);

    public native boolean isMenuSupported();
    public native long createMenu(long ptr);
    public native void destroyMenu(long ptr);
    public native void setMenuAddress(long ptr, String serviceName, String objectPath);

    public native boolean isDecorationSupported();
    public native long createDecoration(long ptr);
    public native void destroyDecoration(long ptr);
    public native void setDecoration(long ptr, int mode); // 0 = no preference, 1 = client side, 2 = server side

    private static final String problem;
    static {
        if (!System.getProperty("os.name").toLowerCase().contains("linux")) {
            problem = "Not running on Linux";
        } else if (!System.getProperty("os.arch").equals("amd64")) {
            problem = "Not running on amd64";
        } else if (System.getProperty("io.gitlab.jfronny.globalmenu.disable") != null) {
            problem = "Explicitly disabled";
        } else {
            String _problem = null;
            try (InputStream is = Native.class.getResourceAsStream("/libnative.so")) {
                Path path = Files.createTempFile("libnative", ".so");
                Files.copy(is, path, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                System.load(path.toString());
            } catch (Throwable e) {
                _problem = "Failed to load native library: " + e.getMessage();
                // We can't log yet, because Idea will complain
//                GlobalMenu.INSTANCE.getLog().error("Failed to load native library", e);
            }
            problem = _problem;
        }
    }

    public boolean isSupported() {
        return problem == null;
    }

    public Optional<String> getProblem() {
        return Optional.ofNullable(problem);
    }
}
