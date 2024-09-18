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
import com.google.android.material.snackbar.Snackbar

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
            // Nach dem Speichern neu laden, um die UI zu aktualisieren
            noteViewModel.loadNoteContent(noteId)
        }

        arguments?.getInt("noteId")?.let {
            noteId = it
            noteViewModel.loadNoteContent(noteId)
        }

        // Beobachte nun `noteContent` im ViewModel, um die UI zu aktualisieren
        noteViewModel.noteContent.observe(viewLifecycleOwner) { content ->
            binding.noteEditText.setText(content)
        }

        // Beobachte auch `error`, um Fehler anzuzeigen
        noteViewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            if (errorMessage != null) {
                // Fehler anzeigen (z.B. Snackbar oder Toast)
                Snackbar.make(view, errorMessage, Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    override fun onPause() {
        super.onPause()

        val content = binding.noteEditText.text.toString()
        noteViewModel.saveNoteContent(noteId, content)
        // Nach dem Speichern in onPause ebenfalls neu laden
        noteViewModel.loadNoteContent(noteId)
    }
}