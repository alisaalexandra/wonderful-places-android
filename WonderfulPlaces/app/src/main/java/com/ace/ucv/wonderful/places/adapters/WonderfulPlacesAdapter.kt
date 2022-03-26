package com.ace.ucv.wonderful.places.adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ace.ucv.wonderful.places.domain.WonderfulPlaceDO
import com.ace.ucv.wonderful.places.R
import com.ace.ucv.wonderful.places.activities.AddWonderfulPlaceActivity
import com.ace.ucv.wonderful.places.activities.MainActivity
import com.ace.ucv.wonderful.places.database.DatabaseHandler
import kotlinx.android.synthetic.main.item_wonderful_place.view.*

open class WonderfulPlacesAdapter(
    private val context: Context,
    private var list: ArrayList<WonderfulPlaceDO>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onClickListener: OnClickListener? = null

    /**
     * Inflates the item views which is designed in xml layout file
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return MyViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_wonderful_place,
                parent,
                false
            )
        )
    }

    /**
     * Binds each item in the ArrayList to a view
     *
     * Called when RecyclerView needs a new {@link ViewHolder} of the given type to represent
     * an item.
     *
     * This new ViewHolder should be constructed with a new View that can represent the items
     * of the given type. You can either create a new View manually or inflate it from an XML
     * layout file.
     */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]

        if (holder is MyViewHolder) {
            holder.itemView.iv_place_image.setImageURI(Uri.parse(model.image))
            holder.itemView.tvTitle.text = model.title
            holder.itemView.tvDescription.text = model.description

            holder.itemView.setOnClickListener {

                if (onClickListener != null) {
                    onClickListener!!.onClick(position, model)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    /**
     * A function to edit the added wonderful place detail and pass the existing details through intent.
     */
    fun notifyEditItem(activity: Activity, position: Int, requestCode: Int) {
        val intent = Intent(context, AddWonderfulPlaceActivity::class.java)
        intent.putExtra(MainActivity.EXTRA_PLACE_DETAILS, list[position])
        activity.startActivityForResult(
                intent,
                requestCode
        )
        // Notify any registered observers that the item at position has changed.
        notifyItemChanged(position)
    }

    /**
     * A function to delete the added wonderful place detail from the local storage.
     */
    fun removeAt(position: Int) {

        val dbHandler = DatabaseHandler(context)
        val isDeleted = dbHandler.deleteWonderfulPlace(list[position])

        if (isDeleted > 0) {
            list.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    /**
     * A function to bind the onclickListener.
     */
    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    interface OnClickListener {
        fun onClick(position: Int, model: WonderfulPlaceDO)
    }

    /**
     * A ViewHolder describes an item view and metadata about its place within the RecyclerView.
     */
    private class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)
}