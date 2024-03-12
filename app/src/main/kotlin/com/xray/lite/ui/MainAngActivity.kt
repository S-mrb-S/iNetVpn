package com.xray.lite.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.tencent.mmkv.MMKV
import com.xray.lite.helper.SimpleItemTouchHelperCallback
import com.xray.lite.service.V2RayServiceManager
import com.xray.lite.ui.adapters.MainRecyclerAdapter
import com.xray.lite.util.MmkvManager
import com.xray.lite.util.Utils
import com.xray.lite.viewmodel.MainViewModel
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import sp.inetvpn.R
import sp.inetvpn.databinding.ActivityAngMainBinding
import java.util.concurrent.TimeUnit

class MainAngActivity : BaseActivity() {
    private lateinit var binding: ActivityAngMainBinding

    private val adapter by lazy { MainRecyclerAdapter(this) }

    private var mItemTouchHelper: ItemTouchHelper? = null
    val mainViewModel: MainViewModel by viewModels()

    private val mainStorage by lazy {
        MMKV.mmkvWithID(
            MmkvManager.ID_MAIN,
            MMKV.MULTI_PROCESS_MODE
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAngMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        // hide toolbar!
        supportActionBar?.hide()

        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        val callback = SimpleItemTouchHelperCallback(adapter)
        mItemTouchHelper = ItemTouchHelper(callback)
        mItemTouchHelper?.attachToRecyclerView(binding.recyclerView)

        binding.headerLayout.llBack.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_right)
        }

        setupViewModel()

    }

    fun startV2Ray() {
        if (mainStorage?.decodeString(MmkvManager.KEY_SELECTED_SERVER).isNullOrEmpty()) {
            return
        }
        V2RayServiceManager.startV2Ray(this)
    }

    fun restartV2Ray() {
        if (mainViewModel.isRunning.value == true) {
            Utils.stopVService(this)
            Observable.timer(500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    startV2Ray()
                }
        }
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
        restartV2Ray()
        super.onBackPressed()
        finish()
        overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_right)
    }

}
