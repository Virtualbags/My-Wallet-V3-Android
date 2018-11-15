package com.blockchain.sunriver.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.annotation.IdRes
import android.support.v4.app.DialogFragment
import android.support.v7.widget.Toolbar
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import com.blockchain.transactions.Memo
import info.blockchain.wallet.util.HexUtils
import io.reactivex.disposables.CompositeDisposable
import piuk.blockchain.androidcoreui.R
import piuk.blockchain.androidcoreui.utils.extensions.gone
import piuk.blockchain.androidcoreui.utils.extensions.visible
import java.lang.Exception

class MemoEditDialog : DialogFragment() {

    private val compositeDisposable = CompositeDisposable()

    init {
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.FullscreenDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(
        R.layout.dialog_edit_memo,
        container,
        false
    ).apply {
        isFocusableInTouchMode = true
        requestFocus()
        dialog.window.setWindowAnimations(R.style.DialogNoAnimations)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolBar(view)

        view.findViewById<View>(R.id.button_ok).setOnClickListener {
            setResultAndDismiss()
        }

        ensureTextIsALong(view)

        setupSpinner(view)

        showKeyboard(view.context)
    }

    private fun setupSpinner(view: View) {
        view.findViewById<Spinner>(R.id.memo_type_spinner)
            .also { spinner ->
                spinner.setupOptions(view.context)
                spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

                    override fun onItemSelected(parent: AdapterView<*>, spinner: View, pos: Int, id: Long) {
                        fieldsAndTypes.forEach { (itemId) ->
                            view.findViewById<View>(itemId).gone()
                        }
                        fieldsAndTypes.forEachIndexed { index, (itemId) ->
                            view.findViewById<View>(itemId).update(pos, itemPosition = index)
                        }
                        validate(view)
                    }

                    private fun View.update(selectedPosition: Int, itemPosition: Int) {
                        if (selectedPosition == itemPosition) {
                            visible()
                        }
                        if (selectedPosition == itemPosition) post { requestFocus() }
                        setOnKeyListener { _, keyCode, _ ->
                            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                                setResultAndDismiss()
                                return@setOnKeyListener true
                            }
                            return@setOnKeyListener false
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {
                    }
                }

                populateFromArguments(spinner)
            }
    }

    private fun setupToolBar(view: View) {
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar_general)
        toolbar.setTitle(R.string.xlm_memo_toolbar_title)
        toolbar.setNavigationOnClickListener { dismiss() }
    }

    private fun ensureTextIsALong(view: View) {
        val validator = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                validate(view)
            }
        }
        fieldsAndTypes.forEach { (id) ->
            view.findViewById<EditText>(id).addTextChangedListener(validator)
        }
    }

    private fun validate(view: View) {
        val value = enteredValue()
        val valid = when (findFieldId(selectedIndex())) {
            R.id.memo_text -> value.length <= 28
            R.id.memo_id -> isValidId(value)
            R.id.memo_hash -> isValidHash(value)
            else -> true
        }
        view.findViewById<View>(R.id.button_ok).isEnabled = valid
    }

    private fun isValidId(s: String): Boolean {
        return try {
            s.toLong()
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun isValidHash(s: String): Boolean {
        return try {
            HexUtils.decodeHex(s.toCharArray()).size == 32
        } catch (e: Exception) {
            false
        }
    }

    private fun populateFromArguments(spinner: Spinner) {
        arguments?.let {
            val argType = it.getString(ARGUMENT_TYPE)
            val index = fieldsAndTypes.indexOfFirst { (_, type) -> type == argType }
            spinner.setSelection(index)
            textView(index).text = it.getString(ARGUMENT_VALUE)
        }
    }

    private fun showKeyboard(context: Context) {
        val inputMethodManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }

    private fun setResultAndDismiss() {
        targetFragment?.onActivityResult(targetRequestCode,
            Activity.RESULT_OK,
            Intent().apply {
                putExtra(ARGUMENT_TYPE, selectedType())
                putExtra(ARGUMENT_VALUE, enteredValue())
            }
        )
        dismiss()
    }

    private fun enteredValue(): String =
        textView(selectedIndex()).text.toString()

    private fun textView(selectedIndex: Int): TextView =
        view!!.findViewById(findFieldId(selectedIndex))

    @IdRes
    private fun findFieldId(selectedIndex: Int) = findFieldAndType(selectedIndex).first

    private fun selectedType() =
        findFieldAndType(selectedIndex()).second

    private val fieldsAndTypes = listOf(
        Pair(R.id.memo_text, "text"),
        Pair(R.id.memo_id, "id"),
        Pair(R.id.memo_hash, "hash"),
        Pair(R.id.memo_hash, "return")
    )

    private fun findFieldAndType(selectedIndex: Int) = fieldsAndTypes[selectedIndex]

    private fun selectedIndex() = view!!.findViewById<Spinner>(R.id.memo_type_spinner).selectedItemPosition

    private fun Spinner.setupOptions(context: Context) {
        ArrayAdapter.createFromResource(
            context,
            R.array.xlm_memo_types,
            R.layout.dialog_edit_memo_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            this.adapter = adapter
        }
    }

    override fun onPause() {
        compositeDisposable.clear()
        super.onPause()
    }

    override fun onDestroy() {
        compositeDisposable.clear()
        super.onDestroy()
    }

    companion object {

        private const val ARGUMENT_VALUE = "VALUE"
        private const val ARGUMENT_TYPE = "TYPE"

        fun toMemo(intent: Intent?): Memo {
            if (intent == null) return Memo.None
            return Memo(
                value = intent.extras.getString(ARGUMENT_VALUE),
                type = intent.extras.getString(ARGUMENT_TYPE)
            )
        }

        fun create(memo: Memo): DialogFragment {
            return MemoEditDialog().apply {
                arguments = Bundle().apply {
                    putString(ARGUMENT_VALUE, memo.value)
                    putString(ARGUMENT_TYPE, memo.type ?: "text")
                }
            }
        }
    }
}
