package com.kage.shareme

import android.os.Bundle
import android.text.Layout.Directions
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.kage.shareme.databinding.FragmentFeedBinding
import com.kage.shareme.databinding.FragmentGirisBinding

class GirisFragment : Fragment() {
    private var _binding: FragmentGirisBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGirisBinding.inflate(inflater, container, false)
        val view = binding.root
        return view


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.uyeOlButton.setOnClickListener { uyeOl(it) }
        binding.girisButton.setOnClickListener { girisYap(it) }

        val currentUser = auth.currentUser
        if (currentUser != null){
            val action = GirisFragmentDirections.actionGirisFragmentToFeedFragment()
            Navigation.findNavController(view).navigate(action)
        }


    }



    fun uyeOl(view: View){

        val email = binding.EmailText.text.toString()
        val password = binding.sifreText.text.toString()

        if (email.isNotEmpty() && password.isNotEmpty()){
            auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener { task ->
                if (task.isSuccessful){
                    val action = GirisFragmentDirections.actionGirisFragmentToFeedFragment()
                    Navigation.findNavController(view).navigate(action)
                }

            }.addOnFailureListener { exception ->
                Toast.makeText(requireContext(),exception.localizedMessage,Toast.LENGTH_LONG).show()}
        }



    }

    fun  girisYap(view: View){
        val email = binding.EmailText.text.toString()
        val password = binding.sifreText.text.toString()

        if (email.isNotEmpty() == password.isNotEmpty()){
            auth.signInWithEmailAndPassword(email,password).addOnSuccessListener { task ->

                val action = GirisFragmentDirections.actionGirisFragmentToFeedFragment()
                Navigation.findNavController(view).navigate(action)
            }.addOnFailureListener { exception ->
                Toast.makeText(requireContext(),exception.localizedMessage,Toast.LENGTH_LONG).show()
            }
        }


    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}