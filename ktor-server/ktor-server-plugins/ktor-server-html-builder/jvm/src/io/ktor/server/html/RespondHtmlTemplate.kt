/*
 * Copyright 2014-2021 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.server.html

import io.ktor.http.*
import io.ktor.server.application.*
import kotlinx.html.*

public suspend fun <TTemplate : Template<HTML>> ApplicationCall.respondHtmlTemplate(
    template: TTemplate,
    status: HttpStatusCode = HttpStatusCode.OK,
    body: TTemplate.() -> Unit
) {
    template.body()
    respondHtml(status) { with(template) { apply() } }
}
