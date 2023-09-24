package info.kgeorgiy.ja.stafeev.exam.tracingproxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;

public class HandlerWithDepth implements InvocationHandler {

    final int depth;
    final Object instance;
    final Object impl;

    HandlerWithDepth(final int depth, final Object instance) {
        this.depth = depth;
        this.instance = instance;
        if (depth == 0) {
            this.impl = instance;
        } else {
            this.impl = new HandlerWithDepth(depth - 1, instance);
        }
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws InvocationTargetException, IllegalAccessException {
        try {
            if (depth == 0) {
                return method.invoke(instance, args);
            }
            System.out.println("Calling " + method.getName() + "(" + Arrays.toString(args) + ") with depth = " + depth);
            return ((HandlerWithDepth) impl).invoke(proxy, method, args);
        } catch (final InvocationTargetException e) {
            throw new InvocationTargetException(e.getCause());
        }
    }
}