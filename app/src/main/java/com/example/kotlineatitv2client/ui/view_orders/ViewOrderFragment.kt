package com.example.kotlineatitv2client.ui.view_orders

import android.app.AlertDialog
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlineatitv2client.Adapter.MyOrderAdapter
import com.example.kotlineatitv2client.Callback.ILoadOrderCallbackListener
import com.example.kotlineatitv2client.Callback.IMyButtonCallback
import com.example.kotlineatitv2client.Common.Common
import com.example.kotlineatitv2client.Common.MySwipeHelper
import com.example.kotlineatitv2client.EventBus.CountCartEvent
import com.example.kotlineatitv2client.EventBus.MenuItemBack
import com.example.kotlineatitv2client.Model.OrderModel
import com.example.kotlineatitv2client.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dmax.dialog.SpotsDialog
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus
import java.lang.StringBuilder
import java.util.*
import kotlin.collections.HashMap

class ViewOrderFragment: Fragment(), ILoadOrderCallbackListener {

    private var viewOrderModel: ViewOrderModel?=null

    internal lateinit var dialog:AlertDialog
    internal lateinit var recycler_order:RecyclerView
    internal lateinit var listener:ILoadOrderCallbackListener


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewOrderModel = ViewModelProviders.of(this).get(ViewOrderModel::class.java)
        val root = inflater.inflate(R.layout.fragment_view_orders,container,false)
        initViews(root)
        loadOrderFromFirebase()

        viewOrderModel!!.mutableLiveDataOrderList.observe(this, Observer {
            Collections.reverse(it!!)
            val adapter = MyOrderAdapter(context!!,it!!.toMutableList())
            recycler_order!!.adapter = adapter
        })

        return root
    }

    private fun loadOrderFromFirebase() {
        dialog.show()

        val orderList = ArrayList<OrderModel>()

        FirebaseDatabase.getInstance().getReference(Common.ORDER_REF)
            .orderByChild("userId")
            .equalTo(Common.currentUser!!.uid!!)
            .limitToLast(100)
            .addListenerForSingleValueEvent(object :ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {
                    listener.onLoadOrderFailed(p0.message!!)
                }

                override fun onDataChange(p0: DataSnapshot) {
                    for (orderSnapShot in p0.children)
                    {
                        val order = orderSnapShot.getValue(OrderModel::class.java)
                        order!!.orderNumber = orderSnapShot.key
                        orderList.add(order!!)
                    }
                    listener.onLoadOrderSuccess(orderList)
                }

            })
    }

    private fun initViews(root: View?) {

        listener = this

        dialog = SpotsDialog.Builder().setContext(context!!).setCancelable(false).build()


        recycler_order = root!!.findViewById(R.id.recycler_order) as RecyclerView
        recycler_order.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(context!!)
        recycler_order.layoutManager = layoutManager
        recycler_order.addItemDecoration(DividerItemDecoration(context!!,layoutManager.orientation))


        val swipe = object : MySwipeHelper(context!!,recycler_order!!,250)
        {
            override fun instantiateMyButton(
                viewHolder: RecyclerView.ViewHolder,
                buffer: MutableList<MyButton>
            ) {
                buffer.add(MyButton(context!!,
                    "Cancel order",
                    30,
                    0,
                    Color.parseColor("#FF3c30"),
                    object : IMyButtonCallback {
                        override fun onClick(pos: Int) {
                            val orderModel = (recycler_order.adapter as MyOrderAdapter).getItemAtPosition(pos)
                            if (orderModel.orderStatus == 0)
                            {
                                val builder = androidx.appcompat.app.AlertDialog.Builder(context!!)
                                builder.setTitle("Cancel Order")
                                    .setMessage("Do you really want to cancel this order?")
                                    .setNegativeButton("NO"){dialogInterface, i->
                                        dialogInterface.dismiss()
                                    }
                                    .setPositiveButton("YES"){dialogInterface, i->

                                        val update_data = HashMap<String,Any>()
                                        update_data.put("orderStatus", -1) //Cancel Order
                                        FirebaseDatabase.getInstance()
                                            .getReference(Common.ORDER_REF)
                                            .child(orderModel.orderNumber!!)
                                            .updateChildren(update_data)
                                            .addOnFailureListener{e->
                                                Toast.makeText(context!!, e.message, Toast.LENGTH_SHORT).show()
                                            }
                                            .addOnSuccessListener {
                                                orderModel.orderStatus = -1 //Local update
                                                (recycler_order.adapter as MyOrderAdapter).setItemAtPosition(pos,orderModel)
                                                Toast.makeText(context!!, "Cancel order successfully", Toast.LENGTH_SHORT).show()
                                            }
                                    }
                                val dialog = builder.create()
                                dialog.show()
                            }
                            else
                            {
                                Toast.makeText(context!!, StringBuilder("Your order status was changed to ")
                                    .append(Common.convertStatusToText(orderModel.orderStatus))
                                    .append(", so you can't cancel it"), Toast.LENGTH_SHORT).show()
                            }
                        }

                    }))
            }

        }

    }

    override fun onLoadOrderSuccess(orderList: List<OrderModel>) {
        dialog.dismiss()
        viewOrderModel!!.setMutableLiveDataOrderList(orderList)
    }

    override fun onLoadOrderFailed(message: String) {
        dialog.dismiss()
        Toast.makeText(context!!,message,Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        EventBus.getDefault().postSticky(MenuItemBack())
        super.onDestroy()
    }
}