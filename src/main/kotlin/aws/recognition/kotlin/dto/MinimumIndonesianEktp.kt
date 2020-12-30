package aws.recognition.kotlin.dto

data class MinimumIndonesianEktp(
    val province:String?,
    val cityOrDistrict:String?,
    val NIK:String?,
    val name:String?,
    val pob:String?,
    val dob:String?,
    val gender:String?,
    val address:String?,
    val maritalStatus:String?
)
