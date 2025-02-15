/*
 * Copyright 2014-2021 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.serialization.kotlinx.cbor

import io.ktor.http.*
import io.ktor.serialization.*
import io.ktor.serialization.kotlinx.*
import kotlinx.serialization.*
import kotlinx.serialization.cbor.*
import kotlinx.serialization.modules.*
import kotlin.native.concurrent.*

/**
 * The default cbor configuration used in [KotlinxSerializationConverter]. The settings are:
 * - defaults are serialized
 *
 * See [Cbor] for more details.
 */
@OptIn(ExperimentalSerializationApi::class)
@SharedImmutable
public val DefaultCbor: Cbor = Cbor {
    encodeDefaults = true
}

/**
 * Register `application/cbor` (or another specified [contentType]) content type
 * to [ContentNegotiation] plugin using kotlinx.serialization.
 *
 * @param cbor format instance (optional)
 * @param contentType to register with, application/cbor by default
 */
@OptIn(ExperimentalSerializationApi::class)
public fun Configuration.cbor(
    cbor: Cbor = DefaultCbor,
    contentType: ContentType = ContentType.Application.Cbor
) {
    serialization(contentType, cbor)
}
