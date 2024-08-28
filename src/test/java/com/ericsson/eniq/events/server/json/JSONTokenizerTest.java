/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2014
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.eniq.events.server.json;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class JSONTokenizerTest {

    private static final char NULL_CHAR = '\u0000';
    private static final char EURO_CHAR = '\u20AC';

    private JSONTokenizer emptyJsonString;
    private JSONTokenizer validJsonString;

    @Before
    public void setup() {
        emptyJsonString = new JSONTokenizer("");
    }

    @Test
    public void syntaxError_message_expectExceptionWithMessage() throws Exception {
        final JSONException syntaxError = emptyJsonString.syntaxError("Some Message");
        assertEquals("Some Message at 0 [character 1 line 1]", syntaxError.getMessage());
        assertTrue(syntaxError instanceof Exception);
    }

    @Test(expected = JSONException.class)
    public void back_emptyString_expectException() throws Exception {
        emptyJsonString.back(); //TODO the error message given here is wrong. It complains about stepping back 2 steps.
    }

    @Test
    public void back_stepBacktwice_expectException() throws Exception {
        validJsonString = new JSONTokenizer("{}");
        validJsonString.nextClean();
        validJsonString.back();
        try {
            validJsonString.back(); // this should throw the exception
            fail();
        } catch (final JSONException e) {
        }
    }

    @Test
    public void back_stepforwardThenBack_expect1stChar() throws Exception {
        validJsonString = new JSONTokenizer("{}");
        final char expected = validJsonString.nextClean();
        validJsonString.back();
        final char result = validJsonString.nextClean();
        assertEquals(expected, result);
    }

    @Test
    public void end_emptyString_expectFalse() throws Exception {
        assertEquals(false, emptyJsonString.end());
    }

    @Test
    public void end_validString_expectFalse() throws Exception {
        validJsonString = new JSONTokenizer("{}");
        assertEquals(false, validJsonString.end());
    }

    @Test
    public void end_stepForwardvalidString_expectFalse() throws Exception {
        validJsonString = new JSONTokenizer("{}");
        validJsonString.nextClean();
        assertEquals(false, validJsonString.end());
    }

    @Test
    public void end_stepForwardOnValidStringPastTheEnd_expectTrue() throws Exception {
        validJsonString = new JSONTokenizer("{}");
        validJsonString.next();
        validJsonString.next();
        assertEquals(false, validJsonString.end());//End only returns true once we go past the last character
        validJsonString.next();
        assertEquals(true, validJsonString.end());
    }

    @Test
    public void end_stepForwardOnEmptyString_expectTrue() throws Exception {
        emptyJsonString.next();
        assertEquals(true, emptyJsonString.end());
    }

    @Test
    public void nextClean_withoutWhiteSpace_expectAllCharacters() throws Exception {
        validJsonString = new JSONTokenizer("{\"1");
        assertEquals('{', validJsonString.nextClean());
        assertEquals('\"', validJsonString.nextClean());
        assertEquals('1', validJsonString.nextClean());
        assertEquals(NULL_CHAR, validJsonString.nextClean());
    }

    @Test
    public void nextClean_withWhiteSpace_expectToSkipWhiteSpaceCharacters() throws Exception {
        validJsonString = new JSONTokenizer("{\" 1");
        assertEquals('{', validJsonString.nextClean());
        assertEquals('\"', validJsonString.nextClean());
        assertEquals('1', validJsonString.nextClean());
        assertEquals(NULL_CHAR, validJsonString.nextClean());
    }

    @Test
    public void next_withWhitespace_expectAllCharacters() throws Exception {
        validJsonString = new JSONTokenizer("{\" \n \r\f \r\n\t1");
        assertEquals('{', validJsonString.next());
        assertEquals('\"', validJsonString.next());
        assertEquals(' ', validJsonString.next());
        assertEquals('\n', validJsonString.next());
        assertEquals(' ', validJsonString.next());
        assertEquals('\r', validJsonString.next());
        assertEquals('\f', validJsonString.next());
        assertEquals(' ', validJsonString.next());
        assertEquals('\r', validJsonString.next());
        assertEquals('\n', validJsonString.next());
        assertEquals('\t', validJsonString.next());
        assertEquals('1', validJsonString.next());
        assertEquals(NULL_CHAR, validJsonString.next());
    }

    @Test
    public void nextValue_withDoubleQuotes_expectStringBetweenDoubleQuotes() throws Exception {
        validJsonString = new JSONTokenizer("\"Key1\"");
        assertEquals("Key1", validJsonString.nextValue());
    }

    @Test
    public void nextValue_withSingleQuotes_expectStringBetweenSingleQuotes() throws Exception {
        validJsonString = new JSONTokenizer("\'Key1\'");
        assertEquals("Key1", validJsonString.nextValue());
    }

    @Test(expected = JSONException.class)
    public void nextValue_unterminatedString_expectException() throws Exception {
        validJsonString = new JSONTokenizer("\'fsdfsdf\n");
        validJsonString.nextValue(); //Should throw an Exception as the String is unterminated
    }

    @Test
    public void nextValue_javaEscapeCharacters_expectStringWithEscapeCharacters() throws Exception {
        validJsonString = new JSONTokenizer("\"\\b \\r \\t \\n \\f \\' \\\" \\\\ \\/\"");
        final String nextValue = (String) validJsonString.nextValue();
        final String expected = "" + '\b' + " " + '\r' + " " + '\t' + " " + '\n' + " " + '\f' + " " + '\'' + " " + '\"' + " " + '\\' + " " + '/';
        assertEquals(expected, nextValue);
    }

    @Test
    public void nextValue_unicodeCharacters_expectStringWithCharacters() throws Exception {
        validJsonString = new JSONTokenizer("\"\\u20AC\"");
        final String nextValue = (String) validJsonString.nextValue();
        final String expected = "" + EURO_CHAR;
        assertEquals(expected, nextValue);
    }

    @Test(expected = JSONException.class)
    public void nextValue_illegalEscapeCharacters_expectException() throws Exception {
        validJsonString = new JSONTokenizer("\"\\g\"");
        validJsonString.nextValue(); //Should throw an Exception as the escaped character is illegal
    }

    @Test
    public void nextValue_TrueAndFalseValuesSeperatedByComma_expectTrueAndFalse() throws Exception {
        validJsonString = new JSONTokenizer("" + true + "#" + false);
        Boolean nextValue = (Boolean) validJsonString.nextValue();
        assertTrue(nextValue);
        validJsonString.next(); //This is the space. Not sure that this is correct way of doing this.
        nextValue = (Boolean) validJsonString.nextValue();
        assertFalse(nextValue);
    }

    @Test
    public void nextValue_withJSONObjectStart_expectJsonObject() throws Exception {
        validJsonString = new JSONTokenizer("{\"Key1\":\"Value1\"}");

        final Object result = validJsonString.nextValue();

        assertTrue(result instanceof JSONObject);

        final JSONObject jsonObjectResult = (JSONObject) result;

        assertEquals("Value1", jsonObjectResult.getString("Key1"));
    }

    @Test
    public void nextValue_withJSONArrayUsingSquareBrackets_expectJsonArray() throws Exception {
        validJsonString = new JSONTokenizer("[{\"Key1\":\"Value1\"}]");
        final Object result = validJsonString.nextValue();

        assertTrue(result instanceof JSONArray);

        final JSONArray jsonArrayResult = (JSONArray) result;

        final JSONObject jsonObject = jsonArrayResult.getJSONObject(0);
        assertEquals("Value1", jsonObject.getString("Key1"));
    }

    @Test
    public void nextValue_withJSONArrayUsingRoundBrackets_expectJsonArray() throws Exception {
        validJsonString = new JSONTokenizer("({\"Key1\":\"Value1\"})");
        final Object result = validJsonString.nextValue();

        assertTrue(result instanceof JSONArray);

        final JSONArray jsonArrayResult = (JSONArray) result;

        final JSONObject jsonObject = jsonArrayResult.getJSONObject(0);
        assertEquals("Value1", jsonObject.getString("Key1"));
    }
}
