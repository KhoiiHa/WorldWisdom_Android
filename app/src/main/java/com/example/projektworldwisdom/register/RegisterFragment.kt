package com.example.projektworldwisdom.register
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.currentUser.observe(viewLifecycleOwner) { firebaseUser ->
            firebaseUser?.let {
                findNavController().navigate(
                    RegisterFragmentDirections.actionRegisterFragmentToHomeFragment()
                )
            }
        }

        binding.btBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btRegister.setOnClickListener {
            val firstname = binding.textEditRegisterFirstname.text.toString()
            val lastname = binding.textEditRegisterLastname.text.toString()
            val email = binding.textEditEmailRegister.text.toString()
            val password = binding.textEditPasswordRegister.text.toString()
            val confirmPassword = binding.textEditConfirmPasswordRegister.text.toString()
            if (password == confirmPassword) {
                viewModel.register(email, password, firstname, lastname)
            } else {
                Toast.makeText(requireContext(), getString(R.string.error_passwords_do_not_match), Toast.LENGTH_SHORT)
                    .show()
            }
        }


    }


}
