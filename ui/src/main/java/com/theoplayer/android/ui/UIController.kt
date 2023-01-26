package com.theoplayer.android.ui

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.ViewGroup
import android.view.ViewPropertyAnimator
import android.widget.RelativeLayout
import com.theoplayer.android.api.THEOplayerView
import com.theoplayer.android.api.player.Player
import com.theoplayer.android.api.player.ReadyState

/**
 * The DefaultUI creates the UI and all it's components. It's also responsible for handling the fade-out logic after a time of no interactions from the user.
 */
open class UIController @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : RelativeLayout(context, attributeSet, defStyleAttr, defStyleRes), PlayerStateListener {

    private lateinit var theoplayerView: THEOplayerView
    private lateinit var player: Player
    private lateinit var uiWrapper: ViewGroup

    private var fadeOutDuration: Long = 1000
    private var fadeOutDelay: Long = 3000
    private var animator: ViewPropertyAnimator? = null

    // Components
    private var theoTitle: THEOTitle? = null
    private var theoPlayPauseButton: THEOPlayPauseButton? = null
    private var theoJumpBackButton: THEOJumpBackButton? = null
    private var theoJumpForwardButton: THEOJumpForwardButton? = null
    private var theoSpinner: THEOSpinner? = null
    private var theoCurrentTime: THEOCurrentTime? = null
    private var theoDuration: THEODuration? = null
    private var theoSeekbar: THEOSeekbar? = null
    private var theoThumbnail: THEOThumbnail? = null
    private var theoLanguageView: THEOLanguageView? = null
    private var languageExit: THEOLanguageExit? = null
    private var theoLanguageButton: THEOLanguageButton? = null
    private var theoPiPButton: THEOPiPButton? = null
    private var theoFullscreenButton: THEOFullscreenButton? = null
    private var theoLiveButton: THEOLiveButton? = null
    private var theoError: THEOError? = null

    /*
    * From https://stackoverflow.com/a/12230251
    *
    * Tracing the source code of the 5.1 Source for the View Class.
    * It would seem that dispatchKeyEvent() is the first method called by the system.
    * Overloading it will prevent any and all key events from being called unless the base version is called.
    * dispatchKeyEvent()'s first move is to attempt to pass the event to an onKeyListener if there is one. This is when onKey() is called.
    * - If the onKey() implementation returns true, dispatchKeyEvent() will return there and other events will not be called.
    * - If there is no onKeyListener or the onKeyListener's onKey() method returned false, dispatchKeyEvent() will then call the KeyEvent's dispatch() method.
    *   Which will then in turn call all the methods in the KeyEvent.Callback interface on your view. This includes onKeyDown() and onKeyUp().
    */
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            if (player.error != null) {
                return super.dispatchKeyEvent(event)
            }

            showTemporarily()

