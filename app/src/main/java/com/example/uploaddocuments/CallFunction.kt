package com.example.uploaddocuments

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import kotlinx.android.synthetic.main.add_query_layout.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.io.InputStream

class CallFunction : DialogFragment() {

    var type: String = ""
    val mUris: MutableList<Uri> = ArrayList()
    var fileListMap = mutableMapOf<String, String>()
    var fileList: ArrayList<String> = ArrayList()
    lateinit var view_image: ImageView
    lateinit var upload_image: ImageView


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater!!.inflate(R.layout.add_query_layout, container, false)

        upload_image = view.findViewById(R.id.upload_image)
        view_image = view.findViewById(R.id.view_image)

        onClickListner()
        return view
    }

    private fun onClickListner() {
        view_image.setOnClickListener {
            chooseAttachment()
        }

        upload_image.setOnClickListener {
            if (mUris.size <= 0) {
                Toast.makeText(context, "Please select file to view", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else {
                Attachment { s: String? -> this.deleteClick(s!!) }
                Attachment.newInstance(fileList, true, mUris)!!
                    .show((context as FragmentActivity).supportFragmentManager, null)
            }
        }
    }

    private fun deleteClick(s: String) {
        fileList.remove(s)
        mUris.clear()
        for (i in fileList) {
            var uri = Uri.parse(i)
            mUris.add(uri)
        }
    }

    fun chooseAttachment() {
        val adb = AlertDialog.Builder(requireActivity())
        val items = arrayOf<CharSequence>("Camera", "File Manager")
        adb.setSingleChoiceItems(items, -1) { d: DialogInterface, n: Int ->
            d.dismiss()

            if (n == 0) {

                var cameraXActivity = Intent(requireActivity(), CameraXActivity::class.java)
                startActivityForResult(cameraXActivity, Constats.PICK_IMAGE_FROM_CAMERA_REQUEST)
                type = "image/*"

            } else if (n == 1) {

                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                intent.type = "*/*"
                val mimetypes = arrayOf(
                    "application/pdf",
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                    "application/msword",
                    "image/png",
                    "image/jpg",
                    "image/jpeg"
                )
                // val mimeTypes = arrayOf("/msword","application/vnd.openxmlformats-officedocument.wordprocessingml.document", "image/png", "image/jpg", "image/jpeg")
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes)
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                startActivityForResult(
                    Intent.createChooser(intent, "ChooseFile"),
                    Constats.PICK_IMAGE_FROM_GALLERY_REQUEST
                )
            }
        }

        adb.setTitle("Select Attachment Type")
        adb.show()
    }

    private fun getMultiPartFromUri(
        type: String,
        key: String?,
        uri: String,
        context: Context
    ): MultipartBody.Part {
        val file1 = File(uri)
        Log.d("gaurav", "getChecboxFormBody: " + file1.absolutePath)
        val requestBody: RequestBody
        if (type.equals("image/*")) requestBody = RequestBody.create(MediaType.parse(type), file1)
        else requestBody = RequestBody.create(MediaType.parse(type), file1)
        return MultipartBody.Part.createFormData(
            "files", /*"document" + "_"
                + System.currentTimeMillis() + Math.random()
                *//*+ "." + getExtensionOfFile(file1.name)*/file1.name, requestBody
        )
    }

    fun createPartFromString(value: String): RequestBody {
        return RequestBody.create(MultipartBody.FORM, value)
    }

    private fun uploadNewDischargeFile(type: String, uris: MutableList<String>, context: Context?) {
        val partsList: MutableList<MultipartBody.Part> = ArrayList()
        for (uri in uris) {
            val multiPartFromUri: MultipartBody.Part = getMultiPartFromUri(type, "", uri, context!!)
            partsList.add(multiPartFromUri)
        }

        val params = HashMap<String, RequestBody>()

        params.apply {
            // _id is the lead id

        }

//        addQuery.queryAdded(params, partsList)
//        updateStatusViewModel.addQuery(params, partsList)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val newUrilist = mutableListOf<Uri>()
        if (resultCode == Activity.RESULT_OK && data != null) {
            if (requestCode == Constats.PICK_IMAGE_FROM_GALLERY_REQUEST) {
                if (data.clipData != null) {
                    val itemCount: Int = data.clipData!!.itemCount
                    var currentItem = 0
                    while (currentItem < itemCount) {
                        val imageUri: Uri = data.clipData!!.getItemAt(currentItem).uri

                        newUrilist.add(imageUri)
                        mUris.add(imageUri)
                        currentItem += 1


                    }
                } else if (data.data != null) {

                    var uri = Uri.parse(data.data.toString())
                    newUrilist.add(uri)
                    mUris.add(uri)
                }


            }
        }
        if (requestCode == Constats.PICK_IMAGE_FROM_CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {

            var filePath = data?.extras?.getString(CameraXActivity.EXTRA_SELFIE)
            var myUri = Uri.fromFile(File(filePath))
            mUris.add(myUri)
            newUrilist.add(myUri)
        }


        if (newUrilist.isNotEmpty()) {
            for (i in 0 until newUrilist.size) {
                val uri = newUrilist[i]
                var path = FileUtils.getPath(requireActivity(), uri) ?: uri.toString()
                val name =
                    path.substring(path.lastIndexOf("/") + 1) //getString(R.string.app_name) + System.currentTimeMillis()
                val localPath = requireActivity().filesDir
                var file = File(localPath, name)
                file.createNewFile()
                type = requireActivity().applicationContext.contentResolver.getType(uri)
                    ?: "image/jpeg"

                var bytesArray: ByteArray? = null
                var inputStream: InputStream? =
                    requireActivity()!!.contentResolver.openInputStream(uri)
                val mimetypes = arrayOf(
                    "application/pdf",
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                    "application/msword"
                )

                if (mimetypes.contains(type)) {
                    bytesArray = inputStream?.readBytes()
                    inputStream?.close()
                } else {
                    try {
                        /* val ei = ExifInterface(path)
                         val orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)*/
//                            Logger.log("ORIENT > $orientation")
                        var bitmap: Bitmap? = null
                        bitmap = BitmapFactory.decodeStream(inputStream) /*if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                ImageDecoder.decodeBitmap(ImageDecoder.createSource(requireActivity().applicationContext.contentResolver, uri))
            }
            else MediaStore.Images.Media.getBitmap(requireActivity().applicationContext.contentResolver, uri)*/
                        //  val newBitmap = FunctionUtils.rotateImage(bitmap, orientation)
                        bytesArray = FileUtils.getCompressedBitmapForUpload(bitmap!!)
                        inputStream?.close()
                        // FunctionUtils.recycleBitmap(newBitmap)
                    } catch (e: Exception) {
                        Toast.makeText(
                            requireActivity(),
                            "Something Went Wrong. Please select from Other source",
                            Toast.LENGTH_LONG
                        ).show()
                        return
                    }
                }

                requireActivity().applicationContext.openFileOutput(file.name, Context.MODE_PRIVATE)
                    .use {
                        it.write(bytesArray)
                        it.flush()
                        it.close()
                    }
                fileList.add(file.path)
                fileListMap[file.path] = type
            }
        }

    }

    override fun onStart() {
        super.onStart()
        if (dialog != null) dialog!!.window!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
    }
}

