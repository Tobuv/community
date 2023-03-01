package com.toubv.community.util;

import com.toubv.community.entity.User;
import org.springframework.stereotype.Component;

@Component
public class HostHolder {

    ThreadLocal<User> threadLocal = new ThreadLocal<>();

    public void set(User user){
        threadLocal.set(user);
    }

    public User get(){
        return threadLocal.get();
    }

    public void clear(){
        threadLocal.remove();
    }
}
