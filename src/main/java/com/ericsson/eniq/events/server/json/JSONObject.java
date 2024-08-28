package com.ericsson.eniq.events.server.json;

import java.util.*;

/**
 * REVISIT: to be replaced by JSON API such as jettison or jackson
 * 
 * A JSONObject is an unordered collection of name/value pairs. Its external form is a string wrapped in curly braces with colons between the names
 * and values, and commas between the values and names. The internal form is an object having <code>get</code> and <code>opt</code> methods for
 * accessing the values by name, and <code>put</code> methods for adding or replacing values by name. The values can be any of these types:
 * <code>Boolean</code>, <code>JSONArray</code>, <code>JSONObject</code>, <code>Number</code>, <code>String</code>, or the
 * <code>JSONObject.NULL</code> object. A JSONObject constructor can be used to convert an external form JSON text into an internal form whose values
 * can be retrieved with the <code>get</code> and <code>opt</code> methods, or to convert values into a JSON text using the <code>put</code> and
 * <code>toString</code> methods. A <code>get</code> method returns a value if one can be found, and throws an exception if one cannot be found. An
 * <code>opt</code> method returns a default value instead of throwing an exception, and so is useful for obtaining optional values.
 * <p/>
 * The generic <code>get()</code> and <code>opt()</code> methods return an object, which you can cast or query for type. There are also typed
 * <code>get</code> and <code>opt</code> methods that do type checking and type coercion for you.
 * <p/>
 * The <code>put</code> methods adds values to an object. For example,
 * 
 * <pre>
 * myString = new JSONObject().put(&quot;JSON&quot;, &quot;Hello, World!&quot;).toString();
 * </pre>
 * 
 * produces the string <code>{"JSON": "Hello, World"}</code>.
 * <p/>
 * The texts produced by the <code>toString</code> methods strictly conform to the JSON syntax rules. The constructors are more forgiving in the texts
 * they will accept:
 * <ul>
 * <li>An extra <code>,</code>&nbsp;<small>(comma)</small> may appear just before the closing brace.</li>
 * <li>Strings may be quoted with <code>'</code>&nbsp;<small>(single quote)</small>.</li>
 * <li>Strings do not need to be quoted at all if they do not begin with a quote or single quote, and if they do not contain leading or trailing
 * spaces, and if they do not contain any of these characters: <code>{ } [ ] / \ : , = ; #</code> and if they do not look like numbers and if they are
 * not the reserved words <code>true</code>, <code>false</code>, or <code>null</code>.</li>
 * <li>Keys can be followed by <code>=</code> or <code>=></code> as well as by <code>:</code>.</li>
 * <li>Values can be followed by <code>;</code> <small>(semicolon)</small> as well as by <code>,</code> <small>(comma)</small>.</li>
 * <li>Numbers may have the <code>0x-</code> <small>(hex)</small> prefix.</li>
 * </ul>
 * 
 */
@SuppressWarnings("PMD.CyclomaticComplexity")
public class JSONObject {

    /**
     * JSONObject.NULL is equivalent to the value that JavaScript calls null, whilst Java's null is equivalent to the value that JavaScript calls
     * undefined.
     */
    private static final class NullObject {

        /**
         * A Null object is equal to the null value and to itself.
         * 
         * @param object An object to test for nullness.
         * @return true if the object parameter is the JSONObject.NULL object or null.
         */
        @Override
        public boolean equals(final Object object) {
            return (object instanceof NullObject) || (object == null) || (object == this);
        }

        @Override
        public int hashCode() {
            return "null".hashCode();
        }

        /**
         * Get the "null" string value.
         * 
         * @return The string "null".
         */
        @Override
        public String toString() {
            return "null";
        }
    }

    /**
     * The BACKING_MAP where the JSONObject's properties are kept.
     */
    private transient final Map<Object, Object> BACKING_MAP = createEmptyBackingMap();

    /**
     * It is sometimes more convenient and less ambiguous to have a <code>NULL</code> object than to use Java's <code>null</code> value.
     * <code>JSONObject.NULL.equals(null)</code> returns <code>true</code>. <code>JSONObject.NULL.toString()</code> returns <code>"null"</code>.
     */
    public static final Object NULL_OBJECT = new NullObject();

    /**
     * Default ctor, construct an empty JSONObject
     */
    public JSONObject() {
    }

