package com.planscollective.plansapp.adapters

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.adapter.FragmentViewHolder
import com.planscollective.plansapp.PLANS_APP
import com.planscollective.plansapp.databinding.CellPlaceBinding
import com.planscollective.plansapp.extension.getDistance
import com.planscollective.plansapp.extension.removeOwnCountry
import com.planscollective.plansapp.interfaces.OnItemTouchListener
import com.planscollective.plansapp.interfaces.OnSwipeTouchListener
import com.planscollective.plansapp.manager.UserInfo
import com.planscollective.plansapp.models.dataModels.PlaceModel
import com.planscollective.plansapp.webServices.place.PlaceWebservice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PlacesAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    private var listener: OnItemTouchListener? = null

    companion object {
        var listPlaces = ArrayList<PlaceModel>()
        var listItems = ArrayList<PlaceFragment>()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateAdapter(
        places: ArrayList<PlaceModel>? = null,
        listener: OnItemTouchListener? = null
    ) {
        places?.also {
            listPlaces.clear()
            listPlaces.addAll(it)
        }
        listener?.also { this.listener = it }

        notifyDataSetChanged()
    }

    override fun createFragment(position: Int): Fragment {
        val fragment = PlaceFragment.getInstance(position, listener)
        if (position < listItems.size ) {
            listItems[position] = fragment
        }else {
            listItems.add(fragment)
        }

        return fragment
    }

    override fun getItemCount(): Int {
        return listPlaces.size
    }

    override fun onBindViewHolder(
        holder: FragmentViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        super.onBindViewHolder(holder, position, payloads)
        if (position < listItems.size && position < listPlaces.size) {
            PLANS_APP.currentActivity?.lifecycleScope?.launch(Dispatchers.Main) {
                listItems[position].setupUI(position, listPlaces[position])
                listItems[position].updateUI(listPlaces[position])
            }
        }
    }

}

class PlaceFragment(
    var index: Int = 0,
    var listener: OnItemTouchListener? = null) : Fragment() {

    var binding: CellPlaceBinding? = null
    val place: PlaceModel?
        get() = PlacesAdapter.listPlaces.takeIf { index >= 0 && index < it.size }?.get(index)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = CellPlaceBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        println("PlaceFragment onViewCreated")
        setupUI()
        updateUI(place)
    }

    override fun onResume() {
        super.onResume()
        println("PlaceFragment onResume")
        updateUI(place)
    }

    @SuppressLint("ClickableViewAccessibility")
    fun setupUI(position: Int? = null, data: PlaceModel? = null) {
        println("PlaceFragment setupUI")

        position?.also { index = it }
        val model = data ?: place

        binding?.root?.setOnTouchListener(object : OnSwipeTouchListener(requireActivity()){
            override fun onSwipeDown() {
                listener?.onItemSwipeDown(index, model)
            }
            override fun onClicked(e: MotionEvent?): Boolean {
                listener?.onItemClick(index, model)
                return true
            }
        })
    }

    fun updateUI(place: PlaceModel? = null) {
        println("PlaceFragment updateUI")

        val item = place ?: this.place ?: return

        binding?.apply {
            tvCategoryName.text = item.category?.name
            item.category?.iconImage?.also { imvCategoryIcon.setImageResource(it) }
            tvPlaceName.text = "${index + 1}. ${item.name}"
            tvPlaceAddress.text = item.address?.removeOwnCountry()
            tvPlaceMile.text = item.location?.getDistance(UserInfo.latitude, UserInfo.longitude)?.let {
                if (it >= 2) "%.2f".format(it) + " Miles" else "%.2f".format(it) + " Mile"
            }

            layoutImageNotAvailable.visibility = View.INVISIBLE
            imvPlaceCover.visibility = View.INVISIBLE

            if (item.photoImage != null) {
                imvPlaceCover.setImageBitmap(item.photoImage)
                imvPlaceCover.visibility = View.VISIBLE
                layoutImageNotAvailable.visibility = View.INVISIBLE
            }else {
                PlaceWebservice.getPlacePhoto(item.place_id) { image, message ->
                    if (image != null) {
                        item.photoImage = image
                        imvPlaceCover.setImageBitmap(image)
                        imvPlaceCover.visibility = View.VISIBLE
                        layoutImageNotAvailable.visibility = View.INVISIBLE
                    }else {
                        imvPlaceCover.visibility = View.INVISIBLE
                        layoutImageNotAvailable.visibility = View.VISIBLE
                    }
                }
            }

        }
    }

    companion object {
        fun getInstance ( index: Int = 0,
                          listener: OnItemTouchListener? = null) : PlaceFragment {
            return PlaceFragment(index, listener)
        }
    }
}


