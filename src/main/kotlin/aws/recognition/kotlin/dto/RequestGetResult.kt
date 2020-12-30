package aws.recognition.kotlin.dto

data class RequestGetResult(
    val listrequest:List<String>?,
    val leftWord:String?,
    val rightWord:String?,
    val regex:String?,
    val groupRegex:Int
)

