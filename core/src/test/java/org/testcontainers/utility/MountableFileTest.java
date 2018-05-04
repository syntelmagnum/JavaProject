package org.testcontainers.utility;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.rnorth.visibleassertions.VisibleAssertions.assertEquals;
import static org.rnorth.visibleassertions.VisibleAssertions.assertFalse;
import static org.rnorth.visibleassertions.VisibleAssertions.assertTrue;

public class MountableFileTest {

    private static final int TEST_FILE_MODE = 0532;
    private static final int BASE_FILE_MODE = 0100000;
    private static final int BASE_DIR_MODE = 0040000;

    @Test
    public void forClasspathResource() throws Exception {
        final MountableFile mountableFile = MountableFile.forClasspathResource("mappable-resource/test-resource.txt");

        performChecks(mountableFile);
    }

    @Test
    public void forClasspathResourceWithAbsolutePath() throws Exception {
        final MountableFile mountableFile = MountableFile.forClasspathResource("/mappable-resource/test-resource.txt");

        performChecks(mountableFile);
    }

    @Test
    public void forClasspathResourceFromJar() throws Exception {
        final MountableFile mountableFile = MountableFile.forClasspathResource("META-INF/dummy_unique_name.txt");

        performChecks(mountableFile);
    }

    @Test
    public void forClasspathResourceFromJarWithAbsolutePath() throws Exception {
        final MountableFile mountableFile = MountableFile.forClasspathResource("/META-INF/dummy_unique_name.txt");

        performChecks(mountableFile);
    }

    @Test
    public void forHostPath() throws Exception {
        final Path file = createTempFile("somepath");
        final MountableFile mountableFile = MountableFile.forHostPath(file.toString());

        performChecks(mountableFile);
    }

    @Test
    public void forHostPathWithSpaces() throws Exception {
        final Path file = createTempFile("some path");
        final MountableFile mountableFile = MountableFile.forHostPath(file.toString());

        performChecks(mountableFile);

        assertTrue("The resolved path contains the original space", mountableFile.getResolvedPath().contains(" "));
        assertFalse("The resolved path does not contain an escaped space", mountableFile.getResolvedPath().contains("\\ "));
    }

    @Test
    public void forHostPathWithPlus() throws Exception {
        final Path file = createTempFile("some+path");
        final MountableFile mountableFile = MountableFile.forHostPath(file.toString());

        performChecks(mountableFile);

        assertTrue("The resolved path contains the original space", mountableFile.getResolvedPath().contains("+"));
        assertFalse("The resolved path does not contain an escaped space", mountableFile.getResolvedPath().contains(" "));
    }

    @Test
    public void forClasspathResourceWithPermission() throws Exception {
        final MountableFile mountableFile = MountableFile.forClasspathResource("mappable-resource/test-resource.txt",
                TEST_FILE_MODE);

        performChecks(mountableFile);
        assertEquals("Valid file mode.", BASE_FILE_MODE | TEST_FILE_MODE, mountableFile.getFileMode());
    }

    @Test
    public void forHostFilePathWithPermission() throws Exception {
        final Path file = createTempFile("somepath");
        final MountableFile mountableFile = MountableFile.forHostPath(file.toString(), TEST_FILE_MODE);
        performChecks(mountableFile);
        assertEquals("Valid file mode.", BASE_FILE_MODE | TEST_FILE_MODE, mountableFile.getFileMode());
    }

    @Test
    public void forHostDirPathWithPermission() throws Exception {
        final Path dir = createTempDir();
        final MountableFile mountableFile = MountableFile.forHostPath(dir.toString(), TEST_FILE_MODE);
        performChecks(mountableFile);
        assertEquals("Valid dir mode.", BASE_DIR_MODE | TEST_FILE_MODE, mountableFile.getFileMode());
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @NotNull
    private Path createTempFile(final String name) throws IOException {
        final File tempParentDir = File.createTempFile("testcontainers", "");
        tempParentDir.delete();
        tempParentDir.mkdirs();
        final Path file = new File(tempParentDir, name).toPath();

        Files.copy(MountableFileTest.class.getResourceAsStream("/mappable-resource/test-resource.txt"), file);
        return file;
    }

    @NotNull
    private Path createTempDir() throws IOException {
        return Files.createTempDirectory("testcontainers");
    }

    private void performChecks(final MountableFile mountableFile) {
        final String mountablePath = mountableFile.getResolvedPath();
        assertTrue("The filesystem path '" + mountablePath + "' can be found", new File(mountablePath).exists());
        assertFalse("The filesystem path '" + mountablePath + "' does not contain any URL escaping", mountablePath.contains("%20"));
    }

}
