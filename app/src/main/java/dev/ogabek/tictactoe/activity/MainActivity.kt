package dev.ogabek.tictactoe.activity

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dev.ogabek.tictactoe.R
import dev.ogabek.tictactoe.databinding.ActivityMainBinding
import dev.ogabek.tictactoe.databinding.WinDialogLayoutBinding
import dev.ogabek.tictactoe.utils.toast

@SuppressLint("SetTextI18n")
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var roomId: String
    private var isNewGame: Boolean = true

    private lateinit var dialog: ProgressDialog

    private val combinations: ArrayList<Array<Int>> = ArrayList()
    private val doneBoxes: ArrayList<String> = ArrayList()

    private var playerId = "0"

    private val databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl("https://tictactoe-ogabekdev-default-rtdb.firebaseio.com/")

    private var opponentFound = false
    private var opponentId = "0"

    private var playerTurn = ""

    private val boxesSelectedBy: Array<String> = arrayOf("", "", "", "", "", "", "", "", "")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setCombinations()
        initViews()

    }

    private fun setCombinations() {
        combinations.add(arrayOf(0, 1, 2))
        combinations.add(arrayOf(3, 4, 5))
        combinations.add(arrayOf(6, 7, 8))
        combinations.add(arrayOf(0, 3, 6))
        combinations.add(arrayOf(1, 4, 7))
        combinations.add(arrayOf(2, 5, 8))
        combinations.add(arrayOf(2, 4, 6))
        combinations.add(arrayOf(0, 4, 8))
    }

    private fun initViews() {

        roomId = intent.getStringExtra("roomID").toString()
        isNewGame = intent.getBooleanExtra("newGame", false)

        playerId = System.currentTimeMillis().toString()

        setRealTimeDatabase()

        setClicks()

        setListeners()

    }

    private fun setRealTimeDatabase() {
        if (isNewGame) {
            roomId = System.currentTimeMillis().toString()
            databaseReference.child("connections").child(roomId).addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!opponentFound) {
                        when (snapshot.childrenCount) {
                            0L -> {
                                snapshot.child("first_player").ref.setValue(playerId)
                                opponentFound = false
                            }
                            1L -> {
                                showProgressDialog(roomId)
                            }
                            2L -> {
                                if (dialog.isShowing) {
                                    dialog.dismiss()
                                }

                                binding.tvRoomID.text = "RoomID : $roomId"
                                opponentFound = true

                                playerTurn = playerId
                                applyTurn(playerTurn)

                            }
                            else -> {
                                toast("Something went wrong. Please try again")
                                val intent = Intent(this@MainActivity, PlayerName::class.java)
                                startActivity(intent)
                                finish()
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    toast("Something went wrong. Please try again")
                    val intent = Intent(this@MainActivity, PlayerName::class.java)
                    startActivity(intent)
                    finish()
                }

            })
        } else {
            databaseReference.child("connections").child(roomId).addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!opponentFound) {
                        if (snapshot.childrenCount == 1L) {
                            snapshot.child("second_player").ref.setValue(playerId)
                            binding.tvRoomID.text = "RoomID : $roomId"
                            binding.llFirstPlayer.setBackgroundResource(R.drawable.player_background_unselected)
                            binding.llSecondPlayer.setBackgroundResource(R.drawable.player_background_selected)
                            playerTurn = snapshot.child("first_player").value as String
                            opponentFound = true
                        } else {
                            toast("You cannot enter the room. Because it busy now")
                            val intent = Intent(this@MainActivity, PlayerName::class.java)
                            startActivity(intent)
                            finish()
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    toast("Something went wrong. Please try again")
                    val intent = Intent(this@MainActivity, PlayerName::class.java)
                    startActivity(intent)
                    finish()
                }

            })
        }
    }

    private fun setClicks() {
        binding.apply {
            ivGame1.setOnClickListener {
                onBoxClick(ivGame1, "1")
            }

            ivGame2.setOnClickListener {
                onBoxClick(ivGame2, "2")
            }

            ivGame3.setOnClickListener {
                onBoxClick(ivGame3, "3")
            }

            ivGame4.setOnClickListener {
                onBoxClick(ivGame4, "4")
            }

            ivGame5.setOnClickListener {
                onBoxClick(ivGame5, "5")
            }

            ivGame6.setOnClickListener {
                onBoxClick(ivGame6, "6")
            }

            ivGame7.setOnClickListener {
                onBoxClick(ivGame7, "7")
            }

            ivGame8.setOnClickListener {
                onBoxClick(ivGame8, "8")
            }

            ivGame9.setOnClickListener {
                onBoxClick(ivGame9, "9")
            }
        }

    }

    private fun onBoxClick(image: ImageView, id: String) {
        if (!doneBoxes.contains(id) && playerTurn == playerId) {
            image.setImageResource(R.drawable.ic_x)

            databaseReference.child("connections").child(roomId).child("turn").child((doneBoxes.size + 1).toString()).child("player_id").setValue(playerId)
            databaseReference.child("connections").child(roomId).child("turn").child((doneBoxes.size + 1).toString()).child("box_position").setValue(id)

            playerTurn = opponentId
            applyTurn(playerTurn)

        }
    }

    private fun setListeners() {
        databaseReference.child("connections").child(roomId).child("turn").addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (data in snapshot.children) {
                    if (data.childrenCount == 2L) {
                        val playerId: String = data.child("player_id").getValue(String::class.java)!!
                        val getBoxPosition: Int = data.child("box_position").getValue(String::class.java)!!.toInt()

                        if (!doneBoxes.contains(getBoxPosition.toString())) {
                            doneBoxes.add(getBoxPosition.toString())

                            when(getBoxPosition) {
                                1 -> {
                                    selectBox(binding.ivGame1, getBoxPosition, playerId)
                                }
                                2 -> {
                                    selectBox(binding.ivGame2, getBoxPosition, playerId)
                                }
                                3 -> {
                                    selectBox(binding.ivGame3, getBoxPosition, playerId)
                                }
                                4 -> {
                                    selectBox(binding.ivGame4, getBoxPosition, playerId)
                                }
                                5 -> {
                                    selectBox(binding.ivGame5, getBoxPosition, playerId)
                                }
                                6 -> {
                                    selectBox(binding.ivGame6, getBoxPosition, playerId)
                                }
                                7 -> {
                                    selectBox(binding.ivGame7, getBoxPosition, playerId)
                                }
                                8 -> {
                                    selectBox(binding.ivGame8, getBoxPosition, playerId)
                                }
                                9 -> {
                                    selectBox(binding.ivGame9, getBoxPosition, playerId)
                                }
                            }

                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

        databaseReference.child("connections").child(roomId).child("won").addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.hasChild("player_id")) {
                    val winPlayerId = snapshot.child("player_id").getValue(String::class.java)

                    if (winPlayerId == playerId) {
                        showResultDialog("You won the Game")
                    } else {
                        showResultDialog("You lose the Game")
                    }

                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun applyTurn(playerTurn: String) {
         binding.apply {
             if (playerTurn == playerId) {
                 llFirstPlayer.setBackgroundResource(R.drawable.player_background_selected)
                 llSecondPlayer.setBackgroundResource(R.drawable.player_background_unselected)
             } else {
                 llFirstPlayer.setBackgroundResource(R.drawable.player_background_unselected)
                 llSecondPlayer.setBackgroundResource(R.drawable.player_background_selected)
             }
         }
    }

    private fun showProgressDialog(roomId: String) {
        dialog = ProgressDialog(this)
        dialog.setCancelable(false)
        dialog.setMessage("Invite your friend by RoomID: $roomId")
        dialog.show()
    }

    private fun selectBox(imageView: ImageView, selectedBoxPosition: Int, selectedByPlayer: String) {
        boxesSelectedBy[selectedBoxPosition - 1] = selectedByPlayer

        playerTurn = if (selectedByPlayer == playerId) {
            imageView.setImageResource(R.drawable.ic_x)
            opponentId
        } else {
            imageView.setImageResource(R.drawable.ic_o)
            playerId
        }

        applyTurn(playerTurn )

        if (isPlayerWon(selectedByPlayer)) {
            databaseReference.child("connections").child(roomId).child("won").child("player_id").setValue(selectedByPlayer)
        }

        if (doneBoxes.size == 9) {
            showResultDialog("It is a draw")
        }

    }

    private fun isPlayerWon(playerId: String): Boolean {
        var isPlayerWon = false

        for (i in 0 until combinations.size) {
            val combination = combinations[i]

            if (boxesSelectedBy[combination[0]] == playerId && boxesSelectedBy[combination[1]] == playerId && boxesSelectedBy[combination[2]] == playerId) {
                isPlayerWon = true
            }

        }

        return isPlayerWon
    }

    private fun showResultDialog(text: String) {
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle("Game Result")
        alertDialog.setMessage(text)
        alertDialog.setCancelable(false)
        alertDialog.setPositiveButton("Start new Game") { _, _ ->
            startActivity(Intent(this, PlayerName::class.java))
            finish()
        }

        alertDialog.show()

    }

}