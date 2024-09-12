package com.rohans.sacrenachat.ui.message

import android.content.Intent
import android.graphics.Camera
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.rohans.sacrenachat.utils.PermissionHelper
import dagger.hilt.android.AndroidEntryPoint
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.models.Message
import javax.inject.Inject

@AndroidEntryPoint
class MessageFragment: Fragment() {

    private var hasNext: Boolean = false;
    private var lastMessage: Message? = null
    private lateinit var adapter: MessageAdapter
    val pageSize = 20

    @Inject
    lateinit var chatClient: ChatClient
    private var channelId: String? = null
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
        setUp();
        observers();
    }

    private fun setUp(){
        adapter = MessageAdapter(requireContext(), arrayListOf(), this::clicked);
        binding.rvMessage.adapter = adapter;
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
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivity(intent);
            }
        }

        channelId?.let {
            viewModel.getNewMessage(it);
        }
    }

    private fun observers(){

        viewModel.message.observe(viewLifecycleOwner, Observer {
            hasNext = it.hasNext;
            adapter.addData(it.messages);
            binding.data = it.messages.firstOrNull();
            if(lastMessage==null) {
                scrollToBottom();
            }
            zeroState();
            lastMessage = it.messages.firstOrNull();
        })

        viewModel.newMessage.observe(viewLifecycleOwner, Observer {
            adapter.addNewData(it);
            scrollToBottom();
            zeroState();
        })

        binding.rvMessage.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                if (firstVisibleItemPosition == 0 && hasNext) {
                    channelId?.let {
                        lastMessage?.let {
                            viewModel.getNexMessage(channelId!!, lastMessage!!, pageSize)
                        }
                    }
                }
            }
        })

    }

    private fun scrollToBottom(){
        val lastPosition = binding.rvMessage.adapter!!.itemCount
        binding.rvMessage.post {
            binding.rvMessage.smoothScrollToPosition(lastPosition)
        }
    }

    private fun zeroState(){
        if(adapter.itemCount>0){
            binding.tvZero.visibility = View.GONE
        } else {
            binding.tvZero.visibility = View.VISIBLE
        }
    }

    private fun clicked(message: Message){}

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

}