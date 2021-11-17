/*
 * Copyright 2014-2021 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.resources

/**
 * Thrown when [de]serialization of the resource failed
 */
public class ResourceSerializationException(message: String) : Exception(message)
