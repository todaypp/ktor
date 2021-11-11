/*
 * Copyright 2014-2021 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.server.testing

import com.typesafe.config.*
import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.engine.*
import io.ktor.server.routing.*
import io.ktor.server.testing.client.*
import io.ktor.util.pipeline.*
import kotlinx.coroutines.*

/**
 * Provides a client attached to [TestApplication].
 */
public interface ClientProvider {
    public val client: HttpClient
    public fun createClient(block: HttpClientConfig<out HttpClientEngineConfig>.() -> Unit): HttpClient
}

/**
 * Represents a configures instance of a test application running locally.
 */
public class TestApplication internal constructor(
    private val builder: ApplicationTestBuilder
) : ClientProvider by builder {

    internal val engine by lazy { builder.engine }

    public fun stop() {
        builder.engine.stop(0, 0)
        builder.externalServices.externalApplications.values.forEach { it.stop() }
        client.close()
    }
}

/**
 * Creates an instance of [TestApplication] configured with builder [block].
 */
public fun TestApplication(
    block: TestApplicationBuilder.() -> Unit
): TestApplication {
    val builder = ApplicationTestBuilder()
    val testApplication = TestApplication(builder)
    builder.block()
    return testApplication
}

/**
 * Registers mocks for external services.
 */
public class ExternalServicesBuilder {
    internal val externalApplications = mutableMapOf<String, TestApplication>()

    /**
     * Registers mock for external service specified by [hosts] and configured with [block].
     */
    public fun hosts(vararg hosts: String, block: Application.() -> Unit) {
        check(hosts.isNotEmpty()) { "hosts can not be empty" }

        val application = TestApplication { applicationModules.add(block) }
        hosts.forEach {
            val protocolWithAuthority = Url(it).protocolWithAuthority
            externalApplications[protocolWithAuthority] = application
        }
    }
}

/**
 * Builder for [TestApplication]
 */
public open class TestApplicationBuilder {

    private var built = false

    internal val externalServices = ExternalServicesBuilder()
    internal val applicationModules = mutableListOf<Application.() -> Unit>()
    internal var environmentBuilder: ApplicationEngineEnvironmentBuilder.() -> Unit = {}
    internal val job = Job()

    internal val environment by lazy {
        built = true
        createTestEnvironment {
            config = HoconApplicationConfig(ConfigFactory.load())
            modules.addAll(applicationModules)
            environmentBuilder()
            parentCoroutineContext += job
        }
    }

    internal val engine by lazy {
        TestApplicationEngine(environment)
    }

    /**
     * Builds mocks for external services using [ExternalServicesBuilder]
     */
    public fun externalServices(block: ExternalServicesBuilder.() -> Unit) {
        checkNotBuilt()
        externalServices.block()
    }

    /**
     * Builds environment using [block]
     */
    public fun environment(block: ApplicationEngineEnvironmentBuilder.() -> Unit) {
        checkNotBuilt()
        environmentBuilder = block
    }

    /**
     * Adds module to [TestApplication]
     */
    public fun application(block: Application.() -> Unit) {
        checkNotBuilt()
        applicationModules.add(block)
    }

    /**
     * Installs a [plugin] into [TestApplication]
     */
    @Suppress("UNCHECKED_CAST")
    public fun <P : Pipeline<*, ApplicationCall>, B : Any, F : Any> install(
        plugin: Plugin<P, B, F>,
        configure: B.() -> Unit = {}
    ) {
        checkNotBuilt()
        applicationModules.add { install(plugin as Plugin<Application, B, F>, configure) }
    }

    /**
     * Installs routing into [TestApplication]
     */
    @ContextDsl
    public fun routing(configuration: Routing.() -> Unit) {
        checkNotBuilt()
        applicationModules.add { routing(configuration) }
    }

    private fun checkNotBuilt() {
        check(!built) {
            "The test application have been already built. " +
                "Make sure you configure the application before first access to client"
        }
    }
}

/**
 * Builder for test that uses [TestApplication]
 */
public class ApplicationTestBuilder : TestApplicationBuilder(), ClientProvider {

    override val client by lazy { createClient { } }

    override fun createClient(block: HttpClientConfig<out HttpClientEngineConfig>.() -> Unit): HttpClient {
        return HttpClient(DelegatingTestClientEngine) {
            engine {
                parentJob = job
                appEngineProvider = { engine }
                externalApplicationsProvider = { externalServices.externalApplications }
            }
            block()
        }
    }
}

/**
 * Creates a test using [TestApplication]
 */
public fun withTestApplication1(
    block: suspend ApplicationTestBuilder.() -> Unit
) {
    val builder = ApplicationTestBuilder()
        .apply { runBlocking { block() } }

    val testApplication = TestApplication(builder)
    testApplication.stop()
}