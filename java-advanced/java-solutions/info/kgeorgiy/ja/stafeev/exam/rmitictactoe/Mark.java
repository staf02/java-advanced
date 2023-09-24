package info.kgeorgiy.ja.stafeev.exam.rmitictactoe;

public enum Mark {
    X,
    O,
    Empty;

    @Override
    public String toString() {
        return switch (this) {
            case Empty -> ".";
            case O -> "0";
            case X -> "X";
        };
    }
}
