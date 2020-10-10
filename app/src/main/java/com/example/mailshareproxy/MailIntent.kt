package com.example.mailshareproxy

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Parcelable

abstract class MailIntent {
	protected val mailIntent = Intent()
	var email = ""
	var subject = ""
	var text = ""
	open var stream = ""
	protected lateinit var intentActionType: String

	abstract fun parseExtraData(intent: Intent)
	abstract fun putExtraStream()

	fun buildIntent(recipient: String) {
		// Build the intent
//		mailIntent = Intent(Intent.ACTION_SENDTO).apply { // do not allow attached files
//			data = Uri.parse("mailto:") // only email apps should handle this
//		mailIntent = Intent(Intent.ACTION_SEND).apply { // allow attached files
//			type = "message/rfc822" // "mailto:" not allowed with "ACTION_SEND" :(

		mailIntent.apply {
			action = intentActionType
			type = "message/rfc822"
			putExtra(Intent.EXTRA_EMAIL, arrayOf(recipient)) // recipients
			putExtra(Intent.EXTRA_SUBJECT, subject)
			putExtra(Intent.EXTRA_TEXT, text)
			putExtraStream()
		}
	}

	fun startMailIntent(activityContext: Context): Boolean {
		val activities =
			activityContext.packageManager.queryIntentActivities(mailIntent, PackageManager.MATCH_DEFAULT_ONLY)
		for (i in 0 .. activities.count()) {
			if (activities[i].activityInfo.packageName.contains(activityContext.packageName)) {
				activities.removeAt(i)
				break
			}
		}
		val isIntentSafe: Boolean = activities.isNotEmpty()

		// Start an activity if it's safe
		if (isIntentSafe) {
			activityContext.startActivity(mailIntent)
		}

		return isIntentSafe
	}
}

class SendAction : MailIntent() {
	init {
		intentActionType = Intent.ACTION_SEND
	}

	override fun parseExtraData(intent: Intent) {
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
	}

	override fun putExtraStream() {
		if (!stream.isBlank()) {
			mailIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(stream))
		}
	}
}

class SendMultipleAction : MailIntent() {
	private lateinit var streams: ArrayList<Parcelable>

	override var stream: String
		get() = streams.toString()
		set(_) {}

	init {
		intentActionType = Intent.ACTION_SEND_MULTIPLE
	}

	override fun parseExtraData(intent: Intent) {
		intent.getParcelableArrayListExtra<Parcelable>(Intent.EXTRA_STREAM)?.let {
			streams = it
		}
	}

	override fun putExtraStream() {
		mailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, streams)
	}
}

