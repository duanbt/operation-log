package top.aceofspades.oplog.core.parse;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.expression.AnnotatedElementKey;
import org.springframework.expression.EvaluationContext;
import org.springframework.util.StringUtils;
import top.aceofspades.oplog.core.service.IFunctionService;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author: duanbt
 * @create: 2021-11-17 14:17
 **/
public class LogValueParser {

    private final LogExpressionEvaluator expressionEvaluator = new LogExpressionEvaluator();

    private IFunctionService functionService;

    private static final Pattern FUNCTION_PATTERN = Pattern.compile("#(\\w+)\\((.*?)\\)");

    private static final Pattern TEMPLATE_EXPRESSION_PATTERN = Pattern.compile("\\{(.+?)}");

    /**
     * 解析调用before function，获取函数返回值
     *
     * @return functionKey -> 返回值
     */
    public Map<String, Object> parseBeforeFunction(Collection<String> templates,
                                                   Class<?> targetClass, Method method, Object[] args, BeanFactory beanFactory) {
        Map<String, Object> functionKeyAndReturnMap = new HashMap<>();
        EvaluationContext evaluationContext = expressionEvaluator.createEvaluationContext(method, args, targetClass, LogExpressionEvaluator.RESULT_UNAVAILABLE, LogExpressionEvaluator.ERROR_MSG_UNAVAILABLE, beanFactory);

        for (String template : templates) {
            Matcher funMatcher = FUNCTION_PATTERN.matcher(template);
            while (funMatcher.find()) {
                //#fun(2,#user.name)
                String functionKey = funMatcher.group(0);
                //fun
                String functionName = funMatcher.group(1);
                //2,#user.name
                String functionArg = funMatcher.group(2);
                //判断是before function
                if (functionService.isBeforeFunction(functionName)) {
                    AnnotatedElementKey methodKey = new AnnotatedElementKey(method, targetClass);
                    Object functionReturn = getFunctionReturn(functionKeyAndReturnMap, functionKey, functionName, functionArg, methodKey, evaluationContext);
                    functionKeyAndReturnMap.put(functionKey, functionReturn);
                }
            }
        }

        return functionKeyAndReturnMap;
    }

    /**
     * 解析模板
     *
     * @return 模板 -> 解析后的值
     */
    public Map<String, String> parseTemplate(Collection<String> templates,
                                             Class<?> targetClass, Method method, Object[] args,
                                             Object result, String errorMsg, Map<String, Object> beforeFunctionKeyAndReturnMap,
                                             BeanFactory beanFactory) {
        Map<String, Object> functionKeyAndReturnMap = new HashMap<>(beforeFunctionKeyAndReturnMap);
        Map<String, String> expressionValues = new HashMap<>();
        EvaluationContext evaluationContext = expressionEvaluator.createEvaluationContext(method, args, targetClass, result, errorMsg, beanFactory);
        for (String template : templates) {
            AnnotatedElementKey methodKey = new AnnotatedElementKey(method, targetClass);
            Matcher templateExpressionMatcher = TEMPLATE_EXPRESSION_PATTERN.matcher(template);
            if(templateExpressionMatcher.find()) {
                //是模板表达式
                StringBuffer templateExpressionValue = new StringBuffer();
                templateExpressionMatcher.reset();
                while (templateExpressionMatcher.find()) {
                    String expression = templateExpressionMatcher.group(1);
                    Object expressionValue = parseFunctionExpressionValue(functionKeyAndReturnMap, evaluationContext, expression, methodKey);
                    templateExpressionMatcher.appendReplacement(templateExpressionValue, String.valueOf(expressionValue));
                }
                templateExpressionMatcher.appendTail(templateExpressionValue);
                expressionValues.put(template, templateExpressionValue.toString());
            }else {
                //是普通表达式
                Object expressionValue = parseFunctionExpressionValue(functionKeyAndReturnMap, evaluationContext, template, methodKey);
                expressionValues.put(template, String.valueOf(expressionValue));
            }
        }

        return expressionValues;
    }

    private Object parseFunctionExpressionValue(Map<String, Object> functionKeyAndReturnMap, EvaluationContext evaluationContext,
                                                String expression, AnnotatedElementKey methodKey) {
        //处理函数返回值：将表达式中的所有函数表达式替换为变量引用, 如 #fun(2,#user.name) 替换为 #_fun_2__user_name_
        Matcher funMatcher = FUNCTION_PATTERN.matcher(expression);
        StringBuffer funRetVarTemplate = new StringBuffer();
        while (funMatcher.find()) {
            //#fun(2,#user.name)
            String functionKey = funMatcher.group(0);
            //fun
            String functionName = funMatcher.group(1);
            //2,#user.name
            String functionArg = funMatcher.group(2);
            //_fun_2__user_name_
            String funRetVariable = functionKey.replaceAll("\\W", "_");
            Object functionReturn = getFunctionReturn(functionKeyAndReturnMap, functionKey, functionName, functionArg, methodKey, evaluationContext);
            evaluationContext.setVariable(funRetVariable, functionReturn);
            funMatcher.appendReplacement(funRetVarTemplate, "#" + funRetVariable);
        }
        funMatcher.appendTail(funRetVarTemplate);
        //使用spring el 求值
        return expressionEvaluator.parseExpression(funRetVarTemplate.toString(), methodKey, evaluationContext);
    }

    private Object[] parseFunctionArgValues(EvaluationContext evaluationContext, String functionArg, AnnotatedElementKey annotatedElementKey) {
        Object[] functionArgValues = null;
        if (StringUtils.hasText(functionArg)) {
            String[] functionArgExpressions = functionArg.split(",");
            functionArgValues = new Object[functionArgExpressions.length];
            for (int i = 0; i < functionArgExpressions.length; i++) {
                Object argValue = expressionEvaluator.parseExpression(functionArgExpressions[i].trim(), annotatedElementKey, evaluationContext);
                functionArgValues[i] = argValue;
            }
        }
        return functionArgValues;
    }

    private Object getFunctionReturn(Map<String, Object> functionKeyAndReturnMap,
                                     String functionKey, String functionName, String functionArg,
                                     AnnotatedElementKey methodKey, EvaluationContext evaluationContext) {
        Object functionReturn = null;
        if(functionKeyAndReturnMap != null) {
            functionReturn = functionKeyAndReturnMap.get(functionKey);
        }
        if(functionReturn == null) {
            Object[] functionArgValues = parseFunctionArgValues(evaluationContext, functionArg, methodKey);
            functionReturn = functionService.apply(functionName, functionArgValues);
        }
        return functionReturn;
    }

    public void setFunctionService(IFunctionService functionService) {
        this.functionService = functionService;
    }

}
