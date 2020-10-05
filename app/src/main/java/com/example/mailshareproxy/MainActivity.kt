package com.example.mailshareproxy

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.widget.Button
import android.widget.EditText
import android.widget.TextView

class MainActivity : Activity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		val pref = getPreferences(Context.MODE_PRIVATE)

		val recipient = pref.getString(
			getString(R.string.recipient_key),
			getString(R.string.recipient_default_value)
		) as String

		when (intent?.action) {
			Intent.ACTION_SEND -> {
				handleSendAction(intent, recipient) // Handle single image being sent
			}
			else -> {
				// Handle other intents, such as being started from the home screen
				setContentView(R.layout.activity_main)

				val editTextRecipient = findViewById<EditText>(R.id.editText_Recipients)
				editTextRecipient.setText(recipient)
				val buttonSave = findViewById<Button>(R.id.button_Save)
				buttonSave.setOnClickListener {
					with(pref.edit()) {
						putString(
							getString(R.string.recipient_key),
							editTextRecipient.text.toString()
						)
						commit()
					}
					killMyApp()
				}
			}
		}
	}

	private fun handleSendAction(intent: Intent, recipient: String) {
		var email = ""
		var subject = ""
		var text = ""
		var stream = ""

		intent.getStringExtra(Intent.EXTRA_EMAIL)?.let {
			email = it
		}
		intent.getStringExtra(Intent.EXTRA_SUBJECT)?.let {
			subject = it
		}
		intent.getStringExtra(Intent.EXTRA_TEXT)?.let {
			text = it
		}
		(intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri)?.let {
			stream = it.toString()
		}

		// Build the intent
//		val mailIntent = Intent(Intent.ACTION_SENDTO).apply { // do not allow attached files
//			data = Uri.parse("mailto:") // only email apps should handle this
		val mailIntent = Intent(Intent.ACTION_SEND).apply { // allow attached files
			type = "message/rfc822" // "mailto:" above not allowed with "ACTION_SEND" :(
			putExtra(Intent.EXTRA_EMAIL, arrayOf(recipient)) // recipients
			putExtra(Intent.EXTRA_SUBJECT, subject)
			putExtra(Intent.EXTRA_TEXT, text)
			if (!stream.isBlank()) {
				putExtra(Intent.EXTRA_STREAM, Uri.parse(stream))
				// You can also attach multiple items by passing an ArrayList of Uris
			}
		}

		// Verify it resolves
		val activities: List<ResolveInfo> = packageManager.queryIntentActivities(mailIntent, 0)
		val isIntentSafe: Boolean = activities.isNotEmpty()

		// Start an activity if it's safe
		if (isIntentSafe) {
			startActivity(mailIntent)
			killMyApp()
		} else {
			setContentView(R.layout.activity_main_info)

			val msgText = findViewById<TextView>(R.id.textView_msgText)
			msgText.text = text
			val msgEmail = findViewById<TextView>(R.id.textView_msgEmail)
			msgEmail.text = email
			val msgSubject = findViewById<TextView>(R.id.textView_msgSubject)
			msgSubject.text = subject
			val msgStream = findViewById<TextView>(R.id.textView_msgStream)
			msgStream.text = stream
		}
	}

	private fun killMyApp() {
		finishAffinity()
//		exitProcess(0)
		// If you will use only finishAffinity(); without System.exit(0); your application will quit
		// but the allocated memory will still be in use by your phone, so... if you want a clean and
		// really quit of an app, use both of them.
	}
}
