/*
 * Copyright 2014-2021 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.client.plugins.json.tests

import io.ktor.client.plugins.json.*

@Suppress("DEPRECATION")
class GsonTest : JsonTest() {
    override val serializerImpl = GsonSerializer()
}
