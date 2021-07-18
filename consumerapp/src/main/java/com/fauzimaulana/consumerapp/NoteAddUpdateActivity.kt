package com.fauzimaulana.consumerapp

import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.fauzimaulana.consumerapp.databinding.ActivityNoteAddUpdateBinding
import com.fauzimaulana.consumerapp.db.DatabaseContract
import com.fauzimaulana.consumerapp.db.DatabaseContract.NoteColumns.Companion.CONTENT_URI
import com.fauzimaulana.consumerapp.db.DatabaseContract.NoteColumns.Companion.DATE
import com.fauzimaulana.consumerapp.entity.Note
import com.fauzimaulana.consumerapp.helper.MappingHelper
import java.text.SimpleDateFormat
import java.util.*

class NoteAddUpdateActivity : AppCompatActivity(), View.OnClickListener {


    private lateinit var binding: ActivityNoteAddUpdateBinding
    private lateinit var uriWithId: Uri

    private var isEdit = false
    private var note: Note? = null
    private var position: Int = 0

    companion object {
        const val EXTRA_NOTE = "extra_note"
        const val EXTRA_POSITION = "extra_position"
        const val ALERT_DIALOG_CLOSE = 10
        const val ALERT_DIALOG_DELETE = 20
    }

    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault())
        val date = Date()

        return dateFormat.format(date)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoteAddUpdateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        note = intent.getParcelableExtra(EXTRA_NOTE)
        if (note != null) {
            position = intent.getIntExtra(EXTRA_POSITION, 0)
            isEdit = true
        } else {
            note = Note()
        }

        val actionBarTitle: String
        val btnTitle: String

        if (isEdit) {
            //uri that we got here will be used for getting data from provider
            //content://com.fauzimaulana.mynotesapp/note/id

            uriWithId = Uri.parse(CONTENT_URI.toString() + "/" + note?.id)

            val cursor = contentResolver.query(uriWithId, null, null, null, null)
            if (cursor !=  null) {
                note = MappingHelper.mapCursorToObject(cursor)
                cursor.close()
            }

            actionBarTitle = "Edit"
            btnTitle = "Update"

            note?.let {
                binding.edtTitle.setText(it.title)
                binding.edtDesc.setText(it.description)
            }
        } else {
            actionBarTitle = "Add"
            btnTitle = "Save"
        }

        supportActionBar?.title = actionBarTitle
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.btnSubmit.text = btnTitle
        binding.btnSubmit.setOnClickListener(this)

    }

    override fun onClick(view: View) {
        if (view.id == R.id.btn_submit) {
            val title = binding.edtTitle.text.toString().trim()
            val desc = binding.edtDesc.text.toString().trim()

            if (title.isEmpty()) {
                binding.edtTitle.error = "This field cannot be empty"
                return
            }

            note?.title = title
            note?.description = desc

            val intent = Intent()
            intent.putExtra(EXTRA_NOTE, note)
            intent.putExtra(EXTRA_POSITION, position)

            val values  = ContentValues()
            values.put(DatabaseContract.NoteColumns.TITLE, title)
            values.put(DatabaseContract.NoteColumns.DESCRIPTION, desc)

            if (isEdit) {
                //content://com.fauzimaulana.mynotesapp/note/id
                contentResolver.update(uriWithId, values, null, null)
                Toast.makeText(this, "One Item Edited", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                values.put(DATE, getCurrentDate())
                //content://com.fauzimaulana.mynotesapp/note
                contentResolver.insert(CONTENT_URI, values)
                Toast.makeText(this, "One Item Saved", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (isEdit) {
            menuInflater.inflate(R.menu.menu_form, menu)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_delete -> showAlertDialog(ALERT_DIALOG_DELETE)
            android.R.id.home -> showAlertDialog(ALERT_DIALOG_CLOSE)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        showAlertDialog(ALERT_DIALOG_CLOSE)
    }

    private fun showAlertDialog(type: Int) {
        val isDialogClose = type == ALERT_DIALOG_CLOSE
        val dialogTitle: String
        val dialogMessage: String

        if (isDialogClose) {
            dialogTitle = "Cancel"
            dialogMessage = "Are you sure? changes you made may not be saved"
        } else {
            dialogTitle = "Delete Note"
            dialogMessage = "Are you sure? this note will be deleted"
        }

        val alertDialogBuilder = AlertDialog.Builder(this)

        alertDialogBuilder.setTitle(dialogTitle)
        alertDialogBuilder
            .setMessage(dialogMessage)
            .setCancelable(false)
            .setPositiveButton("Yes") { _,_ ->
                if (isDialogClose) {
                    finish()
                } else {
                    //content://com.fauzimaulana.mynotesapp/note/id
                    contentResolver.delete(uriWithId, null, null)
                    Toast.makeText(this, "One Item Deleted", Toast.LENGTH_SHORT)
                }
            }
            .setNegativeButton("No") { dialog, _ -> dialog.cancel() }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }
}