    /**
     * Construct a JSONObject from a JSONTokener.
     * 
     * @param tokenizer A JSONTokener object containing the source string.
     * @throws JSONException If there is a syntax error in the source string or a duplicated key.
     */
    protected JSONObject(final JSONTokenizer tokenizer) throws JSONException {
        char character;
        String key;

        if (tokenizer.nextClean() != '{') {
            throw tokenizer.syntaxError("A JSONObject text must begin with '{'");
        }
        for (;;) {
            character = tokenizer.nextClean();
            switch (character) {
                case 0:
                    throw tokenizer.syntaxError("A JSONObject text must end with '}'");
                case '}':
                    return;
                default:
                    tokenizer.back();
                    key = tokenizer.nextValue().toString();
            }

            /*
             * The key is followed by ':'. We will also tolerate '=' or '=>'.
             */

            character = tokenizer.nextClean();
            if (character == '=') {
                if (tokenizer.next() != '>') {
                    tokenizer.back();
                }
            } else if (character != ':') {
                throw tokenizer.syntaxError("Expected a ':' after a key");
            }
            putOnce(key, tokenizer.nextValue());

            /*
             * Pairs are separated by ','. We will also tolerate ';'.
             */

            switch (tokenizer.nextClean()) {
                case ';':
                case ',':
                    if (tokenizer.nextClean() == '}') {
                        return;
                    }
                    tokenizer.back();
                    break;
                case '}':
                    return;
                default:
                    throw tokenizer.syntaxError("Expected a ',' or '}'");
            }
        }
    }

    /**
     * Construct a JSONObject from a source JSON text string. This is the most commonly used JSONObject constructor.
     * 
     * @param source A string beginning with <code>{</code>&nbsp;<small>(left brace)</small> and ending with <code>}</code> &nbsp;<small>(right
     *            brace)</small>.
     * @throws JSONException If there is a syntax error in the source string or a duplicated key.
     */
    public JSONObject(final String source) throws JSONException {
        this(new JSONTokenizer(source));
    }

    /**
     * Get the value object associated with a key.
     * 
     * @param key A key string.
     * @return The object associated with the key.
     * @throws JSONException if the key is not found.
     */
    public Object get(final String key) throws JSONException {
        final Object value = opt(key);
        if (value == null) {
            throw new JSONException("JSONObject[" + quote(key) + "] not found.");
        }
        return value;
    }

    /**
     * 
     * Get the integer object associated with a key.
     * 
     * @param key A key string.
     * @return The integer associated with the key.
     * @throws JSONException if the key is not found.
     */
    public int getInt(final String key) throws NumberFormatException, JSONException {
        return Integer.valueOf((String) get(key));
    }

    /**
     * Get the double object associated with a key.
     * 
     * @param key A key string.
     * @return The double associated with the key.
     * @throws JSONException if the key is not found.
     */
    public double getDouble(final String key) throws NumberFormatException, JSONException {
        return Double.valueOf((String) get(key));
    }

    /**
     * Get the long object associated with a key.
     * 
     * @param key A key string.
     * @return The long associated with the key.
     * @throws NumberFormatException
     * @throws JSONException if the key is not found.
     */
    public long getLong(final String key) throws NumberFormatException, JSONException {
        return Long.valueOf((String) get(key));
    }

    /**
     * Get the JSONArray value associated with a key.
     * 
     * @param key A key string.
     * @return A JSONArray which is the value.
     * @throws JSONException if the key is not found or if the value is not a JSONArray.
     */
    public JSONArray getJSONArray(final String key) throws JSONException {
        final Object value = get(key);
        if (value instanceof JSONArray) {
            return (JSONArray) value;
        }
        throw new JSONException("JSONObject[" + quote(key) + "] is not a JSONArray.");
    }

    /**
     * Get the JSONObject value associated with a key.
     * 
     * @param key A key string.
     * @return A JSONObject which is the value.
     * @throws JSONException if the key is not found or if the value is not a JSONObject.
     */
    public JSONObject getJSONObject(final String key) throws JSONException {
        final Object value = get(key);
        if (value instanceof JSONObject) {
            return (JSONObject) value;
        }
        throw new JSONException("JSONObject[" + quote(key) + "] is not a JSONObject.");
    }

