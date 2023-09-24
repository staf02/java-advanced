package info.kgeorgiy.ja.stafeev.exam.matrix;

import java.math.BigDecimal;

public class BigDecimalMatrixElement extends AbstarctMatrixElement<BigDecimal> {
    BigDecimalMatrixElement(final BigDecimal data) {
        super(data);
    }

    @Override
    public MatrixElement<BigDecimal> add(MatrixElement<BigDecimal> other) {
        return new BigDecimalMatrixElement(data.add(other.get()));
    }

    @Override
    public MatrixElement<BigDecimal> multiply(MatrixElement<BigDecimal> other) {
        return new BigDecimalMatrixElement(data.multiply(other.get()));
    }
}
