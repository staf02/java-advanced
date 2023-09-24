package info.kgeorgiy.ja.stafeev.exam.matrix;

public abstract class AbstarctMatrixElement<T extends Number> implements MatrixElement<T> {

    T data;

    AbstarctMatrixElement(final T data) {
        this.data = data;
    }

    public T get() {
        return data;
    }

    public void set(final T other) {
        this.data = other;
    }
}