    /**
     * Get an array of field names from a JSONObject.
     * 
     * @param jsonObject the JSON object
     * @return An array of field names, or null if there are no names.
     */
    public static String[] getNames(final JSONObject jsonObject) {
        final int length = jsonObject.length();
        String[] names = null;
        if (length > 0) {
            final Iterator<Object> iterator = jsonObject.keys();
            names = new String[length];
            int j = 0;
            while (iterator.hasNext()) {
                names[j++] = (String) iterator.next();
            }
        }
        return names;
    }

    /**
     * Get the string associated with a key.
     * 
     * @param key A key string.
     * @return A string which is the value.
     * @throws JSONException if the key is not found.
     */
    public String getString(final String key) throws JSONException {
        return get(key).toString();
    }

    /**
     * Determine if the JSONObject contains a specific key.
     * 
     * @param key A key string.
     * @return true if the key exists in the JSONObject.
     */
    public boolean has(final String key) {
        return this.BACKING_MAP.containsKey(key);
    }

    /**
     * Determine if the value associated with the key is null or if there is no value.
     * 
     * @param key A key string.
     * @return true if there is no value associated with the key or if the value is the JSONObject.NULL object.
     */
    public boolean isNull(final String key) {
        return JSONObject.NULL_OBJECT.equals(opt(key));
    }

    /**
     * Get an enumeration of the keys of the JSONObject.
     * 
     * @return An iterator of the keys.
     */
    public Iterator<Object> keys() {
        return this.BACKING_MAP.keySet().iterator();
    }

    /**
     * Get the number of keys stored in the JSONObject.
     * 
     * @return The number of keys in the JSONObject.
     */
    public int length() {
        return this.BACKING_MAP.size();
    }

    /**
     * Produce a JSONArray containing the names of the elements of this JSONObject.
     * 
     * @return A JSONArray containing the key strings, or null if the JSONObject is empty.
     */
    public JSONArray names() {
        final JSONArray ja = new JSONArray();
        final Iterator<Object> keys = keys();
        while (keys.hasNext()) {
            ja.put(keys.next());
        }
        return ja.length() == 0 ? null : ja;
    }

    /**
     * Produce a string from a Number.
     * 
     * @param n A Number
     * @return A String.
     * @throws JSONException If n is a non-finite number.
     */
    static public String numberToString(final Number n) throws JSONException {
        if (n == null) {
            throw new JSONException("Null pointer");
        }
        testValidity(n);

        // Shave off trailing zeros and decimal point, if possible.
        String s = n.toString();
        if ((s.indexOf('.') > 0) && (s.indexOf('e') < 0) && (s.indexOf('E') < 0)) {
            while (s.endsWith("0")) {
                s = s.substring(0, s.length() - 1);
            }
            if (s.endsWith(".")) {
                s = s.substring(0, s.length() - 1);
            }
        }
        return s;
    }

    /**
     * Get an optional value associated with a key.
     * 
     * @param key A key string.
     * @return An object which is the value, or null if there is no value.
     */
    public final Object opt(final String key) {
        return key == null ? null : this.BACKING_MAP.get(key);
    }

    /**
     * Put a key/value pair in the JSONObject. If the value is null, then the key will be removed from the JSONObject if it is present.
     * 
     * @param key A key string.
     * @param value An object which is the value. It should be of one of these types: Boolean, Double, Integer, JSONArray, JSONObject, Long, String,
     *            or the JSONObject.NULL object.
     * @return this.
     * @throws JSONException If the value is non-finite number or if the key is null.
     */
    public final JSONObject put(final String key, final Object value) throws JSONException {
        if (key == null) {
            throw new JSONException("NullObject key.");
        }
        if (value != null) {
            testValidity(value);
            this.BACKING_MAP.put(key, value);
        } else {
            remove(key);
        }
        return this;
    }

    /**
     * Put a key/value pair in the JSONObject, but only if the key and the value are both non-null, and only if there is not already a member with
     * that name.
     * 
     * @param key BACKING_MAP key
     * @param value BACKING_MAP value
     * @return his.
     * @throws JSONException if the key is a duplicate
     */
    public final JSONObject putOnce(final String key, final Object value) throws JSONException {
        if ((key != null) && (value != null)) {
            if (opt(key) != null) {
                throw new JSONException("Duplicate key \"" + key + "\"");
            }
            put(key, value);
        }
        return this;
    }

