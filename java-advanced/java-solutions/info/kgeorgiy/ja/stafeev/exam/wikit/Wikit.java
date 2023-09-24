package info.kgeorgiy.ja.stafeev.exam.wikit;

import info.kgeorgiy.ja.stafeev.utils.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Wikit {

    private final Logger logger;
    private final URL wikiAddress;
    private final Map<String, String> contents;

    public Wikit(final Logger logger, final URL wikiAddress) {
        this.logger = logger;
        this.wikiAddress = wikiAddress;

        contents = new HashMap<>();
    }

    public String extractData(final String json) {
        final int pos = json.indexOf("\"extract\":");
        if (pos == -1) {
            return "";
        }
        return json.substring(pos + 11, json.indexOf("\"}", pos));
    }

    public void loadArticle(final String article) {
        final String content;
        if (!contents.containsKey(article)) {
            try {
                final URL url = makeSearchQuery(article);
                try (final BufferedReader dis = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {
                    String myString;
                    final StringBuilder result = new StringBuilder();
                    while ((myString = dis.readLine()) != null) {
                        result.append(myString);
                    }
                    content = extractData(result.toString());
                    contents.put(article, content);
                }
            } catch (final URISyntaxException | IOException e) {
                logger.logLocalizedError("cannot-send-request", e);
                return;
            }
        } else {
            content = contents.get(article);
        }
        logger.log(content);
    }

    private static String makeQueryString(final String article) {
        return String.format("action=query&format=json&titles=%s&prop=extracts&exintro=True&explaintext=True", article);
    }
    private URL makeSearchQuery(final String article) throws URISyntaxException, MalformedURLException {
        final URI address = wikiAddress.toURI();
        final String newQuery = makeQueryString(article);
        return new URL(new URI(address.getScheme(), address.getAuthority(),
                address.getPath(), newQuery, address.getFragment()).toString());
    }

    public static void main(final String[] args) {
        if (args == null || args.length == 0) {
            System.err.println("Usage: Wikit <locale> [wiki_address]");
            return;
        }

        final Locale locale = Locale.forLanguageTag(args[0]);
        final ResourceBundle bundle;
        try {
            bundle = ResourceBundle.getBundle("info.kgeorgiy.ja.stafeev.exam.wikit.ResourceBundle", locale);
        } catch (final MissingResourceException e) {
            System.err.println("Supported only en_US and ru_RU");
            return;
        }
        final Logger logger = new Logger(bundle);

        final URL wikiAddress;
        try {
            if (args.length > 1) {
                wikiAddress = new URL(args[1]);
            } else {
                wikiAddress = new URL("https://en.wikipedia.org/w/api.php");
            }
        } catch (final MalformedURLException e) {
            logger.logLocalizedError("bad-wiki", e);
            return;
        }

        final Wikit wikit = new Wikit(logger, wikiAddress);
        final Scanner scanner = new Scanner(System.in);
        logger.logLocalizedMessage("supported-commands");
        String query = scanner.nextLine();
        while (!query.isEmpty()) {
            wikit.loadArticle(query);
            query = scanner.nextLine();
        }
    }
}
