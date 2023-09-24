package info.kgeorgiy.ja.stafeev.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.CodeSource;
import java.util.*;
import java.util.function.Predicate;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.zip.ZipEntry;

/**
 * Implementation of {@link JarImpler} which allows generate {@code .java} code for given parent class.
 *
 * @author Grigorij Stafeev
 * Judge not lest ye be judged
 */
public class Implementor implements JarImpler {

    private static final String END = ";";
    private static final String TAB = "\t";

    private static final Predicate<Class<?>> isPrivate = clazz -> Modifier.isPrivate(clazz.getModifiers());

    private Class<?> token;
    private Path path;

    private final Manifest manifest;

    public Implementor() {
        this.manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        manifest.getMainAttributes().put(Attributes.Name.IMPLEMENTATION_VENDOR, "Grigorius Stafeus");
    }

    private static String concatenate(final List<String> strings) {
        return String.join(System.lineSeparator(), strings);
    }

    private void check(final Class<?> aClass) throws ImplerException {
        if (aClass.isArray()) {
            throw new ImplerException("Cannot implement " + aClass + " : is array");
        }
        if (aClass.isPrimitive()) {
            throw new ImplerException("Cannot implement " + aClass + " : is primitive");
        }
        if (aClass == Enum.class) {
            throw new ImplerException("Cannot implement " + aClass + " : is enum");
        }
        if (Modifier.isPrivate(aClass.getModifiers()) || Modifier.isFinal(aClass.getModifiers())) {
            throw new ImplerException("Class is private of final");
        }
    }

    private String getPackageString() {
        return token.getPackage() + END;
    }

    private String getClassName() {
        return token.getSimpleName() + "Impl";
    }

    private String getClassString() {
        return "class " + getClassName() + " " + (token.isInterface() ? "implements " : "extends ") + token.getCanonicalName();
    }

    private String getReturnValue(final Class<?> returnValue) throws ImplerException {
        if (isPrivate.test(returnValue)) {
            throw new ImplerException("Cannot override method with private return type");
        }
        if (returnValue == boolean.class) {
            return "false";
        }
        if (returnValue.isPrimitive()) {
            return "0";
        }
        return "null";
    }

    private String getMethodBody(final Method method) throws ImplerException {
        final Class<?> returnValue = method.getReturnType();
        if (returnValue == void.class) {
            return "";
        }
        return "return " + getReturnValue(returnValue) + END;
    }

    private String getArgsString(final Executable executable) throws ImplerException {
        if (Arrays.stream(executable.getParameterTypes()).anyMatch(isPrivate)) {
            throw new ImplerException("");
        }
        return "(" + IntStream.range(0, executable.getParameterCount())
                .mapToObj(i -> executable.getParameterTypes()[i].getCanonicalName() + " arg" + i)
                .collect(Collectors.joining(", ")) + ")";
    }

    private String getExceptionsString(final Executable executable) {
        final Class<?>[] exceptions = executable.getExceptionTypes();
        if (exceptions.length == 0) {
            return "";
        }
        return " throws " + Arrays.stream(exceptions).map(Class::getCanonicalName).collect(Collectors.joining(", "));
    }

    private String getMethodSignature(final Method method) throws ImplerException {
        return Modifier.toString(method.getModifiers() & ~Modifier.ABSTRACT & ~Modifier.TRANSIENT)
                + " " + method.getReturnType().getCanonicalName()
                + " " + method.getName() +
                getArgsString(method) + getExceptionsString(method);
    }

    private String getMethodString(final Method method) throws ImplerException {
        return concatenate(List.of(
                TAB + getMethodSignature(method) + " {",
                TAB + TAB + getMethodBody(method),
                TAB + "}"
        ));
    }

    private record MethodHolder(Method method) implements Comparable<MethodHolder> {

        /**
         * Creates directories for the given path.
         *
         * @return holded method
         */
        Method getMethod() {
            return method;
        }

        @Override
        public int hashCode() {
            return Objects.hash(Arrays.hashCode(method.getParameterTypes()), method.getName());
        }

        @Override
        public int compareTo(MethodHolder o) {
            if (Objects.equals(method.getReturnType(), o.method.getReturnType())) {
                return 0;
            } else if (method.getReturnType().isAssignableFrom(o.method.getReturnType())) {
                return 1;
            }
            return -1;
        }
    }

