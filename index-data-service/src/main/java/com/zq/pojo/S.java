package com.zq.pojo;

import java.lang.reflect.Field;

public class S {
    public static void main(String[] args) {
        String s = "AAA";
        try {
            Field value = String.class.getDeclaredField("value");
            value.setAccessible(true);
            char[] va = (char[]) value.get(s);
            va[1] = 'B';
            System.out.println(s);
            System.out.println(s == "AAA");
            System.out.println("AAA".toString());
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
