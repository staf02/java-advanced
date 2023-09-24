package info.kgeorgiy.ja.stafeev.exam.matrix;

import java.math.BigInteger;

public class BigIntegerMatrixElement extends AbstarctMatrixElement<BigInteger> {
    BigIntegerMatrixElement(final BigInteger data) {
        super(data);
    }

    @Override
    public MatrixElement<BigInteger> add(MatrixElement<BigInteger> other) {
        return new BigIntegerMatrixElement(data.add(other.get()));
    }

    @Override
    public MatrixElement<BigInteger> multiply(MatrixElement<BigInteger> other) {
        return new BigIntegerMatrixElement(data.multiply(other.get()));
    }
}
