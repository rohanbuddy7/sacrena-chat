package com.rohans.sacrenachat.ui.message

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.widget.NestedScrollView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.loannetwork.app.ui.billingCompany.bottomsheets.state.MessageAdapter
import com.rohans.sacrenachat.R
import com.rohans.sacrenachat.databinding.FragmentMessageBinding
import com.rohans.sacrenachat.ui.fullscreen.FullScreenActivity
import com.rohans.sacrenachat.utils.CameraHelper
import com.rohans.sacrenachat.utils.PermissionHelper
import dagger.hilt.android.AndroidEntryPoint
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.models.Message
import java.io.File
import java.io.IOException
import javax.inject.Inject

@AndroidEntryPoint
class MessageFragment: Fragment() {

    private var prevPage: Int = 0;
    private var page: Int = 0;
    private var state: Parcelable? = null
    private var photoFile: File? = null;
    private lateinit var cameraResult: ActivityResultLauncher<Intent>
    private var hasNext: Boolean = false;
    private var lastMessage: Message? = null
    private lateinit var adapter: MessageAdapter
    val pageSize = 9
    private var tempLastMessage: Message? = null

    @Inject
    lateinit var chatClient: ChatClient
    private var channelId: String? = null
    private var cName: String? = null
    private var cImage: String? = null
    lateinit var binding: FragmentMessageBinding
    val viewModel by viewModels<MessageViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_message, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        channelId = arguments?.getString("channelId");
        cName = arguments?.getString("cName");
        cImage = arguments?.getString("cImage");
        binding.cName = cName;
        binding.cImage = cImage;

        setUp();
        observers();
    }

    private fun setUp(){
        adapter = MessageAdapter(requireContext(), arrayListOf(), this::imageClicked);
        val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, true)
        layoutManager.stackFromEnd = false
        binding.rvMessage.setLayoutManager(layoutManager)
        binding.rvMessage.adapter = adapter;
        //binding.nestedScroll.setOnScrollChangeListener(listener);
        binding.rvMessage.isNestedScrollingEnabled = true;
        textChanges();

        channelId?.let {
            viewModel.getMessage(it, pageSize);
        }

        binding.ivSend.setOnClickListener {
            if(binding.et.text.trim().isNotEmpty()) {
                channelId?.let {
                    viewModel.sendMessage(binding.et.text.toString(), it)
                    binding.et.text.clear();
                }
            }
        }

        binding.ivBack.setOnClickListener {
            findNavController().popBackStack();
        }

        binding.ivCam.setOnClickListener {
            val granted = PermissionHelper.checkCameraPermissions(requireContext(), requireActivity())
            if(granted){
                openCamera(requireContext())
            }
        }

        cameraResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val file = photoFile;
                val channelId = arguments?.getString("channelId");
                channelId?.let {
                    file?.let {
                        viewModel.sendFile(channelId!!, file);
                    }
                }
            }
        }

        channelId?.let {
            viewModel.getNewMessage(it);
        }

        binding.rvMessage.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()

                if (lastVisibleItemPosition == adapter.itemCount-1 && hasNext && page!=prevPage) {
                    prevPage = page;
                    channelId?.let {
                        lastMessage?.let {
                            viewModel.getNexMessage(channelId!!, lastMessage!!, pageSize)
                        }
                    }
                }
            }
        })

    }

    private fun observers(){

        viewModel.message.observe(viewLifecycleOwner, Observer {
            hasNext = it.hasNext;
            page++;
            state = binding.rvMessage.layoutManager?.onSaveInstanceState();
            adapter.addData(it.messages)
            binding.rvMessage.layoutManager?.onRestoreInstanceState(state)
            if(lastMessage==null) {
                scrollToBottom();
            }
            zeroState();
            lastMessage = it.messages.lastOrNull();
        })

        viewModel.newMessage.observe(viewLifecycleOwner, Observer {
            adapter.addNewData(it);
            scrollToBottom();
            zeroState();
        })

    }

    private fun scrollToBottom(){
        binding.rvMessage.post {
            binding.rvMessage.scrollToPosition(0)
        }
    }

    private fun zeroState(){
        if(adapter.itemCount>0){
            binding.tvZero.visibility = View.GONE
        } else {
            binding.tvZero.visibility = View.VISIBLE
        }
    }

    private fun imageClicked(message: Message){
        val intent = Intent(context, FullScreenActivity::class.java)
        intent.putExtra("imagePath", message.attachments.firstOrNull()?.imageUrl)
        startActivity(intent)
    }

    private fun textChanges(){
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                var value = R.drawable.ic_send;
                if(binding.et.text.trim().isNotEmpty()){
                    value = R.drawable.ic_send_active;
                }
                Glide.with(requireContext()).load(value).into(binding.ivSend)
            }
        }
        binding.et.addTextChangedListener(textWatcher)
    }

    private fun openCamera(context: Context) {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        try {
            photoFile = CameraHelper.createImageFile(context);
        } catch (ex: IOException) {
            print(ex)
        }

        photoFile?.also {
            val photoURI: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                it
            )
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            cameraIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            cameraResult.launch(cameraIntent)
        }
    }

}