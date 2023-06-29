package com.droid_app_dev.network_module

import com.google.gson.annotations.SerializedName

data class Facilitiesdata(

    @SerializedName("facilities" ) var facilities : ArrayList<Facilities>            = arrayListOf(),
    @SerializedName("exclusions" ) var exclusions : ArrayList<ArrayList<Exclusions>> = arrayListOf())



data class Options (

    @SerializedName("name" ) var name : String? = null,
    @SerializedName("icon" ) var icon : String? = null,
    @SerializedName("id"   ) var id   : String? = null

)

data class Facilities (

    @SerializedName("facility_id" ) var facilityId : String?            = null,
    @SerializedName("name"        ) var name       : String?            = null,
    @SerializedName("options"     ) var options    : ArrayList<Options> = arrayListOf()

)

data class Exclusions (

    @SerializedName("facility_id" ) var facilityId : String? = null,
    @SerializedName("options_id"  ) var optionsId  : String? = null

)