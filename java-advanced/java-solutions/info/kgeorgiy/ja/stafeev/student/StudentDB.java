package info.kgeorgiy.ja.stafeev.student;
import info.kgeorgiy.java.advanced.student.Group;
import info.kgeorgiy.java.advanced.student.GroupName;
import info.kgeorgiy.java.advanced.student.Student;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

public class StudentDB implements info.kgeorgiy.java.advanced.student.AdvancedQuery {

    //Comparators
    private final static Comparator<Student> STUDENT_BY_NAME =
        Comparator.comparing(Student::getLastName)
            .thenComparing(Student::getFirstName)
            .reversed()
            .thenComparingInt(Student::getId);

    private final static Comparator<Group> GROUP_BY_NAME = Comparator.comparing(Group::getName);

    private static <T, U> Set<Map.Entry<T, Integer>> groupBy(Collection<Student> students, Function<Student, T> getKey,
                                                             Function<Student, U> getValue) {
        return students.stream()
            .collect(Collectors.groupingBy(getKey, Collectors.mapping(getValue,
                Collectors.collectingAndThen(Collectors.toSet(), Set::size))))
            .entrySet();
    }

    private static <T, U> T getMinElement(Collection<U> s, Comparator<U> cmp, Function<U, T> getter, T defaultValue) {
        return s.stream()
            .min(cmp)
            .map(getter)
            .orElse(defaultValue);
    }

    // :NOTE: можно повыносить больше
    @Override
    public String getMostPopularName(Collection<Student> students) {
        return getMinElement(groupBy(students, Student::getFirstName, Student::getGroup),
            Map.Entry.<String, Integer>comparingByValue().reversed().thenComparing(Map.Entry::getKey),
            Map.Entry::getKey,
            "");
    }

    @Override
    public GroupName getLargestGroupFirstName(Collection<Student> students) {
        return getMinElement(groupBy(students, Student::getGroup, Student::getFirstName),
            Comparator.comparing(Map.Entry<GroupName, Integer>::getValue).reversed().thenComparing(Map.Entry::getKey),
            Map.Entry::getKey,
            null);
    }

    private static <T> List<T> getDataById(Collection<Student> students, Function<Student, T> getField, int[] ids) {
        return Arrays.stream(ids)
            .mapToObj(students.stream().collect(Collectors.groupingBy(Student::getId))::get)
            .flatMap(Collection::stream)
            .map(getField)
            .toList();
    }

    @Override
    public List<String> getFirstNames(Collection<Student> students, int[] ids) {
        return getDataById(students, Student::getFirstName, ids);
    }

    @Override
    public List<String> getLastNames(Collection<Student> students, int[] ids) {
        return getDataById(students, Student::getLastName, ids);
    }

    @Override
    public List<GroupName> getGroups(Collection<Student> students, int[] ids) {
        return getDataById(students, Student::getGroup, ids);
    }

    private static String getFullName(Student student) {
        return student.getFirstName() + " " + student.getLastName();
    }

    @Override
    public List<String> getFullNames(Collection<Student> students, int[] ids) {
        return getDataById(students, StudentDB::getFullName, ids);
    }

    private List<Group> getGroupsBy(Collection<Student> students, Comparator<Student> comparator) {
        return students.stream()
            .sorted(comparator)
            .collect(Collectors.groupingBy(Student::getGroup, Collectors.toList()))
            .entrySet()
            .stream()
            .map(pair -> new Group(pair.getKey(), pair.getValue()))
            .sorted(GROUP_BY_NAME)
            .toList();
    }

    @Override
    public List<Group> getGroupsByName(Collection<Student> students) {
        return getGroupsBy(students, STUDENT_BY_NAME);
    }

    @Override
    public List<Group> getGroupsById(Collection<Student> students) {
        return getGroupsBy(students, Comparator.naturalOrder());
    }
    @Override
    public GroupName getLargestGroup(Collection<Student> students) {
        return getMinElement(getGroupsByName(students),
            Comparator.comparingInt((Group group) -> group.getStudents().size())
                .thenComparing(GROUP_BY_NAME).reversed(),
            Group::getName, null);
    }

    private static <T> List<T> getData(List<Student> students, Function<Student, T> getter) {
        return students.stream().map(getter).toList();
    }

    @Override
    public List<String> getFirstNames(List<Student> students) {
        return getData(students, Student::getFirstName);
    }

    @Override
    public List<String> getLastNames(List<Student> students) {
        return getData(students, Student::getLastName);
    }

    @Override
    public List<GroupName> getGroups(List<Student> students) {
        return getData(students, Student::getGroup);
    }

    @Override
    public List<String> getFullNames(List<Student> students) {
        return getData(students, StudentDB::getFullName);
    }

    @Override
    public Set<String> getDistinctFirstNames(List<Student> students) {
        return students.stream().map(Student::getFirstName).collect(Collectors.toSet());
    }

    @Override
    public String getMaxStudentFirstName(List<Student> students) {
        return getMinElement(students, Comparator.<Student>naturalOrder().reversed(), Student::getFirstName, "");
    }


    private static List<Student> sortStudentsBy(Collection<Student> students, Comparator<Student> cmp) {
        return students.stream().sorted(cmp).toList();
    }
    @Override
    public List<Student> sortStudentsById(Collection<Student> students) {
        return sortStudentsBy(students, Comparator.naturalOrder());
    }

    @Override
    public List<Student> sortStudentsByName(Collection<Student> students) {
        return sortStudentsBy(students, STUDENT_BY_NAME);
    }

    // :NOTE: предикаты имеют один вид
    private static <T> List<Student> findStudents(Collection<Student> students, T field, Function<Student, T> getter) {
        return students.stream().filter(student -> field.equals(getter.apply(student))).sorted(STUDENT_BY_NAME).toList();
    }

    @Override
    public List<Student> findStudentsByFirstName(Collection<Student> students, String name) {
        return findStudents(students, name, Student::getFirstName);
    }

    @Override
    public List<Student> findStudentsByLastName(Collection<Student> students, String name) {
        return findStudents(students, name, Student::getLastName);
    }

    @Override
    public List<Student> findStudentsByGroup(Collection<Student> students, GroupName group) {
        return findStudents(students, group, Student::getGroup);
    }

    @Override
    public Map<String, String> findStudentNamesByGroup(Collection<Student> students, GroupName group) {
        return students.stream()
            .filter((Student student) -> group.equals(student.getGroup()))
            .collect(Collectors.toMap(Student::getLastName, Student::getFirstName,
                BinaryOperator.minBy(Comparator.naturalOrder())));
    }
}
