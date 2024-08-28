package com.ericsson.eniq.events.server.json;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * REVISIT: to be replaced by JSON API such as jettison or jackson
 * 
 * A JSONArray is an ordered sequence of values. Its external text form is a
 * string wrapped in square brackets with commas separating the values. The
 * internal form is an object having <code>get</code> and <code>opt</code>
 * methods for accessing the values by index, and <code>put</code> methods for
 * adding or replacing values. The values can be any of these types:
 * <code>Boolean</code>, <code>JSONArray</code>, <code>JSONObject</code>,
 * <code>Number</code>, <code>String</code>, or the
 * <code>JSONObject.NULL object</code>.
 * <p>
 * The constructor can convert a JSON text into a Java object. The
 * <code>toString</code> method converts to JSON text.
 * <p>
 * A <code>get</code> method returns a value if one can be found, and throws an
 * exception if one cannot be found. An <code>opt</code> method returns a
 * default value instead of throwing an exception, and so is useful for
 * obtaining optional values.
 * <p>
 * The generic <code>get()</code> and <code>opt()</code> methods return an
 * object which you can cast or query for type. There are also typed
 * <code>get</code> and <code>opt</code> methods that do type checking and type
 * coercion for you.
 * <p>
 * The texts produced by the <code>toString</code> methods strictly conform to
 * JSON syntax rules. The constructors are more forgiving in the texts they will
 * accept:
 * <ul>
 * <li>An extra <code>,</code>&nbsp;<small>(comma)</small> may appear just
 *     before the closing bracket.</li>
 * <li>The <code>null</code> value will be inserted when there
 *     is <code>,</code>&nbsp;<small>(comma)</small> elision.</li>
 * <li>Strings may be quoted with <code>'</code>&nbsp;<small>(single
 *     quote)</small>.</li>
 * <li>Strings do not need to be quoted at all if they do not begin with a quote
 *     or single quote, and if they do not contain leading or trailing spaces,
 *     and if they do not contain any of these characters:
 *     <code>{ } [ ] / \ : , = ; #</code> and if they do not look like numbers
 *     and if they are not the reserved words <code>true</code>,
 *     <code>false</code>, or <code>null</code>.</li>
 * <li>Values can be separated by <code>;</code> <small>(semicolon)</small> as
 *     well as by <code>,</code> <small>(comma)</small>.</li>
 * <li>Numbers may have the
 *     <code>0x-</code> <small>(hex)</small> prefix.</li>
 * </ul>
 */
@SuppressWarnings("PMD.CyclomaticComplexity")
public class JSONArray {

    /**
     * Array property storage
     */
    private transient final List<Object> BACKING_LIST = new ArrayList<Object>();

    /**
     * Default ctor.
     */
    public JSONArray() {/* default ctor */
    }

    /**
     * Construct a JSONArray from a source JSON text.
     * @param source     A string that begins with
     * <code>[</code>&nbsp;<small>(left bracket)</small>
     *  and ends with <code>]</code>&nbsp;<small>(right bracket)</small>.
     *  @throws JSONException If there is a syntax error.
     */
    public JSONArray(final String source) throws JSONException {
        this(new JSONTokenizer(source));
    }

    /**
     * Construct a JSONArray from a JSONTokenizer.
     * @param tokenizer A JSONTokenizer
     * @throws JSONException If there is a syntax error.
     */
    protected JSONArray(final JSONTokenizer tokenizer) throws JSONException {
        this();
        char c = tokenizer.nextClean();
        char q;
        if (c == '[') {
            q = ']';
        } else if (c == '(') {
            q = ')';
        } else {
            throw tokenizer.syntaxError("A JSONArray text must start with '['");
        }
        if (tokenizer.nextClean() == ']') {
            return;
        }
        tokenizer.back();
        for (;;) {
            if (tokenizer.nextClean() == ',') {
                tokenizer.back();
                this.BACKING_LIST.add(null);
            } else {
                tokenizer.back();
                this.BACKING_LIST.add(tokenizer.nextValue());
            }
            c = tokenizer.nextClean();
            switch (c) {
            case ';':
            case ',':
                if (tokenizer.nextClean() == ']') {
                    return;
                }
                tokenizer.back();
                break;
            case ']':
            case ')':
                if (q != c) {
                    throw tokenizer.syntaxError("Expected a '" + q + "'");
                }
                return;
            default:
                throw tokenizer.syntaxError("Expected a ',' or ']'");
            }
        }
    }

