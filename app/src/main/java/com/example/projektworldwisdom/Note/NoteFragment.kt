package com.example.projektworldwisdom.Note

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.projektworldwisdom.databinding.FragmentNoteBinding
import com.example.projektworldwisdom.local.NoteRepository
import com.example.projektworldwisdom.local.QuoteDatabase

class NoteFragment : Fragment() {

    private val noteViewModel: NoteViewModel by viewModels {
        val database = QuoteDatabase.getDatabase(requireContext())
        val noteDao = database.noteDao()
        val repository = NoteRepository(noteDao)
        NoteViewModelFactory(repository)
    }

    private var _binding: FragmentNoteBinding? = null
    private val binding get() = _binding!!
    private var noteId: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNoteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.noteEditText.addTextChangedListener {
            val content = it.toString()
            noteViewModel.saveNoteContent(noteId, content)
        }

        arguments?.getInt("noteId")?.let {
            noteId = it
            noteViewModel.loadNoteContent(noteId)
        }

        noteViewModel.noteContent.observe(viewLifecycleOwner) { content ->
            binding.noteEditText.setText(content)
        }
    }

    override fun onPause() {
        super.onPause()

        val content = binding.noteEditText.text.toString()
        noteViewModel.saveNoteContent(noteId, content)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}