    /**
     * Produce a string in double quotes with backslash sequences in all the right places. A backslash will be inserted within </, allowing JSON text
     * to be delivered in HTML. In JSON text, a string cannot contain a control character or an unescaped quote or backslash.
     * 
     * @param string A String
     * @return A String correctly formatted for insertion in a JSON text.
     */
    public static String quote(final String string) {
        if ((string == null) || (string.length() == 0)) {
            return "\"\"";
        }

        char b;
        char c = 0;
        final int len = string.length();
        final StringBuilder sb = new StringBuilder(len + 4);

        sb.append('"');
        for (int i = 0; i < len; i += 1) {
            b = c;
            c = string.charAt(i);
            switch (c) {
                case '\\':
                case '"':
                    sb.append('\\');
                    sb.append(c);
                    break;
                case '/':
                    if (b == '<') {
                        sb.append('\\');
                    }
                    sb.append(c);
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                default:
                    if ((c < ' ') || ((c >= '\u0080') && (c < '\u00a0')) || ((c >= '\u2000') && (c < '\u2100'))) {
                        final String t = "000" + Integer.toHexString(c);
                        sb.append("\\u").append(t.substring(t.length() - 4));
                    } else {
                        sb.append(c);
                    }
            }
        }
        sb.append('"');
        return sb.toString();
    }

    /**
     * Remove a name and its value, if present.
     * 
     * @param key The name to be removed.
     * @return The value that was associated with the name, or null if there was no value.
     */
    public Object remove(final String key) {
        return this.BACKING_MAP.remove(key);
    }

    /**
     * Get an enumeration of the keys of the JSONObject. The keys will be sorted alphabetically.
     * 
     * @return An iterator of the keys.
     */
    public Iterator<Object> sortedKeys() {
        return new TreeSet<Object>(this.BACKING_MAP.keySet()).iterator();
    }

    /**
     * Try to convert a string into a number, boolean, or null. If the string can't be converted, return the string.
     * 
     * @param s A String.
     * @return A simple JSON value.
     */
    public static Object stringToValue(final String s) {

        Object result = null;

        if ("".equals(s)) {
            result = s;
        } else if ("true".equalsIgnoreCase(s)) {
            result = Boolean.TRUE;
        } else if ("false".equalsIgnoreCase(s)) {
            result = Boolean.FALSE;
        } else if ("null".equalsIgnoreCase(s)) {
            result = JSONObject.NULL_OBJECT;
        } else {

            /*
             * If it might be a number, try converting it. We support the non-standard 0x- convention. If a number cannot be produced, then the value
             * will just be a string. Note that the 0x-, plus, and implied string conventions are non-standard. A JSON parser may accept non-JSON
             * forms as long as it accepts all correct JSON forms.
             */

            final char b = s.charAt(0);
            if (((b >= '0') && (b <= '9')) || (b == '.') || (b == '-') || (b == '+')) {
                if ((b == '0') && (s.length() > 2) && ((s.charAt(1) == 'x') || (s.charAt(1) == 'X'))) {
                    try {
                        result = Integer.parseInt(s.substring(2), 16);
                    } catch (final Exception ignore) {
                    }
                }
                try {
                    if ((s.indexOf('.') > -1) || (s.indexOf('e') > -1) || (s.indexOf('E') > -1)) {
                        result = Double.valueOf(s);
                    } else {
                        final Long myLong = Long.valueOf(s);
                        if (myLong == myLong.intValue()) {
                            result = myLong.intValue();
                        } else {
                            result = myLong;
                        }
                    }
                } catch (final Exception ignore) {
                }
            }
        }

        return (result != null ? result : s);
    }

    /**
     * Throw an exception if the object is an NaN or infinite number.
     * 
     * @param value The object to test.
     * @throws JSONException If o is a non-finite number.
     */
    static void testValidity(final Object value) throws JSONException {
        if (value != null) {
            if (value instanceof Double) {
                final Double doubleValue = (Double) value;
                if (doubleValue.isInfinite() || doubleValue.isNaN()) {
                    throw new JSONException("JSON does not allow non-finite numbers.");
                }
            } else if (value instanceof Float) {
                final Float floatValue = (Float) value;
                if (floatValue.isInfinite() || floatValue.isNaN()) {
                    throw new JSONException("JSON does not allow non-finite numbers.");
                }
            }
        }
    }

