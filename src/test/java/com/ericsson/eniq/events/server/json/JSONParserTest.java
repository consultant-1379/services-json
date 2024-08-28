package com.ericsson.eniq.events.server.json;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.ericsson.eniq.events.server.test.FileReader;

public class JSONParserTest {

    public static final String JSON_FILE = "TestJsonFile.json";

    private static final String JSON_ARRAY_EXAMPLE_1 = "[\"a\", \"b\", \"c\", \"d\"]";

    private static final String[] JSON_ARRAY_EXAMPLE_1_ARRAY_OBJECT = new String[] { "a", "b", "c", "d" };

    private static final String JSON_ARRAY_EXAMPLE_2 = "[{ \"username\" : \"admin\", \"password\" : \"123\" }, { \"username\" : \"bbvb\", \"password\" : \"sdfsdf\" }, { \"username\" : \"asd\", \"password\" : \"222\" }]";

    private static final String JSON_ARRAY_EXAMPLE_3 = "({ \"username\" : \"admin\", \"password\" : \"123\" }, { \"username\" : \"bbvb\", \"password\" : \"sdfsdf\" }, { \"username\" : \"asd\", \"password\" : \"222\" })";

    private static final String JSON_ARRAY_EXAMPLE_4_MISSING_OPENING_DELIM = "{ \"username\" : \"admin\", \"password\" : \"123\" }]";

    private static final String JSON_ARRAY_EXAMPLE_4_MISSING_CLOSING_DELIM = "[{ \"username\" : \"admin\", \"password\" : \"123\" }";

    private static final String TABS = "tabs";

    @Test
    public void testRemoveAndReAddElements() throws JSONException, IOException {
        final JSONObject uiMetaData = readSampleJsonFile();
        JSONArray tabs = (JSONArray) uiMetaData.get(TABS);
        assertNotNull(tabs);
        assertEquals(4, tabs.length());

        uiMetaData.remove(TABS);

        try {
            uiMetaData.get(TABS);
        } catch (final JSONException expected) {
            // tabs don't exist
        }

        // re-add tabs        
        uiMetaData.put(TABS, tabs);

        tabs = (JSONArray) uiMetaData.get(TABS);
        assertEquals(4, tabs.length());

    }

    @Test(expected = JSONException.class)
    public void testRemoveElements() throws JSONException, IOException {
        final JSONObject sampleJSON = readSampleJsonFile();
        final JSONArray tabs = (JSONArray) sampleJSON.get(TABS);
        assertEquals(4, tabs.length());
        sampleJSON.remove(TABS);
        assertNull(sampleJSON.get(TABS));

    }

    @Test
    public void testNestedArrayAccess() throws JSONException, IOException {
        final JSONObject uiMetaData = readSampleJsonFile();
        // fetch grid objects using different access approach
        final JSONArray grids = uiMetaData.getJSONArray("grids");
        assertNotNull(grids);
    }

    private JSONObject readSampleJsonFile() throws JSONException, IOException {
        final InputStream fileInputStream = ClassLoader.getSystemClassLoader().getResourceAsStream(JSON_FILE);
        if (fileInputStream == null) {
            fail("The file " + JSON_FILE + " could not be found on the classpath");
        }
        final JSONObject uiMetaData = new JSONObject(FileReader.readInputStream(fileInputStream));
        return uiMetaData;
    }

    @Test
    public void test_create_JSONObject() throws JSONException {
        final JSONObject json = new JSONObject("{}");
        assertNotNull(json);
        assertEquals("{}", json.toString());
    }

    @Test
    public void test_create_JSONArray_DEFAULT() {
        final JSONArray jsonArray = new JSONArray();
        assertNotNull(jsonArray);
    }

    @Test(expected = JSONException.class)
    public void test_create_JSONObject_with_unterminated_string() throws JSONException {
        new JSONObject("{\"name\" : \"Declan}");
    }

