package com.kage.shareme

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.kage.shareme.databinding.FragmentFeedBinding
import com.kage.shareme.databinding.FragmentYuklemeBinding
import java.util.UUID


class YuklemeFragment : Fragment() {
    private var _binding: FragmentYuklemeBinding? = null
    private val binding get() = _binding!!
    private lateinit var permissonLauncher : ActivityResultLauncher<String>
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private var secilenGorsel : Uri? = null //data/data/media/image.jpg gibi bir URL tutan veri tipi
    private var secilenBitmap : Bitmap? = null // görsel yada ses gibi diğer veri tiplerine çeviren veri tipi
    private lateinit var storage : FirebaseStorage
    private lateinit var auth : FirebaseAuth
    private lateinit var db : FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerLauncher()

        storage = Firebase.storage
        auth = Firebase.auth
        db = Firebase.firestore

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentYuklemeBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.paylasButton.setOnClickListener { gonderiYukle(it) }
        binding.imageView.setOnClickListener { gorselSec(it) }
        binding.paylasButton.setOnClickListener { gonderiYukle(it) }
    }

    fun gonderiYukle(view: View){
       val uuid = UUID.randomUUID()
        val randomID = "${uuid}.jpg"

        val reference = storage.reference
        val gorselReference = reference.child("images").child(randomID)

        if (secilenGorsel != null){
            gorselReference.putFile(secilenGorsel!!).addOnSuccessListener { uploadTask ->
                //URL'yi alma işlemi yap.
                gorselReference.downloadUrl.addOnSuccessListener { uri ->
                    val downloadUrl = uri.toString()
                    //Url'de alındığına göre veri tabanına kayıt işlemi yapılacak.
                    //Bu işlemi Value-Key eşleşmesine göre belirleyerek veri tabanına kayıt yapıyoruz.

                    if (auth.currentUser != null){
                        val postMAP = hashMapOf<String,Any>()
                        postMAP.put("downloadUrl",downloadUrl)
                        postMAP.put("email",auth.currentUser!!.email.toString())
                        postMAP.put("comment",binding.commentEditText.text.toString())
                        postMAP.put("tarih",Timestamp.now())

                        db.collection("Posts").add(postMAP).addOnSuccessListener{ documentReference ->
                            //veri yüklenmesi için gereken yolu gösteriyor.
                            val action = YuklemeFragmentDirections.actionYuklemeFragmentToFeedFragment()
                            Navigation.findNavController(view).navigate(action)

                        }.addOnFailureListener { exception ->
                            Toast.makeText(requireContext(),exception.localizedMessage,Toast.LENGTH_LONG).show()
                        }
                    }


                }
            }.addOnFailureListener { exception ->
                Toast.makeText(requireContext(),exception.localizedMessage,Toast.LENGTH_LONG).show()
            }
        }


    }

    fun gorselSec(view: View){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            //izin verilip verilmediğini soran kod kalıbı.
            if (ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED){
                //izin verilmemiş ise izin istememiz.
                if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),Manifest.permission.READ_MEDIA_IMAGES)){
                    //Eğer true dönüyorsa, Snackbar göstererek neden izin istediğimizi tekrar belirtmemiz gerekiyor.
                    Snackbar.make(view,"Galeri'ye erişerek görsel seçilmesi gerekiyor",Snackbar.LENGTH_INDEFINITE).setAction("İzin Ver",
                        View.OnClickListener {
                            //izin isteyeceğiz.
                            permissonLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                        }
                    ).show()
                } else{
                    //izin isteyeceğiz
                    permissonLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                }
            }
            //izin verilmiş, direkt olarak galeriye erişebiliriz.
            else{
                val intentToGalerry = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGalerry)
            }
        }
        else{
            //izin verilip verilmediğini soran kod kalıbı.
            if (ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                //izin verilmemiş ise izin istememiz.
                if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),Manifest.permission.READ_EXTERNAL_STORAGE)){
                    //Eğer true dönüyorsa, Snackbar göstererek neden izin istediğimizi tekrar belirtmemiz gerekiyor.
                    Snackbar.make(view,"Galeri'ye erişerek görsel seçilmesi gerekiyor",Snackbar.LENGTH_INDEFINITE).setAction(
                        "İzin Ver",
                        View.OnClickListener {
                            //izin isteyeceğiz.
                            permissonLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                        }
                    ).show()
                } else{
                    //izin isteyeceğiz
                    permissonLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }
            //izin verilmiş, direkt olarak galeriye erişebiliriz.
            else{
                val intentToGalerry = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGalerry)
            }
        }
    }
    //Galeriden seçilen veriyi kullanmak için gereken kısım.
    fun registerLauncher(){
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK){
                val intenFromData = result.data
                if (intenFromData != null){
                    secilenGorsel=intenFromData.data
                    try {
                        if (Build.VERSION.SDK_INT >= 28){
                            //Yeni yöntem
                            val source = ImageDecoder.createSource(requireActivity().contentResolver,secilenGorsel!!)
                            secilenBitmap = ImageDecoder.decodeBitmap(source)
                            binding.imageView.setImageBitmap(secilenBitmap)
                        }
                        else{
                            //Eski yöntem
                            secilenBitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver,secilenGorsel)
                            binding.imageView.setImageBitmap(secilenBitmap)
                        }
                    }
                    catch (e: Exception) {
                        println(e.localizedMessage)
                    }
                }
            }
        }
        //İzin alındıktan sonra Galeriye erişemek için gereken kısım!!
        permissonLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){ result ->
            if (result){
                //izin verildi, galeriye  gidebiliriz.
                val intentToGalerry = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGalerry)
            }
            else{
                Toast.makeText(requireContext(),"İzin verilmedi!",Toast.LENGTH_LONG).show()
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}