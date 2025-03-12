package org.simplenativehooks.staticResources;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.simplenativehooks.utilities.FileUtility;
import org.simplenativehooks.utilities.Function;
import org.slf4j.LoggerFactory;

public abstract class AbstractBootstrapResource {

    private static final Logger LOGGER = Logger.getLogger(AbstractBootstrapResource.class.getName());

    protected static org.slf4j.Logger logger = LoggerFactory.getLogger(AbstractBootstrapResource.class.getName());

    protected void extractResources() throws IOException, URISyntaxException {
//        logger.info("try to extract native hook resource: " + getExtractingDest().getAbsolutePath());
//        if (!FileUtility.createDirectory(getExtractingDest().getAbsolutePath())) {
//            LOGGER.warning("Failed to extract " + getName() + " resources");
//            return;
//        }

        final String path = getRelativeSourcePath();
        logger.info("getRelativeSourcePath(): " + path);

        /*
         * WHen failed to load resource in this way, I forgot the maven resource package configuration before mvn install.
         */
        String classpathResource = "" + path + "/" + NativeHookBootstrapResources.getNativeHookExecutableName();
        logger.info("load native resource from classpath: " + classpathResource);
        ClassLoader cl = AbstractBootstrapResource.class.getClassLoader();
        InputStream ins = cl.getResourceAsStream(classpathResource);
        if (ins == null) {
            logger.warn("FUCK!!! failed to load classpath resource : " + classpathResource);
            return;
        }
        try {
            logger.info("mkdir if non-exist for local dest location: " + NativeHookBootstrapResources.getNativeHookDirectory().getAbsolutePath());
            if (!NativeHookBootstrapResources.getNativeHookDirectory().exists()) {
                FileUtils.forceMkdir(NativeHookBootstrapResources.getNativeHookDirectory());
            }
            File localHook = new File(NativeHookBootstrapResources.getNativeHookDirectory(), NativeHookBootstrapResources.getNativeHookExecutableName());
            logger.info("copy native hook from classpath to local location: " + localHook);
            FileUtils.copyInputStreamToFile(ins, localHook);

            logger.info("make native hook executable...");
            localHook.setExecutable(true);
        } finally {
            ins.close();
        }


//        logger.info("extract from current jar with arg: (path=" + path + ", dest=" + getExtractingDest() + ")");
//        FileUtility.extractFromCurrentJar(path, getExtractingDest(), new Function<String, Boolean>() {
//            @Override
//            public Boolean apply(String name) {
//                return correctExtension(name);
//            }
//        }, new Function<String, Boolean>() {
//            @Override
//            public Boolean apply(String name) {
//                return postProcessing(name);
//            }
//        });
    }

    protected boolean postProcessing(String name) {
        return true;
    }

    protected abstract boolean correctExtension(String name);

    protected abstract String getRelativeSourcePath();

    protected abstract File getExtractingDest();

    protected abstract String getName();
}
