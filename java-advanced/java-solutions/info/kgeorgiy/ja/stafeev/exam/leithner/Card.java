package info.kgeorgiy.ja.stafeev.exam.leithner;

import java.util.Objects;

public class Card {
    private final String word;
    private final String translate;

    Card(final String word, final String translate) {
        this.word = word;
        this.translate = translate;
    }

    public String getWord() {
        return word;
    }

    public String getTranslate() {
        return translate;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Card card = (Card) o;
        return Objects.equals(word, card.word) && Objects.equals(translate, card.translate);
    }
}
