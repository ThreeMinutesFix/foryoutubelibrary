package com.dooo.android

import android.content.Context
import android.util.AttributeSet
import og.android.lib.toggleiconview.ToggleIconView

class PausePlay @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) :
        ToggleIconView(
                context, attrs, defStyleAttr,
                R.drawable.rounded_pause_to_play,
                R.drawable.rounded_play_to_pause
        )
