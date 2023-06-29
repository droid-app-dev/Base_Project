package com.droid_app_dev.baseproject

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.droid_app_dev.database_module.RoomDataBase
import com.droid_app_dev.network_module.ApiService
import kotlinx.coroutines.launch
import javax.inject.Inject

class MainViewmodel   @Inject constructor(val movie:RoomDataBase,val remote:ApiService):ViewModel(
) {

    init {



        viewModelScope.launch {

          val s= remote.getfacilities()

            Log.d("Hello",""+s.toString());

        }

    }




}