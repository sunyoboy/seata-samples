package com.lieve.parser;

/**
 * @author sunlijiang
 * @date 2019/12/14
 */

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import static com.lieve.parser.Constants.CHAR_REPLACE;
import static com.lieve.parser.Constants.CHAR_SYMBOL;
import static com.lieve.parser.Constants.ZH_REGEX;
import static java.util.regex.Pattern.compile;

/**
 * 几何题解析器
 * 试题分析：根据中心对称得出OB=OD，OA=OC，求出OF=OE，根据SAS推出△DOF≌△BOE即可．
 * 证明：∵△ABO与△CDO关于O点中心对称，
 * ∴OB=OD，OA=OC，
 * ∵AF=CE，
 * ∴OF=OE，
 * ∵在△DOF和△BOE中
 * $\left\{\begin{matrix}OB=OD\\\angle DOF=\angle BOE\\OF=OE\\\end{matrix}\right.$
 * ∴△DOF≌△BOE（SAS），
 * ∴FD=BE．
 */

@Slf4j
public class GeometryExpressionParser {

    public static final String SAS_STRING = "SAS";
    public static final String SAS_STRING_NOTE = "(SAS)";
    public static final String LINE_END_TAG = "<br/>";
    public static final String TRIANGLE_STRING_SOURCE = "△";
    public static final String ANGLE_STRING_SOURCE = "∠";
    public static final String ANGLE_STRING_TARGET = "\\angle ";
    public static final String TRIANGLE_STRING_TARGET = "\\triangle ";

    public static final ImmutableList<String> PATTERN_STRING_LIST = ImmutableList.of("∴", "∵");
    private static final int BYTE_BUFFER_SIZE = 1024 * 1024 * 8;
    private static final char[] BYTE_BUFFER = new char[BYTE_BUFFER_SIZE];

    public static final String DOLLAR_SYMBOL = "$";
    private static final ImmutableMap<String, String> symbolMap = ImmutableMap
            .of("$≌$", "\\overset{\\Large∽}{=}","（","(","）",")","°", "^\\circ$")
            ;

    public static void main(String[] args) {
        List<String> result = getStringFromFile();
        printListString(result);
        // getHandledExpressionType2("△ABO与△CDO关于O点中心对称");
    }

    public static void printListString(List<String> list) {
        for (String line : list) {
            System.out.println(line);
        }
    }

