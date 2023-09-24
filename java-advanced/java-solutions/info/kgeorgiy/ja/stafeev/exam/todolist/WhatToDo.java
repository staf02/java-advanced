package info.kgeorgiy.ja.stafeev.exam.todolist;

public class WhatToDo {
    private final String what;
    private boolean isDone;

    public WhatToDo(final String what) {
        this.what = what;
        this.isDone = false;
    }

    public WhatToDo(final String what, final boolean isDone) {
        this.what = what;
        this.isDone = isDone;
    }

    public boolean isDone() {
        return isDone;
    }

    public void setDone() {
        this.isDone = true;
    }

    private String getDoneOrNot() {
        return isDone ? "1" : "0";
    }

    @Override
    public String toString() {
        return String.format("%s [%s]", what, getDoneOrNot());
    }
}
