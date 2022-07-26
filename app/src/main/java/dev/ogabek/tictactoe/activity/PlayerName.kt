package dev.ogabek.tictactoe.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import dev.ogabek.tictactoe.databinding.ActivityPlayerNameBinding
import dev.ogabek.tictactoe.utils.toast

class PlayerName : AppCompatActivity() {

    private lateinit var binding: ActivityPlayerNameBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerNameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()

    }

    private fun initViews() {
        binding.apply {
            btnEnterGame.setOnClickListener {
                val roomID = etRoomID.text.toString().trim()

                if (roomID.isEmpty()) {
                    toast("Please Enter Room ID")
                } else {
                    val intent = Intent(this@PlayerName, MainActivity::class.java)
                    intent.putExtra("newGame", false)
                    intent.putExtra("roomID", roomID)
                    startActivity(intent)
                    finish()
                }

            }

            btnCreateGame.setOnClickListener {
                val intent = Intent(this@PlayerName, MainActivity::class.java)
                intent.putExtra("newGame", true)
                startActivity(intent)
                finish()
            }

        }
    }

}