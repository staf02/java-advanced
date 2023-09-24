package info.kgeorgiy.ja.stafeev.exam.matrix;

public class LongMatrixElement extends AbstarctMatrixElement<Long> {

    LongMatrixElement(final Long data) {
        super(data);
    }

    @Override
    public MatrixElement<Long> add(final MatrixElement<Long> other) {
        return new LongMatrixElement(data + other.get());
    }

    @Override
    public MatrixElement<Long> multiply(final MatrixElement<Long> other) {
        return new LongMatrixElement(data * other.get());
    }
}
