package com.yunbao.common.utils;

import java.math.BigDecimal;
// +———————————————————————————————————
// | Created by Yunbao
// +———————————————————————————————————
// | Copyright (c) 2013~2022 http://www.yunbaokj.com All rights reserved.
// +———————————————————————————————————
// | Author: https://gitee.com/yunbaokeji
// +———————————————————————————————————
// | Date: 2022-04-30
// +———————————————————————————————————
public class CalculateUtil {

    public static BigDecimal multiply1(String num1, String num2) {
        BigDecimal bigDecimal = new BigDecimal(num1);
        BigDecimal bigDecima2 = new BigDecimal(num2);
        return bigDecimal.multiply(bigDecima2);
    }

    public static double multiply2(String num1, String num2) {
        BigDecimal bigDecimal = new BigDecimal(num1);
        BigDecimal bigDecima2 = new BigDecimal(num2);
        return bigDecimal.multiply(bigDecima2).doubleValue();
    }
}
