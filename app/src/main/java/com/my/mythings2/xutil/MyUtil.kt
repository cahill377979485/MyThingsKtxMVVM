package com.my.mythings2.xutil

import android.graphics.Color
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.my.mythings2.model.MyRepository
import com.my.mythings2.model.bean.Thing
import me.drakeet.multitype.Items
import me.drakeet.multitype.MultiTypeAdapter
import java.util.*

/**
 * @author 文琳
 * @time 2020/6/17 9:51
 * @desc 封装一些代码很多或者常用的方法
 */
object MyUtil {

    /**
     * 设置长按拖动
     */
    fun setHelper(rv: RecyclerView?, items: Items?, adapter: MultiTypeAdapter) {
        val helper = ItemTouchHelper(object : ItemTouchHelper.Callback() {
            override fun getMovementFlags(
                recyclerView: RecyclerView,
                viewHolder: ViewHolder
            ): Int {
                return if (recyclerView.layoutManager is GridLayoutManager) {
                    val dragFlags =
                        ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
                    val swipeFlags = 0
                    makeMovementFlags(dragFlags, swipeFlags)
                } else {
                    val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
                    val swipeFlags = 0
                    makeMovementFlags(dragFlags, swipeFlags)
                }
            }

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: ViewHolder,
                target: ViewHolder
            ): Boolean {
                val fromPosition = viewHolder.adapterPosition //得到拖动ViewHolder的position
                val toPosition = target.adapterPosition //得到目标ViewHolder的position
                if (fromPosition < toPosition) {
                    for (i in fromPosition until toPosition) {
                        Collections.swap(items, i, i + 1)
                    }
                } else {
                    for (i in fromPosition downTo toPosition + 1) {
                        Collections.swap(items, i, i - 1)
                    }
                }
                adapter.notifyItemMoved(fromPosition, toPosition)
                return true
            }

            override fun onSwiped(viewHolder: ViewHolder, direction: Int) {}

            override fun onSelectedChanged(
                viewHolder: ViewHolder?,
                actionState: Int
            ) {
                if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
                    viewHolder?.itemView?.setBackgroundColor(Color.LTGRAY)
                }
                if (actionState == ItemTouchHelper.ACTION_STATE_IDLE && items != null && items.size > 0) {
                    val list: MutableList<Thing> = ArrayList()
                    for (i in items.indices) {
                        val o = items[i]
                        if (o is Thing) {
                            o.position = i
                            list.add(o)
                        }
                    }
                    MyRepository().save(list)
                }
                super.onSelectedChanged(viewHolder, actionState)
            }

            override fun clearView(
                recyclerView: RecyclerView,
                viewHolder: ViewHolder
            ) {
                super.clearView(recyclerView, viewHolder)
                viewHolder.itemView.setBackgroundColor(0)
            }
        })
        helper.attachToRecyclerView(rv)
    }

    /**
     * 解析物品和价格
     */
    fun getNameAndPriceArrayByRegex(str: String): Array<String> {
        val arr = arrayOf(str, "")
        str.let {
            Regex("-?\\d*\\.?\\d*$").find(str)?.let {//匹配出价格，将价格替换成空值，则相当于删除了价格，剩下的就是物品名称
                arr[0] = str.replace(it.value, "")
                arr[1] = it.value
            }
            //价格只要是以.xx0 .x0 .0 .00结尾的都要做去除小数的处理
            Regex("\\.\\d*(0+)$").find(arr[1])?.let { result ->
                result.groups[1]?.value?.let {
                    arr[1] = result.value.replace(it, "", ignoreCase = true)
                }
            }
            //前面的处理之后，还需要针对几种特殊情况进行处理，包括：直接以“.”结尾(需改成0)、类似“123.”(需去掉小数点)、类似“.132”(需在最前面加上“0”)
            when {
                arr[1] == "." -> arr[1] = "0"
                arr[1].endsWith(".") -> arr[1] = arr[1].substring(0, arr[1].length - 1)
                arr[1].startsWith(".") -> arr[1] = "0" + arr[1]
            }
        }
        return arr
    }

    /**
     * 解析物品和价格
     */
    fun getNameAndPrice(str: String): Array<String> {
        val arr = arrayOf("", "")
        str.let {
            var priceStr = ""
            var dotNum = 0 //防止出现“1.8.1”这类出现，所以小数点上限为1
            for (i in it.length - 1 downTo 0) {
                val s = it.substring(i, i + 1)
                priceStr = if (s.matches("\\.|\\d".toRegex())) {
                    if (s == ".") dotNum++
                    if (dotNum > 1) break
                    s + priceStr
                } else {
                    break
                }
            }
            val index = if (priceStr.isNotEmpty()) str.indexOf(priceStr) else str.length
            arr[0] = str.substring(0, index) //直接裁剪掉价格只剩名字，如果没有价格，则取原字符串
            arr[1] = if (priceStr.isEmpty()) "0" else priceStr
        }
        return arr
    }
}