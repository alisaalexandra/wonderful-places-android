package com.ace.ucv.wonderful.places.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ace.ucv.wonderful.places.R
import com.ace.ucv.wonderful.places.domain.WonderfulPlaceDO
import kotlinx.android.synthetic.main.activity_wonderful_place_detail.*

class WonderfulPlaceDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wonderful_place_detail)

        var wonderfulPlaceDetailModel: WonderfulPlaceDO? = null

        if (intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)) {
            wonderfulPlaceDetailModel =
                intent.getSerializableExtra(MainActivity.EXTRA_PLACE_DETAILS) as WonderfulPlaceDO
        }

        if (wonderfulPlaceDetailModel != null) {

            setSupportActionBar(toolbar_wonderful_place_detail)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.title = wonderfulPlaceDetailModel.title

            toolbar_wonderful_place_detail.setNavigationOnClickListener { onBackPressed() }

            iv_place_image.setImageURI(Uri.parse(wonderfulPlaceDetailModel.image))
            tv_description.text = wonderfulPlaceDetailModel.description
            tv_location.text = wonderfulPlaceDetailModel.location
        }

        btn_view_on_map.setOnClickListener {
            val intent = Intent(this@WonderfulPlaceDetailActivity, MapActivity::class.java)
            intent.putExtra(MainActivity.EXTRA_PLACE_DETAILS, wonderfulPlaceDetailModel)
            startActivity(intent)
        }
    }
}