    @Test
    public void test_create_JSONObject_with_JSON_array_element() throws JSONException {
        final JSONObject json = new JSONObject("{name = parent}");
        assertNotNull(json);
        assertEquals("{\"name\":\"parent\"}", json.toString());
        json.put("child", new JSONArray(JSON_ARRAY_EXAMPLE_1_ARRAY_OBJECT));

        assertNotNull(json.toString());

        assertTrue(json.has("child"));

        final JSONArray child = (JSONArray) json.get("child");
        assertEquals("[\"a\",\"b\",\"c\",\"d\"]", child.toString());
    }

    @Test
    public void test_create_JSONObject_with_special_escaped_chars() throws JSONException {
        JSONObject jo;

        jo = new JSONObject("{\"name\" : \"\\n\"}");
        assertNotNull(jo);
        assertEquals("{\"name\":\"\\n\"}", jo.toString());

        jo = new JSONObject("{\"name\" : \"\\b B \\t T \\n N \\f F \\r R\"}");
        assertNotNull(jo);
        assertEquals("{\"name\":\"\\b B \\t T \\n N \\f F \\r R\"}", jo.toString());

        // 0x0058 is ascii for 'X'
        jo = new JSONObject("{\"unicode\" : \"\\u0058\"}");
        assertNotNull(jo);
        assertEquals("{\"unicode\":\"X\"}", jo.toString());
    }

    @Test
    public void test_create_JSONObject_with_empty_and_null_values() throws JSONException {
        JSONObject jo;

        jo = new JSONObject();
        jo.put("name", "");
        jo.put("value", "X");
        assertEquals("{\"name\":\"\",\"value\":\"X\"}", jo.toString());
        // remove value by putting null as value for element.
        final Object value = null;
        jo.put("value", value);
        assertEquals("{\"name\":\"\"}", jo.toString());

    }

    @Test
    public void test_create_JSONObject_with_double_and_float_values() throws JSONException {
        final JSONObject jo = new JSONObject();
        jo.put("d1", 1.0);
        jo.put("f1", 2.1f);
        assertEquals("{\"d1\":1,\"f1\":2.1}", jo.toString());
    }

    @Test(expected = JSONException.class)
    public void test_create_JSONObject_with_illegal_double_values() throws JSONException {
        final JSONObject jo = new JSONObject();
        jo.put("d1", Double.POSITIVE_INFINITY);
    }

    @Test(expected = JSONException.class)
    public void test_create_JSONObject_with_illegal_float_values() throws JSONException {
        final JSONObject jo = new JSONObject();
        jo.put("f1", Float.POSITIVE_INFINITY);
    }

    @Test(expected = JSONException.class)
    public void test_create_JSONObject_with_duplicate_key_value() throws JSONException {
        new JSONObject("{\"name\":\"\",\"name\":\"\"}");
    }

    @Test
    public void test_create_empty_JSONArray() throws JSONException {
        final JSONArray jsonArray = new JSONArray("[]");
        assertNotNull(jsonArray);
        assertEquals(0, jsonArray.length());
    }

    @Test
    public void test_create_JSONArray_EXAMPLE_1() throws JSONException {
        final JSONArray jsonArray = new JSONArray(JSON_ARRAY_EXAMPLE_1);
        assertNotNull(jsonArray);

        assertEquals(4, jsonArray.length());

        String element;

        element = (String) jsonArray.get(0);
        assertNotNull(element);
        assertEquals("a", element);

        element = (String) jsonArray.get(3);
        assertNotNull(element);
        assertEquals("d", element);

    }

    @Test
    public void test_to_string_JSONArray_EXAMPLE_1() throws JSONException {
        final JSONArray jsonArray = new JSONArray(JSON_ARRAY_EXAMPLE_1);
        final String formattedArray = jsonArray.toString();
        assertEquals("[\"a\",\"b\",\"c\",\"d\"]", formattedArray);
    }

