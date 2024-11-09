package com.example.sudoku

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var main: ConstraintLayout
    private lateinit var errorsText: TextView
    private lateinit var cellsText: TextView
    private lateinit var generateButton: Button
    private lateinit var sudokuLayout: GridLayout
    private lateinit var sudokuCellDrawable: Drawable
    private lateinit var sudokuCellAltDrawable: Drawable
    private lateinit var sudokuCellAltAlphaDrawable: Drawable
    private lateinit var sudokuCellCurDrawable: Drawable
    private lateinit var sudokuCellFalse: Drawable
    private lateinit var buttonNumber: Drawable
    private lateinit var buttonNumberActive: Drawable
    private lateinit var buttonNumberTrue: Drawable
    private lateinit var num1: Button
    private lateinit var num2: Button
    private lateinit var num3: Button
    private lateinit var num4: Button
    private lateinit var num5: Button
    private lateinit var num6: Button
    private lateinit var num7: Button
    private lateinit var num8: Button
    private lateinit var num9: Button

    private var pref: SharedPreferences? = null
    private var sudoku: Array<Int> = arrayOf()
    private var sudokuNull: Array<Int> = arrayOf()
    private var currentNum: String = ""
    private var sudokuButtons: MutableList<Pair<Button, Array<Int>>> = mutableListOf()
    private lateinit var numberButtons: Array<Button>
    private var errors: Int = 0
    private var notNullCount: Int = 0

    @SuppressLint("ResourceAsColor", "SetTextI18n", "UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }

        pref = getSharedPreferences("TABLE", MODE_PRIVATE)
        sudoku = getSudokuPerf()
        sudokuNull = getSudokuNullPerf()
        errors = getErrorsPerf()

        installSplashScreen()

        enableEdgeToEdge()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setContentView(R.layout.activity_main)

        main = findViewById(R.id.main)
        errorsText = findViewById(R.id.errors)
        cellsText = findViewById(R.id.cells)
        generateButton = findViewById(R.id.generateButton)
        sudokuLayout = findViewById(R.id.sudokuLayout)
        num1 = findViewById(R.id.num1)
        num2 = findViewById(R.id.num2)
        num3 = findViewById(R.id.num3)
        num4 = findViewById(R.id.num4)
        num5 = findViewById(R.id.num5)
        num6 = findViewById(R.id.num6)
        num7 = findViewById(R.id.num7)
        num8 = findViewById(R.id.num8)
        num9 = findViewById(R.id.num9)
        numberButtons = arrayOf(num1, num2, num3, num4, num5, num6, num7, num8, num9)
        sudokuCellDrawable = getDrawable(R.drawable.shape_sudoku_cell)!!
        sudokuCellAltDrawable = getDrawable(R.drawable.shape_sudoku_cell_alt)!!
        sudokuCellAltAlphaDrawable = getDrawable(R.drawable.shape_sudoku_cell_alt_alpha)!!
        sudokuCellCurDrawable = getDrawable(R.drawable.shape_sudoku_cell_cur)!!
        sudokuCellFalse = getDrawable(R.drawable.shape_sudoku_cell_false)!!
        buttonNumber = getDrawable(R.drawable.shape_button_numbers)!!
        buttonNumberActive = getDrawable(R.drawable.shape_button_numbers_active)!!
        buttonNumberTrue = getDrawable(R.drawable.shape_button_numbers_true)!!

        updateErrors()
        updateCells()

        for (numBtn in numberButtons) {
            numBtn.setOnClickListener {
                if (numBtn.background != buttonNumberTrue) {
                    clearSudoku()
                    if (currentNum == numBtn.text.toString()) {
                        currentNum = ""
                        numBtn.background = buttonNumber
                    } else {
                        currentNum = numBtn.text.toString()
                        for (n in numberButtons) {
                            if (n.background != buttonNumberTrue) {
                                n.background = buttonNumber
                            }
                        }
                        numBtn.background = buttonNumberActive
                    }
                }
            }
        }

        main.setOnClickListener {
            clearSudoku()
            currentNum = ""
            for (n in numberButtons) {
                if (n.background != buttonNumberTrue) {
                    n.background = buttonNumber
                }
            }
        }

        generateButton.setOnClickListener{
            sudokuLayout.removeAllViews()
            for (nb in numberButtons) nb.background = buttonNumber
            sudoku = generateSudoku()
            sudokuNull = removeCells(sudoku, Random.nextInt(40,64))
            sudokuButtons = mutableListOf()
            currentNum = ""
            errors = 0
            updateErrors()
            updateCells()
            drawSudoku()
        }

        findViewById<View>(android.R.id.content).post {
            drawSudoku()
        }
    }

    override fun onPause() {
        super.onPause()
        saveSudokuPerf(sudoku)
        saveSudokuNullPerf(sudokuNull)
        saveErrorsPerf(errors)
    }

    private fun drawSudoku() {
        if (!sudoku.contentEquals(arrayOf())) {
            var num = 0
            for (i in 0..8) {
                for (j in 0..8) {
                    val button = Button(this).apply {
                        layoutParams = GridLayout.LayoutParams().apply {
                            background = if (
                                (j in 3..5 && (i in 0..2 || i in 6..8))
                                ||
                                ((j in 0..2 || j in 6..8) && i in 3..5)
                            ) {
                                sudokuCellAltAlphaDrawable
                            } else {
                                sudokuCellDrawable
                            }
                            textSize = 15F
                            if (sudokuNull[num] != 0) text = sudoku[num].toString()
                            rowSpec = GridLayout.spec(i, 1f)
                            columnSpec = GridLayout.spec(j, 1f)
                            width = 0
                            height = 0
                        }
                    }
                    sudokuButtons += button to arrayOf(i, j)
                    button.setTextColor(Color.parseColor("#191939"))
                    button.setOnClickListener {
                        if (currentNum != "") {
                            if (button.text == "") {
                                if (sudoku[i * 9 + j].toString() == currentNum) {
                                    sudokuNull[i * 9 + j] = currentNum.toInt()
                                    button.text = currentNum
                                    clearSudoku()
                                    var count = 0
                                    for (btn in sudokuButtons) {
                                        if (button.text != "" && btn.first.text == button.text) {
                                            count += 1
                                            btn.first.background = sudokuCellAltDrawable
                                        }
                                    }
                                    if (count == 9) {
                                        numberButtons[currentNum.toInt() - 1].background =
                                            buttonNumberTrue
                                        currentNum = ""
                                    }
                                    updateCells()
                                } else {
                                    clearSudoku()
                                    button.text = currentNum
                                    button.background = sudokuCellFalse
                                    errors += 1
                                    updateErrors()
                                }
                            }
                        } else {
                            clearSudoku()
                            for (btn in sudokuButtons) {
                                val row = btn.second[0]
                                val column = btn.second[1]
                                var bools: Array<Boolean> = arrayOf()

                                when (i) {
                                    in 0..2 -> if (row in 0..2) {
                                        bools += true
                                    }

                                    in 3..5 -> if (row in 3..5) {
                                        bools += true
                                    }

                                    in 6..8 -> if (row in 6..8) {
                                        bools += true
                                    }

                                    else -> bools += false
                                }

                                when (j) {
                                    in 0..2 -> if (column in 0..2) {
                                        bools += true
                                    }

                                    in 3..5 -> if (column in 3..5) {
                                        bools += true
                                    }

                                    in 6..8 -> if (column in 6..8) {
                                        bools += true
                                    }

                                    else -> bools += false
                                }

                                if ((i == row || j == column) || bools.contentEquals(
                                        arrayOf(
                                            true,
                                            true
                                        )
                                    )
                                ) {
                                    btn.first.background = sudokuCellCurDrawable
                                }
                                if (button.text != "" && btn.first.text == button.text) {
                                    btn.first.background = sudokuCellAltDrawable
                                }
                            }
                            button.background = sudokuCellAltDrawable
                        }
                    }
                    num += 1
                    sudokuLayout.addView(button)
                }
            }
        }
    }

    private fun clearSudoku() {
        for (btn in sudokuButtons) {
            val button = btn.first
            val row = btn.second[0]
            val column = btn.second[1]
            if (button.background == sudokuCellFalse) {
                button.text = ""
            }
            button.background = if (
                (column in 3..5 && (row in 0..2 || row in 6..8))
                ||
                ((column in 0..2 || column in 6..8) && row in 3..5)
            ) {
                sudokuCellAltAlphaDrawable
            } else {
                sudokuCellDrawable
            }
        }
    }

    private fun updateErrors() {
        errorsText.text = "$errors"
    }

    @SuppressLint("SetTextI18n")
    private fun updateCells() {
        notNullCount = 81-sudokuNull.myCount(0)
        if (notNullCount != 81) {
            cellsText.text="$notNullCount/81 (-${81-notNullCount})"
        } else {
            cellsText.text = ""
        }
    }

    private fun generateSudoku() : Array<Int> {
        val py = Python.getInstance()
        val moduleMain = py.getModule("sudoku")
        val sudoku = moduleMain.callAttr("create_sudoku").toJava(Array<Int>::class.java)
        return sudoku
    }

    @Suppress("SameParameterValue")
    private fun removeCells(sudoku:Array<Int>, nullCells: Int) : Array<Int> {
        val py = Python.getInstance()
        val moduleSolution = py.getModule("sudoku_solution")

        val result = moduleSolution.callAttr("remove_numbers", sudoku, nullCells).toJava(Array<Int>::class.java)
        return result
    }

    @Suppress("KotlinConstantConditions")
    private fun <T> Array<T>.myCount(element: Int): Int {
        var count = 0
        for (item in this) {
            if (item == element) {
                count++
            }
        }
        return count
    }

    private fun getSudokuPerf(): Array<Int> {
        val prefs: SharedPreferences = this.getPreferences(Context.MODE_PRIVATE)
        val gson = Gson()
        val json = prefs.getString("sudoku", null)
        val type = object : TypeToken<Array<Int>>() {}.type
        return gson.fromJson(json, type) ?: arrayOf()
    }

    private fun saveSudokuPerf(arr: Array<Int>) {
        val prefs: SharedPreferences = this.getPreferences(Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = prefs.edit()
        val gson = Gson()
        val json: String = gson.toJson(arr)
        editor.putString("sudoku", json)
        editor.apply()
    }

    private fun getSudokuNullPerf(): Array<Int> {
        val prefs: SharedPreferences = this.getPreferences(Context.MODE_PRIVATE)
        val gson = Gson()
        val json = prefs.getString("sudokuNull", null)
        val type = object : TypeToken<Array<Int>>() {}.type
        return gson.fromJson(json, type) ?: arrayOf()
    }

    private fun saveSudokuNullPerf(arr: Array<Int>) {
        val prefs: SharedPreferences = this.getPreferences(Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = prefs.edit()
        val gson = Gson()
        val json: String = gson.toJson(arr)
        editor.putString("sudokuNull", json)
        editor.apply()
    }

    private fun getErrorsPerf(): Int {
        val prefs: SharedPreferences = this.getPreferences(Context.MODE_PRIVATE)
        return prefs.getInt("errors", 0)
    }

    private fun saveErrorsPerf(errors: Int) {
        val prefs: SharedPreferences = this.getPreferences(Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = prefs.edit()
        editor.putInt("errors", errors)
        editor.apply()
    }
}


