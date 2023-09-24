package info.kgeorgiy.ja.stafeev.exam.matrix;

public interface MatrixElement<T extends Number> {
    T get();
    void set(T other);
    MatrixElement<T> add(MatrixElement<T> other);
    MatrixElement<T> multiply(MatrixElement<T> other);
}
