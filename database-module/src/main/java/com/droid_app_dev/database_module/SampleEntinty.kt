package com.droid_app_dev.database_module

import androidx.room.Entity
import com.google.gson.annotations.SerializedName


@Entity(tableName = "sample")
data class SampleEntinty (@SerializedName("id") val id: Int,
                          @SerializedName("description") val description: String,
                          @SerializedName("image") val image: String,
                          @SerializedName("title") val title: String,
                          @SerializedName("category") val category: String,)





    fun SampleEntinty.toDomain() = SampleEntinty(
        id = id,
        image = image,
        description = description,
        title = title,
        category = category
    )