    /**
     * Construct a JSONArray from an array
     * @param array object to be wrapped
     * @throws JSONException If not an array.
     */
    public JSONArray(final Object array) throws JSONException {
        this();
        if (array.getClass().isArray()) {
            final int length = Array.getLength(array);
            for (int i = 0; i < length; i += 1) {
                this.put(JSONObject.wrap(Array.get(array, i)));
            }
        } else {
            throw new JSONException("JSONArray initial value should be a string or collection or array.");
        }
    }

    /**
     * Get the object value associated with an index.
     * @param index
     *  The index must be between 0 and length() - 1.
     * @return An object value.
     * @throws JSONException If there is no value for the index.
     */
    public Object get(final int index) throws JSONException {
        final Object object = opt(index);
        if (object == null) {
            throw new JSONException("JSONArray[" + index + "] not found.");
        }
        return object;
    }

    /**
     * Get the JSONObject associated with an index.
     * @param index subscript
     * @return      A JSONObject value.
     * @throws JSONException If there is no value for the index or if the
     * value is not a JSONObject
     */
    public JSONObject getJSONObject(final int index) throws JSONException {
        final Object object = get(index);
        if (object instanceof JSONObject) {
            return (JSONObject) object;
        }
        throw new JSONException("JSONArray[" + index + "] is not a JSONObject.");
    }

    /**
     * Get the string associated with an index.
     * @param index The index must be between 0 and length() - 1.
     * @return      A string value.
     * @throws JSONException If there is no value for the index.
     */
    public String getString(final int index) throws JSONException {
        return get(index).toString();
    }

    /**
     * Determine if the value is null.
     * @param index The index must be between 0 and length() - 1.
     * @return true if the value at the index is null, or if there is no value.
     */
    public boolean isNull(final int index) {
        return JSONObject.NULL_OBJECT.equals(opt(index));
    }

    /**
     * Make a string from the contents of this JSONArray. The
     * <code>separator</code> string is inserted between each element.
     * Warning: This method assumes that the data structure is acyclical.
     * @param separator A string that will be inserted between the elements.
     * @return a string.
     * @throws JSONException If the array contains an invalid number.
     */
    public String join(final String separator) throws JSONException {
        final int len = length();
        final StringBuilder sb = new StringBuilder();

        for (int i = 0; i < len; i += 1) {
            if (i > 0) {
                sb.append(separator);
            }
            sb.append(JSONObject.valueToString(this.BACKING_LIST.get(i)));
        }
        return sb.toString();
    }

    /**
     * Get the number of elements in the JSONArray, included nulls.
     *
     * @return The length (or size).
     */
    public int length() {
        return this.BACKING_LIST.size();
    }

    /**
     * Get the optional object value associated with an index.
     * @param index The index must be between 0 and length() - 1.
     * @return      An object value, or null if there is no
     *              object at that index.
     */
    public Object opt(final int index) {
        return (index < 0 || index >= length()) ? null : this.BACKING_LIST.get(index);
    }

    /**
     * Append an object value. This increases the array's length by one.
     * @param value An object value.  The value should be a
     *  Boolean, Double, Integer, JSONArray, JSONObject, Long, or String, or the
     *  JSONObject.NULL object.
     * @return this.
     */
    public final JSONArray put(final Object value) {
        this.BACKING_LIST.add(value);
        return this;
    }

    /**
     * Remove an index and close the hole.
     * @param index The index of the element to be removed.
     * @return The value that was associated with the index,
     * or null if there was no value.
     */
    public Object remove(final int index) {
        final Object object = opt(index);
        this.BACKING_LIST.remove(index);
        return object;
    }

    /**
     * Extracted in order to inject more formatting when printing json results in tests
     * See subclass for more details
     * 
     * @return          empty list
     */
    protected List<Object> createEmptyBackingList() {
        return new ArrayList<Object>();
    }

    /**
     * Make a JSON text of this JSONArray. For compactness, no
     * unnecessary whitespace is added. If it is not possible to produce a
     * syntactically correct JSON text then null will be returned instead. This
     * could occur if the array contains an invalid number.
     * <p>
     * Warning: This method assumes that the data structure is acyclical.
     *
     * @return a printable, displayable, transmittable
     *  representation of the array.
     */
    @Override
    public String toString() {
        String result;
        try {
            result = '[' + join(",") + ']';
        } catch (final Exception e) {
            result = "[]";
        }
        return result;
    }
}