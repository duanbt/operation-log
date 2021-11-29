package top.aceofspades.oplog.core.service.impl;

import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import top.aceofspades.oplog.core.service.IFunctionService;
import top.aceofspades.oplog.core.service.IParseFunction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: duanbt
 * @create: 2021-11-17 17:34
 **/
public class DefaultFunctionServiceImpl implements IFunctionService {

    private final Map<String, IParseFunction> allFunctionMap;

    public DefaultFunctionServiceImpl(List<IParseFunction> parseFunctions) {
        allFunctionMap = new HashMap<>();
        if (CollectionUtils.isEmpty(parseFunctions)) {
            return;
        }
        for (IParseFunction parseFunction : parseFunctions) {
            if (!StringUtils.hasText(parseFunction.functionName())) {
                continue;
            }
            allFunctionMap.put(parseFunction.functionName(), parseFunction);
        }
    }

    @Override
    public Object apply(String functionName, Object[] args) {
        IParseFunction function = allFunctionMap.get(functionName);
        if(function == null) {
            throw new IllegalArgumentException("function does not exist: " + functionName);
        }
        return function.apply(args);
    }

    @Override
    public boolean isBeforeFunction(String functionName) {
        return allFunctionMap.get(functionName) != null && allFunctionMap.get(functionName).isBefore();
    }
}
