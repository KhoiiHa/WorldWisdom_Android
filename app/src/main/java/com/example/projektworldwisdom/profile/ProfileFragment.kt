package com.example.projektworldwisdom.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.projektworldwisdom.R

class ProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Verknüpft dieses Fragment mit deinem Layout: fragment_profile.xml
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }
}