    public static void readContentFromFile() {
        InputStream inputStream = GeometryExpressionParser.class.getClassLoader().getResourceAsStream("com/lieve/base/expression.txt");

        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        int size = 0;
        StringBuilder stringBuilder = new StringBuilder();
        try {
            while ((size = inputStreamReader.read(BYTE_BUFFER)) != -1) {
                stringBuilder.append(new String(BYTE_BUFFER));
            }
            System.out.println(stringBuilder.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getMaxtrixExpression(String line) {
        line = line.replace("\\left\\{","").replace("\\right", "").replace("matrix", "cases");
        return line;
    }

    public static String replaceSymbol(String line) {
        for(Map.Entry<String, String> entry : symbolMap.entrySet()) {
            if (line.contains(entry.getKey())) {
                line = line.replace(entry.getKey(), entry.getValue());
            }
        }
        return line;
    }

    public static List<String> getStringFromFile() {
        List<String> lines = Lists.newArrayListWithCapacity(0);
        try {
            lines = Files.readLines(new File("/Users/moka/envConfig/lieve-base/src/main/java/com/lieve/base/expression.txt"), Charset.forName("UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<String> result = Lists.newArrayListWithExpectedSize(lines.size());
        for (String source : lines) {
            String target = getTriangleHandledExpression(source);
            target = replaceSas(target);
            target = getCharacterHandledExpression(target, CHAR_SYMBOL);
            target = getConvertedExpress(target);
            target = getHandledExpression(target);
            target = appendLineEndTag(target);

            target = getMaxtrixExpression(target);
            target = replaceSymbol(target);
            target = getHandledExpression2(target);
            result.add(target);
            // System.out.println(source + " -> " + target);
        }
        return result;
    }

    public static String replaceSas(String line) {
        return line.replace(SAS_STRING, new StringBuilder("$\\rm ").append(SAS_STRING).append(DOLLAR_SYMBOL));
    }

    public static String appendLineEndTag(String line) {
        return new StringBuilder(line).append(LINE_END_TAG).toString();
    }

    private static String getConvertedExpress(String line) {
        for (String symbol : PATTERN_STRING_LIST) {
            if (line.contains(symbol)) {
                line = line.replace(symbol, new StringBuffer(DOLLAR_SYMBOL).append(symbol).append(DOLLAR_SYMBOL));
            }


            if (line.contains(TRIANGLE_STRING_SOURCE)) {
                line = line.replace(TRIANGLE_STRING_SOURCE, TRIANGLE_STRING_TARGET);
            }

            if (line.contains(ANGLE_STRING_SOURCE)) {
                line = line.replace(ANGLE_STRING_SOURCE, ANGLE_STRING_TARGET);
            }

        }
        return line;
    }


    public static String getHandledExpression(String line) {
        if (line.contains("matrix")) {
            return line;
        }
        Pattern pattern = compile("([A-Z0-9]+=[A-Z0-9]+)");
        Matcher matcher = pattern.matcher(line);
        while (matcher.find()) {
            String matchString = matcher.group(1);
            line = line.replace(matchString, new StringBuffer(DOLLAR_SYMBOL).append(matchString).append(DOLLAR_SYMBOL));
        }

        return line;
    }




    public static String getHandledExpression2(String line) {
        if (line.contains("matrix")) {
            return line;
        }
        Pattern pattern = compile("([A-Z]+⊥[A-Z]+)");
        Matcher matcher = pattern.matcher(line);
        while (matcher.find()) {
            String matchString = matcher.group(1);
            line = line.replace(matchString, new StringBuffer(DOLLAR_SYMBOL).append(matchString.replace("⊥", "\\perp")).append(DOLLAR_SYMBOL));
        }

        pattern = compile("([A-Z]+∥[A-Z]+)");
        matcher = pattern.matcher(line);
        while (matcher.find()) {
            String matchString = matcher.group(1);
            line = line.replace(matchString, new StringBuffer(DOLLAR_SYMBOL).append(matchString.replace("∥", "{//}")).append(DOLLAR_SYMBOL));
        }
        return line;
    }

    public static String getTriangleHandledExpression2(String line) {
        return "";
    }

    public static String getCharacterHandledExpression(String line) {
        Pattern pattern = compile("([A-Z])");
        Matcher matcher = pattern.matcher(line);
        while (matcher.find()) {
            String matchString = matcher.group(1);
//            System.out.println(matchString);
            line = line.replace(matchString, new StringBuffer(DOLLAR_SYMBOL).append(matchString).append(DOLLAR_SYMBOL));
        }
        return line;
    }

    public static String getTriangleHandledExpression(String line) {
        Pattern pattern = compile("(△[A-Z]+)");
        Matcher matcher = pattern.matcher(line);
        while (matcher.find()) {
            String matchString = matcher.group(1);
//            System.out.println(matchString);
            line = line.replace(matchString, new StringBuffer(DOLLAR_SYMBOL).append(matchString).append(DOLLAR_SYMBOL));
        }

        pattern = compile("(∠[A-Z]+)");
        matcher = pattern.matcher(line);
        while (matcher.find()) {
            String matchString = matcher.group(1);
//            System.out.println(matchString);
            line = line.replace(matchString, new StringBuffer(DOLLAR_SYMBOL).append(matchString).append(DOLLAR_SYMBOL));
        }
        return line;
    }


    public static String getCharacterHandledExpression(String line, String regex) {
        Pattern pattern = compile(regex);
        Matcher matcher = pattern.matcher(line);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String matchString = matcher.group(2);
            log.debug("groupCount: {},matchString: {}", matcher.groupCount(), matchString);
            if (!StringUtils.isEmpty(matchString) && !Pattern.matches(ZH_REGEX, matchString)) {
                log.debug("matchString : {}", matchString);
                matcher.appendReplacement(sb, new StringBuffer(matcher.group(1)).append(CHAR_REPLACE).append(matchString).append(CHAR_REPLACE).toString());
            }
        }
        matcher.appendTail(sb);

        return sb.toString().replace(CHAR_REPLACE, DOLLAR_SYMBOL);
    }



}
