/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package aws.recognition.kotlin

import aws.recognition.kotlin.dto.MinimumIndonesianEktp
import aws.recognition.kotlin.dto.RequestGetResult
import aws.recognition.kotlin.dto.ResultCharAndWord
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.regions.Regions
import com.amazonaws.services.rekognition.AmazonRekognition
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder
import com.amazonaws.services.rekognition.model.*
import org.apache.commons.text.similarity.LongestCommonSubsequence
import java.util.*
import java.util.regex.Pattern
import java.util.stream.Collectors

class App {
    val greeting: String
        get() {
            return "Testing aws recognition"
        }
}


fun main(args: Array<String>) {
    val accessKey = ""
    val secretKey = ""
    val imageFile = ""
    val bucket = ""

    val credentialsProvider = getAWSCredential(accessKey, secretKey)
    var result: DetectTextResult = DetectTextResult()
    try {
        result = getAmazonRekognitionClient(credentialsProvider)
            .detectText(buildAmazonRekognitionRequest(credentialsProvider, imageFile, bucket))
    } catch (e: AmazonRekognitionException) {
        e.printStackTrace()
    }
    val a = extractMinimumEktp(result)
    return println(App().greeting)

}

private fun getAWSCredential(accessKey: String, secretKey: String): AWSStaticCredentialsProvider {
    return AWSStaticCredentialsProvider(
        BasicAWSCredentials(
            accessKey, secretKey
        )
    )
}


private fun getAmazonRekognitionClient(awsStaticCredentialsProvider: AWSStaticCredentialsProvider): AmazonRekognition {
    return AmazonRekognitionClientBuilder
        .standard()
        .withCredentials(awsStaticCredentialsProvider)
        .withRegion(Regions.AP_SOUTHEAST_1)
        .build()
}

private fun buildAmazonRekognitionRequest(
    awsStaticCredentialsProvider: AWSStaticCredentialsProvider,
    image: String, bucket: String
): DetectTextRequest {
    val request = DetectTextRequest()
    request.setRequestCredentialsProvider(awsStaticCredentialsProvider)
    request.withImage(
        Image()
            .withS3Object(
                S3Object()
                    .withName(image)
                    .withBucket(bucket)
            )
    )
    return request
}


private fun extractMinimumEktp(request: DetectTextResult): MinimumIndonesianEktp {
    val line = request.getTextDetections().stream()
        .filter { b -> b.type.equals("LINE", ignoreCase = true) }
        .map { s -> s.detectedText }
        .collect(Collectors.toList())

    return MinimumIndonesianEktp(
        trimProvince(line.get(0)),
        trimCityOrDistrict(line.get(1)),
        getValueNik(request),
        getValueName(line),
        getValuePob(line),
        getValueDob(line),
        getValueGender(line),
        getValueAddress(line),
        getValueMaritalStatus(line)
    )
}

private fun trimCityOrDistrict(request: String) :String{
    val patternKabupaten = Regex("KABUP")
    val patternKota = Regex("KOTA")
    val result : String
    if(patternKabupaten.containsMatchIn(request)) {
        result =  request.substring(10)
    }else if (patternKota.containsMatchIn(request)){
        result = request.substring(5)
    }else{
        result = request
    }
    return result
}

private fun trimProvince(request: String) :String{
    val pattern = Regex("X")
    val result : String
    if(pattern.containsMatchIn(request)) {
        result =  request.substring(11)
    }else{
        if(request.get(8)!==' '){
            return request.substring(8)
        }
        result = request.substring(9)
    }
    return result
}

private fun countWords(inputString: String): Int {
    //Split String by Space
    val strArray = inputString.split(" ".toRegex()).toTypedArray() // Spilt String by Space
    val sb = StringBuilder()
    var countWord = 0

    //iterate String array
    strArray.forEach { s ->
        if (s != "") {
            countWord++
        }
    }
    return countWord
}


