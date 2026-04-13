package com.liquidglass.demo

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.liquidglass.demo.databinding.ActivityMainBinding
import com.liquidglass.demo.ui.ColorAdapter
import com.liquidglass.demo.ui.ColorItem

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val palette = intArrayOf(
        0xFFE53935.toInt(), 0xFFD81B60.toInt(), 0xFF8E24AA.toInt(), 0xFF5E35B1.toInt(),
        0xFF3949AB.toInt(), 0xFF1E88E5.toInt(), 0xFF039BE5.toInt(), 0xFF00ACC1.toInt(),
        0xFF00897B.toInt(), 0xFF43A047.toInt(), 0xFF7CB342.toInt(), 0xFFC0CA33.toInt(),
        0xFFFDD835.toInt(), 0xFFFFB300.toInt(), 0xFFFB8C00.toInt(), 0xFFF4511E.toInt(),
        0xFF6D4C41.toInt(), 0xFF757575.toInt(), 0xFF546E7A.toInt(), 0xFF00BCD4.toInt(),
    )
    private val titles = arrayOf(
        "Aurora Borealis","Crimson Tide","Jade Forest","Solar Flare","Ocean Drift",
        "Velvet Dusk","Golden Hour","Midnight Bloom","Sapphire Rain","Ember Glow",
        "Teal Storm","Lilac Haze","Copper Rust","Arctic Fox","Magenta Wave",
        "Olive Branch","Cobalt Blue","Rose Quartz","Tangerine Sky","Mint Breeze"
    )
    private val subtitles = arrayOf(
        "Spectacular light display","Deep sea currents","Ancient woodland paths","High energy particles",
        "Gentle coastal waves","Twilight atmosphere","Warm afternoon light","Night flowers opening",
        "Monsoon season begins","Volcanic activity nearby","Powerful weather front","Morning mist rising",
        "Oxidised metal beauty","Winter wildlife sighting","Tropical weather band","Mediterranean foliage",
        "Deep water pigments","Mineral crystal formation","Desert sunset colours","Fresh spearmint fields"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
        } else {
            @Suppress("DEPRECATION")
            window.setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            )
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecycler()
        setupNavBar()
        setupButton()
        setupPanel()
    }

    private fun setupRecycler() {
        val items = (0 until 350).map { i ->
            ColorItem(
                palette[i % palette.size],
                "${titles[i % titles.size]} #${i + 1}",
                subtitles[i % subtitles.size],
                i % 3 == 0
            )
        }
        binding.recycler.layoutManager = LinearLayoutManager(this)
        binding.recycler.adapter = ColorAdapter(items)
        binding.recycler.addOnScrollListener(object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
                binding.hostLayout.invalidate()
            }
        })
    }

    private fun setupNavBar() {
        binding.navBar.addTab(android.R.drawable.ic_menu_compass,     "Home")
        binding.navBar.addTab(android.R.drawable.ic_menu_search,      "Search")
        binding.navBar.addTab(android.R.drawable.ic_menu_gallery,     "Gallery")
        binding.navBar.addTab(android.R.drawable.ic_menu_preferences, "Settings")

        binding.navBar.onTabSelected = { index ->
            Toast.makeText(this, "Tab $index selected", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupButton() {
        binding.glassButton.setOnClickListener {
            Toast.makeText(this, "Button clicked!", Toast.LENGTH_SHORT).show()
        }
    }


    private fun setupPanel() {
        binding.glassSwitch.setOnCheckedChangeListener { _, isChecked ->
            binding.panelText.text = if (isChecked) "ON" else "OFF"
            Toast.makeText(this, if (isChecked) "On" else "Off", Toast.LENGTH_SHORT).show()
        }

        binding.panelImage.setOnClickListener {
            Toast.makeText(this, "Camera clicked!", Toast.LENGTH_SHORT).show()
        }

        binding.panelText.setOnClickListener {
            Toast.makeText(this, "Text clicked!", Toast.LENGTH_SHORT).show()
        }
    }
}