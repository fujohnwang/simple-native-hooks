package org.simplenativehooks.staticResources;

import java.io.File;
import java.util.concurrent.atomic.AtomicReference;

import org.simplenativehooks.NativeHookInitializer;
import org.simplenativehooks.utilities.FileUtility;
import org.simplenativehooks.utilities.OSIdentifier;

public class NativeHookBootstrapResources extends AbstractBootstrapResource {

    /**
     * If we would like to customize the extract location, set this public variable.
     */
    public static final AtomicReference<File> nativeHookExtractFSDir = new AtomicReference<File>();

    /**
     * Change and extend to allow custom prefix file system root dir.
     *
     * @return
     */
    public static File getNativeHookDirectory() {
        File rootDir = nativeHookExtractFSDir.get();
        File nativeHookDir = null;
        if (rootDir == null) {
            nativeHookDir = new File(FileUtility.joinPath("resources", "nativehooks", getOSDir()));
        } else {
            nativeHookDir = new File(rootDir, FileUtility.joinPath("resources", "nativehooks", getOSDir()));
        }
        logger.info("getNativeHookDirectory with rootDir=" + rootDir);
        logger.info("getNativeHookDirectory with final result dir=" + nativeHookDir);
        return nativeHookDir;
    }

    /**
     * ADDED by darren
     *
     * @return
     */
    public static String getNativeHookExecutableName() {
        String file = "";

        if (OSIdentifier.IS_WINDOWS) {
            file = "RepeatHook.exe";
        }
        if (OSIdentifier.IS_LINUX) {
            file = "RepeatHook.out";
        }
        if (OSIdentifier.IS_OSX) {
            file = "RepeatHook.out";
        }
        return file;
    }

    public static File getNativeHookExecutable() {
        File exec = new File(FileUtility.joinPath(getNativeHookDirectory().getAbsolutePath(), getNativeHookExecutableName()));
        logger.info("native hook exec: " + exec);
        return exec;
    }

    @Override
    protected boolean postProcessing(String name) {
        if (OSIdentifier.IS_LINUX) {
            if (NativeHookInitializer.USE_X11_ON_LINUX) {
                if (name.endsWith("RepeatHookX11Key.out") || name.endsWith("RepeatHookX11Mouse.out")) {
                    return new File(name).setExecutable(true);
                }
            } else if (name.endsWith("RepeatHook.out")) {
                return new File(name).setExecutable(true);
            }
        }
        if (OSIdentifier.IS_OSX && name.endsWith("RepeatHook.out")) {
            return new File(name).setExecutable(true);
        }
        return true;
    }

    @Override
    protected boolean correctExtension(String name) {
        if (OSIdentifier.IS_WINDOWS) {
            return name.endsWith("RepeatHook.exe");
        }
        if (OSIdentifier.IS_LINUX) {
            if (NativeHookInitializer.USE_X11_ON_LINUX) {
                return name.endsWith("RepeatHookX11Key.out") || name.endsWith("RepeatHookX11Mouse.out");
            } else {
                return name.endsWith("RepeatHook.out");
            }
        }
        if (OSIdentifier.IS_OSX) {
            return name.endsWith("RepeatHook.out");
        }
        throw new IllegalStateException("OS is unsupported.");
    }

    @Override
    protected String getRelativeSourcePath() {
        return "org/simplenativehooks/" + getOSDir() + "/nativecontent";
    }

    @Override
    protected File getExtractingDest() {
        File rootDir = nativeHookExtractFSDir.get();
        if (rootDir == null) {
            return new File(FileUtility.joinPath("resources", "nativehooks", getOSDir()));
        } else {
            return new File(rootDir, FileUtility.joinPath("resources", "nativehooks", getOSDir()));
        }
    }

    @Override
    protected String getName() {
        return "NativeHook";
    }

    private static String getOSDir() {
        if (OSIdentifier.IS_WINDOWS) {
            return "windows";
        } else if (OSIdentifier.IS_LINUX) {
            if (NativeHookInitializer.USE_X11_ON_LINUX) {
                return "x11";
            } else {
                return "linux";
            }
        } else if (OSIdentifier.IS_OSX) {
            return "osx";
        }
        throw new IllegalStateException("OS is unsupported.");
    }
}
