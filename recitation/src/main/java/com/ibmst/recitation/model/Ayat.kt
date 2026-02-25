package com.ibmst.recitation.model

class Ayat(
    var number: Int,
    var text: String,
    var translationNo: String,
    var translationEn: String,
    var commentary: String,
) {
    var audioTalawat: String? = null
    var audioTranslation: String? = null
}
