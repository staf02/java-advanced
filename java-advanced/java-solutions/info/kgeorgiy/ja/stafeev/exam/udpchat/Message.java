package info.kgeorgiy.ja.stafeev.exam.udpchat;

public class Message {

    private String userName;
    private final String message;
    public Message(final String text) {
        if (text.startsWith("USER")) {
            this.userName = text.substring(text.indexOf("USER") + 4, text.indexOf(System.lineSeparator()));
            this.message = text.substring(text.indexOf(System.lineSeparator()) + System.lineSeparator().length());
        } else {
            throw new IllegalArgumentException("Bad format");
        }
    }

    void setUserName(final String userName) {
        this.userName = userName;
    }

    String getUserName() {
        return userName;
    }

    public Message(final String to, final String message) {
        this.userName = to;
        this.message = message;
    }

    public String toString() {
        return "USER" + userName + System.lineSeparator() + message;
    }

    public String clientMessage() {
        return userName + "> " + message;
    }
}
