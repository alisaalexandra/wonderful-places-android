package com.ace.ucv.wonderful.places.activities

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*
import com.ace.ucv.wonderful.places.R
import com.ace.ucv.wonderful.places.adapters.WonderfulPlacesAdapter
import com.ace.ucv.wonderful.places.database.DatabaseHandler
import com.ace.ucv.wonderful.places.domain.WonderfulPlaceDO
import com.ace.ucv.wonderful.places.utils.SwipeToDeleteCallback
import com.ace.ucv.wonderful.places.utils.SwipeToEditCallback


class MainActivity : AppCompatActivity() {

    private lateinit var notificationManager: NotificationManager
    private lateinit var notificationChannel: NotificationChannel
    private lateinit var builder: Notification.Builder
    private val channelId = "i.apps.notifications"
    private val description = "Test notification"

    /**
     * This function is auto created by Android when the Activity Class is created.
     */
    @SuppressLint("UnspecifiedImmutableFlag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewAccelerometer.setOnClickListener {
            val accelerometerIntent = Intent(this@MainActivity, AccelerometerActivity::class.java)
            startActivity(accelerometerIntent)
        }

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val pendingIntent =
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        fabAddWonderfulPlace.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                notificationChannel =
                    NotificationChannel(channelId, description, NotificationManager.IMPORTANCE_HIGH)
                notificationChannel.enableLights(true)
                notificationChannel.lightColor = Color.GREEN
                notificationChannel.enableVibration(false)
                notificationManager.createNotificationChannel(notificationChannel)

                builder = Notification.Builder(this, channelId)
                    .setSmallIcon(R.drawable.ic_notifications_none_black)
                    .setContentTitle("Hello Stranger!")
                    .setContentText("Thanks for using our application.")
                    .setLargeIcon(
                        BitmapFactory.decodeResource(
                            this.resources,
                            R.drawable.ic_notifications_none_black
                        )
                    )
                    .setContentIntent(pendingIntent)
            } else {

                builder = Notification.Builder(this)
                    .setSmallIcon(R.drawable.ic_notifications_none_black)
                    .setLargeIcon(
                        BitmapFactory.decodeResource(
                            this.resources,
                            R.drawable.ic_notifications_none_black
                        )
                    )
                    .setContentTitle("Hello Stranger!")
                    .setContentText("Thanks for using our application.")
                    .setContentIntent(pendingIntent)
            }
            notificationManager.notify(1234, builder.build())
            val intent = Intent(this@MainActivity, AddWonderfulPlaceActivity::class.java)

            startActivityForResult(intent, ADD_PLACE_ACTIVITY_REQUEST_CODE)
        }

        getWonderfulPlacesListFromLocalDB()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ADD_PLACE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                getWonderfulPlacesListFromLocalDB()
            } else {
                Log.e("Activity", "Cancelled or Back Pressed")
            }
        }
    }

    /**
     * A function to get the list of wonderful place from local database.
     */
    private fun getWonderfulPlacesListFromLocalDB() {

        val dbHandler = DatabaseHandler(this)

        val getWonderfulPlacesList = dbHandler.getWonderfulPlaces()

        if (getWonderfulPlacesList.size > 0) {
            rv_wonderful_places_list.visibility = View.VISIBLE
            tv_no_records_available.visibility = View.GONE
            setupWonderfulPlacesRecyclerView(getWonderfulPlacesList)
        } else {
            rv_wonderful_places_list.visibility = View.GONE
            tv_no_records_available.visibility = View.VISIBLE
        }
    }

    /**
     * A function to populate the recyclerview to the UI.
     */
    private fun setupWonderfulPlacesRecyclerView(wonderfulPlacesList: ArrayList<WonderfulPlaceDO>) {

        rv_wonderful_places_list.layoutManager = LinearLayoutManager(this)
        rv_wonderful_places_list.setHasFixedSize(true)

        val placesAdapter = WonderfulPlacesAdapter(this, wonderfulPlacesList)
        rv_wonderful_places_list.adapter = placesAdapter

        placesAdapter.setOnClickListener(object :
            WonderfulPlacesAdapter.OnClickListener {
            override fun onClick(position: Int, model: WonderfulPlaceDO) {
                val intent = Intent(this@MainActivity, WonderfulPlaceDetailActivity::class.java)
                intent.putExtra(EXTRA_PLACE_DETAILS, model)
                startActivity(intent)
            }
        })

        val editSwipeHandler = object : SwipeToEditCallback(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = rv_wonderful_places_list.adapter as WonderfulPlacesAdapter
                adapter.notifyEditItem(
                    this@MainActivity,
                    viewHolder.adapterPosition,
                    ADD_PLACE_ACTIVITY_REQUEST_CODE
                )
            }
        }
        val editItemTouchHelper = ItemTouchHelper(editSwipeHandler)
        editItemTouchHelper.attachToRecyclerView(rv_wonderful_places_list)

        val deleteSwipeHandler = object : SwipeToDeleteCallback(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = rv_wonderful_places_list.adapter as WonderfulPlacesAdapter
                adapter.removeAt(viewHolder.adapterPosition)
                getWonderfulPlacesListFromLocalDB()
            }
        }
        val deleteItemTouchHelper = ItemTouchHelper(deleteSwipeHandler)
        deleteItemTouchHelper.attachToRecyclerView(rv_wonderful_places_list)
    }

    companion object {
        private const val ADD_PLACE_ACTIVITY_REQUEST_CODE = 1
        internal const val EXTRA_PLACE_DETAILS = "extra_place_details"
    }
}