package com.example.projektworldwisdom.login
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.projektworldwisdom.R
import com.example.projektworldwisdom.databinding.FragmentLoginBinding
import com.example.projektworldwisdom.viewmodel.AuthenticationViewModel


class LoginFragment : Fragment() {
    private lateinit var binding: FragmentLoginBinding
    private val viewModel: AuthenticationViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        viewModel.currentUser.observe(viewLifecycleOwner) { firebaseUser ->
            firebaseUser?.let {
                if (findNavController().currentDestination?.id == R.id.loginFragment) {
                    // Use the action so nav_graph popUpTo/backstack rules are applied.
                    findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                }
            }
        }

        viewModel.loginError.observe(viewLifecycleOwner) { errorMessage ->
            if (errorMessage != null) {
                // Zeige die Fehlermeldung an
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
            }
        }



        binding.btLogin.setOnClickListener {
            val email = binding.tietEmail.text.toString()
            val password = binding.tietPassword.text.toString()
            viewModel.login(email, password)

        }

    }
}
