package com.ace.ucv.wonderful.places.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.ace.ucv.wonderful.places.R
import com.ace.ucv.wonderful.places.database.DatabaseHandler
import com.ace.ucv.wonderful.places.domain.WonderfulPlaceDO
import com.ace.ucv.wonderful.places.utils.GetAddressFromLatLng
import com.google.android.gms.location.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.activity_add_wonderful_place.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

class AddWonderfulPlaceActivity : AppCompatActivity(), View.OnClickListener {

    /**
     * An variable to get an instance calendar using the default time zone and locale.
     */
    private var calendar = Calendar.getInstance()

    /**
     * A variable for DatePickerDialog OnDateSetListener.
     * The listener used to indicate the user has finished selecting a date. Which we will be initialize later on.
     */
    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener

    private var saveImageToInternalStorage: Uri? = null

    private var mapLatitude: Double = 0.0 // A variable which will hold the latitude value.
    private var mapLongitude: Double = 0.0 // A variable which will hold the longitude value.

    private var mapWonderfulPlaceDetails: WonderfulPlaceDO? = null

    // A fused location client variable which is further user to get the user's current location
    private lateinit var mapFusedLocationClient: FusedLocationProviderClient

    /**
     * This function is auto created by Android when the Activity Class is created.
     */
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        //This call the parent constructor
        super.onCreate(savedInstanceState)

        // This is used to align the xml view to this class
        setContentView(R.layout.activity_add_wonderful_place)

        setSupportActionBar(toolbar_add_place) // Use the toolbar to set the action bar.
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // This is to use the home back button.
        // Setting the click event to the back button
        toolbar_add_place.setNavigationOnClickListener {
            onBackPressed()
        }

        // Initialize the Fused location variable
        mapFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        /**
         * Initialize the places sdk if it is not initialized earlier using the api key.
         */
        if (!Places.isInitialized()) {
            Places.initialize(
                this@AddWonderfulPlaceActivity,
                resources.getString(R.string.google_maps_api_key)
            )
        }

