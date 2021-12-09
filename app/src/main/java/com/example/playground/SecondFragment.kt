package com.example.playground

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.playground.databinding.FragmentSecondBinding
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SecondFragment : Fragment() {

    private val pagedDataProvider = PagedDataProvider()
    private var _binding: FragmentSecondBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonSecond.setOnClickListener {
            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
        }

        binding.rcv.adapter = DataAdapter(pagedDataProvider)
        binding.rcv.layoutManager = LinearLayoutManager(requireContext())

        lifecycleScope.launchWhenStarted {
            pagedDataProvider.observeData().collect { list ->
                Log.d(
                    "SecondFragment",
                    "Received list with IDs from ${list.first().uid} to ${list.last().uid}"
                )
                val dataAdapter = binding.rcv.adapter as DataAdapter
                val oldList = dataAdapter.dataSet
                val diffCallback = UserDiffCallback(oldList, list)
                val diffResult = DiffUtil.calculateDiff(diffCallback)
                dataAdapter.dataSet.clear()
                dataAdapter.dataSet.addAll(list)

                diffResult.dispatchUpdatesTo(dataAdapter)
            }
        }

        binding.rcv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val firstVisibleItemPosition =
                    (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                pagedDataProvider.onScroll(firstVisibleItemPosition)
            }
        })

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


class DataAdapter(private val pagedDataProvider: PagedDataProvider) :
    RecyclerView.Adapter<UserViewHolder>() {

    var lastVisibleUpperPosition = 0
    var lastVisibleBottomPosition = 0

    val dataSet = mutableListOf<User>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        // Inflate the custom layout
        val contactView = inflater.inflate(R.layout.item_layout, parent, false)
        // Return a new holder instance
        return UserViewHolder(contactView)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = dataSet[position]
        holder.data = user
        holder.id.text = user.uid.toString()
        holder.firstName.text = user.firstName
        holder.secondName.text = user.lastName
    }

    override fun getItemCount(): Int = dataSet.size

    override fun onViewAttachedToWindow(holder: UserViewHolder) {

        super.onViewAttachedToWindow(holder)
        val absoluteAdapterPosition = holder.absoluteAdapterPosition
//        if(absoluteAdapterPosition > lastVisibleUpperPosition) {
//            lastVisibleBottomPosition++
//        } else if(absoluteAdapterPosition < lastVisibleUpperPosition) {
//            lastVisibleUpperPosition--
//
//        }
//        Log.d(
//            "DataAdapter",
//            "onViewAttachedToWindow holder position: $absoluteAdapterPosition, UID: ${holder.data.uid}, total visible items: $lastVisibleUpperPosition"
//        )
//        if (absoluteAdapterPosition == THRESHOLD) {
//            pagedDataProvider.onThresholdReached()
//        }
    }


    override fun onViewDetachedFromWindow(holder: UserViewHolder) {
        super.onViewDetachedFromWindow(holder)
//        Log.d("DataAdapter", "onViewDetachedFromWindow holder for item id ${holder.data.uid}")
    }
}

class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    lateinit var data: User
    val id = view.findViewById<TextView>(R.id.user_id)
    val firstName = view.findViewById<TextView>(R.id.first_name)
    val secondName = view.findViewById<TextView>(R.id.second_name)
}

class UserDiffCallback(
    private val mOldList: List<User>,
    private val mNewList: List<User>
) : DiffUtil.Callback() {

    override fun getOldListSize() = mOldList.size

    override fun getNewListSize() = mNewList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        // add a unique ID property on Contact and expose a getId() method
        return mOldList[oldItemPosition].uid == mNewList[newItemPosition].uid
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldContact = mOldList[oldItemPosition]
        val newContact = mNewList[newItemPosition]

        return oldContact.firstName == newContact.firstName && oldContact.lastName == newContact.lastName
    }

}