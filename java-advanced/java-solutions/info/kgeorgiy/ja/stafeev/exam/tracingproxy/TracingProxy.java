package info.kgeorgiy.ja.stafeev.exam.tracingproxy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;

public class TracingProxy {
    public static <T> T newInstance(final int depth, final ClassLoader classLoader, final Class<?>[] interfaces, final T instanceFrom) {
        return (T) Proxy.newProxyInstance(classLoader, interfaces, new HandlerWithDepth(depth, instanceFrom));
    }

    public static void main(final String[] args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Printer testClass = new TestClass();
        Printer instance = newInstance(5, TestClass.class.getClassLoader(), TestClass.class.getInterfaces(), testClass);
        instance.print();
    }
}