            when (event.keyCode) {
                KeyEvent.KEYCODE_BACK -> {
                    languageExit?.performClick()
                    showTemporarily()
                    return true
                }
                KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                    if (player.isPaused) {
                        player.play()
                    } else {
                        player.pause()
                    }
                }
                KeyEvent.KEYCODE_MEDIA_PLAY -> {
                    player.play()
                }
                KeyEvent.KEYCODE_MEDIA_PAUSE -> {
                    player.pause()
                }
                KeyEvent.KEYCODE_MEDIA_REWIND -> {
                    player.currentTime = player.currentTime - 10
                }
                KeyEvent.KEYCODE_MEDIA_FAST_FORWARD -> {
                    player.currentTime = player.currentTime + 10
                }
            }
        }

        return super.dispatchKeyEvent(event)
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        showTemporarily()
        return super.dispatchTouchEvent(event)
    }

    fun initialize(theoplayerView: THEOplayerView) {
        this.theoplayerView = theoplayerView
        this.player = theoplayerView.player
        this.uiWrapper = findViewById(R.id.theo_ui_wrapper)

        isFocusable = true
        descendantFocusability = ViewGroup.FOCUS_AFTER_DESCENDANTS

        setupTitle()
        setupPlayPauseButton()
        setupJumpBackButton()
        setupJumpForwardButton()
        setupSpinner()
        setupCurrentTime()
        setupDuration()
        setupSeekBar()
        setupThumbnail()
        setupLanguageView()
        setupLanguageButton()
        setupPiPButton()
        setupFullscreenButton()
        setupLiveButton()
        setupError()

        onSourceChange()
    }

    override fun onSourceChange() {
        uiWrapper.visibility = VISIBLE

        theoTitle?.visibility = VISIBLE
        theoPlayPauseButton?.visibility = VISIBLE
        theoJumpBackButton?.visibility = GONE
        theoJumpForwardButton?.visibility = GONE
        theoSpinner?.visibility = GONE
        theoCurrentTime?.visibility = GONE
        theoDuration?.visibility = GONE
        theoSeekbar?.visibility = GONE
        theoThumbnail?.visibility = GONE
        theoLanguageView?.visibility = GONE
        theoLanguageButton?.visibility = GONE
        theoPiPButton?.visibility = GONE
        theoFullscreenButton?.visibility = GONE
        theoLiveButton?.visibility = GONE
        theoError?.visibility = GONE

        show()
    }

    override fun onPlay() {
        setStatePlaying()
        showTemporarily()
    }

    override fun onPlaying() {
        setStatePlaying()
        showTemporarily()
    }

    override fun onPause() {
        show()
    }

    override fun onReadyStateChanged(readyState: ReadyState) {
        if (readyState <= ReadyState.HAVE_CURRENT_DATA && !player.isEnded) {
            theoSpinner?.visibility = VISIBLE
        } else {
            theoSpinner?.visibility = GONE
        }
    }

    override fun onEnded() {
        theoSpinner?.visibility = GONE
        theoJumpForwardButton?.visibility = GONE

        show()
    }

    override fun onError() {
        uiWrapper.visibility = GONE

        theoTitle?.visibility = VISIBLE
        theoPlayPauseButton?.visibility = GONE
        theoJumpBackButton?.visibility = GONE
        theoJumpForwardButton?.visibility = GONE
        theoSpinner?.visibility = GONE
        theoCurrentTime?.visibility = GONE
        theoDuration?.visibility = GONE
        theoSeekbar?.visibility = GONE
        theoThumbnail?.visibility = GONE
        theoLanguageView?.visibility = GONE
        theoLanguageButton?.visibility = GONE
        theoPiPButton?.visibility = GONE
        theoFullscreenButton?.visibility = GONE
        theoLiveButton?.visibility = GONE
        theoError?.visibility = VISIBLE

        show()
    }

    private fun setStatePlaying() {
        if (player.duration == Double.POSITIVE_INFINITY) {
            setStatePlayingLive()
        } else {
            setStatePlayingVod()
        }
    }

    private fun setStatePlayingVod() {
        uiWrapper.visibility = VISIBLE

        theoTitle?.visibility = VISIBLE
        theoPlayPauseButton?.visibility = VISIBLE
        theoJumpBackButton?.visibility = VISIBLE
        theoJumpForwardButton?.visibility = VISIBLE
        theoCurrentTime?.visibility = VISIBLE
        theoDuration?.visibility = VISIBLE
        theoSeekbar?.visibility = VISIBLE
        theoThumbnail?.visibility = GONE
        theoLanguageButton?.visibility = VISIBLE
        theoPiPButton?.visibility = VISIBLE
        theoFullscreenButton?.visibility = VISIBLE
        theoLiveButton?.visibility = GONE
        theoError?.visibility = GONE
    }

    private fun setStatePlayingLive() {
        uiWrapper.visibility = VISIBLE

        theoTitle?.visibility = VISIBLE
        theoPlayPauseButton?.visibility = VISIBLE
        theoJumpBackButton?.visibility = GONE
        theoJumpForwardButton?.visibility = GONE
        theoCurrentTime?.visibility = VISIBLE
        theoDuration?.visibility = GONE
        theoSeekbar?.visibility = VISIBLE
        theoThumbnail?.visibility = GONE
        theoLanguageButton?.visibility = VISIBLE
        theoPiPButton?.visibility = VISIBLE
        theoFullscreenButton?.visibility = VISIBLE
        theoLiveButton?.visibility = VISIBLE
        theoError?.visibility = GONE
    }

    private fun setupTitle() {
        theoTitle = findViewById(R.id.theo_title)
    }

    private fun setupPlayPauseButton() {
        theoPlayPauseButton = findViewById(R.id.theo_playpause)
        theoPlayPauseButton?.setPlayer(player)
    }

    private fun setupJumpBackButton() {
        theoJumpBackButton = findViewById(R.id.theo_jumpback)
        theoJumpBackButton?.setPlayer(player)
    }

    private fun setupJumpForwardButton() {
        theoJumpForwardButton = findViewById(R.id.theo_jumpforward)
        theoJumpForwardButton?.setPlayer(player)
    }

    private fun setupSpinner() {
        theoSpinner = findViewById(R.id.theo_spinner)
    }

    private fun setupCurrentTime() {
        theoCurrentTime = findViewById(R.id.theo_currentTime)
        theoCurrentTime?.setPlayer(player)
    }

    private fun setupDuration() {
        theoDuration = findViewById(R.id.theo_duration)
        theoDuration?.setPlayer(player)
    }

    private fun setupSeekBar() {
        theoSeekbar = findViewById(R.id.theo_seekbar)
        theoSeekbar?.setPlayer(player)
    }

    private fun setupThumbnail() {
        theoThumbnail = findViewById(R.id.theo_thumbnail)
        theoThumbnail?.setPlayer(player)

        // Attach listener for thumbnail placement.
        theoThumbnail?.let {
            theoSeekbar?.addOnSeekBarChangeListener(it)
        }
    }

    private fun setupLanguageView() {
        theoLanguageView = findViewById(R.id.theo_language_view)
        theoLanguageView?.setPlayer(player)

        languageExit = findViewById(R.id.theo_language_exit)
        languageExit?.setOnClickListener {
            theoLanguageView?.visibility = GONE
        }
    }

    private fun setupLanguageButton() {
        theoLanguageButton = findViewById(R.id.theo_language)
        theoLanguageButton?.setOnClickListener {
            show()
            theoLanguageView?.visibility = VISIBLE
            theoLanguageView?.requestFocus()
        }
    }

    private fun setupPiPButton() {
        theoPiPButton = findViewById(R.id.theo_pip)

        if (theoplayerView.piPManager == null) {
            theoPiPButton?.visibility = GONE
            theoPiPButton = null
        } else {
            theoPiPButton?.setTHEOplayerView(theoplayerView)
        }
    }

    private fun setupFullscreenButton() {
        theoFullscreenButton = findViewById(R.id.theo_fullscreen)
        theoFullscreenButton?.setTHEOplayerView(theoplayerView)
    }

    private fun setupLiveButton() {
        theoLiveButton = findViewById(R.id.theo_live)
        theoLiveButton?.setPlayer(player)
    }

    private fun setupError() {
        theoError = findViewById(R.id.theo_error_message)
        theoError?.setPlayer(player)
    }

    /**
     * Sets how long the fading out of the UI of the player should take.
     */
    fun setFadeDuration(duration: Long) {
        this.fadeOutDuration = duration
    }

    /**
     * Sets after how much time (ms), of no interaction, the UI of the player should start fading out.
     */
    fun setFadeDelay(delay: Long) {
        this.fadeOutDelay = delay
    }

    /**
     * Sets the title of the content.
     *
     * - This will be shown once the source has loaded, together with the big play-button.
     */
    fun setTitle(title: String) {
        theoTitle?.text = title
    }

    /**
     * Instantly shows the player UI. After [fadeOutDelay] the UI will start fading out.
     */
    fun showTemporarily() {
        show()
        if (!player.isEnded && !player.isPaused && player.error == null && theoLanguageView?.visibility != VISIBLE) {
            animate(0.0f, fadeOutDelay, fadeOutDuration)
        }
    }

    /**
     * Instantly shows the player UI. It will never fade out.
     */
    fun show() {
        animator?.cancel()
        uiWrapper.alpha = 1.0f
        uiWrapper.visibility = VISIBLE
    }

    /**
     * Instantly hides the player UI.
     */
    fun hide() {
        animator?.cancel()
        uiWrapper.alpha = 0.0f
        uiWrapper.visibility = GONE
    }

    /**
     * Animate the UI as a whole to a certain opacity.
     *
     * @param alpha the opacity for the UI, from 0.0 to 1.0 representing (0 to 100%).
     * @param delay after which time the UI starts to fade to target alpha, in ms.
     * @param duration how long it takes for the UI to fade to target alpha, in ms.
     */
    private fun animate(alpha: Float, delay: Long, duration: Long) {
        animator = uiWrapper.animate()
        animator?.alpha(alpha)
        animator?.startDelay = delay
        animator?.duration = duration
        animator?.withEndAction {
            if (alpha == 0.0f) {
                uiWrapper.visibility = GONE
            } else {
                uiWrapper.visibility = VISIBLE
            }
        }
        animator?.start()
    }

    override fun release() {
        animator?.cancel()

        theoPlayPauseButton?.release()
        theoJumpBackButton?.release()
        theoJumpForwardButton?.release()
        theoCurrentTime?.release()
        theoDuration?.release()
        theoSeekbar?.release()
        theoThumbnail?.release()
        theoLanguageView?.release()
        theoPiPButton?.release()
        theoFullscreenButton?.release()
        theoLiveButton?.release()
        theoError?.release()

        theoTitle?.text = null
        theoTitle = null
        theoPlayPauseButton = null
        theoJumpBackButton = null
        theoJumpForwardButton = null
        theoSpinner = null
        theoCurrentTime = null
        theoDuration = null
        theoSeekbar = null
        theoThumbnail = null
        theoLanguageView = null
        languageExit = null
        theoLanguageButton = null
        theoPiPButton = null
        theoFullscreenButton = null
        theoLiveButton = null
        theoError = null
    }

}