    @Test
    public void test_add_elements_to_JSONArray_EXAMPLE_1() throws JSONException {
        final JSONArray jsonArray = new JSONArray(JSON_ARRAY_EXAMPLE_1);
        assertNotNull(jsonArray);

        assertEquals(4, jsonArray.length());

        String element;

        element = (String) jsonArray.get(0);
        assertNotNull(element);
        assertEquals("a", element);

        element = (String) jsonArray.get(3);
        assertNotNull(element);
        assertEquals("d", element);

        jsonArray.put("e");
        assertEquals(5, jsonArray.length());

        element = (String) jsonArray.get(4);
        assertNotNull(element);
        assertEquals("e", element);

    }

    @Test(expected = JSONException.class)
    public void test_create_JSONArray_EXAMPLE_1_fail_to_access_elements_as_JSONObject() throws JSONException {
        final JSONArray jsonArray = new JSONArray(JSON_ARRAY_EXAMPLE_1);
        assertNotNull(jsonArray);
        assertEquals(4, jsonArray.length());
        jsonArray.getJSONObject(0);
    }

    @Test
    public void test_create_JSONArray_from_string_array() throws JSONException {
        final JSONArray jsonArray = new JSONArray(JSON_ARRAY_EXAMPLE_1_ARRAY_OBJECT);
        assertNotNull(jsonArray);

        assertEquals(4, jsonArray.length());

        String element;

        element = (String) jsonArray.get(0);
        assertNotNull(element);
        assertEquals("a", element);

        element = (String) jsonArray.get(3);
        assertNotNull(element);
        assertEquals("d", element);

    }

    @Test
    public void test_create_JSONArray_from_int_array() throws JSONException {
        final JSONArray jsonArray = new JSONArray(new int[] { 1, 2, 3, 4 });
        assertNotNull(jsonArray);
        assertEquals(4, jsonArray.length());

        Integer element;
        element = (Integer) jsonArray.get(0);
        assertNotNull(element);
        assertEquals(1, element.intValue());
        assertEquals("[1,2,3,4]", jsonArray.toString());
    }

    @Test
    public void test_create_JSONArray_from_double_array() throws JSONException {
        final JSONArray jsonArray = new JSONArray(new double[] { 1.0, 2.1, 3.2, 4.3 });
        assertNotNull(jsonArray);
        assertEquals(4, jsonArray.length());

        Double element;
        element = (Double) jsonArray.get(0);
        assertNotNull(element);
        assertEquals(new Double(1.0), element);
        assertEquals("[1,2.1,3.2,4.3]", jsonArray.toString());
    }

    @Test
    public void test_create_JSONArray_from_float_array() throws JSONException {
        final JSONArray jsonArray = new JSONArray(new float[] { 1.0f, 2.1f, 3.2f, 4.3f });
        assertNotNull(jsonArray);
        assertEquals(4, jsonArray.length());

        Float element;
        element = (Float) jsonArray.get(0);
        assertNotNull(element);
        assertEquals(new Float(1.0), element);
        assertEquals("[1,2.1,3.2,4.3]", jsonArray.toString());
    }

    @Test
    public void test_create_JSONArray_from_array_of_string_arrays() throws JSONException {
        final JSONArray jsonArray = new JSONArray(new String[][] { { "01", "02", "03" }, { "11", "12", "13" },
                { "21", "22", "23" }, null });

        assertNotNull(jsonArray);
        assertEquals(4, jsonArray.length());

        final JSONArray array = (JSONArray) jsonArray.get(0);
        assertEquals("[\"01\",\"02\",\"03\"]", array.toString());

    }

    @Test
    public void test_create_JSONArray_from_array_of_double_arrays() throws JSONException {
        final JSONArray jsonArray = new JSONArray(new Double[][] { { 1.0, 1.1, 1.2 }, { 2.0, 2.1, 2.2 },
                { 3.0, 3.1, 3.2 } });

        assertNotNull(jsonArray);
        assertEquals(3, jsonArray.length());

        final JSONArray array = (JSONArray) jsonArray.get(0);
        assertEquals("[1,1.1,1.2]", array.toString());
    }

    @Test(expected = JSONException.class)
    public void test_create_JSONArray_from_non_array_object_fail() throws JSONException {
        new JSONArray(new Object());
    }

