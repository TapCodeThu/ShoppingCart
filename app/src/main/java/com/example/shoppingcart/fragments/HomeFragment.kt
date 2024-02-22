package com.example.shoppingcart.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import com.example.shoppingcart.R
import com.example.shoppingcart.adapter.FoodAdapter
import com.example.shoppingcart.databinding.FragmentHomeBinding
import com.example.shoppingcart.eventbus.UpdateCart
import com.example.shoppingcart.listener.ICartLoadListener
import com.example.shoppingcart.listener.IFoodLoadListener
import com.example.shoppingcart.model.CartModel
import com.example.shoppingcart.model.FoodModel
import com.example.shoppingcart.untils.MyDecoration
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.Locale


class HomeFragment : Fragment(), IFoodLoadListener, ICartLoadListener {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var naviController: NavController
     lateinit var iFoodLoadListener: IFoodLoadListener
     lateinit var cartLoadListener: ICartLoadListener
    private lateinit var foodAdapter: FoodAdapter
    private val foods : MutableList<FoodModel> = ArrayList()
    private lateinit var auth: FirebaseAuth


    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        if(EventBus.getDefault().hasSubscriberForEvent(UpdateCart::class.java))
            EventBus.getDefault().removeStickyEvent(UpdateCart::class.java)
        EventBus.getDefault().unregister(this)
    }
    @Subscribe(threadMode = ThreadMode.MAIN,sticky = true)
    public fun onUpdateCartEvent(event: UpdateCart){
        countCartFromFirebase()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.progressLoad.visibility = View.VISIBLE

        auth = FirebaseAuth.getInstance()

        iNit(view)
        loadFoodFromFirebase()
        countCartFromFirebase()
        actionButton()

    }

    private fun countCartFromFirebase() {
        val cartModels : MutableList<CartModel> = ArrayList()
        FirebaseDatabase.getInstance().getReference("Cart")
            .child(auth.currentUser!!.uid)
            .addValueEventListener(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    cartModels.clear()
                   for(cartSnapshot in snapshot.children){
                       val cartModel = cartSnapshot.getValue(CartModel::class.java)
                       cartModel!!.key = cartSnapshot.key
                       cartModels.add(cartModel)
                   }
                    cartLoadListener.onCartLoadSuccess(cartModels)
                }

                override fun onCancelled(error: DatabaseError) {
                   cartLoadListener.onCartLoadFailed(error.message)
                }

            })

    }

    private fun actionButton() {

        //action search
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false

            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterList(newText)
               return true
            }

        })

        //go to cart fragment
        binding.ivCart.setOnClickListener {
            naviController.navigate(R.id.action_homeFragment_to_cartFragment)

        }
    }

    private fun filterList(query: String?) {
        if(query != null){
            val filterList = ArrayList<FoodModel>()
            for(i in foods){
                if(i.name!!.lowercase(Locale.ROOT).contains(query)){
                    filterList.add(i)
                }
            }
            if(filterList.isEmpty()){
                //No do
            }
            else{

                foodAdapter.setFilterList(filterList)


            }
        }



    }

    private fun loadFoodFromFirebase() {

        FirebaseDatabase.getInstance().getReference("foods")
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                        for(dataSnapshot in snapshot.children){
                            val foodModel = dataSnapshot.getValue(FoodModel::class.java)
                            foodModel!!.key = dataSnapshot.key
                            foods.add(foodModel)
                        }
                        binding.progressLoad.visibility = View.GONE
                        iFoodLoadListener.onFoodLoadSuccess(foods)

                    }
                    else{
                        binding.progressLoad.visibility = View.GONE
                        iFoodLoadListener.onFoodLoadFailed("Không có món ăn từ server!!")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    binding.progressLoad.visibility = View.GONE
                   iFoodLoadListener.onFoodLoadFailed(error.message)
                }

            })
    }

    private fun iNit(view: View) {
        naviController = Navigation.findNavController(view)
        iFoodLoadListener = this
        cartLoadListener = this
        binding.recyclerview.layoutManager = GridLayoutManager(requireContext(),2)
        binding.recyclerview.addItemDecoration(MyDecoration())

    }

    override fun onFoodLoadSuccess(foodModelList: List<FoodModel>?) {
        foodAdapter = FoodAdapter(requireContext(),foodModelList!!,cartLoadListener )
        binding.recyclerview.adapter = foodAdapter

    }

    override fun onFoodLoadFailed(message: String?) {
       Toast.makeText(requireContext(),message,Toast.LENGTH_SHORT).show()
    }

    override fun onCartLoadSuccess(cartModelList: List<CartModel>?) {
        var cartSum = 0
        for(cartModel in cartModelList!!) cartSum += cartModel.quantity
        binding.bage.setNumber(cartSum)

    }

    override fun onCartLoadFailed(message: String?) {
        Toast.makeText(requireContext(),message,Toast.LENGTH_SHORT).show()
    }


}