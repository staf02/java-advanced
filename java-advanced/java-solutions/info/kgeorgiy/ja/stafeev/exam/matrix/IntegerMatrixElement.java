package info.kgeorgiy.ja.stafeev.exam.matrix;

public class IntegerMatrixElement extends AbstarctMatrixElement<Integer> {
    IntegerMatrixElement(final Integer data) {
        super(data);
    }

    @Override
    public MatrixElement<Integer> add(MatrixElement<Integer> other) {
        return new IntegerMatrixElement(data + other.get());
    }

    @Override
    public MatrixElement<Integer> multiply(MatrixElement<Integer> other) {
        return new IntegerMatrixElement(data * other.get());
    }
}
