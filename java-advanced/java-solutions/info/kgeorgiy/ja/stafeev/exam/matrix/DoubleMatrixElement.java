package info.kgeorgiy.ja.stafeev.exam.matrix;

public class DoubleMatrixElement extends AbstarctMatrixElement<Double> {
    DoubleMatrixElement(final Double data) {
        super(data);
    }

    @Override
    public MatrixElement<Double> add(MatrixElement<Double> other) {
        return new DoubleMatrixElement(data + other.get());
    }

    @Override
    public MatrixElement<Double> multiply(MatrixElement<Double> other) {
        return new DoubleMatrixElement(data * other.get());
    }
}
