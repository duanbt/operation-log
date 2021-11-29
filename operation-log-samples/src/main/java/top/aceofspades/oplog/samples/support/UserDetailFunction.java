package top.aceofspades.oplog.samples.support;

import org.springframework.stereotype.Component;
import top.aceofspades.oplog.core.service.IParseFunction;
import top.aceofspades.oplog.samples.beans.User;

/**
 * User userDetail(String idCard)
 * @author: duanbt
 * @create: 2021-11-25 19:30
 **/
@Component
public class UserDetailFunction implements IParseFunction {

    @Override
    public boolean isBefore() {
        return true;
    }

    @Override
    public String functionName() {
        return "userDetail";
    }

    @Override
    public User apply(Object[] args) {
        if(args != null) {
            Object arg = args[0];
            if(arg != null) {
                if(arg instanceof String) {
                    return queryUserDetailByName((String)arg);
                }
            }
        }
        return null;
    }

    private User queryUserDetailByName(String name) {
        User user = new User();
        user.setName(name);
        user.setAge(18);
        return user;
    }
}
