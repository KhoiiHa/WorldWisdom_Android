package com.example.projektworldwisdom.ui


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.projektworldwisdom.R
import com.example.projektworldwisdom.databinding.FragmentRegisterBinding
import com.example.projektworldwisdom.viewmodel.AuthenticationViewModel


class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthenticationViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.currentUser.observe(viewLifecycleOwner) { firebaseUser ->
            firebaseUser?.let {
                findNavController().navigate(R.id.action_registerFragment_to_homeFragment)
            }
        }

//        // Beobachten von Registrierungfehlern
//        viewModel.registrationError.observe(viewLifecycleOwner) { errorMessage ->
//            errorMessage?.let {
//                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
//            }
//        }

        binding.btBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btRegister.setOnClickListener {
            val email = binding.textEditEmailRegister.text.toString().trim()
            val password = binding.textEditPasswordRegister.text.toString()

            // Eingabefelder validieren
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Bitte füllen Sie alle Felder aus", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(requireContext(), "Das Passwort muss mindestens 6 Zeichen lang sein", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Registrierung durchführen
            viewModel.register(email, password)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}