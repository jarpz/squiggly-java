package com.github.bohnman.squiggly.function;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.bohnman.squiggly.function.annotation.SquigglyMethod;
import com.github.bohnman.squiggly.util.SquigglyUtils;
import com.github.bohnman.squiggly.util.array.ArrayWrapper;
import com.github.bohnman.squiggly.util.array.ArrayWrappers;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class DefaultFunctions {

    public static void main(String[] args) {
        Object array = new int[] {4, 5, 6};
    }

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    // Collection Functions
    @SquigglyMethod
    public Object first(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof String) {
            String string = (String) value;
            return string.isEmpty() ? "" : string.substring(0, 1);
        }

        if (value.getClass().isArray()) {
            ArrayWrapper wrapper = ArrayWrappers.create(value);
            int len = wrapper.size();
            return len == 0 ? null : wrapper.get(0);
        }

        if (value instanceof Iterable) {
            Iterable iterable = (Iterable) value;
            return Iterables.getFirst(iterable, null);
        }

        return value;
    }

    @SquigglyMethod
    public Object last(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof String) {
            String string = (String) value;
            return string.isEmpty() ? "" : string.substring(string.length() - 1);
        }

        if (value.getClass().isArray()) {
            ArrayWrapper wrapper = ArrayWrappers.create(value);
            int len = wrapper.size();
            return len == 0 ? null : wrapper.get(len - 1);
        }

        if (value instanceof Iterable) {
            Iterable iterable = (Iterable) value;
            return Iterables.getLast(iterable, null);
        }

        return value;
    }

    public static Object keys(Object value) {
        if (value.getClass().isArray()) {
            int length = ArrayWrappers.create(value).size();
            int[] keys = new int[length];
            for (int i = 0; i < length; i++) {
                keys[i] = i;
            }

            return keys;
        }

        if (value instanceof Iterable) {
            int length = Iterables.size((Iterable) value);
            List<Integer> keys = new ArrayList<>(length);

            for (int i = 0; i < length; i++) {
                keys.add(i);
            }

            return keys;
        }

        if (value instanceof Map) {
            return Lists.newArrayList(((Map) value).keySet());
        }

        return Collections.emptyList();
    }

    @SquigglyMethod
    public static Object pick(Object... indexes) {
        return null;
    }

    @SquigglyMethod
    public static Object reverse(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof String) {
            return StringUtils.reverse((String) value);
        }

        if (value.getClass().isArray()) {
            return ArrayWrappers.create(value).reverse().getArray();
        }

        if (value instanceof Iterable) {
            List list = Lists.newArrayList((Iterable) value);
            Collections.reverse(list);
            return list;
        }

        return value;
    }

    // TODO: pick
    // TODO: sort/orderBy
    // TODO: filter/where
    // TODO: keys Jtwig

    @SquigglyMethod
    public static Object limit(Object value, int limit) {
        if (limit < 0) {
            return slice(value, limit);
        } else {
            return slice(value, 0, limit);
        }
    }


    @SquigglyMethod
    public static Object slice(Object value, int start) {
        if (value instanceof String) {
            return StringUtils.substring((String) value, start);
        }

        if (value.getClass().isArray()) {
            ArrayWrapper wrapper = ArrayWrappers.create(value);
            int len = wrapper.size();
            int realStart = normalizeIndex(start, len);
            return wrapper.slice(realStart).getArray();
        }

        if (value instanceof Iterable) {
            Iterable iterable = (Iterable) value;
            List list = (iterable instanceof List) ? (List) iterable : Lists.newArrayList(iterable);
            int realStart = normalizeIndex(start, list.size());
            int realEnd = list.size();
            return (realStart <= realEnd) ? Collections.emptyList() : list.subList(realStart, realEnd);
        }

        return value;
    }


    @SquigglyMethod
    public static Object slice(Object value, int start, int end) {
        if (value instanceof String) {
            return StringUtils.substring((String) value, start, end);
        }

        if (value.getClass().isArray()) {
            ArrayWrapper wrapper = ArrayWrappers.create(value);
            int len = wrapper.size();
            int realStart = normalizeIndex(start, len);
            int realEnd = normalizeIndex(end, len);
            return wrapper.slice(realStart, realEnd).getArray();
        }

        if (value instanceof Iterable) {
            Iterable iterable = (Iterable) value;
            List list = (iterable instanceof List) ? (List) iterable : Lists.newArrayList(iterable);
            int realStart = normalizeIndex(start, list.size());
            int realEnd = normalizeIndex(end, list.size());
            return (realStart <= realEnd) ? Collections.emptyList() : list.subList(realStart, realEnd);
        }

        return value;
    }

    private static int normalizeIndex(int index, int length) {
        if (length == 0) {
            return 0;
        }

        if (index < 0) {
            return Math.max(0, length + index);
        }

        return Math.min(index, length);
    }


    // String Functions

    @SquigglyMethod
    public static String format(String value, Object... args) {
        if (value == null) {
            return null;
        }

        try {
            return String.format(value, args);
        } catch (IllegalFormatException e) {
            return value;
        }
    }

    @SquigglyMethod
    public static String join(Object value, String separator) {
        if (value == null) {
            return null;
        }

        if (separator == null) {
            separator = "";
        }

        if (value.getClass().isArray()) {
            ArrayWrapper wrapper = ArrayWrappers.create(value);
            int len = wrapper.size();
            StringBuilder builder = new StringBuilder();

            for (int i = 0; i < len; i++) {
                if (i > 0) {
                    builder.append(separator);
                }

                builder.append(SquigglyUtils.toString(wrapper.get(i)));
            }

            return builder.toString();
        }

        if (value instanceof Iterable) {
            List list = (value instanceof List) ? (List) value : Lists.newArrayList((Iterable) value);
            StringBuilder builder = new StringBuilder();

            for (int i = 0; i < list.size(); i++) {
                if (i > 0) {
                    builder.append(separator);
                }

                builder.append(SquigglyUtils.toString(list.get(i)));
            }

            return builder.toString();
        }

        return SquigglyUtils.toString(value);
    }

    public static List<String> split(String value, Object separator) {
        if (value == null) {
            return Collections.emptyList();
        }

        if (separator == null) {
            return Collections.singletonList(value);
        }

        if (separator instanceof String) {
            return Arrays.asList(StringUtils.split(value, (String) separator));
        }

        if (separator instanceof Pattern) {
            return Splitter.on((Pattern) separator).splitToList(value);
        }

        return Collections.singletonList(value);
    }

    @SquigglyMethod
    public static String repeat(String string, Integer times) {
        if (times == null) {
            times = 0;
        }

        return StringUtils.repeat(string, times);
    }


    @SquigglyMethod
    public static Object parseJson(String json) {
        try {
            return OBJECT_MAPPER.readValue(json, Object.class);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @SquigglyMethod
    public static String repeat(String string, String seperator, Integer times) {
        if (times == null) {
            times = 0;
        }

        return StringUtils.repeat(string, seperator, times);
    }

    @SquigglyMethod
    public static String leftPad(String value, int size, String padding) {
        return StringUtils.leftPad(value, size, padding);
    }

    @SquigglyMethod
    public static String rightPad(String value, int size, String padding) {
        return StringUtils.rightPad(value, size, padding);
    }

    @SquigglyMethod
    public static String replace(String value, Object search, String replace) {
        if (value == null) {
            return null;
        }

        if (search == null) {
            return value;
        }

        if (search instanceof String) {
            return StringUtils.replace(value, (String) search, replace);
        }

        if (search instanceof Pattern) {
            return ((Pattern) search).matcher(value).replaceAll(replace);
        }

        return value;
    }

    @SquigglyMethod
    public static String replaceFirst(String value, Object search, String replace) {
        if (value == null) {
            return null;
        }

        if (search == null) {
            return value;
        }

        if (search instanceof String) {
            return StringUtils.replace(value, (String) search, replace, 1);
        }

        if (search instanceof Pattern) {
            return ((Pattern) search).matcher(value).replaceFirst(replace);
        }

        return value;
    }

    // TODO: concat: JTwig


    @SquigglyMethod(aliases = "capitalise")
    public static String capitalize(String value) {
        return StringUtils.capitalize(value);
    }

    @SquigglyMethod
    public static String lower(String value) {
        return StringUtils.upperCase(value);
    }

    @SquigglyMethod
    public static String trim(String value) {
        return StringUtils.trim(value);
    }

    @SquigglyMethod
    public static String upper(String value) {
        return StringUtils.upperCase(value);
    }

    // Number functions

    @SquigglyMethod
    public static Number abs(Number n) {
        if (n == null) {
            return null;
        }

        return Math.abs(n.doubleValue());
    }

    @SquigglyMethod
    public static Number add(Number n1, Number n2) {
        if (n1 == null) {
            return null;
        }

        if (n2 == null) {
            return n1;
        }

        return n1.doubleValue() + n2.doubleValue();
    }

    @SquigglyMethod
    public static Number subtract(Number n1, Number n2) {
        if (n1 == null) {
            return null;
        }

        if (n2 == null) {
            return n1;
        }

        return n1.doubleValue() - n2.doubleValue();
    }

    @SquigglyMethod
    public static Number multiply(Number n1, Number n2) {
        if (n1 == null) {
            return null;
        }

        if (n2 == null) {
            return 0;
        }

        return n1.doubleValue() * n2.doubleValue();
    }

    @SquigglyMethod
    public static Number divide(Number n1, Number n2) {
        if (n1 == null) {
            return null;
        }

        if (n2 == null) {
            return 0;
        }

        return n1.doubleValue() / n2.doubleValue();
    }

    @SquigglyMethod
    public static Number ceil(Number n) {
        if (n == null) return null;
        return Math.ceil(n.doubleValue());
    }

    @SquigglyMethod
    public static Number floor(Number n) {
        if (n == null) return null;
        return Math.floor(n.doubleValue());
    }

    @SquigglyMethod
    public static Number round(Number n) {
        if (n == null) return null;
        return Math.round(n.doubleValue());
    }

    @SquigglyMethod
    public static Number min(Number n1, Number n2) {
        if (n1 == null && n2 == null) {
            return null;
        }

        if (n1 == null) {
            return n2;
        }

        if (n2 == null) {
            return n1;
        }

        return Math.min(n1.doubleValue(), n2.doubleValue());
    }

    @SquigglyMethod
    public static Number max(Number n1, Number n2) {
        if (n1 == null && n2 == null) {
            return null;
        }

        if (n1 == null) {
            return n2;
        }

        if (n2 == null) {
            return n1;
        }

        return Math.max(n1.doubleValue(), n2.doubleValue());
    }

    @SquigglyMethod
    public static Number parseNumber(String value, String pattern) {
        try {
            DecimalFormat parser = new DecimalFormat(pattern);
            return parser.parse(value);
        } catch (IllegalArgumentException | ParseException e) {
            return null;
        }
    }

    @SquigglyMethod
    public static Number parseNumber(String value, String pattern, String... otherPatterns) {
        Number number = parseNumber(value, pattern);

        if (number == null) {
            for (String otherPattern : otherPatterns) {
                number = parseNumber(value, otherPattern);

                if (number != null) {
                    break;
                }
            }
        }

        return number;
    }

    //

    // TODO: parseNumber

    //-------------------------------------------------------------------------
    // Object Functions
    //-------------------------------------------------------------------------

    @SquigglyMethod("defaultEmpty")
    public Object defaultEmpty(Object o1, Object o2) {
        return isEmpty(o1) ? o2 : o1;
    }

    @SquigglyMethod("defaultEmpty")
    public Object defaultEmpty(Object o1, Object o2, Object... oN) {
        Object value = isEmpty(o1) ? o2 : o1;

        if (isEmpty(value)) {
            for (Object o : oN) {
                if (!isEmpty(o)) {
                    value = o;
                    break;
                }
            }
        }

        return value;
    }

    private static boolean isEmpty(Object o) {
        if (o == null) {
            return true;
        }

        if (o instanceof String && ((String) o).isEmpty()) {
            return true;
        }

        if (o.getClass().isArray() && ArrayWrappers.create(o).isEmpty()) {
            return true;
        }

        if (o instanceof Iterable && Iterables.isEmpty((Iterable) o)) {
            return true;
        }

        return false;
    }


    @SquigglyMethod("default")
    public Object defaultObject(Object o1, Object o2) {
        return (o1 == null) ? o2 : o1;
    }

    @SquigglyMethod("default")
    public Object defaultObject(Object o1, Object o2, Object... oN) {
        Object value = (o1 == null) ? o2 : o1;

        if (value == null) {
            for (Object o : oN) {
                if (o != null) {
                    value = o;
                    break;
                }
            }
        }

        return value;
    }

    @SquigglyMethod(aliases = "val")
    public static Object value(Object to) {
        return to;
    }

    @SquigglyMethod(aliases = "val")
    public static Object value(Object input, Object to) {
        return to;
    }


    // Date functions
    // TODO: formatDate appkit
    // TODO: formatDuration appkit
    // TODO: add(Number, String unit)
    // TODO: subtract(Number, String unit)
    // TODO: round(Number, String unit)
    // TODO: ceil(Number, String unit)
    // TODO: floor(Number, String unit)
    // TODO: parseDate


    @SquigglyMethod
    public static Object foo(Object value, String... args) {
        System.out.println("foo(" + value + ")" + Arrays.toString(args));
        return value;
    }


}