private fun getValueNik(request: DetectTextResult): String? {
    val word = request.textDetections.stream()
        .filter { b: TextDetection -> b.type.equals("WORD", ignoreCase = true) }
        .map { obj: TextDetection -> obj.detectedText }
        .collect(Collectors.toList())
    return word
        .stream()
        .filter(Pattern.compile("[0-9]{16}").asPredicate())
        .map { o: String? -> Objects.toString(o) }
        .collect(Collectors.joining())
}

private fun getYourValue(requestGetResult: RequestGetResult): String? {
    val name: String = requestGetResult.listrequest!!.stream().map { x ->
        var scoring: Map.Entry<String, Map.Entry<Int, Int>>? = null
        scoring = AbstractMap.SimpleEntry<String, Map.Entry<Int, Int>>(
            x, AbstractMap.SimpleEntry(
                LongestCommonSubsequence().apply(requestGetResult.leftWord, x.toLowerCase()),
                LongestCommonSubsequence().apply(requestGetResult.rightWord, x.toLowerCase())
            )
        )
        scoring
    }.sorted { stringEntryEntry, t1 -> t1!!.value.key.compareTo(stringEntryEntry!!.value.key) }
        .map { x -> x!!.key }
        .findFirst()
        .orElse(null)
    val tempt: String = requestGetResult.listrequest.stream().map { x ->
        var scoring: Map.Entry<String, Map.Entry<Int, Int>>? = null
        scoring = AbstractMap.SimpleEntry<String, Map.Entry<Int, Int>>(
            x, AbstractMap.SimpleEntry(
                LongestCommonSubsequence().apply(requestGetResult.leftWord, x.toLowerCase()),
                LongestCommonSubsequence().apply(requestGetResult.rightWord, x.toLowerCase())
            )
        )
        scoring
    }.sorted { stringEntryEntry, t1 -> t1!!.value.value.compareTo(stringEntryEntry!!.value.value) }.map { x -> x!!.key }
        .findFirst().orElse(null)
    val combine: String = java.lang.String.join(
        " ",
        requestGetResult.listrequest!!.subList(
            requestGetResult.listrequest!!.indexOf(name),
            requestGetResult.listrequest.indexOf(tempt) + 1
        )
    )
    val p = Pattern.compile(requestGetResult.regex, Pattern.CASE_INSENSITIVE)
    val matcher = p.matcher(combine)
    var group = ""
    if (matcher.matches()) {
        group = matcher.group(requestGetResult.groupRegex).replace(":", "").trim { it <= ' ' }
    }
    return group
}


private fun getValueName(request: List<String>): String? {
    val regex = "nama(.*?)(.*?)temp(.*?)$"
    val req = RequestGetResult(request, "nama", "tempat", regex, 2)
    return getYourValue(req)
}

private fun getValuePob(request: List<String>): String? {
    val regex = "(.+?)hir (.+?)([., ]+?)([0-9 \\-]+?) jen(.*?)"
    val req = RequestGetResult(request, "tempat", "jenis", regex, 2)
    return getYourValue(req)
}

private fun getValueDob(request: List<String>): String? {
    val regex = "(.+?)hir (.+?)([., ]+?)([0-9 \\-]+?) jen(.*?)"
    val req = RequestGetResult(request, "tempat", "jenis", regex, 4)
    return getYourValue(req)
}

private fun getValueGender(request: List<String>): String? {
    val regex = "(.*?)min (.*?) gol(.*?)"
    val req = RequestGetResult(request, "kelamin", "gol", regex, 2)
    return getYourValue(req)
}

private fun getValueAddress(request: List<String>): String? {
    val regex = "(.+?)ala(.+?) (.+)rt(.*?)"
    val req = RequestGetResult(request, "ala", "rt/rw", regex, 3)
    return getYourValue(req)
}

private fun getValueMaritalStatus(request: List<String>): String? {
    val regex = "(.*?)winan(.*)KAWIN peker(.*?)"
    val req = RequestGetResult(request, "status", "pekerjaan", regex, 2)
    return getYourValue(req)!!.trim() + "KAWIN"
}
