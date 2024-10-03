package com.example.projektworldwisdom.ui
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.projektworldwisdom.databinding.FragmentLoginBinding
import com.example.projektworldwisdom.viewmodel.AuthenticationViewModel


class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthenticationViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Beobachte den aktuellen Benutzer
        viewModel.currentUser.observe(viewLifecycleOwner) { firebaseUser ->
            firebaseUser?.let {
                findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToHomeFragment())
            }
        }

        // Beobachte Fehler beim Login
        viewModel.loginError.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }

        // Login-Button-Klick-Listener
        binding.btLogin.setOnClickListener {
            val email = binding.tietEmail.text.toString().trim()
            val password = binding.tietPassword.text.toString()

            // Eingabefelder validieren
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Bitte geben Sie E-Mail und Passwort ein", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Login-Versuch
            viewModel.login(email, password)
        }

        // Register-Button-Klick-Listener
        binding.btRegister.setOnClickListener {
            findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToRegisterFragment())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