        if (intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)) {
            mapWonderfulPlaceDetails =
                intent.getSerializableExtra(MainActivity.EXTRA_PLACE_DETAILS) as WonderfulPlaceDO
        }

        // create an OnDateSetListener
        dateSetListener =
            DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, monthOfYear)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                updateDateInView()
            }

        /*
        Here the calender instance what we have created before will give us the current date
        which is formatted in the format in function
         */
        updateDateInView()

        if (mapWonderfulPlaceDetails != null) {
            supportActionBar?.title = "Edit Wonderful Place"

            et_title.setText(mapWonderfulPlaceDetails!!.title)
            et_description.setText(mapWonderfulPlaceDetails!!.description)
            et_date.setText(mapWonderfulPlaceDetails!!.date)
            et_location.setText(mapWonderfulPlaceDetails!!.location)
            mapLatitude = mapWonderfulPlaceDetails!!.latitude
            mapLongitude = mapWonderfulPlaceDetails!!.longitude

            saveImageToInternalStorage = Uri.parse(mapWonderfulPlaceDetails!!.image)

            iv_place_image.setImageURI(saveImageToInternalStorage)

            btn_save.text = "UPDATE"
        }

        et_date.setOnClickListener(this)
        tv_add_image.setOnClickListener(this)
        btn_save.setOnClickListener(this)
        et_location.setOnClickListener(this)
        tv_select_current_location.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v!!.id) {R.id.et_date -> {
                DatePickerDialog(
                    this@AddWonderfulPlaceActivity,
                    dateSetListener, // This is the variable which have created globally
                    // and initialized in setupUI method.

                    // set DatePickerDialog to point to today's date when it loads up
                    calendar.get(Calendar.YEAR),
                    // Here the cal instance is created globally
                    // and used everywhere in the class where it is required.

                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            }

            R.id.tv_add_image -> {
                val pictureDialog = AlertDialog.Builder(this)
                pictureDialog.setTitle("Select Action")
                val pictureDialogItems =
                    arrayOf("Select photo from gallery", "Capture photo from camera")
                pictureDialog.setItems(
                    pictureDialogItems
                ) { _, which ->
                    when (which) {
                        // Here we have create the methods for image selection from GALLERY
                        0 -> choosePhotoFromGallery()
                        1 -> takePhotoFromCamera()
                    }
                }
                pictureDialog.show()
            }

            R.id.et_location -> {
                try {
                    // These are the list of fields which we required is passed
                    val fields = listOf(
                        Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG,
                        Place.Field.ADDRESS
                    )
                    // Start the autocomplete intent with a unique request code.
                    val intent =
                        Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                            .build(this@AddWonderfulPlaceActivity)
                    startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE)
                } catch (exception: Exception) {
                    exception.printStackTrace()
                }
            }

            R.id.tv_select_current_location -> {

                if (!isLocationEnabled()) {
                    Toast.makeText(
                        this,
                        "Your location provider is turned off. Please turn it on.",
                        Toast.LENGTH_SHORT
                    ).show()

                    // This will redirect you to settings from where you need to turn on the location provider.
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(intent)
                } else {
                    Dexter.withActivity(this)
                        .withPermissions(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                        .withListener(object : MultiplePermissionsListener {
                            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                                if (report!!.areAllPermissionsGranted()) {

                                    requestNewLocationData()
                                }
                            }

                            override fun onPermissionRationaleShouldBeShown(
                                permissions: MutableList<PermissionRequest>?,
                                token: PermissionToken?
                            ) {
                                showRationalDialogForPermissions()
                            }
                        }).onSameThread()
                        .check()
                }
            }

            R.id.btn_save -> {

                when {
                    et_title.text.isNullOrEmpty() -> {
                        Toast.makeText(this, "Please enter title", Toast.LENGTH_SHORT).show()
                    }
                    et_description.text.isNullOrEmpty() -> {
                        Toast.makeText(this, "Please enter description", Toast.LENGTH_SHORT)
                            .show()
                    }
                    et_location.text.isNullOrEmpty() -> {
                        Toast.makeText(this, "Please select location", Toast.LENGTH_SHORT)
                            .show()
                    }
                    saveImageToInternalStorage == null -> {
                        Toast.makeText(this, "Please add image", Toast.LENGTH_SHORT).show()
                    }
                    else -> {

                        // Assigning all the values to data model class.
                        val wonderfulPlace = WonderfulPlaceDO(
                            if (mapWonderfulPlaceDetails == null) 0 else mapWonderfulPlaceDetails!!.id,
                            et_title.text.toString(),
                            saveImageToInternalStorage.toString(),
                            et_description.text.toString(),
                            et_date.text.toString(),
                            et_location.text.toString(),
                            mapLatitude,
                            mapLongitude
                        )

                        val dbHandler = DatabaseHandler(this)

                        if (mapWonderfulPlaceDetails == null) {
                            val addWonderfulPlace = dbHandler.addWonderfulPlace(wonderfulPlace)

                            if (addWonderfulPlace > 0) {
                                setResult(Activity.RESULT_OK);
                                finish()
                            }
                        } else {
                            val updateWonderfulPlace = dbHandler.updateWonderfulPlace(wonderfulPlace)

                            if (updateWonderfulPlace > 0) {
                                setResult(Activity.RESULT_OK);
                                finish()
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Receive the result from a previous call to
     * {@link #startActivityForResult(Intent, int)}.  This follows the
     * related Activity API as described there in
     * {@link Activity#onActivityResult(int, int, Intent)}.
     *
     * @param requestCode The integer request code originally supplied to
     *                    startActivityForResult(), allowing you to identify who this
     *                    result came from.
     * @param resultCode The integer result code returned by the child activity
     *                   through its setResult().
     * @param data An Intent, which can return result data to the caller
     *               (various data can be attached to Intent "extras").
     */
    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GALLERY) {
                if (data != null) {
                    val contentURI = data.data
                    try {
                        // Here this is used to get an bitmap from URI
                        @Suppress("DEPRECATION")
                        val selectedImageBitmap =
                            MediaStore.Images.Media.getBitmap(this.contentResolver, contentURI)

                        saveImageToInternalStorage =
                            saveImageToInternalStorage(selectedImageBitmap)
                        Log.e("Saved Image : ", "Path :: $saveImageToInternalStorage")

                        // Set the selected image from GALLERY to imageView.
                        iv_place_image!!.setImageBitmap(selectedImageBitmap)
                    } catch (e: IOException) {
                        e.printStackTrace()
                        Toast.makeText(this@AddWonderfulPlaceActivity, "Failed!",
                            Toast.LENGTH_SHORT).show()
                    }
                }
            } else if (requestCode == CAMERA) {
                val thumbnail: Bitmap = data!!.extras!!.get("data") as Bitmap

                saveImageToInternalStorage =
                    saveImageToInternalStorage(thumbnail)
                Log.e("Saved Image : ", "Path :: $saveImageToInternalStorage")

                iv_place_image!!.setImageBitmap(thumbnail)
            } else if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {

                val place: Place = Autocomplete.getPlaceFromIntent(data!!)

                et_location.setText(place.address)
                mapLatitude = place.latLng!!.latitude
                mapLongitude = place.latLng!!.longitude
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            Log.e("Canceled", "Canceled")
        }
    }

    /**
     * A function to update the selected date in the UI with selected format.
     * This function is created because every time we don't need to add format
     * which we have added here to show it in the UI.
     */
    private fun updateDateInView() {
        val myFormat = "dd.MM.yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        et_date.setText(
            sdf.format(calendar.time).toString()
        )
    }

    /**
     * A method is used for image selection from GALLERY / PHOTOS of phone storage.
     */
    private fun choosePhotoFromGallery() {
        Dexter.withActivity(this)
            .withPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    if (report!!.areAllPermissionsGranted()) {
                        val galleryIntent = Intent(
                            Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        )

                        startActivityForResult(galleryIntent, GALLERY)
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    showRationalDialogForPermissions()
                }
            }).onSameThread().check()
    }

    /**
     * A method is used  asking the permission for camera and storage and image capturing and selection from Camera.
     */
    private fun takePhotoFromCamera() {
        Dexter.withActivity(this)
            .withPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    if (report!!.areAllPermissionsGranted()) {
                        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        startActivityForResult(intent, CAMERA)
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    showRationalDialogForPermissions()
                }
            }).onSameThread().check()
    }

    /**
     * A function used to show the alert dialog when the permissions are denied and need to allow
     * it from settings app info.
     */
    private fun showRationalDialogForPermissions() {
        AlertDialog.Builder(this)
            .setMessage("It Looks like you have turned off permissions required for this feature. " +
                    "It can be enabled under Application Settings")
            .setPositiveButton(
                "GO TO SETTINGS"
            ) { _, _ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    /**
     * A function to save a copy of an image to internal storage for WonderfulPlaceApp to use.
     */
    private fun saveImageToInternalStorage(bitmap: Bitmap): Uri {
        val wrapper = ContextWrapper(applicationContext)
        /**
         * The Mode Private here is
         * File creation mode: the default mode, where the created file can only
         * be accessed by the calling application (or all applications sharing the
         * same user ID).
         */
        var file = wrapper.getDir(IMAGE_DIRECTORY, Context.MODE_PRIVATE)

        // Create a file to save the image
        file = File(file, "${UUID.randomUUID()}.jpg")

        try {
            // Get the file output stream
            val stream: OutputStream = FileOutputStream(file)

            // Compress bitmap
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)

            // Flush the stream
            stream.flush()

            // Close stream
            stream.close()
        } catch (ioException: IOException) {
            ioException.printStackTrace()
        }

        return Uri.parse(file.absolutePath)
    }

    /**
     * A function which is used to verify that the location or let's GPS is enable or not of
     * the user's device.
     */
    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    /**
     * A function to request the current location. Using the fused location provider client.
     */
    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        val mapLocationRequest = LocationRequest()
        mapLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mapLocationRequest.interval = 0
        mapLocationRequest.fastestInterval = 0
        mapLocationRequest.numUpdates = 1

        mapFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mapFusedLocationClient.requestLocationUpdates(mapLocationRequest, mLocationCallback,
            Looper.myLooper())
    }

    /**
     * A location callback object of fused location provider client where we will get the current
     * location details.
     */
    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val mapLastLocation: Location = locationResult.lastLocation
            mapLatitude = mapLastLocation.latitude
            Log.e("Current Latitude", "$mapLatitude")
            mapLongitude = mapLastLocation.longitude
            Log.e("Current Longitude", "$mapLongitude")

            val addressTask = GetAddressFromLatLng(this@AddWonderfulPlaceActivity,
                mapLatitude, mapLongitude)

            addressTask.setAddressListener(object :
                GetAddressFromLatLng.AddressListener {
                override fun onAddressFound(address: String?) {
                    Log.e("Address ::", "" + address)
                    et_location.setText(address)
                }

                override fun onError() {
                    Log.e("Get Address ::", "Something is wrong...")
                }
            })
            addressTask.getAddress()
        }
    }

    companion object {
        private const val GALLERY = 1
        private const val CAMERA = 2
        private const val IMAGE_DIRECTORY = "WonderfulPlacesImages"
        private const val PLACE_AUTOCOMPLETE_REQUEST_CODE = 3
    }
}