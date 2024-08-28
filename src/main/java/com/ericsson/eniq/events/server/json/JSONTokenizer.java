package com.ericsson.eniq.events.server.json;

import java.io.*;

/**
 * REVISIT: to be replaced by JSON API such as jettison or jackson
 * 
 * Package scope internal class
 * 
 * A JSONTokenizer takes a source string and extracts characters and tokens from it.
 * 
 * @see JSONObject
 * @see JSONArray
 */
@SuppressWarnings("PMD.CyclomaticComplexity")
public class JSONTokenizer {

    private int character;

    private boolean eof;

    private int index; //NOPMD - erroneous PMD warning

    private int line; //NOPMD - erroneous PMD warning

    private char previous;

    private boolean usePrevious;

    private final Reader reader;

    /**
     * Construct a JSONTokenizer from a string.
     * 
     * @param s source JSON string.
     */
    public JSONTokenizer(final String s) {
        this.reader = new StringReader(s);
        this.eof = false;
        this.usePrevious = false;
        this.previous = 0;
        this.index = 0;
        this.character = 1;
        this.line = 1;
    }

    /**
     * Back up one character. This provides a sort of lookahead capability, so that you can test for a digit or letter before attempting to parse the
     * next number or identifier.
     * 
     * @throws JSONException if backtrack error occurs
     */
    public void back() throws JSONException {
        if (usePrevious || (index <= 0)) {
            throw new JSONException("Stepping back two steps is not supported");
        }
        this.index -= 1;
        this.character -= 1;
        this.usePrevious = true;
        this.eof = false;
    }

    public boolean end() {
        return eof && !usePrevious;
    }

    /**
     * Get the next character in the source string.
     * 
     * @return The next character, or 0 if past the end of the source string.
     * @throws JSONException if input prematurely exhausted
     */
    public char next() throws JSONException {
        int c;
        if (this.usePrevious) {
            this.usePrevious = false;
            c = this.previous;
        } else {
            try {
                c = this.reader.read();
            } catch (final IOException exception) {
                throw new JSONException(exception);
            }

            if (c <= 0) { // End of stream
                this.eof = true;
                c = 0;
            }
        }
        this.index++;
        if (this.previous == '\r') {
            this.line++;
            this.character = c == '\n' ? 0 : 1;
        } else if (c == '\n') {
            this.line++;
            this.character = 0;
        } else {
            this.character++;
        }
        this.previous = (char) c;
        return this.previous;
    }

    /**
     * Get the next char in the string, skipping whitespace.
     * 
     * @throws JSONException if stream prematurely exhausted
     * @return A character, or 0 if there are no more characters.
     */
    public char nextClean() throws JSONException {
        for (;;) {
            final char c = next();
            if ((c == 0) || (c > ' ')) {
                return c;
            }
        }
    }

    /**
     * Return the characters up to the next close quote character. Backslash processing is done. The formal JSON format does not allow strings in
     * single quotes, but an implementation is allowed to accept them.
     * 
     * @param quote The quoting character, either " or '
     * @return A String.
     * @throws JSONException Unterminated string.
     */
    private String nextString(final char quote) throws JSONException {
        char c;
        final StringBuilder sb = new StringBuilder();
        for (;;) {
            c = next();
            switch (c) {
                case 0:
                case '\n':
                case '\r':
                    throw syntaxError("Unterminated string");
                case '\\':
                    c = next();
                    switch (c) {
                        case 'b':
                            sb.append('\b');
                            break;
                        case 't':
                            sb.append('\t');
                            break;
                        case 'n':
                            sb.append('\n');
                            break;
                        case 'f':
                            sb.append('\f');
                            break;
                        case 'r':
                            sb.append('\r');
                            break;
                        case 'u':
                            sb.append((char) Integer.parseInt(next(4), 16));
                            break;
                        case '"':
                        case '\'':
                        case '\\':
                        case '/':
                            sb.append(c);
                            break;
                        default:
                            throw syntaxError("Illegal escape.");
                    }
                    break;
                default:
                    if (c == quote) {
                        return sb.toString();
                    }
                    sb.append(c);
            }
        }
    }

    /**
     * Get the next n characters.
     * 
     * @param n The number of characters to take.
     * @return A string of n characters.
     * @throws JSONException Substring bounds error if there are not n characters remaining in the source string.
     */
    private String next(final int n) throws JSONException {
        String result;
        if (n == 0) {
            result = "";
        } else {
            final char[] buffer = new char[n];
            int pos = 0;
            while (pos < n) {
                buffer[pos++] = next();
                if (end()) {
                    throw syntaxError("Substring bounds error");
                }
            }
            result = new String(buffer);
        }

        return result;
    }

    /**
     * Get the next value. The value can be a JSONArray, JSONObject, String or the JSONObject.NULL_OBJECT.
     * 
     * @return An object.
     * @throws JSONException If syntax error.
     */
    protected Object nextValue() throws JSONException {
        char c = nextClean();

        Object rv = null;

        switch (c) {
            case '"':
            case '\'':
                rv = nextString(c);
                break;
            case '{':
                back();
                rv = createJSONObject();
                break;
            case '[':
            case '(':
                back();
                rv = createJSONArray();
                break;
            default:
                break;
        }

        if (rv == null) {
            /*
             * Handle unquoted text. This could be the values true, false, or null, or it can be a number. An implementation (such as this one) is
             * allowed to also accept non-standard forms.
             * 
             * Accumulate characters until we reach the end of the text or a formatting character.
             */

            final StringBuilder sb = new StringBuilder();
            while ((c >= ' ') && (",:]}/\\\"[{;=#".indexOf(c) < 0)) {
                sb.append(c);
                c = next();
            }
            back();

            final String s = sb.toString().trim();
            if ("".equals(s)) {
                throw syntaxError("Missing value");
            }
            rv = JSONObject.stringToValue(s);
        }

        return rv;
    }

    /**
     * Extracted in order to inject more formatting when printing json results in tests See subclass for more details
     * 
     * @return new json array object
     */
    protected JSONArray createJSONArray() throws JSONException {
        return new JSONArray(this);
    }

    /**
     * Extracted in order to inject more formatting when printing json results in tests See subclass for more details
     * 
     * @return new json object
     */
    protected JSONObject createJSONObject() throws JSONException {
        return new JSONObject(this);
    }

    /**
     * Make a JSONException to signal a syntax error.
     * 
     * @param message The error message.
     * @return A JSONException object, suitable for throwing
     */
    public JSONException syntaxError(final String message) {
        return new JSONException(message + toString());
    }

    /**
     * Make a printable string of this JSONTokener.
     * 
     * @return " at {index} [character {character} line {line}]"
     */
    @Override
    public String toString() {
        return " at " + index + " [character " + this.character + " line " + this.line + "]";
    }
}