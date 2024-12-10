package com.example.skincure.helper

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import com.example.skincure.R

class NameEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : AppCompatEditText(context, attrs), View.OnTouchListener {

    private var clearButtonIcon: Drawable? = null
    private var nameIcon: Drawable? = null

    init {
        setOnTouchListener(this)

        clearButtonIcon = ContextCompat.getDrawable(context, R.drawable.ic_clear)
        nameIcon = ContextCompat.getDrawable(context, R.drawable.ic_person)
        setButtonDrawables(endOfTheText = nameIcon)
        setPadding(paddingRight + 48, paddingTop, paddingRight, paddingBottom)

        setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                setButtonDrawables(endOfTheText = nameIcon)
            } else {
                if (!text.isNullOrEmpty()) {
                    setButtonDrawables(endOfTheText = clearButtonIcon)
                }
            }
        }

        addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                showClearButton(s?.isNotEmpty() == true)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        addValidation { input ->
            when {
                input.isEmpty() -> context.getString(R.string.required_field)
                else -> null
            }
        }
        isValid()
    }

    internal fun showClearButton(show: Boolean) {
        if (text.isNullOrEmpty()) {
            setButtonDrawables(endOfTheText = nameIcon)
        } else if (show) {
            setButtonDrawables(endOfTheText = clearButtonIcon)
        }
    }

    private fun setButtonDrawables(
        startOfTheText: Drawable? = null,
        topOfTheText: Drawable? = null,
        endOfTheText: Drawable? = null,
        bottomOfTheText: Drawable? = null,
    ) {
        setCompoundDrawablesWithIntrinsicBounds(
            startOfTheText,
            topOfTheText,
            endOfTheText,
            bottomOfTheText
        )
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        if (compoundDrawables[2] != null) {
            val clearButtonStart =
                (clearButtonIcon?.intrinsicWidth?.let { width - paddingEnd - it } ?: 0).toFloat()
            val isClearButtonClicked = event?.x!! > clearButtonStart

            if (isClearButtonClicked) {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        showClearButton(true)
                        return true
                    }

                    MotionEvent.ACTION_UP -> {
                        text?.clear()
                        showClearButton(false)
                        return true
                    }
                }
            }
        }
        return false
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    fun edError(error: CharSequence?) {
        if (error == null) {
            setError(null, null)
        } else {
            setError(error, null)
        }
    }

    fun isValid(): Boolean {
        val input = text.toString()
        return when {
            input.isEmpty() -> {
                edError(context.getString(R.string.name_required))
                false
            }

            else -> {
                edError(null)
                true
            }
        }
    }

    fun addValidation(validation: (String) -> String?) {
        addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable?) {
                val errorText = validation(s.toString())
                if (errorText != null) {
                    edError(errorText)
                } else {
                    edError(null)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }
}