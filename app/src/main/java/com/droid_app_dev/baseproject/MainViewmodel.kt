package com.droid_app_dev.baseproject

import androidx.lifecycle.ViewModel
import com.droid_app_dev.database_module.RoomDataBase
import javax.inject.Inject

class MainViewmodel   @Inject constructor(val movie:RoomDataBase):ViewModel(
) {

    init {

    }




}