package com.kage.shareme.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.kage.shareme.R
import com.kage.shareme.adapter.PostAdapter
import com.kage.shareme.databinding.FragmentFeedBinding
import com.kage.shareme.model.Post

class FeedFragment : Fragment(), PopupMenu.OnMenuItemClickListener {
    private var _binding: FragmentFeedBinding? = null
    private val binding get() = _binding!!
    private lateinit var popup : PopupMenu
    private lateinit var auth : FirebaseAuth
    private lateinit var db : FirebaseFirestore
    var postList : ArrayList<Post> = arrayListOf()
    private var adapter : PostAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        db = Firebase.firestore
    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFeedBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.floatingActionButton.setOnClickListener { floatingButtonTiklandi(it) }

        popup = PopupMenu(requireContext(),binding.floatingActionButton)
        val inflater = popup.menuInflater
        inflater.inflate(R.menu.floating_button_menu,popup.menu)

        popup.setOnMenuItemClickListener(this)

        firestoreVerileriAL()

        adapter = PostAdapter(postList)
        binding.feedRcyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.feedRcyclerView.adapter = adapter
    }

    fun firestoreVerileriAL(){
        db.collection("Posts").addSnapshotListener { value, error ->
            if (error != null){
                Toast.makeText(requireContext(),error.localizedMessage,Toast.LENGTH_LONG).show()
            } else {
                if (value != null && !value.isEmpty){
                    postList.clear()
                    val documents = value.documents
                    for (document in documents){
                      val comment = document.get("comment") as String // casting işlemi
                      val email = document.get("email") as String
                      val downloadUrl = document.get("downloadUrl") as String

                        val post = Post(comment,email,downloadUrl)
                        postList.add(post)
                      }
                    adapter?.notifyDataSetChanged()

                    }
                }
            }
        }


    fun floatingButtonTiklandi(view: View){

        popup.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }



    override fun onMenuItemClick(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.yuklemeItem){
            val action = FeedFragmentDirections.actionFeedFragmentToYuklemeFragment()
            Navigation.findNavController(requireView()).navigate(action)
        }
        else if(item?.itemId == R.id.cikisItem){
            val action = FeedFragmentDirections.actionFeedFragmentToGirisFragment()
            Navigation.findNavController(requireView()).navigate(action)
            auth.signOut()
        }

        return true
    }


}