    /**
     * Produce a JSONArray containing the values of the members of this JSONObject.
     * 
     * @param names A JSONArray containing a list of key strings. This determines the sequence of the values in the result.
     * @return A JSONArray of values.
     * @throws JSONException If any of the values are non-finite numbers.
     */
    public JSONArray toJSONArray(final JSONArray names) throws JSONException {
        if ((names == null) || (names.length() == 0)) {
            return null;
        }
        final JSONArray ja = new JSONArray();
        for (int i = 0; i < names.length(); i += 1) {
            ja.put(this.opt(names.getString(i)));
        }
        return ja;
    }

    /**
     * Make a JSON text of this JSONObject. For compactness, no whitespace is added. If this would not result in a syntactically correct JSON text,
     * then null will be returned instead.
     * <p/>
     * Warning: This method assumes that the data structure is acyclical.
     * 
     * @return a printable, displayable, portable, transmittable representation of the object, beginning with <code>{</code>&nbsp;<small>(left
     *         brace)</small> and ending with <code>}</code>&nbsp;<small>(right brace)</small>.
     */
    @Override
    public String toString() {
        try {
            final Iterator<Object> keys = keys();
            final StringBuilder sb = new StringBuilder("{");

            while (keys.hasNext()) {
                if (sb.length() > 1) {
                    sb.append(',');
                }
                final Object key = keys.next();
                sb.append(quote(key.toString()));
                sb.append(':');
                sb.append(valueToString(this.BACKING_MAP.get(key)));
            }
            sb.append('}');
            return sb.toString();
        } catch (final Exception e) {
            return null;
        }
    }

    /**
     * Extracted in order to inject more formatting when printing json results in tests See subclass for more details
     * 
     * @return empty map
     */
    protected Map<Object, Object> createEmptyBackingMap() {
        return new HashMap<Object, Object>();
    }

    /**
     * Make a JSON text of an Object value. If the object has an value.toJSONString() method, then that method will be used to produce the JSON text.
     * The method is required to produce a strictly conforming text. If the object does not contain a toJSONString method (which is the most common
     * case), then a text will be produced by other means. If the value is an array or Collection, then a JSONArray will be made from it and its
     * toJSONString method will be called. If the value is a BACKING_MAP, then a JSONObject will be made from it and its toJSONString method will be
     * called. Otherwise, the value's toString method will be called, and the result will be quoted.
     * <p/>
     * <p/>
     * Warning: This method assumes that the data structure is acyclical.
     * 
     * @param value The value to be serialized.
     * @return a printable, displayable, transmittable representation of the object, beginning with <code>{</code>&nbsp;<small>(left brace)</small>
     *         and ending with <code>}</code>&nbsp;<small>(right brace)</small>.
     * @throws JSONException If the value is or contains an invalid number.
     */
    protected static String valueToString(final Object value) throws JSONException {
        if ((value == null) || value.equals(NULL_OBJECT)) {
            return "null";
        }
        if (value instanceof Number) {
            return numberToString((Number) value);
        }
        if ((value instanceof Boolean) || (value instanceof JSONObject) || (value instanceof JSONArray)) {
            return value.toString();
        }
        if (value.getClass().isArray()) {
            return new JSONArray(value).toString();
        }
        return quote(value.toString());
    }

    /**
     * Wrap an object, if necessary. If the object is null, return the NULL object. If it is an array or collection, wrap it in a JSONArray. If it is
     * a map, wrap it in a JSONObject. If it is a standard property (Double, String, et al) then it is already wrapped. If the wrapping fails, then
     * null is returned.
     * 
     * @param object The object to wrap
     * @return The wrapped value
     */
    static Object wrap(final Object object) {
        try {
            Object result;
            if (object == null) {
                result = NULL_OBJECT;
            } else if ((object instanceof JSONObject) || (object instanceof JSONArray) || NULL_OBJECT.equals(object) || (object instanceof Byte)
                    || (object instanceof Character) || (object instanceof Short) || (object instanceof Integer) || (object instanceof Long)
                    || (object instanceof Boolean) || (object instanceof Float) || (object instanceof Double) || (object instanceof String)) {
                result = object;
            } else if (object.getClass().isArray()) {
                result = new JSONArray(object);
            } else {
                result = null;
            }

            return result;

        } catch (final Exception exception) {
            return null;
        }
    }

}