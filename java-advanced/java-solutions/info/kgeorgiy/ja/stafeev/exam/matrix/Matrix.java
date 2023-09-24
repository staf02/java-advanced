package info.kgeorgiy.ja.stafeev.exam.matrix;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Matrix<T extends Number> {

    final List<List<MatrixElement<T>>> data;
    final int n, m;

    boolean transposed;

    Matrix(int n, int m) {
        this.data = new ArrayList<>(Collections.nCopies(n, new ArrayList<>(Collections.nCopies(m, null))));
        this.n = n;
        this.m = m;
        transposed = false;
    }

    public MatrixElement<T> get(final int i, final int j) {
        if (transposed) {
            return data.get(j).get(i);
        } else {
            return data.get(i).get(j);
        }
    }

    public void set(final int i, final int j, final MatrixElement<T> value) {
        if (transposed) {
            data.get(j).set(i, value);
        } else {
            data.get(i).set(j, value);
        }
    }

    public int getN() {
        if (transposed) {
            return m;
        }
        return n;
    }

    public int getM() {
        if (transposed) {
            return n;
        }
        return m;
    }

    public Matrix<T> add(final Matrix<T> other) {
        if (getM() != other.getM() && getN() != other.getN()) {
            throw new IllegalArgumentException("");
        }
        for (int i = 0; i < getN(); i++) {
            for (int j = 0; j < getM(); j++) {
                set(i, j, get(i, j).add(other.get(i, j)));
            }
        }
        return this;
    }

    public Matrix<T> multiply(final Matrix<T> other) {
        if (getM() != other.getN()) {
            throw new IllegalArgumentException("");
        }
        int k = getM();
        for (int i = 0; i < k; i++) {
            for (int j = 0; j < k; j++) {
                for (int l = 0; l < k; l++) {
                    set(i, j, get(i, j).multiply(other.get(k, j)));
                }
            }
        }
        return this;
    }

    public void transpose() {
        transposed = !transposed;
    }
}
