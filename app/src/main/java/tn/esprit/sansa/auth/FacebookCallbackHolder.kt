package tn.esprit.sansa.auth

import com.facebook.CallbackManager

object FacebookCallbackHolder {
    val callbackManager: CallbackManager = CallbackManager.Factory.create()
}
