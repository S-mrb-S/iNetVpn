package com.xray.lite.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import sp.hamrahvpn.R
import sp.hamrahvpn.databinding.ActivityAngMainBinding
import com.xray.lite.helper.SimpleItemTouchHelperCallback
import com.xray.lite.ui.adapters.MainRecyclerAdapter
import com.xray.lite.viewmodel.MainViewModel

class MainAngActivity : BaseActivity() {
    private lateinit var binding: ActivityAngMainBinding

    private val adapter by lazy { MainRecyclerAdapter(this) }

    private var mItemTouchHelper: ItemTouchHelper? = null
    val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAngMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        // hide toolbar!
        supportActionBar?.hide();

        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        val callback = SimpleItemTouchHelperCallback(adapter)
        mItemTouchHelper = ItemTouchHelper(callback)
        mItemTouchHelper?.attachToRecyclerView(binding.recyclerView)

        binding.ivServerSettings.setOnClickListener {
            startActivity(Intent(this, MainSettingsV2ray::class.java))
            overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_left);
        }

        binding.llServerBack.setOnClickListener {
            finish();
            overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_right);
        }

        setupViewModel()

    }

    private fun setupViewModel() {
        mainViewModel.startListenBroadcast()
    }

    public override fun onResume() {
        super.onResume()
        mainViewModel.reloadServerList()
    }

    public override fun onPause() {
        super.onPause()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        finish();
        overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_right);
    }

}
