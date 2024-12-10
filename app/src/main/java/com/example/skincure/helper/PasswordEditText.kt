package com.example.skincure.helper

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import com.example.skincure.R

class PasswordEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : AppCompatEditText(context, attrs), View.OnTouchListener {

    private var lockIcon: Drawable? = null
    private var eyeIconVisible: Drawable? = null
    private var eyeIconInvisible: Drawable? = null
    private var isPasswordVisible = false

    init {
        setOnTouchListener(this)
        lockIcon = ContextCompat.getDrawable(context, R.drawable.ic_lock)
        eyeIconVisible = ContextCompat.getDrawable(context, R.drawable.ic_eye_visibility)
        eyeIconInvisible = ContextCompat.getDrawable(context, R.drawable.ic_eye_visibility_off)
        setButtonDrawables(endOfTheText = lockIcon)
        transformationMethod = PasswordTransformationMethod.getInstance()
        setPadding(paddingRight + 48, paddingTop, paddingRight, paddingBottom)

        setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                setButtonDrawables(endOfTheText = lockIcon)
            } else {
                if (!text.isNullOrEmpty()) {
                    setButtonDrawables(endOfTheText = eyeIconInvisible)
                }
            }
        }

        addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                showClearButton(s?.isNotEmpty() == true)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
        addValidation { password ->
            if (password.length >= 8) null else context.getString(R.string.password_minimum_length)
        }
    }

    internal fun showIcon(show: Boolean) {
        if (show) {
            if (isPasswordVisible) {
                setButtonDrawables(endOfTheText = eyeIconVisible)
            } else {
                setButtonDrawables(endOfTheText = eyeIconInvisible)
            }
        } else {
            setButtonDrawables(endOfTheText = lockIcon)
        }
    }

    internal fun showClearButton(show: Boolean) {
        if (show) {
            showIcon(true)
        } else {
            setButtonDrawables(endOfTheText = lockIcon)
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
            val eyeIconStart = (eyeIconInvisible?.intrinsicWidth?.let { width - paddingEnd - it }
                ?: 0).toFloat()
            val isEyeIconClicked = event?.x!! > eyeIconStart

            if (isEyeIconClicked && !text.isNullOrEmpty()) {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        setButtonDrawables(endOfTheText = eyeIconVisible)
                        return true
                    }

                    MotionEvent.ACTION_UP -> {
                        isPasswordVisible = !isPasswordVisible
                        transformationMethod = if (isPasswordVisible) {
                            HideReturnsTransformationMethod.getInstance()
                        } else {
                            PasswordTransformationMethod.getInstance()
                        }
                        setButtonDrawables(
                            endOfTheText = if (isPasswordVisible) eyeIconVisible else eyeIconInvisible
                        )
                        setSelection(text?.length ?: 0)
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
                edError(context.getString(R.string.required_field))
                false
            }

            input.length < 8 -> {
                edError(context.getString(R.string.password_minimum_length))
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