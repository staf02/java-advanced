package info.kgeorgiy.ja.stafeev.exam.pluginmanager;

import info.kgeorgiy.ja.stafeev.utils.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

public class Plugin implements Runnable {

    private final String className;
    private final String classPath;
    private Runnable innerRunnable;

    public Plugin(String className, String classPath) {
        this.className = className;
        this.classPath = classPath;
    }

    public void load(final Logger logger) throws PluginException {
        final Path path;
        try {
            // :NOTE: * Classpath is not valid path
            path = Path.of(classPath);
        } catch (final InvalidPathException e) {
            throw new PluginException(logger.makeMessage("incorrect-classpath", e));
        }
        final URL url;
        try {
            url = path.toUri().toURL();
        } catch (MalformedURLException e) {
            throw new PluginException(logger.makeMessage("incorrect-classpath", e));
        }
        final ClassLoader cl = new URLClassLoader(new URL[]{url});
        final Class<?> c;
        try {
            c = cl.loadClass(className);
        } catch (final ClassNotFoundException e) {
            throw new PluginException(logger.makeMessage("class-not-found-message", e));
        }
        final Constructor<?> constructor;
        try {
            constructor = c.getConstructor();
        } catch (final NoSuchMethodException e) {
            throw new PluginException(logger.makeMessage("ctor-load-error", e));
        }
        try {
            if ((Runnable.class).isAssignableFrom(c)) {
                innerRunnable = (Runnable) constructor.newInstance();
            } else {
                throw new PluginException(logger.makeMessage("class-is-not-runnable"));
            }
        } catch (final InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new PluginException(logger.makeMessage("ctor-load-error", e));
        }
    }

    @Override
    public void run() {
        innerRunnable.run();
    }
}
