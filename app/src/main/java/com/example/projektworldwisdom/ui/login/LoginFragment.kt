package com.example.projektworldwisdom.ui.login
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.projektworldwisdom.viewmodel.AuthenticationViewModel
import com.projekt.worldwisdom.databinding.FragmentLoginBinding

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

        viewModel.logout()


        viewModel.currentUser.observe(viewLifecycleOwner) { firebaseUser ->
            firebaseUser?.let {
                findNavController().navigate(LoginFragmentDirections.actionNavigationHomeToNavigationDashboard())
            }

        }



        binding.btLogin.setOnClickListener {
            val email = binding.tietEmail.text.toString()
            val password = binding.tietPassword.text.toString()
            viewModel.login(email, password)

        }

    }
}