    @Test(expected = JSONException.class)
    public void test_create_JSONArray_EXAMPLE_1_failed_access() throws JSONException {
        final JSONArray jsonArray = new JSONArray(JSON_ARRAY_EXAMPLE_1);
        assertNotNull(jsonArray);
        assertEquals(4, jsonArray.length());
        jsonArray.get(5);
    }

    @Test
    public void test_create_JSONArray_with_null_values() throws JSONException {
        final JSONArray jsonArray = new JSONArray(new String[] { null, null, null });
        assertNotNull(jsonArray);
        assertEquals(3, jsonArray.length());
        final String formattedArray = jsonArray.toString();
        assertEquals("[null,null,null]", formattedArray);
    }

    @Test
    public void test_create_JSONArray_EXAMPLE_2() throws JSONException {
        final JSONArray jsonArray = new JSONArray(JSON_ARRAY_EXAMPLE_2);
        assertNotNull(jsonArray);

        JSONObject element;

        element = (JSONObject) jsonArray.get(0);
        assertNotNull(element);
        assertEquals("admin", element.get("username"));
        assertEquals("123", element.get("password"));

        // access using getJSONObject
        final JSONObject jo = jsonArray.getJSONObject(0);
        assertNotNull(jo);
        assertEquals("admin", jo.get("username"));
        assertEquals("123", jo.get("password"));

    }

    @Test
    public void test_create_JSONArray_EXAMPLE_3() throws JSONException {
        final JSONArray jsonArray = new JSONArray(JSON_ARRAY_EXAMPLE_3);
        assertNotNull(jsonArray);

        JSONObject element;

        element = (JSONObject) jsonArray.get(0);
        assertNotNull(element);
        assertEquals("admin", element.get("username"));
        assertEquals("123", element.get("password"));
    }

    @Test(expected = JSONException.class)
    public void test_create_JSONArray_EXAMPLE_4_missing_opening_delimiter() throws JSONException {
        new JSONArray(JSON_ARRAY_EXAMPLE_4_MISSING_OPENING_DELIM);
    }

    @Test(expected = JSONException.class)
    public void test_create_JSONArray_EXAMPLE_4_missing_closing_delimiter() throws JSONException {
        new JSONArray(JSON_ARRAY_EXAMPLE_4_MISSING_CLOSING_DELIM);
    }

    private static final String JSON_OBJECT_NESTED_EXAMPLE = "{\n" + "    \"name\": \"Jack (\\\"Bee\\\") Nimble\", \n"
            + "    \"format\": {\n" + "        \"type\":       \"rect\", \n" + "        \"width\":      1920, \n"
            + "        \"height\":     1080, \n" + "        \"interlace\":  false, \n"
            + "        \"visible\":  true, \n" + "        \"tag\":  null, \n" + "        \"frame rate\": 24\n"
            + "    }\n" + "}";

    @Test
    public void test_create_nested_JSONObject() throws JSONException {
        final JSONObject json = new JSONObject(JSON_OBJECT_NESTED_EXAMPLE);
        assertNotNull(json);

        assertEquals(true, json.has("name"));
        assertEquals("Jack (\"Bee\") Nimble", json.get("name"));

        assertEquals(true, json.has("format"));
        JSONObject format = (JSONObject) json.get("format");
        assertNotNull(format);

        // alternative access method for nested objects
        format = json.getJSONObject("format");
        assertNotNull(format);

        assertEquals(true, format.has("type"));
        assertEquals("rect", format.get("type"));

        assertEquals(true, format.has("width"));
        assertEquals("1920", format.get("width").toString());

        assertEquals(true, format.has("height"));
        assertEquals("1080", format.get("height").toString());
        assertEquals(1080, format.get("height"));

        assertEquals(true, format.has("interlace"));
        assertEquals("false", format.get("interlace").toString());
        assertEquals(false, format.get("interlace"));

        assertEquals(true, format.has("visible"));
        assertEquals("true", format.get("visible").toString());
        assertEquals(true, format.get("visible"));

        assertEquals(true, format.has("tag"));
        assertEquals("null", format.get("tag").toString());
        assertEquals(JSONObject.NULL_OBJECT, format.get("tag"));

        assertEquals(true, format.has("frame rate"));
        assertEquals("24", format.get("frame rate").toString());
        assertEquals(24, format.get("frame rate"));

    }