    private static List<MethodHolder> filterMethods(final Method[] methods) {
        return Arrays.stream(methods)
                .filter(method -> Modifier.isAbstract(method.getModifiers()) || Modifier.isFinal(method.getModifiers()))
                .map(MethodHolder::new)
                .toList();
    }

    private String getMethodsString() throws ImplerException {
        final Set<MethodHolder> result = new HashSet<>(filterMethods(token.getMethods()));
        Class<?> clazz = token;
        do {
            result.addAll(filterMethods(clazz.getDeclaredMethods()));
        } while ((clazz = clazz.getSuperclass()) != null);
        final List<Method> methods = result.stream()
                .collect(Collectors.groupingBy(MethodHolder::hashCode))
                .values()
                .stream().map(methodHolders -> {
                    Collections.sort(methodHolders);
                    return methodHolders.get(0);
                })
                .map(MethodHolder::getMethod)
                .filter(method -> !Modifier.isFinal(method.getModifiers()))
                .toList();
        final List<String> toConcatenate = new ArrayList<>();
        for (final Method method : methods) {
            toConcatenate.add(getMethodString(method));
        }
        return concatenate(toConcatenate);
    }

    private String getConstructorSignature(final Constructor<?> constructor) throws ImplerException {
        return Modifier.toString(constructor.getModifiers() & ~Modifier.ABSTRACT & ~Modifier.TRANSIENT)
                + " " + getClassName() +
                getArgsString(constructor) + getExceptionsString(constructor);
    }

    private String getConstructorBody(final Constructor<?> constructor) {
        return "super" + "(" +
                IntStream.range(0, constructor.getParameterCount()).mapToObj(i -> "arg" + i)
                        .collect(Collectors.joining(", "))
                + ")" + END;
    }

    private String getConstructorString(final Constructor<?> constructor) throws ImplerException {
        return concatenate(List.of(
                TAB + getConstructorSignature(constructor) + " {",
                TAB + TAB + getConstructorBody(constructor),
                TAB + "}"
        ));
    }

    private String getConstructorsString() throws ImplerException {
        if (token.isInterface()) {
            return "";
        }
        boolean hasDefault = Arrays.stream(token.getDeclaredConstructors())
                .anyMatch(constructor -> constructor.getParameterTypes().length == 0);
        boolean hasNotPrivate = Arrays.stream(token.getDeclaredConstructors())
                .anyMatch(constructor -> !Modifier.isPrivate(constructor.getModifiers()));
        if (hasDefault && !hasNotPrivate) {
            throw new ImplerException("Cannot implement class constructors");
        }
        final List<Constructor<?>> constructors = Arrays.stream(token.getDeclaredConstructors())
                .filter(constructor -> !Modifier.isPrivate(constructor.getModifiers())).toList();
        final List<String> toConcatenate = new ArrayList<>();
        for (final Constructor<?> constructor : constructors) {
            toConcatenate.add(getConstructorString(constructor));
        }
        return concatenate(toConcatenate);
    }

    /**
     * Produces code implementing class or interface specified by provided {@code token}.
     * <p>
     * Generated class' name should be the same as the class name of the type token with {@code Impl} suffix
     * added. Generated source code should be placed in the correct subdirectory of the specified
     * {@code root} directory and have correct file name. For example, the implementation of the
     * interface {@link java.util.List} should go to {@code $root/java/util/ListImpl.java}
     *
     *
     * @param token type token to create implementation for.
     * @param path root directory.
     * @throws info.kgeorgiy.java.advanced.implementor.ImplerException when implementation cannot be
     * generated.
     */
    @Override
    public void implement(final Class<?> token, final Path path) throws ImplerException {
        check(token);

        this.token = token;
        this.path = path;

        final String code = concatenate(List.of(
                getPackageString(),
                getClassString() + " {",
                getConstructorsString(),
                getMethodsString(),
                "}")
        );
        writeCode(code);
    }

    private void createDirectories(final Path path) throws ImplerException {
        final Path parentPath = path.getParent();
        if (parentPath != null) {
            try {
                Files.createDirectories(parentPath);
            } catch (final FileAlreadyExistsException e) {
                throw new ImplerException("Invalid path: the directory name exists but is not a directory.", e);
            } catch (final IOException ignored) {
                // Nothing happens right now.
            }
        }
    }

