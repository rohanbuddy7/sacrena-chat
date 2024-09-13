package com.rohans.sacrenachat.ui.channel

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.loannetwork.app.ui.billingCompany.bottomsheets.state.ChannelAdapter
import com.rohans.sacrenachat.R
import com.rohans.sacrenachat.databinding.FragmentChannelBinding
import com.rohans.sacrenachat.model.CustomChannel
import com.rohans.sacrenachat.utils.Constants
import dagger.hilt.android.AndroidEntryPoint
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.extensions.internal.users
import javax.inject.Inject

@AndroidEntryPoint
class ChannelFragment: Fragment() {

    private lateinit var adapter: ChannelAdapter

    @Inject
    lateinit var chatClient: ChatClient
    lateinit var binding: FragmentChannelBinding;
    val viewModel by viewModels<ChannelViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_channel, container, false)
        return binding.root;
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUp();
        observers()
    }

    private fun setUp() {
        adapter = ChannelAdapter(requireContext(), arrayListOf(), this::clicked);
        binding.rvChannel.adapter = adapter;

        viewModel.clearChannel()
        viewModel.connectUser()
        viewModel.queryChannel()
    }

    private fun observers() {
        viewModel.channels.observe(viewLifecycleOwner, Observer {
            if(it.isNotEmpty()) {
                adapter.addData(it)
            }
        })
    }

    private fun clicked(customChannel: CustomChannel){
        val bundle = Bundle();
        bundle.putString("channelId", customChannel.channel.id)
        bundle.putString("cName", customChannel.channel.members.filter { it.user.id != Constants.aliceUserId }.firstOrNull()?.user?.name)
        bundle.putString("cImage", customChannel.channel.members.filter { it.user.id != Constants.aliceUserId }.firstOrNull()?.user?.image)
        findNavController().navigate(R.id.messageFragment, bundle);
    }

}