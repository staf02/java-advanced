package info.kgeorgiy.ja.stafeev.exam.classrelations;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ClassRelations {

    final Class<?> firstClass;
    final Class<?> secondClass;

    ClassRelations(final Class<?> firstClass, final Class<?> secondClass) {
        this.firstClass = firstClass;
        this.secondClass = secondClass;
    }

    public boolean checkIdentity() {
        return firstClass == secondClass;
    }

    private static List<Class<?>> getSuperClasses(final Class<?> clazz) {
        final List<Class<?>> superClasses = new ArrayList<>();
        superClasses.add(clazz);
        Class<?> superClass = clazz;
        while (superClass.getSuperclass() != null) {
            superClass = superClass.getSuperclass();
            superClasses.add(superClass);
        }
        return superClasses;
    }

    public List<Class<?>> getCommonParent() {
        final List<Class<?>> aSuperClasses = getSuperClasses(firstClass);
        final List<Class<?>> bSuperClasses = getSuperClasses(secondClass);

        aSuperClasses.retainAll(bSuperClasses);
        return aSuperClasses;
    }

    public boolean isSamePackage() {
        return firstClass.getPackage() == secondClass.getPackage();
    }

    private static Set<Class<?>> getInterfaces(final Class<?> clazz) {
        final Set<Class<?>> interfaces = new HashSet<>(List.of(clazz.getInterfaces()));
        Class<?> superClass = clazz;
        while (superClass.getSuperclass() != null) {
            superClass = superClass.getSuperclass();
            interfaces.addAll(List.of(superClass.getInterfaces()));
        }
        return interfaces;
    }

    public Set<Class<?>> getCommonInterfaces() {
        final Set<Class<?>> aInterfaces = getInterfaces(firstClass);
        final Set<Class<?>> bInterfaces = getInterfaces(secondClass);
        aInterfaces.retainAll(bInterfaces);
        return aInterfaces;
    }

    public static void main(final String[] args) {
        if (args == null || args.length != 2) {
            System.err.println("Invalid args");
            return;
        }
        final Class<?> a, b;
        try {
            a = Class.forName(args[0]);
            b = Class.forName(args[1]);
        } catch (final ClassNotFoundException e) {
            System.err.println("Cannot load class : " + e.getMessage());
            return;
        }
        ClassRelations classRelations = new ClassRelations(a, b);
        if (classRelations.isSamePackage()) {
            System.out.println("Classes " + a + " and " + b + " are from the same package : " + a.getPackage());
        }
        if (classRelations.checkIdentity()) {
            System.out.println("Classes are identical");
        }
        else {
            if (a.isAssignableFrom(b)) {
                System.out.println(a + " is superclass of " + b);
            } else if (b.isAssignableFrom(a)) {
                System.out.println(b + " is superclass of " + a);
            }
        }
        List<Class<?>> commonParents = classRelations.getCommonParent();
        System.out.println("Common superclasses");
        for (final Class<?> commonParent : commonParents) {
            System.out.println(commonParent);
        }
        System.out.println("Common interfaces");
        final Set<Class<?>> commonInterfaces = classRelations.getCommonInterfaces();
        for (final Class<?> commonInterface : commonInterfaces) {
            System.out.println(commonInterface);
        }
    }
}
