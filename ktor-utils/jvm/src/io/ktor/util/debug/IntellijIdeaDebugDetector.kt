/*
 * Copyright 2014-2021 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.util.debug

import java.lang.management.*

internal actual object IntellijIdeaDebugDetector {
    actual val isDebuggerConnected: Boolean by lazy {
        ManagementFactory.getRuntimeMXBean()
            .inputArguments.toString()
            .contains("jdwp")
    }
}
