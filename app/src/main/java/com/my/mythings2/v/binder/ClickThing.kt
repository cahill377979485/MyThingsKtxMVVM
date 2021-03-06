package com.my.mythings2.v.binder

import com.my.mythings2.m.bean.Thing

/**
 * @author 文琳
 * @time 2020/6/22 16:00
 * @desc 物品的点击事件
 */
interface ClickThing {
    fun onClick(thing: Thing)
}