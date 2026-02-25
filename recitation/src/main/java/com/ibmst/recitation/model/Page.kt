package com.ibmst.recitation.model

import java.util.ArrayList

class Page(var pageNo: Int, var suraNo: Int, var ayatStart: Int, var ayatEnd: Int) {
    var isSuraStart = false
    var ayatList: ArrayList<Ayat> = arrayListOf()

    /* sura tuba*/
    val isBismillahPage: Boolean
        get() = if (ayatList.size > 0 && suraNo != 9) {
            ayatList[0].number == 1
        } else {
            false
        }

    override fun toString(): String {
        return "Page{" +
            " ayatStart=" + ayatStart +
            ", ayatEnd=" + ayatEnd +
            ", pageNo=" + pageNo +
            ", Ayats=" + (if (ayatList != null) ayatList!!.size else 0) +
            '}'
    }
}
