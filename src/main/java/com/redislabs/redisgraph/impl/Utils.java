package com.redislabs.redisgraph.impl;

import org.apache.commons.text.translate.AggregateTranslator;
import org.apache.commons.text.translate.CharSequenceTranslator;
import org.apache.commons.text.translate.LookupTranslator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utilities class
 */
public class Utils {
    public static final List<String> DUMMY_LIST = new ArrayList<>(0);
    public static final Map<String, List<String>> DUMMY_MAP = new HashMap<>(0);
    public static final String COMPACT_STRING = "--COMPACT";

    private static final CharSequenceTranslator ESCAPE_CHYPER;
    static {
        final Map<CharSequence, CharSequence> escapeJavaMap = new HashMap<>();
        escapeJavaMap.put("\'", "\\'");
        escapeJavaMap.put("\"", "\\\"");
        ESCAPE_CHYPER = new AggregateTranslator(new LookupTranslator(Collections.unmodifiableMap(escapeJavaMap)));
    }
    
    private Utils() {}

    /**
     * Append the input string surrounded with quotation marks.
     * @param str - a string
     */
    private static void quoteString(StringBuilder sb, String str) {
        str = str.replace("\"", "\\\"");
        sb.ensureCapacity(sb.length() + str.length() + 2);
        sb.append('"').append(str).append('"');
    }


    /**
     * Prepare and formats a query and query arguments
     * @param query - query
     * @param args - query arguments
     * @return formatted query
     * @deprecated use {@link #prepareQuery(String, Map)} instead.
     */
    @Deprecated
    public static String prepareQuery(String query, Object ...args){
        if(args.length > 0) {
            for(int i=0; i<args.length; ++i) {
                if(args[i] instanceof String) {
                    args[i] = "\'" + ESCAPE_CHYPER.translate((String)args[i]) + "\'";
                }
            }
            query = String.format(query, args);
        }
        return query;
    }

    /**
     * Prepare and formats a query and query arguments
     * @param query - query
     * @param params - query parameters
     * @return query with parameters header
     */
    public static String prepareQuery(String query, Map<String, Object> params){
        StringBuilder sb = new StringBuilder("CYPHER ");
        for(Map.Entry<String, Object> entry : params.entrySet()) {
            sb.append(entry.getKey()).append('=');
            valueToString(sb, entry.getValue());
            sb.append(' ');
        }
        sb.append(query);
        return sb.toString();
    }

    private static void arrayToString(StringBuilder sb, Object[] arr) {
        sb.append('[');
        if (arr.length > 0) {
            valueToString(sb, arr[0]);
            for (int i = 1; i < arr.length; i++) {
                sb.append(',').append(' ').append(arr[i]);
            }
        }
        sb.append(']');
    }

    private static void valueToString(StringBuilder sb, Object value) {
        if (value == null) {
            sb.append("null");
        } else if (value instanceof String) {
            quoteString(sb, (String) value);
        } else if (value instanceof Character) {
            quoteString(sb, ((Character) value).toString());
        } else if (value instanceof Object[]) {
            arrayToString(sb, (Object[]) value);
        } else if (value instanceof List) {
            List<Object> list = (List<Object>) value;
            arrayToString(sb, list.toArray());
        } else {
            sb.append(value);
        }
    }

    /**
     * Prepare and format a procedure call and its arguments
     * @param procedure - procedure to invoke
     * @param args - procedure arguments
     * @param kwargs - procedure output arguments
     * @return formatter procedure call
     */
    public static String prepareProcedure(String procedure, List<String> args, Map<String, List<String>> kwargs) {
        StringBuilder querySB = new StringBuilder(procedure.length() + 7);
        querySB.append("CALL ").append(procedure).append('(');
        if (!args.isEmpty()) {
            quoteString(querySB, args.get(0));
            for (int i = 1; i < args.size(); i++) {
                querySB.append(',');
                quoteString(querySB, args.get(i));
            }
        }
        querySB.append(')');
        List<String> kwargsList = kwargs.getOrDefault("y", null);
        if (kwargsList != null && !kwargsList.isEmpty()) {
            querySB.append(kwargsList.get(0));
            for (int i = 1; i < kwargsList.size(); i++) {
                querySB.append(',').append(kwargsList.get(i));

            }
        }
        return querySB.toString();
    }
}