    @Test
    public void testNullObjectIdentity() {
        final Set<Object> s = new HashSet<Object>();
        s.add(JSONObject.NULL_OBJECT);
        s.add(JSONObject.NULL_OBJECT);
        assertEquals(1, s.size());
        assertTrue(s.contains(JSONObject.NULL_OBJECT));

        assertEquals(3392903, JSONObject.NULL_OBJECT.hashCode());
        assertEquals("null", JSONObject.NULL_OBJECT.toString());
    }

    @Test
    public void test_get_names_from_JSONObject() throws JSONException {
        final JSONObject json = new JSONObject(JSON_OBJECT_NESTED_EXAMPLE);
        assertNotNull(json);

        final String[] names = JSONObject.getNames(json);
        assertNotNull(names);
        assertEquals(2, names.length);
        assertTrue(Arrays.asList(names).contains("name"));
        assertTrue(Arrays.asList(names).contains("format"));

        // alternatively create a JSONArray of the names
        final JSONArray nameArray = json.names();
        assertEquals(2, nameArray.length());
        assertEquals("[\"name\",\"format\"]", nameArray.toString());

        // now create a JSON array with the values
        final JSONArray valueArray = json.toJSONArray(nameArray);
        assertEquals(2, valueArray.length());
        assertEquals(
                "[\"Jack (\\\"Bee\\\") Nimble\",{\"height\":1080,\"visible\":true,\"tag\":null,\"width\":1920,\"type\":\"rect\",\"frame rate\":24,\"interlace\":false}]",
                valueArray.toString());
    }

    @Test
    public void test_JSONObject_put_and_get() throws JSONException {
        final JSONObject jo = new JSONObject();
        jo.put("elementA", "firstName");
        jo.put("elementB", "lastName");

        Object value;

        value = jo.get("elementA");
        assertEquals("firstName", value);

        value = jo.get("elementB");
        assertEquals("lastName", value);
    }

    @Test(expected = JSONException.class)
    public void test_JSONObject_failed_access() throws JSONException {
        final JSONObject jo = new JSONObject();
        jo.put("elementA", "firstName");
        jo.get("elementX");
    }

    @Test
    public void test_JSONObject_safe_access() throws JSONException {
        final JSONObject jo = new JSONObject();
        jo.put("elementA", "firstName");
        assertEquals(false, jo.has("elementX"));
    }

    @Test(expected = JSONException.class)
    public void test_null_key() throws JSONException {
        final JSONObject jo = new JSONObject();
        jo.put(null, "firstName");
    }

    @Test
    public void test_create_JSONException() {
        final NullPointerException npe = new NullPointerException("A null pointer was encountered");
        JSONException je = new JSONException(npe);
        assertEquals(npe, je.getCause());

        je = new JSONException("Something bad happened!");
        assertEquals("Something bad happened!", je.getMessage());
    }

    @Test
    public void testStringToValue() {

        assertEquals("", JSONObject.stringToValue(""));
        assertEquals(Boolean.TRUE, JSONObject.stringToValue("true"));
        assertEquals(Boolean.TRUE, JSONObject.stringToValue("tRuE"));
        assertEquals(Boolean.FALSE, JSONObject.stringToValue("false"));
        assertEquals(Boolean.FALSE, JSONObject.stringToValue("FaLse"));
        assertEquals(1234567890189898298L, JSONObject.stringToValue("1234567890189898298"));
        assertEquals(Integer.valueOf(1234567891), JSONObject.stringToValue("1234567891"));
        assertEquals(1.23, JSONObject.stringToValue("1.23"));
        assertEquals("Ericsson", JSONObject.stringToValue("Ericsson"));

    }

}
