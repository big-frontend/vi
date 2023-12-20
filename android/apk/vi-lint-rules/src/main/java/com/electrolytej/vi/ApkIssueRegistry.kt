package com.electrolytej.vi

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.detector.api.CURRENT_API

internal const val PRIORITY = 10 // Does not matter anyway within Lint.

class ApkIssueRegistry : IssueRegistry() {
    override val api get() = CURRENT_API
    override val minApi get() = 10

    override val issues
        get() = listOf(
            ISSUE_ENUM_USAGE,
            ISSUE_ANIMATION_DRAWABLE_USAGE,
        )
}
