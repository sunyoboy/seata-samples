package com.lieve.parser;

import com.google.common.collect.Lists;
import com.google.common.io.Files;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.lieve.parser.Constants.CHAR_REGEX;
import static com.lieve.parser.Constants.CHAR_REPLACE;
import static com.lieve.parser.GeometryExpressionParser.*;
import static com.lieve.parser.GeometryExpressionParser.appendLineEndTag;
import static com.lieve.parser.GeometryExpressionParser.printListString;
import static java.util.regex.Pattern.compile;

/**
 * 代数题解析器
 * @author sunlijiang
 * @date 2019/12/14
 */
@Slf4j
public class AlgebraExpressionParser {
    public static void main(String[] args) {
        List<String> result = getStringFromFile();
        printListString(result);
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
            String target;
            target= getCharacterHandledExpression(source, CHAR_REGEX);
            //target = replaceSymbol(source);
            target = appendLineEndTag(target);
            result.add(target);
            // System.out.println(source + " -> " + target);
        }
        return result;
    }

    public static String getCharacterHandledExpression(String line, String regex) {
        Pattern pattern = compile(regex);
        Matcher matcher = pattern.matcher(line);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String matchString = matcher.group(0);
            matcher.appendReplacement(sb, new StringBuffer(CHAR_REPLACE).append(matchString).append(CHAR_REPLACE).toString());
        }
        matcher.appendTail(sb);

        return sb.toString().replace(CHAR_REPLACE, DOLLAR_SYMBOL);
    }


}
