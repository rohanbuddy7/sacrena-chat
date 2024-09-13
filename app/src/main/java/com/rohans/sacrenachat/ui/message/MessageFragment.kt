package com.rohans.sacrenachat.ui.message

import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.core.widget.NestedScrollView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
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
        adapter = MessageAdapter(requireContext(), arrayListOf(), this::clicked);
        val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, true)
        layoutManager.stackFromEnd = false
        binding.rvMessage.setLayoutManager(layoutManager)
        binding.rvMessage.adapter = adapter;
        binding.nestedScroll.setOnScrollChangeListener(listener);
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
            if(lastMessage==null) {
                scrollToBottom();
            } else {
                scrollToBottomItem();
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

    var listener = object : NestedScrollView.OnScrollChangeListener{
        override fun onScrollChange(
            v: NestedScrollView,
            scrollX: Int,
            scrollY: Int,
            oldScrollX: Int,
            oldScrollY: Int
        ) {
            if (scrollY == 0) {
                if (hasNext && lastMessage?.id!=tempLastMessage?.id ) {
                    channelId?.let {
                        lastMessage?.let {
                            tempLastMessage = lastMessage!!;
                            viewModel.getNexMessage(channelId!!, lastMessage!!, pageSize)
                        }
                    }
                }
            }
        }
    }

    private fun scrollToBottomItem(){
        val hide = 6;
        val lastPosition = (binding.rvMessage.adapter?.itemCount?:0) - (pageSize-hide);
        binding.rvMessage.post {
            val y = binding.rvMessage.getChildAt(lastPosition).getY();
            binding.nestedScroll.post {
                binding.nestedScroll.fling(0)
                binding.nestedScroll.smoothScrollTo(0, y.toInt())
            }
        }
    }

    private fun scrollToBottom(){
        binding.rvMessage.post {
            binding.nestedScroll.post {
                binding.nestedScroll.smoothScrollTo(0, binding.nestedScroll.getChildAt(0).bottom)
            }
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