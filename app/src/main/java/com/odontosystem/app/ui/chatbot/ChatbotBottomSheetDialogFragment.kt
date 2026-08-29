package com.odontosystem.app.ui.chatbot

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.odontosystem.app.data.remote.RetrofitClient
import com.odontosystem.app.databinding.DialogChatbotBinding
import com.odontosystem.app.repository.ChatRepository

class ChatbotBottomSheetDialogFragment : BottomSheetDialogFragment() {

    private var _binding: DialogChatbotBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ChatbotViewModel
    private lateinit var chatAdapter: ChatbotAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogChatbotBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val repository = ChatRepository(RetrofitClient.apiService)
        viewModel = ChatbotViewModel(repository)

        setupRecyclerView()
        setupListeners()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatbotAdapter()
        binding.rvChatMessages.apply {
            layoutManager = LinearLayoutManager(requireContext()).apply {
                stackFromEnd = true
            }
            adapter = chatAdapter
        }
    }

    private fun setupListeners() {
        binding.btnCloseChat.setOnClickListener { dismiss() }

        binding.btnSendChat.setOnClickListener {
            val text = binding.etChatInput.text.toString().trim()
            if (text.isNotEmpty()) {
                viewModel.sendMessage(text)
                binding.etChatInput.setText("")
            }
        }
    }

    private fun observeViewModel() {
        viewModel.messages.observe(viewLifecycleOwner) { list ->
            chatAdapter.submitList(list)
            if (list.isNotEmpty()) {
                binding.rvChatMessages.smoothScrollToPosition(list.size - 1)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
