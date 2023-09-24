package info.kgeorgiy.ja.stafeev.exam.udptictactoe;

import java.util.HashMap;

public class Game {

    private final Mark[][] board;
    private int freeCells = 9;
    Game() {
        board = new Mark[3][3];

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                board[i][j] = Mark.Empty;
            }
        }
    }

    public Mark getActivePlayer() {
        if (freeCells % 2 == 1) {
            return Mark.X;
        } else {
            return Mark.O;
        }
    }

    public boolean free(final int x, final int y) {
        return board[x][y] == Mark.Empty;
    }

    public void set(final int x, final int y, final Mark mark) {
        if (mark == Mark.Empty) {
            return;
        }
        if (free(x, y)) {
            board[x][y] = mark;
            freeCells--;
        }
    }
    public static Game fromString(final String str) {
        Game result = new Game();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                final Mark mark;
                switch (str.charAt(i * 3 + j)) {
                    case 'X' -> mark = Mark.X;
                    case 'O' -> mark = Mark.O;
                    default -> mark = Mark.Empty;
                }
                result.set(i, j, mark);
            }
        }
        return result;
    }

    public String renderGame() {
        final StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                stringBuilder.append(board[i][j]);
            }
            stringBuilder.append(System.lineSeparator());
        }
        return stringBuilder.toString();
    }

    private boolean checkWin(final Mark mark) {
        return board[0][0] == board[0][1] && board[0][1] == board[0][2] && board[0][2] == mark ||
                board[1][0] == board[1][1] && board[1][1] == board[1][2] && board[1][2] == mark ||
                board[2][0] == board[2][1] && board[2][1] == board[2][2] && board[2][2] == mark ||
                board[0][0] == board[1][1] && board[1][1] == board[2][2] && board[2][2] == mark ||
                board[2][0] == board[1][1] && board[1][1] == board[0][2] && board[0][2] == mark ||
                board[0][0] == board[1][0] && board[1][0] == board[2][0] && board[2][0] == mark ||
                board[0][1] == board[1][1] && board[1][1] == board[2][1] && board[2][1] == mark ||
                board[0][2] == board[1][2] && board[1][2] == board[2][2] && board[2][2] == mark;
    }

    public GameResult gameResult() {
        if (checkWin(Mark.X)) {
            return GameResult.X_WIN;
        } else if (checkWin(Mark.O)) {
            return GameResult.O_WIN;
        } else if (freeCells == 0) {
            return GameResult.DRAW;
        } else {
            return GameResult.NOT_FINISHED;
        }
    }

    @Override
    public String toString() {
        final StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                stringBuilder.append(board[i][j]);
            }
        }
        return stringBuilder.toString();
    }
}