    private void writeCode(final String code) throws ImplerException {
        Path outputPath = getJavaPathFromRoot();
        createDirectories(outputPath);
        try (final BufferedWriter writer = Files.newBufferedWriter(outputPath)) {
            try {
                for (char c : code.toCharArray()) {
                    writer.write(c < 128 ? String.valueOf(c) : String.format("\\u%04x", (int) c));
                }
            } catch (IOException e) {
                throw new ImplerException(e.getMessage());
            }

        } catch (final IOException e) {
            throw new ImplerException("Cannot open file : " + e.getMessage());
        }
    }

    private Path createTempOutputPath(final Path root) throws ImplerException {
        createDirectories(root);
        try {
            return Files.createTempDirectory(root.toAbsolutePath().getParent(), "tempImpl");
        } catch (IOException e) {
            throw new ImplerException("Can't create temp directory.", e);
        }
    }

    private static final SimpleFileVisitor<Path> DELETE_VISITOR = new SimpleFileVisitor<>() {
        @Override
        public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
            Files.delete(file);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) throws IOException {
            Files.delete(dir);
            return FileVisitResult.CONTINUE;
        }
    };

    private void deleteTempOutputPath(final Path path) {
        try {
            if (Files.exists(path)) {
                Files.walkFileTree(path, DELETE_VISITOR);
            }
        } catch (final IOException e) {
            System.err.println("Cannot delete temp folder");
        }
    }

    protected String getJarPath() {
        return token.getPackageName().replace('.', '/') + '/' + getClassName() + ".class";
    }
    private Path getJarPathFromRoot() {
        return path.resolve(getJarPath());
    }

    protected String getJavaPath() {
        return token.getPackageName().replace('.', '/') + '/' + getClassName() + ".java";
    }
    private Path getJavaPathFromRoot() {
        return path.resolve(getJavaPath());
    }

    private void compile() throws ImplerException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new ImplerException("Compilation error: Can't find java compiler.");
        }
        try {
            CodeSource classPathSource = token.getProtectionDomain().getCodeSource();
            int returnCode;
            if (classPathSource != null) {
                returnCode = compiler.run(null, null, null, "-cp",
                        Paths.get(classPathSource.getLocation().toURI()).toString(),
                        getJavaPathFromRoot().toString());
            } else {
                returnCode = compiler.run(null, null, null,
                        getJavaPathFromRoot().toString());
            }
            if (returnCode != 0) {
                throw new ImplerException("Compilation error: code can't be compiled. Status code: " + returnCode);
            }
        } catch (URISyntaxException e) {
            throw new ImplerException("Compilation error: Can't fetch additional resources.", e);
        }
    }

    private void createJar(final Path root) throws ImplerException {
        try (final JarOutputStream jar = new JarOutputStream(Files.newOutputStream(root), manifest)) {
            jar.putNextEntry(new ZipEntry(getJarPath()));
            Files.copy(Paths.get(getJarPathFromRoot().toString()), jar);
        } catch (IOException e) {
            throw new ImplerException("Can't create JAR.", e);
        }
    }

    /**
     * Produces <var>.jar</var> file implementing class or interface specified by provided <var>token</var>.
     * <p>
     * Generated class' name should be the same as the class name of the type token with <var>Impl</var> suffix
     * added.
     *
     * @param token type token to create implementation for.
     * @param path target <var>.jar</var> file.
     * @throws ImplerException when implementation cannot be generated.
     */
    @Override
    public void implementJar(final Class<?> token, final Path path) throws ImplerException {
        check(token);

        Path tempPath = createTempOutputPath(path);
        try {
            implement(this.token, tempPath);
            compile();
            createJar(path);
        } finally {
            deleteTempOutputPath(tempPath);
        }
    }

    private static Class<?> getClass(final String name) throws ImplerException {
        try {
            return Class.forName(name);
        } catch (final ClassNotFoundException e) {
            throw new ImplerException("Class not found : " + e.getMessage());
        }
    }

    private static Path tryGetPath(final String path) throws ImplerException {
        try {
            return Path.of(path);
        } catch (final InvalidPathException e) {
            throw new ImplerException("Invalid path : " + e.getMessage());
        }
    }
    public static void main(final String[] args) {
        if (args == null || args.length < 2 || args.length > 3 || (args.length == 2 && !args[0].equals("jar"))) {
            System.err.println("Invalid number of args: set -jar flag [optional], class token and root path.");
        } else {
            try {
                if (args.length == 2) {
                    new Implementor().implement(getClass(args[0]), tryGetPath(args[1]));
                } else {
                    new Implementor().implementJar(getClass(args[1]), tryGetPath(args[2]));
                }
            } catch (ImplerException e) {
                System.err.println(e.getMessage());
            }
        }
    }
}