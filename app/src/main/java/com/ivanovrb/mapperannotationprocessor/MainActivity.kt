package com.ivanovrb.mapperannotationprocessor

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ivanovrb.data.CarDto
import com.ivanovrb.data.UserMap
import com.ivanovrb.data.mapToCar
import com.ivanovrb.data.mapToUser
import com.ivanovrb.mapperannotationprocessor.data.CatDto
import com.ivanovrb.mapperannotationprocessor.data.mapToCat

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        CatDto().mapToCat()
        UserMap().mapToUser()
        CarDto().mapToCar()
    }
}
