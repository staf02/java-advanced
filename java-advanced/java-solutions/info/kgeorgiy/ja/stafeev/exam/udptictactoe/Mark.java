package info.kgeorgiy.ja.stafeev.exam.udptictactoe;

public enum Mark {
    X,
    O,
    Empty;

    @Override
    public String toString() {
        return switch (this) {
            case Empty -> ".";
            case O -> "O";
            case X -> "X";
        };
    }
}
